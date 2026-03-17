package com.example.themoviedb.core.di

import com.example.themoviedb.core.data.remote.ApiConstants
import com.example.themoviedb.core.data.remote.TmdbApiService
import com.example.themoviedb.core.data.remote.createPlatformHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    single {
        createPlatformHttpClient().config {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                headers.append("Authorization", "Bearer ${ApiConstants.ACCESS_TOKEN}")
                headers.append("accept", "application/json")
            }
        }
    }

    single { TmdbApiService(get()) }
}
