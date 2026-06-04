package com.vitalcheck.ui.screen.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalcheck.data.repository.FoodLogRepository
import com.vitalcheck.domain.model.FoodEntry
import com.vitalcheck.domain.usecase.LogFoodUseCase
import com.vitalcheck.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodLogUiState(
    val entries: List<FoodEntry> = emptyList(),
    val dailyCalories: Int = 0,
    val isLogging: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FoodLogViewModel @Inject constructor(
    private val logFoodUseCase: LogFoodUseCase,
    private val foodLogRepository: FoodLogRepository
) : ViewModel() {

    private val today = DateUtils.today()
    private val _isLogging = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FoodLogUiState> = combine(
        foodLogRepository.observeFoodLogs(today),
        foodLogRepository.observeDailyCalories(today),
        _isLogging,
        _error
    ) { entries, calories, isLogging, error ->
        FoodLogUiState(
            entries = entries,
            dailyCalories = calories,
            isLogging = isLogging,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoodLogUiState()
    )

    fun logFood(rawInput: String) {
        if (rawInput.isBlank()) return
        viewModelScope.launch {
            _isLogging.value = true
            _error.value = null
            try {
                logFoodUseCase(rawInput)
            } catch (e: Exception) {
                _error.value = "Failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isLogging.value = false
            }
        }
    }

    fun logManualCalories(label: String, calories: Int) {
        if (calories <= 0) return
        viewModelScope.launch {
            _error.value = null
            try {
                foodLogRepository.logManualCalories(
                    label = label.ifBlank { "Manual entry" },
                    calories = calories
                )
            } catch (e: Exception) {
                _error.value = "Failed to save: ${e.message}"
            }
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            foodLogRepository.deleteEntry(id)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
