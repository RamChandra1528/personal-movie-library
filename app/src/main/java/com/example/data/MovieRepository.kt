package com.example.data

import kotlinx.coroutines.flow.Flow

class MovieRepository(private val movieDao: MovieDao) {

    val allMovies: Flow<List<MovieEntity>> = movieDao.getAllMovies()

    fun getMovieById(id: Long): Flow<MovieEntity?> = movieDao.getMovieById(id)

    fun searchMovies(query: String): Flow<List<MovieEntity>> = movieDao.searchMovies(query)

    suspend fun insertMovie(movie: MovieEntity): Long = movieDao.insertMovie(movie)

    suspend fun updateMovie(movie: MovieEntity) = movieDao.updateMovie(movie)

    suspend fun deleteMovie(movie: MovieEntity) = movieDao.deleteMovie(movie)

    suspend fun deleteMovieById(id: Long) = movieDao.deleteMovieById(id)

    suspend fun findByExactTitle(title: String): MovieEntity? = movieDao.findByExactTitle(title)

    suspend fun getCount(): Int = movieDao.getCount()
}
