package com.sodapop.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sodapop.app.ui.theme.SodaRed

@Composable
fun VoiceInputButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isListening) SodaRed else MaterialTheme.colorScheme.primaryContainer,
        label = "mic_color"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = if (isListening) "停止录音" else "开始录音",
            modifier = Modifier.size(24.dp)
        )
    }
}
