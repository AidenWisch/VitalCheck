package com.vitalcheck.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalcheck.ui.theme.VitalRed

@Composable
fun LockoutBanner(reason: String?, modifier: Modifier = Modifier) {
    val reasonText = when {
        reason == null -> "Apps are locked."
        reason.contains("calorie_surplus") -> "Calorie target exceeded."
        reason.contains("missed_workout") -> "Workout missed."
        reason.contains("missed_steps") -> "Step goal not reached."
        else -> "Goals not met."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = VitalRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = VitalRed
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LOCKOUT ACTIVE",
                color = VitalRed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Text(
            text = "$reasonText Instagram, Snapchat, and YouTube are blocked until you fix this.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
