package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY addedTimestamp DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    fun getMovieById(id: Long): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' ORDER BY addedTimestamp DESC")
    fun searchMovies(query: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity): Long

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: Long)

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getCount(): Int

    @Query("SELECT * FROM movies WHERE LOWER(title) = LOWER(:title) LIMIT 1")
    suspend fun findByExactTitle(title: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE LOWER(title) = LOWER(:title) AND ((releaseYear = :releaseYear) OR (releaseYear IS NULL AND :releaseYear IS NULL)) LIMIT 1")
    suspend fun findByTitleAndYear(title: String, releaseYear: Int?): MovieEntity?

    @Query("SELECT * FROM movies ORDER BY addedTimestamp DESC")
    suspend fun getAllMoviesOnce(): List<MovieEntity>
}
