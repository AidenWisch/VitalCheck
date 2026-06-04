package com.vitalcheck.data.remote.fitbit

import com.vitalcheck.data.remote.fitbit.dto.ActivitySummaryDto
import com.vitalcheck.data.remote.fitbit.dto.HeartRateDto
import com.vitalcheck.data.remote.fitbit.dto.SleepLogDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FitbitApiService {

    @GET("/1/user/-/activities/date/{date}.json")
    suspend fun getDailyActivitySummary(
        @Path("date") date: String
    ): ActivitySummaryDto

    @GET("/1/user/-/activities/heart/date/{date}/1d.json")
    suspend fun getHeartRateByDate(
        @Path("date") date: String
    ): HeartRateDto

    @GET("/1.2/user/-/sleep/date/{date}.json")
    suspend fun getSleepLogByDate(
        @Path("date") date: String
    ): SleepLogDto
}
