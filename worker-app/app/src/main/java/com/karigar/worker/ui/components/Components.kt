package com.karigar.worker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "btnScale")
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
            }
        } else {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun money(v: Double?): String {
    if (v == null) return "-"
    return if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
}

fun statusLabel(status: String?): String = when (status) {
    "assigned" -> "Assigned"
    "in_progress" -> "In progress"
    "awaiting_payment" -> "Awaiting payment"
    "completed" -> "Completed"
    else -> status ?: ""
}
