package com.vitalcheck.data.remote.anthropic.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "content") val content: List<ContentBlock>,
    @Json(name = "model") val model: String,
    @Json(name = "stop_reason") val stopReason: String?,
    @Json(name = "usage") val usage: Usage
)

@JsonClass(generateAdapter = true)
data class ContentBlock(
    @Json(name = "type") val type: String,
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "input_tokens") val inputTokens: Int,
    @Json(name = "output_tokens") val outputTokens: Int
)
