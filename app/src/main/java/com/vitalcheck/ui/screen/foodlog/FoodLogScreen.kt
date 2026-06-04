package com.vitalcheck.ui.screen.foodlog

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitalcheck.ui.component.FoodEntryRow
import com.vitalcheck.ui.theme.VitalOrange
import com.vitalcheck.ui.theme.VitalRed

@Composable
fun FoodLogScreen(
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var manualLabel by remember { mutableStateOf("") }
    var manualCalories by remember { mutableStateOf("") }
    var isManualMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Food Log",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Daily calorie total
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "%,d".format(state.dailyCalories),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = VitalOrange
            )
            Text(
                text = " / 2,000 cal today",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !isManualMode,
                onClick = { isManualMode = false },
                label = { Text("AI Estimate") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            )
            FilterChip(
                selected = isManualMode,
                onClick = { isManualMode = true },
                label = { Text("Manual Entry") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isManualMode) {
            // Manual calorie entry
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualLabel,
                    onValueChange = { manualLabel = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("What (e.g. lunch)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = manualCalories,
                    onValueChange = { manualCalories = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.width(100.dp),
                    placeholder = { Text("Cal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        val cal = manualCalories.toIntOrNull() ?: 0
                        if (cal > 0) {
                            viewModel.logManualCalories(manualLabel, cal)
                            manualLabel = ""
                            manualCalories = ""
                        }
                    },
                    enabled = manualCalories.isNotBlank() && (manualCalories.toIntOrNull() ?: 0) > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add calories",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // AI estimation input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Describe what you ate...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    enabled = !state.isLogging,
                    singleLine = false,
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        viewModel.logFood(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !state.isLogging
                ) {
                    if (state.isLogging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Log food",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Error message
        state.error?.let { error ->
            Text(
                text = error,
                color = VitalRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Food entries list
        if (state.entries.isEmpty()) {
            Text(
                text = "No food logged today. Log what you ate above.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    FoodEntryRow(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}
