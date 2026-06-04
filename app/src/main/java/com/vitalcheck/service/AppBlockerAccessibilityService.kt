package com.vitalcheck.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.vitalcheck.data.local.VitalCheckDatabase
import com.vitalcheck.util.Constants
import com.vitalcheck.util.DateUtils
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppBlockerAccessibilityService : AccessibilityService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServiceEntryPoint {
        fun database(): VitalCheckDatabase
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var database: VitalCheckDatabase? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        )
        database = entryPoint.database()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName !in Constants.BLOCKED_PACKAGES) return

        scope.launch {
            val today = DateUtils.today()
            val lockoutState = database?.lockoutStateDao()?.getByDate(today) ?: return@launch

            if (lockoutState.isLocked) {
                val intent = Intent(applicationContext, LockoutOverlayActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("reason", lockoutState.reason)
                    putExtra("blocked_app", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
