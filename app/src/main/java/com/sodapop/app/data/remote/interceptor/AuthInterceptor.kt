package com.sodapop.app.data.remote.interceptor

import com.sodapop.app.data.preferences.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = preferences.getApiKeySync()
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .apply {
                if (apiKey.isNotBlank()) {
                    addHeader("Authorization", "Bearer $apiKey")
                }
            }
            .build()
        return chain.proceed(request)
    }
}
