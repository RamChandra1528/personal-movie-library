package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Watch statuses supported by CineVault.
 */
object WatchStatus {
    const val PLAN_TO_WATCH = "PLAN_TO_WATCH"
    const val WATCHING = "WATCHING"
    const val COMPLETED = "COMPLETED"
    const val DROPPED = "DROPPED"

    val allStatuses = listOf(PLAN_TO_WATCH, WATCHING, COMPLETED, DROPPED)

    fun getDisplayName(status: String): String = when (status) {
        PLAN_TO_WATCH -> "Plan to Watch"
        WATCHING -> "Watching"
        COMPLETED -> "Completed"
        DROPPED -> "Dropped"
        else -> status
    }
}

/**
 * Persistent Movie entity stored in CineVault's Room database.
 */
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
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
    val personalRating: Float = 0f, // 0 to 5 stars
    val watchStatus: String = WatchStatus.PLAN_TO_WATCH,
    val watchedDate: String? = null,
    val personalNotes: String? = null,
    val isFavorite: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    val genresList: List<String>
        get() = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val castList: List<String>
        get() = if (cast.isBlank()) emptyList() else cast.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val formattedRuntime: String
        get() = runtimeMinutes?.let { mins ->
            val h = mins / 60
            val m = mins % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        } ?: "N/A"
}
