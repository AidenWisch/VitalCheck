package com.vitalcheck.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalcheck.data.repository.AiPlanRepository
import com.vitalcheck.data.repository.FoodLogRepository
import com.vitalcheck.data.repository.LockoutRepository
import com.vitalcheck.data.repository.MetricsRepository
import com.vitalcheck.data.repository.StreakRepository
import com.vitalcheck.domain.model.CoachingPlan
import com.vitalcheck.domain.model.DailyMetrics
import com.vitalcheck.domain.model.LockoutState
import com.vitalcheck.domain.model.Streak
import com.vitalcheck.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val todayMetrics: DailyMetrics? = null,
    val yesterdayMetrics: DailyMetrics? = null,
    val plan: CoachingPlan? = null,
    val streak: Streak? = null,
    val lockoutState: LockoutState? = null,
    val foodCaloriesToday: Int = 0,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val metricsRepository: MetricsRepository,
    aiPlanRepository: AiPlanRepository,
    streakRepository: StreakRepository,
    lockoutRepository: LockoutRepository,
    foodLogRepository: FoodLogRepository
) : ViewModel() {

    private val today = DateUtils.today()
    private val yesterday = DateUtils.yesterday()
    private val _isSyncing = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        metricsRepository.observeMetrics(today),
        metricsRepository.observeMetrics(yesterday),
        aiPlanRepository.observePlan(today),
        streakRepository.observeStreak(today),
        lockoutRepository.observeLockoutState(today),
        foodLogRepository.observeDailyCalories(today),
        _isSyncing
    ) { values ->
        DashboardUiState(
            todayMetrics = values[0] as DailyMetrics?,
            yesterdayMetrics = values[1] as DailyMetrics?,
            plan = values[2] as CoachingPlan?,
            streak = values[3] as Streak?,
            lockoutState = values[4] as LockoutState?,
            foodCaloriesToday = values[5] as Int,
            isSyncing = values[6] as Boolean,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                metricsRepository.syncMetrics(today)
            } catch (_: Exception) { }
            _isSyncing.value = false
        }
    }
}
