package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.service.OperationType
import com.example.service.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedTime: Long? = null,
    val cloudCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * Coordinates bi-directional synchronization between local Room SQLite cache
 * and cloud Firestore collection scoped under /users/{userId}/movies.
 *
 * Adheres strictly to the custom database ID invariant:
 * FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
 */
class FirestoreSyncRepository(
    private val db: FirebaseFirestore,
    private val movieDao: MovieDao
) {
    // Secondary constructor resolving database ID from resources
    constructor(context: Context, movieDao: MovieDao) : this(
        db = getFirestoreInstance(context),
        movieDao = movieDao
    )

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mergeMutex = Mutex()
    private var moviesSnapshotListener: ListenerRegistration? = null
    @Volatile
    private var activeUserId: String? = null
    @Volatile
    private var lastActiveUserId: String? = null

    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _cloudSearchHistory = MutableStateFlow<List<CloudSearchRecord>>(emptyList())
    val cloudSearchHistory: StateFlow<List<CloudSearchRecord>> = _cloudSearchHistory.asStateFlow()

    /**
     * Start observing real-time cloud collection for the authenticated user.
     * When user signs in, any changes on the cloud or restoring on a new device
     * are merged automatically into local Room.
     */
    fun startSync(userId: String) {
        if (activeUserId == userId && moviesSnapshotListener != null) return
        val previousUser = lastActiveUserId
        stopSync()
        activeUserId = userId
        lastActiveUserId = userId

        _syncStatus.value = _syncStatus.value.copy(isSyncing = true, errorMessage = null)
        val moviesCol = db.collection("users").document(userId).collection("movies")

        // Handle user switch: if switching from another user, clear old user's movies
        if (previousUser != null && previousUser != userId) {
            repositoryScope.launch {
                try {
                    movieDao.deleteAllMovies()
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing local cache for user switch", e)
                }
            }
        }

        moviesSnapshotListener = moviesCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, moviesCol.path)
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    errorMessage = error.localizedMessage ?: "Sync error"
                )
                return@addSnapshotListener
            }

            if (snapshot != null) {
                repositoryScope.launch {
                    try {
                        val cloudMovies = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(
                                    CloudMovie::class.java,
                                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error deserializing cloud movie: ${doc.id}", e)
                                null
                            }
                        }

                        mergeCloudMovies(cloudMovies)

                        _syncStatus.value = SyncStatus(
                            isSyncing = false,
                            lastSyncedTime = System.currentTimeMillis(),
                            cloudCount = cloudMovies.size,
                            errorMessage = null
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to reconcile cloud movies to Room", e)
                        _syncStatus.value = _syncStatus.value.copy(
                            isSyncing = false,
                            errorMessage = e.localizedMessage
                        )
                    }
                }
            }
        }

        // Also fetch search history
        loadSearchHistory(userId)

        // Perform initial deterministic sync & restore
        repositoryScope.launch {
            restoreThenBackUpMissingMovies(userId, moviesCol)
        }
    }

    fun stopSync() {
        moviesSnapshotListener?.remove()
        moviesSnapshotListener = null
        activeUserId = null
        _syncStatus.value = _syncStatus.value.copy(isSyncing = false)
    }

    /**
     * Complete cleanup on logout: stops listener and clears local cache
     * so user data does not remain accessible to subsequent users.
     */
    suspend fun stopSyncAndClearLocal() {
        stopSync()
        lastActiveUserId = null
        try {
            movieDao.deleteAllMovies()
            _cloudSearchHistory.value = emptyList()
            _syncStatus.value = SyncStatus(
                isSyncing = false,
                lastSyncedTime = null,
                cloudCount = 0,
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local cache on logout", e)
        }
    }

    /**
     * Backup a single movie to the user's cloud vault.
     */
    suspend fun uploadMovie(userId: String, movie: MovieEntity) {
        val docId = makeDocId(movie)
        val docRef = db.collection("users").document(userId)
            .collection("movies").document(docId)

        try {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true, errorMessage = null)
            val cloudMovie = movie.toCloudMovie(
                userId = userId,
                cloudDocId = docId
            )

            val writeMap = cloudMovie.toWriteMap(isCreate = true)
            docRef.set(writeMap, com.google.firebase.firestore.SetOptions.merge()).await()

            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncedTime = System.currentTimeMillis(),
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.w(TAG, "Cloud write failed (will use offline cache/retry): ${e.message}")
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                errorMessage = "Sync pending: ${e.localizedMessage ?: "Offline"}"
            )
        }
    }

    /**
     * Delete a movie from cloud vault.
     */
    suspend fun deleteMovieFromCloud(userId: String, movie: MovieEntity) {
        val docId = makeDocId(movie)
        val docRef = db.collection("users").document(userId)
            .collection("movies").document(docId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    /**
     * Synchronize all local movies to cloud (e.g., initial backup on first sign in).
     */
    suspend fun backupAllLocalMovies(userId: String, movies: List<MovieEntity>) {
        _syncStatus.value = _syncStatus.value.copy(isSyncing = true, errorMessage = null)
        var count = 0
        try {
            for (movie in movies) {
                uploadMovie(userId, movie)
                count++
            }
            _syncStatus.value = SyncStatus(
                isSyncing = false,
                lastSyncedTime = System.currentTimeMillis(),
                cloudCount = count,
                errorMessage = null
            )
        } catch (e: Exception) {
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                errorMessage = e.localizedMessage ?: "Backup pending"
            )
        }
    }

    /**
     * Restore the signed-in user's cloud vault first, then upload only local movies which are
     * absent from the cloud. Cloud data wins for duplicates during this initial pass, avoiding
     * an old device cache overwriting a newer cloud record after reinstalling the app.
     */
    private suspend fun restoreThenBackUpMissingMovies(
        userId: String,
        moviesCol: com.google.firebase.firestore.CollectionReference
    ) {
        try {
            val snapshot = moviesCol.get().await()
            if (activeUserId != userId) return
            val cloudMovies = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(CloudMovie::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deserializing cloud movie: ${doc.id}", e)
                    null
                }
            }
            mergeCloudMovies(cloudMovies)
            if (activeUserId != userId) return

            val cloudIds = snapshot.documents.map { it.id }.toHashSet()
            val localMovies = movieDao.getAllMoviesOnce()
            localMovies
                .filter { makeDocId(it) !in cloudIds }
                .forEach { uploadMovie(userId, it) }

            _syncStatus.value = SyncStatus(
                isSyncing = false,
                lastSyncedTime = System.currentTimeMillis(),
                cloudCount = maxOf(cloudMovies.size, localMovies.size),
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Initial cloud restore/backup failed", e)
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                errorMessage = e.localizedMessage ?: "Cloud sync failed"
            )
        }
    }

    private suspend fun mergeCloudMovies(cloudMovies: List<CloudMovie>) {
        // The snapshot listener and initial restore can arrive together. Serializing their
        // read-then-insert work prevents duplicate Room rows on a fresh install.
        mergeMutex.withLock {
            for (cloudMovie in cloudMovies) {
                // A title alone is not a stable key: remakes commonly share a title. Match the
                // same title and release year so both records survive local/cloud reconciliation.
                val existingLocal = movieDao.findByTitleAndYear(cloudMovie.title, cloudMovie.releaseYear)
                if (existingLocal != null) {
                    movieDao.updateMovie(cloudMovie.toEntity(localId = existingLocal.id))
                } else {
                    movieDao.insertMovie(cloudMovie.toEntity(localId = 0))
                }
            }
        }
    }

    /**
     * Sync user settings to cloud.
     */
    suspend fun saveUserSettings(userId: String, sortOrder: String, preferredType: String) {
        val docRef = db.collection("users").document(userId)
            .collection("settings").document("user_settings")
        try {
            val settingsData = mapOf(
                "userId" to userId,
                "sortOrder" to sortOrder,
                "preferredType" to preferredType,
                "backupEnabled" to true,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            docRef.set(settingsData).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    /**
     * Log and sync search history item to cloud.
     */
    suspend fun recordSearch(userId: String, query: String, contentType: String) {
        if (query.isBlank()) return
        val searchId = "search_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val docRef = db.collection("users").document(userId)
            .collection("search_history").document(searchId)
        try {
            val recordData = mapOf(
                "id" to searchId,
                "query" to query.take(200),
                "contentType" to contentType,
                "timestamp" to System.currentTimeMillis(),
                "userId" to userId,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            docRef.set(recordData).await()
            loadSearchHistory(userId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    fun loadSearchHistory(userId: String) {
        val searchCol = db.collection("users").document(userId).collection("search_history")
        searchCol.limit(20).get().addOnSuccessListener { snapshot ->
            val records = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(
                        CloudSearchRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.timestamp }
            _cloudSearchHistory.value = records
        }.addOnFailureListener { e ->
            handleFirestoreError(e, OperationType.LIST, searchCol.path)
        }
    }

    private fun makeDocId(movie: MovieEntity): String {
        val cleanTitle = movie.title.trim().lowercase().replace(Regex("[^a-z0-9]"), "_").trim('_').take(30)
        val safeTitle = if (cleanTitle.isBlank()) "item_${movie.id.takeIf { it > 0 } ?: (movie.addedTimestamp % 100000)}" else cleanTitle
        val year = movie.releaseYear ?: 0
        return "movie_${safeTitle}_${year}"
    }

    companion object {
        private const val TAG = "FirestoreSyncRepo"

        fun getFirestoreInstance(context: Context): FirebaseFirestore {
            val appContext = context.applicationContext
            if (com.google.firebase.FirebaseApp.getApps(appContext).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(appContext)
            }
            val databaseId = appContext.getString(R.string.firestore_database_id)
            return FirebaseFirestore.getInstance(databaseId)
        }
    }
}
