package com.vitalcheck.data.remote.anthropic

import com.vitalcheck.data.remote.anthropic.dto.MessagesRequestDto
import com.vitalcheck.data.remote.anthropic.dto.MessagesResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AnthropicApiService {

    @POST("/v1/messages")
    suspend fun createMessage(
        @Body request: MessagesRequestDto
    ): MessagesResponseDto
}
