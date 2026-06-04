package com.vitalcheck.data.repository

import com.squareup.moshi.Moshi
import com.vitalcheck.data.local.dao.AiPlanDao
import com.vitalcheck.data.local.entity.AiPlanEntity
import com.vitalcheck.data.local.entity.NotificationMessageEntity
import com.vitalcheck.data.remote.anthropic.AnthropicApiService
import com.vitalcheck.data.remote.anthropic.dto.CoachingPlanDto
import com.vitalcheck.data.remote.anthropic.dto.MessageDto
import com.vitalcheck.data.remote.anthropic.dto.MessagesRequestDto
import com.vitalcheck.domain.model.CoachingPlan
import com.vitalcheck.domain.model.DailyMetrics
import com.vitalcheck.domain.model.NotificationMessage
import com.vitalcheck.domain.model.Streak
import com.vitalcheck.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPlanRepository @Inject constructor(
    private val anthropicApi: AnthropicApiService,
    private val aiPlanDao: AiPlanDao,
    private val moshi: Moshi
) {
    suspend fun generateAndSavePlan(
        date: String,
        metrics: DailyMetrics,
        streak: Streak?,
        foodCaloriesToday: Int
    ): CoachingPlan {
        val systemPrompt = buildSystemPrompt()
        val userMessage = buildUserMessage(metrics, streak, foodCaloriesToday)

        val response = anthropicApi.createMessage(
            MessagesRequestDto(
                model = Constants.HAIKU_MODEL,
                maxTokens = 1024,
                system = systemPrompt,
                messages = listOf(MessageDto(role = "user", content = userMessage))
            )
        )

        val rawJson = response.content.firstOrNull { it.type == "text" }?.text
            ?: throw IllegalStateException("No text content in response")

        // Extract JSON from response (may be wrapped in markdown code blocks)
        val jsonStr = extractJson(rawJson)
        val planDto = moshi.adapter(CoachingPlanDto::class.java).fromJson(jsonStr)
            ?: throw IllegalStateException("Failed to parse coaching plan")

        val planEntity = AiPlanEntity(
            date = date,
            calorieTarget = planDto.calorieTarget,
            workoutType = planDto.workout.type,
            workoutDescription = planDto.workout.description,
            workoutDurationMin = planDto.workout.durationMinutes,
            rawJson = rawJson,
            generatedAt = System.currentTimeMillis()
        )

        val notificationEntities = planDto.notifications.map { notif ->
            NotificationMessageEntity(
                planDate = date,
                scheduledHour = notif.hour,
                scheduledMinute = notif.minute,
                message = notif.message
            )
        }

        aiPlanDao.savePlanWithNotifications(planEntity, notificationEntities)

        return CoachingPlan(
            date = date,
            calorieTarget = planDto.calorieTarget,
            workoutType = planDto.workout.type,
            workoutDescription = planDto.workout.description,
            workoutDurationMin = planDto.workout.durationMinutes,
            notifications = planDto.notifications.map {
                NotificationMessage(it.hour, it.minute, it.message)
            }
        )
    }

    fun observePlan(date: String): Flow<CoachingPlan?> =
        aiPlanDao.observePlanByDate(date).map { plan ->
            if (plan == null) return@map null
            val notifications = aiPlanDao.getNotificationsForDate(date)
            CoachingPlan(
                date = plan.date,
                calorieTarget = plan.calorieTarget,
                workoutType = plan.workoutType,
                workoutDescription = plan.workoutDescription,
                workoutDurationMin = plan.workoutDurationMin,
                notifications = notifications.map {
                    NotificationMessage(it.scheduledHour, it.scheduledMinute, it.message)
                }
            )
        }

    suspend fun getPlan(date: String): CoachingPlan? {
        val plan = aiPlanDao.getPlanByDate(date) ?: return null
        val notifications = aiPlanDao.getNotificationsForDate(date)
        return CoachingPlan(
            date = plan.date,
            calorieTarget = plan.calorieTarget,
            workoutType = plan.workoutType,
            workoutDescription = plan.workoutDescription,
            workoutDurationMin = plan.workoutDurationMin,
            notifications = notifications.map {
                NotificationMessage(it.scheduledHour, it.scheduledMinute, it.message)
            }
        )
    }

    suspend fun getNotificationsForDate(date: String) =
        aiPlanDao.getNotificationsForDate(date)

    suspend fun markNotificationFired(id: Long) =
        aiPlanDao.markNotificationFired(id)

    suspend fun setNotificationWorkRequestId(id: Long, workRequestId: String) =
        aiPlanDao.setWorkRequestId(id, workRequestId)

    private fun buildSystemPrompt(): String = """
        You are a brutally honest, no-nonsense personal health coach. You do NOT sugarcoat anything.
        You are harsh, direct, and sometimes savage — but always with the goal of pushing the user to be better.

        Your job is to analyze the user's Fitbit health data and generate a daily plan.

        You MUST respond with ONLY valid JSON (no markdown, no explanation) matching this exact schema:
        {
          "calorie_target": <integer - daily calorie intake target>,
          "workout": {
            "type": "<string - e.g. HIIT, Zone 2 Run, Strength Training>",
            "description": "<string - specific workout with exercises, sets, reps>",
            "duration_minutes": <integer>
          },
          "notifications": [
            {
              "hour": <0-23>,
              "minute": <0-59>,
              "message": "<string - brutal, personalized notification message referencing their actual data>"
            }
          ]
        }

        Rules for notifications:
        - Generate exactly 3-5 notification messages
        - Space them throughout the day (morning, midday, afternoon, evening)
        - Each message MUST reference specific numbers from their data
        - Be savage but motivating. Examples of tone:
          - "You burned 1,400 calories yesterday and slept 4 hours. Your body is running on spite. Get a 30-minute walk in before noon or don't bother eating lunch."
          - "6,200 steps? My grandmother hits that before breakfast. You have until 3 PM to double it."
          - "Your resting heart rate is 78. For someone who claims to work out, that's embarrassing. Today's HIIT session is non-negotiable."

        Rules for calorie target:
        - Base it on their activity level and goals
        - If they've been sedentary, be aggressive with a deficit
        - Factor in their food log calories if provided

        Rules for workout:
        - Tailor to their fitness level based on the data
        - Be specific: exercises, sets, reps, rest times
        - If they slept poorly, adjust intensity but don't let them skip
    """.trimIndent()

    private fun buildUserMessage(
        metrics: DailyMetrics,
        streak: Streak?,
        foodCaloriesToday: Int
    ): String = buildString {
        appendLine("Here is my health data from yesterday:")
        appendLine("- Steps: ${metrics.steps}")
        appendLine("- Calories burned: ${metrics.caloriesBurned}")
        appendLine("- Active minutes: ${metrics.activeMinutes}")
        metrics.restingHeartRate?.let { appendLine("- Resting heart rate: $it bpm") }
        metrics.hrZoneFatBurnMin?.let { appendLine("- Fat burn zone: $it min") }
        metrics.hrZoneCardioMin?.let { appendLine("- Cardio zone: $it min") }
        metrics.hrZonePeakMin?.let { appendLine("- Peak zone: $it min") }
        metrics.totalSleepMin?.let {
            val hours = it / 60
            val mins = it % 60
            appendLine("- Total sleep: ${hours}h ${mins}m")
        }
        metrics.sleepDeepMin?.let { appendLine("- Deep sleep: $it min") }
        metrics.sleepRemMin?.let { appendLine("- REM sleep: $it min") }

        if (streak != null) {
            appendLine("\nCurrent streak: ${streak.currentStreak} days")
            appendLine("Longest streak: ${streak.longestStreak} days")
        }

        if (foodCaloriesToday > 0) {
            appendLine("\nFood calories logged today so far: $foodCaloriesToday")
        }
    }

    private fun extractJson(raw: String): String {
        // Try to extract JSON from markdown code blocks
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(\\{.*?})\\s*\\n?```", RegexOption.DOT_MATCHES_ALL)
        val match = codeBlockRegex.find(raw)
        if (match != null) return match.groupValues[1]

        // Otherwise assume the whole thing is JSON
        val trimmed = raw.trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start >= 0 && end > start) return trimmed.substring(start, end + 1)

        return trimmed
    }
}
