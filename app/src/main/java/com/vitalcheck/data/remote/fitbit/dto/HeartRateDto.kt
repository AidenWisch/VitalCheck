package com.vitalcheck.data.remote.fitbit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HeartRateDto(
    @Json(name = "activities-heart") val activitiesHeart: List<HeartRateDay>
)

@JsonClass(generateAdapter = true)
data class HeartRateDay(
    @Json(name = "dateTime") val dateTime: String,
    @Json(name = "value") val value: HeartRateValue
)

@JsonClass(generateAdapter = true)
data class HeartRateValue(
    @Json(name = "restingHeartRate") val restingHeartRate: Int? = null,
    @Json(name = "heartRateZones") val heartRateZones: List<HeartRateZone>
)
