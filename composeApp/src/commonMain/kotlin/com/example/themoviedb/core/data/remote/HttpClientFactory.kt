package com.example.themoviedb.core.data.remote

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient
