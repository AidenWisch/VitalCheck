package com.vitalcheck.data.repository

import com.squareup.moshi.Moshi
import com.vitalcheck.data.local.dao.FoodLogDao
import com.vitalcheck.data.local.entity.FoodLogEntity
import com.vitalcheck.data.remote.anthropic.AnthropicApiService
import com.vitalcheck.data.remote.anthropic.dto.FoodEstimateDto
import com.vitalcheck.data.remote.anthropic.dto.MessageDto
import com.vitalcheck.data.remote.anthropic.dto.MessagesRequestDto
import com.vitalcheck.domain.model.FoodEntry
import com.vitalcheck.util.Constants
import com.vitalcheck.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodLogRepository @Inject constructor(
    private val anthropicApi: AnthropicApiService,
    private val foodLogDao: FoodLogDao,
    private val moshi: Moshi
) {
    suspend fun logFood(rawInput: String): FoodEntry {
        val date = DateUtils.today()

        val response = anthropicApi.createMessage(
            MessagesRequestDto(
                model = Constants.HAIKU_MODEL,
                maxTokens = 256,
                system = FOOD_SYSTEM_PROMPT,
                messages = listOf(MessageDto(role = "user", content = rawInput))
            )
        )

        val rawJson = response.content.firstOrNull { it.type == "text" }?.text
            ?: throw IllegalStateException("No text content in response")

        val jsonStr = extractJson(rawJson)
        val estimate = moshi.adapter(FoodEstimateDto::class.java).fromJson(jsonStr)
            ?: throw IllegalStateException("Failed to parse food estimate")

        val entity = FoodLogEntity(
            date = date,
            rawInput = rawInput,
            estimatedCalories = estimate.estimatedCalories,
            confidence = estimate.confidence,
            loggedAt = System.currentTimeMillis()
        )

        val id = foodLogDao.insert(entity)
        return entity.copy(id = id).toDomain()
    }

    fun observeFoodLogs(date: String): Flow<List<FoodEntry>> =
        foodLogDao.observeByDate(date).map { list -> list.map { it.toDomain() } }

    fun observeDailyCalories(date: String): Flow<Int> =
        foodLogDao.observeDailyCalories(date)

    suspend fun getDailyCalories(date: String): Int =
        foodLogDao.getDailyCalories(date)

    suspend fun logManualCalories(label: String, calories: Int): FoodEntry {
        val date = DateUtils.today()
        val entity = FoodLogEntity(
            date = date,
            rawInput = label,
            estimatedCalories = calories,
            confidence = "manual",
            loggedAt = System.currentTimeMillis()
        )
        val id = foodLogDao.insert(entity)
        return entity.copy(id = id).toDomain()
    }

    suspend fun deleteEntry(id: Long) =
        foodLogDao.deleteById(id)

    private fun extractJson(raw: String): String {
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(\\{.*?\\})\\s*\\n?```", RegexOption.DOT_MATCHES_ALL)
        val match = codeBlockRegex.find(raw)
        if (match != null) return match.groupValues[1]
        val trimmed = raw.trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start >= 0 && end > start) return trimmed.substring(start, end + 1)
        return trimmed
    }

    companion object {
        private val FOOD_SYSTEM_PROMPT = """
            You are a nutrition estimation engine. Given a food description, respond with ONLY valid JSON (no markdown, no explanation) matching this schema:
            {
              "estimated_calories": <integer>,
              "confidence": "high" | "medium" | "low",
              "items": [{"name": "<string>", "calories": <integer>}]
            }
            Be accurate. When uncertain, overestimate slightly. Account for typical portion sizes.
            Always break down combo meals into individual items.
        """.trimIndent()
    }
}

private fun FoodLogEntity.toDomain() = FoodEntry(
    id = id,
    date = date,
    rawInput = rawInput,
    estimatedCalories = estimatedCalories,
    confidence = confidence,
    loggedAt = loggedAt
)
