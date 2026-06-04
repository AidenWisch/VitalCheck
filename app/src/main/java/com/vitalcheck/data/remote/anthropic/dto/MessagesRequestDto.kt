package com.vitalcheck.data.remote.anthropic.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesRequestDto(
    @Json(name = "model") val model: String,
    @Json(name = "max_tokens") val maxTokens: Int,
    @Json(name = "system") val system: String? = null,
    @Json(name = "messages") val messages: List<MessageDto>
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)
