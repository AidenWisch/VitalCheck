package com.vitalcheck.data.remote.fitbit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SleepLogDto(
    @Json(name = "summary") val summary: SleepSummary?
)

@JsonClass(generateAdapter = true)
data class SleepSummary(
    @Json(name = "totalMinutesAsleep") val totalMinutesAsleep: Int,
    @Json(name = "stages") val stages: SleepStages? = null
)

@JsonClass(generateAdapter = true)
data class SleepStages(
    @Json(name = "deep") val deep: Int,
    @Json(name = "light") val light: Int,
    @Json(name = "rem") val rem: Int,
    @Json(name = "wake") val wake: Int
)
