package com.sodapop.app.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = modifier
    )
}
