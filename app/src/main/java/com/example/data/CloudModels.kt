package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Type-safe Firestore entity for saved movies in personal cloud collection.
 * Follows Canonical Firestore Data Class conventions with @DocumentId,
 * no-arg default parameters, and @ServerTimestamp.
 */
@IgnoreExtraProperties
data class CloudMovie(
    @DocumentId val id: String = "",
    val title: String = "",
    val originalTitle: String? = null,
    val releaseYear: Int? = null,
    val releaseDate: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val genres: String = "",
    val synopsis: String = "",
    val director: String = "",
    val cast: String = "",
    val runtimeMinutes: Int? = null,
    val language: String? = "English",
    val externalRating: String? = null,
    val sourceUrl: String? = null,
    val sourceAttribution: String? = "Public Web / Wikipedia",
    val personalRating: Float = 0f,
    val watchStatus: String = WatchStatus.PLAN_TO_WATCH,
    val watchedDate: String? = null,
    val personalNotes: String? = null,
    val isFavorite: Boolean = false,
    val addedTimestamp: Long = 0L,
    val contentType: String = EntertainmentType.MOVIE,
    val alternateTitles: String = "",
    val imdbId: String? = null,
    val similarTitles: String = "",
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toEntity(localId: Long = 0): MovieEntity {
        return MovieEntity(
            id = localId,
            title = title,
            originalTitle = originalTitle,
            releaseYear = releaseYear,
            releaseDate = releaseDate,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            genres = genres,
            synopsis = synopsis,
            director = director,
            cast = cast,
            runtimeMinutes = runtimeMinutes,
            language = language,
            externalRating = externalRating,
            sourceUrl = sourceUrl,
            sourceAttribution = sourceAttribution ?: "Public Web / Wikipedia",
            personalRating = personalRating,
            watchStatus = watchStatus,
            watchedDate = watchedDate,
            personalNotes = personalNotes,
            isFavorite = isFavorite,
            addedTimestamp = if (addedTimestamp > 0) addedTimestamp else System.currentTimeMillis(),
            contentType = contentType,
            alternateTitles = alternateTitles,
            imdbId = imdbId,
            similarTitles = similarTitles
        )
    }

    /**
     * Map representation for explicit Firestore writes with null-filtering.
     */
    fun toWriteMap(isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "title" to title,
            "genres" to genres,
            "synopsis" to synopsis,
            "director" to director,
            "cast" to cast,
            "personalRating" to personalRating.toDouble(),
            "watchStatus" to watchStatus,
            "isFavorite" to isFavorite,
            "addedTimestamp" to addedTimestamp,
            "contentType" to contentType,
            "alternateTitles" to alternateTitles,
            "similarTitles" to similarTitles,
            "userId" to userId,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        originalTitle?.let { map["originalTitle"] = it }
        releaseYear?.let { if (it in 0..2200) map["releaseYear"] = it }
        releaseDate?.let { map["releaseDate"] = it }
        posterUrl?.let { map["posterUrl"] = it }
        backdropUrl?.let { map["backdropUrl"] = it }
        runtimeMinutes?.let { map["runtimeMinutes"] = it }
        language?.let { map["language"] = it }
        externalRating?.let { map["externalRating"] = it }
        sourceUrl?.let { map["sourceUrl"] = it }
        sourceAttribution?.let { map["sourceAttribution"] = it }
        watchedDate?.let { map["watchedDate"] = it }
        personalNotes?.let { map["personalNotes"] = it }
        imdbId?.let { map["imdbId"] = it }

        if (createdAt != null) {
            map["createdAt"] = createdAt
        } else {
            map["createdAt"] = FieldValue.serverTimestamp()
        }

        return map
    }
}

fun MovieEntity.toCloudMovie(
    userId: String,
    cloudDocId: String? = null,
    existingCreatedAt: Timestamp? = null
): CloudMovie {
    val docId = cloudDocId ?: "movie_${id.takeIf { it > 0 } ?: (title.lowercase().replace(Regex("[^a-z0-9]"), "_") + "_" + (releaseYear ?: 0))}"
    return CloudMovie(
        id = docId,
        title = title,
        originalTitle = originalTitle,
        releaseYear = releaseYear,
        releaseDate = releaseDate,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        genres = genres,
        synopsis = synopsis,
        director = director,
        cast = cast,
        runtimeMinutes = runtimeMinutes,
        language = language,
        externalRating = externalRating,
        sourceUrl = sourceUrl,
        sourceAttribution = sourceAttribution,
        personalRating = personalRating,
        watchStatus = watchStatus,
        watchedDate = watchedDate,
        personalNotes = personalNotes,
        isFavorite = isFavorite,
        addedTimestamp = addedTimestamp,
        contentType = contentType,
        alternateTitles = alternateTitles,
        imdbId = imdbId,
        similarTitles = similarTitles,
        userId = userId,
        createdAt = existingCreatedAt
    )
}

@IgnoreExtraProperties
data class CloudUserSettings(
    val userId: String = "",
    val sortOrder: String = "RECENTLY_ADDED",
    val preferredType: String = "ALL",
    val backupEnabled: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudSearchRecord(
    @DocumentId val id: String = "",
    val query: String = "",
    val contentType: String = "ALL",
    val timestamp: Long = 0L,
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
