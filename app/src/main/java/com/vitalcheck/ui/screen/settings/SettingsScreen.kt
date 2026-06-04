package com.vitalcheck.ui.screen.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalcheck.ui.theme.DarkCard
import com.vitalcheck.ui.theme.VitalGreen
import com.vitalcheck.ui.theme.VitalRed

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Fitbit connection
        SettingsRow(
            icon = Icons.Default.Watch,
            title = "Fitbit Connection",
            isEnabled = viewModel.isFitbitConnected(),
            enabledText = "Connected",
            disabledText = "Not connected",
            onAction = null
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Usage stats permission
        SettingsRow(
            icon = Icons.Default.DataUsage,
            title = "Usage Stats Permission",
            isEnabled = viewModel.isUsageStatsPermissionGranted(),
            enabledText = "Granted",
            disabledText = "Required for app lockout",
            onAction = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Accessibility service
        SettingsRow(
            icon = Icons.Default.Accessibility,
            title = "Accessibility Service",
            isEnabled = viewModel.isAccessibilityServiceEnabled(),
            enabledText = "Enabled",
            disabledText = "Required for app lockout",
            onAction = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Blocked Apps",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Instagram, Snapchat, YouTube",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "These apps will be locked when you miss your daily goals.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    isEnabled: Boolean,
    enabledText: String,
    disabledText: String,
    onAction: (() -> Unit)?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) VitalGreen else VitalRed
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isEnabled) VitalGreen else VitalRed,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = if (isEnabled) enabledText else disabledText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEnabled) VitalGreen else VitalRed
                    )
                }
            }
            if (!isEnabled && onAction != null) {
                TextButton(onClick = onAction) {
                    Text("Enable")
                }
            }
        }
    }
}
