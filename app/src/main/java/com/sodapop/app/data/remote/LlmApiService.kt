package com.sodapop.app.data.remote

import com.sodapop.app.data.remote.dto.ChatCompletionRequest
import com.sodapop.app.data.remote.dto.ChatCompletionResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface LlmApiService {

    @POST("chat/completions")
    suspend fun chatCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse

    @Streaming
    @POST("chat/completions")
    suspend fun chatCompletionStream(
        @Body request: ChatCompletionRequest
    ): ResponseBody
}
