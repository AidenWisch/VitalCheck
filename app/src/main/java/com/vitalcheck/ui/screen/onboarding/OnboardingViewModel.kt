package com.vitalcheck.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import com.vitalcheck.data.remote.fitbit.FitbitAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val fitbitAuthManager: FitbitAuthManager
) : ViewModel() {

    fun getAuthUrl(): String = fitbitAuthManager.buildAuthUrl()

    fun isAuthenticated(): Boolean = fitbitAuthManager.hasValidToken()
}
