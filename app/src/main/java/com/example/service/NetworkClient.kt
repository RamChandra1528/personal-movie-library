package com.example.service

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Shared high-performance HTTP client with connection pooling and fast fail-fast timeouts.
 * Reuses TCP connections across Wikipedia, TVMaze, and Anime queries to reduce handshake latency.
 */
object NetworkClient {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(12, 5, TimeUnit.MINUTES))
        .connectTimeout(3500, TimeUnit.MILLISECONDS)
        .readTimeout(3500, TimeUnit.MILLISECONDS)
        .writeTimeout(3500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()
}
