package com.sodapop.app.data.remote.interceptor

import com.sodapop.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class DynamicBaseUrlInterceptor @Inject constructor(
    private val preferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val baseUrl = runBlocking {
            preferences.llmConfig.first().baseUrl
        }

        if (baseUrl.isBlank()) {
            return chain.proceed(originalRequest)
        }

        val newBaseUrl = baseUrl.trimEnd('/').toHttpUrlOrNull() ?: return chain.proceed(originalRequest)

        // Original request path from Retrofit is like "/v1/chat/completions"
        // We only need the endpoint part: "chat/completions"
        // The placeholder base URL is "https://placeholder.local/v1/"
        // So we strip the "/v1" prefix from the original path
        val originalPath = originalRequest.url.encodedPath  // e.g. "/v1/chat/completions"
        val endpointPath = originalPath.removePrefix("/v1/") // e.g. "chat/completions"

        // Build new URL: user's base URL + "/" + endpoint
        val basePath = newBaseUrl.encodedPath.trimEnd('/')  // e.g. "/v1"
        val fullPath = "$basePath/$endpointPath"            // e.g. "/v1/chat/completions"

        val newUrl = originalRequest.url.newBuilder()
            .scheme(newBaseUrl.scheme)
            .host(newBaseUrl.host)
            .port(newBaseUrl.port)
            .encodedPath(fullPath)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
