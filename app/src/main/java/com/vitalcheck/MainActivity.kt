package com.vitalcheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vitalcheck.data.remote.fitbit.FitbitAuthManager
import com.vitalcheck.ui.navigation.VitalCheckNavGraph
import com.vitalcheck.ui.theme.VitalCheckTheme
import com.vitalcheck.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var fitbitAuthManager: FitbitAuthManager

    private val _targetScreen = MutableStateFlow<String?>(null)
    val targetScreen: StateFlow<String?> = _targetScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleFitbitRedirect(intent)
        handleNotificationDeepLink(intent)
        setContent {
            VitalCheckTheme {
                VitalCheckNavGraph(
                    isAuthenticated = fitbitAuthManager.hasValidToken(),
                    targetScreen = targetScreen
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleFitbitRedirect(intent)
        handleNotificationDeepLink(intent)
    }

    private fun handleFitbitRedirect(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "vitalcheck" && uri.host == "callback") {
            val code = uri.getQueryParameter("code") ?: return
            fitbitAuthManager.handleAuthCode(code)
        }
    }

    private fun handleNotificationDeepLink(intent: Intent?) {
        val screen = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_SCREEN)
        if (screen != null) {
            _targetScreen.value = screen
        }
    }

    fun clearTargetScreen() {
        _targetScreen.value = null
    }
}
