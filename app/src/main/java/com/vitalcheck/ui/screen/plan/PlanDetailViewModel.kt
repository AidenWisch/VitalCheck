package com.vitalcheck.ui.screen.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalcheck.data.repository.AiPlanRepository
import com.vitalcheck.domain.model.CoachingPlan
import com.vitalcheck.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    aiPlanRepository: AiPlanRepository
) : ViewModel() {

    val plan: StateFlow<CoachingPlan?> = aiPlanRepository.observePlan(DateUtils.today())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
