package com.sodapop.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sodapop.app.domain.model.MemoryLayer
import com.sodapop.app.ui.theme.SodaGreen
import com.sodapop.app.ui.theme.SodaOrange
import com.sodapop.app.ui.theme.SodaPurple

@Composable
fun LayerBadge(
    layer: MemoryLayer,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (layer) {
        MemoryLayer.FRAGMENT -> "碎片" to SodaOrange
        MemoryLayer.TOPIC -> "主题" to SodaGreen
        MemoryLayer.BELIEF -> "信念" to SodaPurple
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
