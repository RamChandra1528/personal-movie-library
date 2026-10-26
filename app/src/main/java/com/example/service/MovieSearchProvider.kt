package com.example.service

/**
 * Provider-independent search interface for public movie information discovery.
 * Permitted sources can be added or swapped without altering UI or business logic.
 */
interface MovieSearchProvider {
    val providerName: String
    suspend fun search(query: String): List<DiscoveredMovie>
    suspend fun getMovieDetails(identifier: String): DiscoveredMovie?
}
