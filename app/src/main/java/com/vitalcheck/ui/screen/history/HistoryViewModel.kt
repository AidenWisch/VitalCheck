package com.vitalcheck.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalcheck.data.local.dao.FoodLogDao
import com.vitalcheck.data.local.dao.DailyMetricsDao
import com.vitalcheck.data.local.dao.LockoutStateDao
import com.vitalcheck.data.local.dao.StreakDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayRecord(
    val date: String,
    val caloriesConsumed: Int,
    val calorieTarget: Int,
    val steps: Int?,
    val activeMinutes: Int?,
    val goalsMet: Boolean?,
    val currentStreak: Int,
    val wasLocked: Boolean
)

data class HistoryUiState(
    val days: List<DayRecord> = emptyList(),
    val isLoading: Boolean = true,
    val totalDaysTracked: Int = 0,
    val totalGoalsMet: Int = 0,
    val bestStreak: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val foodLogDao: FoodLogDao,
    private val metricsDao: DailyMetricsDao,
    private val streakDao: StreakDao,
    private val lockoutStateDao: LockoutStateDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val calorySummaries = foodLogDao.getDailyCalorySummaries()
            val streaks = streakDao.getRecent(90)
            val streakMap = streaks.associateBy { it.date }

            // Get all unique dates from food logs and streaks
            val allDates = (calorySummaries.map { it.date } + streaks.map { it.date })
                .distinct()
                .sortedDescending()

            val days = allDates.map { date ->
                val foodCals = calorySummaries.find { it.date == date }?.total ?: 0
                val metrics = metricsDao.getByDate(date)
                val streak = streakMap[date]
                val lockout = lockoutStateDao.getByDate(date)

                DayRecord(
                    date = date,
                    caloriesConsumed = foodCals,
                    calorieTarget = 2000,
                    steps = metrics?.steps,
                    activeMinutes = metrics?.activeMinutes,
                    goalsMet = streak?.goalsMet,
                    currentStreak = streak?.currentStreak ?: 0,
                    wasLocked = lockout?.isLocked ?: false
                )
            }

            val totalGoalsMet = days.count { it.goalsMet == true }
            val bestStreak = streaks.maxOfOrNull { it.longestStreak } ?: 0

            _uiState.value = HistoryUiState(
                days = days,
                isLoading = false,
                totalDaysTracked = days.size,
                totalGoalsMet = totalGoalsMet,
                bestStreak = bestStreak
            )
        }
    }
}
