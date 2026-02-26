package com.example.themoviedb.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class TmdbAuthInterceptor(
    private val accessToken: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .header("accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(newRequest)
    }
}
