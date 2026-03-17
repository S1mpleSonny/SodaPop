package com.sodapop.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.sodapop.app.util.SimpleMarkdown

@Composable
fun StreamingText(
    text: String,
    modifier: Modifier = Modifier
) {
    val parsed = remember(text) { SimpleMarkdown.parse(text) }

    Text(
        text = parsed,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}
