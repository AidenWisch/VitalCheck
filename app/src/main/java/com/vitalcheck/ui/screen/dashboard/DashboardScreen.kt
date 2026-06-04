package com.vitalcheck.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitalcheck.ui.component.LockoutBanner
import com.vitalcheck.ui.component.StatCard
import com.vitalcheck.ui.component.StreakBadge
import com.vitalcheck.ui.theme.DarkCard
import com.vitalcheck.ui.theme.VitalGreen
import com.vitalcheck.ui.theme.VitalOrange
import com.vitalcheck.ui.theme.VitalRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    onNavigateToPlan: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VitalCheck",
                style = MaterialTheme.typography.headlineLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                state.streak?.let { streak ->
                    if (streak.currentStreak > 0) {
                        StreakBadge(currentStreak = streak.currentStreak)
                    }
                }
                // Manual sync button
                IconButton(
                    onClick = { viewModel.syncNow() },
                    enabled = !state.isSyncing
                ) {
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Fitbit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lockout banner
        val lockout = state.lockoutState
        if (lockout != null && lockout.isLocked) {
            LockoutBanner(reason = lockout.reason)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Today's live stats
        val todayMetrics = state.todayMetrics
        if (todayMetrics != null) {
            Text(
                text = "Today (Live)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Steps",
                    value = "%,d".format(todayMetrics.steps),
                    valueColor = if (todayMetrics.steps >= 8000) VitalGreen else VitalRed
                )
                StatCard(
                    label = "Burned",
                    value = "%,d".format(todayMetrics.caloriesBurned),
                    valueColor = VitalOrange
                )
                StatCard(
                    label = "Active Min",
                    value = "${todayMetrics.activeMinutes}",
                    valueColor = if (todayMetrics.activeMinutes >= 20) VitalGreen else VitalRed
                )
                todayMetrics.restingHeartRate?.let { rhr ->
                    StatCard(
                        label = "RHR",
                        value = "$rhr bpm"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Yesterday's stats
        val yesterdayMetrics = state.yesterdayMetrics
        if (yesterdayMetrics != null) {
            Text(
                text = "Yesterday",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Steps",
                    value = "%,d".format(yesterdayMetrics.steps),
                    valueColor = if (yesterdayMetrics.steps >= 8000) VitalGreen else VitalRed
                )
                StatCard(
                    label = "Burned",
                    value = "%,d".format(yesterdayMetrics.caloriesBurned),
                    valueColor = VitalOrange
                )
                StatCard(
                    label = "Active Min",
                    value = "${yesterdayMetrics.activeMinutes}",
                    valueColor = if (yesterdayMetrics.activeMinutes >= 20) VitalGreen else VitalRed
                )
                yesterdayMetrics.totalSleepMin?.let { sleep ->
                    StatCard(
                        label = "Sleep",
                        value = "${sleep / 60}h ${sleep % 60}m",
                        valueColor = if (sleep >= 420) VitalGreen else VitalOrange
                    )
                }
            }
        } else if (todayMetrics == null) {
            Text(
                text = "No data synced yet. Tap the sync button or connect Fitbit to get started.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Food calories today
        val plan = state.plan
        val calorieTarget = plan?.calorieTarget ?: 2000
        Text(
            text = "Today's Calories",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "%,d".format(state.foodCaloriesToday),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.foodCaloriesToday > calorieTarget)
                            VitalRed else VitalGreen
                    )
                    Text(
                        text = "consumed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%,d".format(calorieTarget),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Plan summary
        if (plan != null) {
            Text(
                text = "Today's Plan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                onClick = onNavigateToPlan,
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = plan.workoutType,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${plan.workoutDurationMin} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = plan.workoutDescription,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${plan.notifications.size} notifications scheduled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Streak info
        state.streak?.let { streak ->
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Streaks",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Current",
                    value = "${streak.currentStreak}",
                    valueColor = if (streak.currentStreak > 0) VitalGreen else VitalRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Best",
                    value = "${streak.longestStreak}",
                    valueColor = VitalOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
