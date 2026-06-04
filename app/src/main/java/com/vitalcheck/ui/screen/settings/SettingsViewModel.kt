package com.vitalcheck.ui.screen.settings

import android.app.AppOpsManager
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.vitalcheck.data.remote.fitbit.FitbitAuthManager
import com.vitalcheck.service.AppBlockerAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val fitbitAuthManager: FitbitAuthManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun isFitbitConnected(): Boolean = fitbitAuthManager.hasValidToken()

    fun isUsageStatsPermissionGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val serviceName = "${context.packageName}/${AppBlockerAccessibilityService::class.java.canonicalName}"
        return enabledServices.contains(serviceName)
    }
}
