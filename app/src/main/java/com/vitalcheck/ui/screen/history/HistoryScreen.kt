package com.vitalcheck.ui.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.vitalcheck.ui.component.StatCard
import com.vitalcheck.ui.theme.DarkCard
import com.vitalcheck.ui.theme.VitalGreen
import com.vitalcheck.ui.theme.VitalOrange
import com.vitalcheck.ui.theme.VitalRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
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
            .padding(16.dp)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Summary stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Days Tracked",
                value = "${state.totalDaysTracked}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Goals Met",
                value = "${state.totalGoalsMet}",
                valueColor = VitalGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Best Streak",
                value = "${state.bestStreak}",
                valueColor = VitalOrange,
                modifier = Modifier.weight(1f)
            )
        }

        // Success rate
        if (state.totalDaysTracked > 0) {
            val successRate = (state.totalGoalsMet * 100) / state.totalDaysTracked
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$successRate% success rate",
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    successRate >= 80 -> VitalGreen
                    successRate >= 50 -> VitalOrange
                    else -> VitalRed
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.days.isEmpty()) {
            Text(
                text = "No history yet. Start logging food and syncing Fitbit to build your track record.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.days, key = { it.date }) { day ->
                    DayCard(day)
                }
            }
        }
    }
}

@Composable
private fun DayCard(day: DayRecord) {
    val date = LocalDate.parse(day.date)
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val formatted = date.format(DateTimeFormatter.ofPattern("MMM d"))
    val overCalories = day.caloriesConsumed > day.calorieTarget

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            when (day.goalsMet) {
                true -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Goals met",
                    tint = VitalGreen,
                    modifier = Modifier.size(28.dp)
                )
                false -> Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Goals missed",
                    tint = VitalRed,
                    modifier = Modifier.size(28.dp)
                )
                null -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "No data",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Date and details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$dayOfWeek $formatted",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (day.wasLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = VitalRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (day.currentStreak > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${day.currentStreak}d streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = VitalOrange
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${day.caloriesConsumed} / ${day.calorieTarget} cal",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overCalories) VitalRed else VitalGreen
                    )
                    day.steps?.let {
                        Text(
                            text = "%,d steps".format(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (it >= 8000) VitalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    day.activeMinutes?.let {
                        Text(
                            text = "${it}m active",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (it >= 20) VitalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
