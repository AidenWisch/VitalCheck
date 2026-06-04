package com.vitalcheck.service

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalcheck.ui.theme.VitalCheckTheme

class LockoutOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reason = intent.getStringExtra("reason") ?: "goals_missed"
        val blockedApp = intent.getStringExtra("blocked_app") ?: ""

        setContent {
            VitalCheckTheme {
                LockoutScreen(
                    reason = reason,
                    blockedApp = blockedApp,
                    onGoBack = { finish() }
                )
            }
        }
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        // Go to home screen instead of back to blocked app
        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
private fun LockoutScreen(
    reason: String,
    blockedApp: String,
    onGoBack: () -> Unit
) {
    val appName = when {
        blockedApp.contains("instagram") -> "Instagram"
        blockedApp.contains("snapchat") -> "Snapchat"
        blockedApp.contains("youtube") -> "YouTube"
        else -> "This app"
    }

    val reasonText = when {
        reason.contains("calorie_surplus") -> "You exceeded your calorie target."
        reason.contains("missed_workout") -> "You skipped your workout."
        reason.contains("missed_steps") -> "You didn't hit your step goal."
        else -> "You didn't meet your goals."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color(0xFFFF3B30),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$appName is LOCKED",
            color = Color(0xFFFF3B30),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = reasonText,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Log a workout or hit your step goal to unlock.",
            color = Color(0xFF888888),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGoBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF3B30)
            )
        ) {
            Text(
                text = "Go to VitalCheck",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
