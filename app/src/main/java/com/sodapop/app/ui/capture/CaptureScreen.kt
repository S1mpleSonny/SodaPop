package com.sodapop.app.ui.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sodapop.app.domain.model.ThoughtType
import com.sodapop.app.ui.components.VoiceInputButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onDismiss: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val content by viewModel.content.collectAsState()
    val thoughtType by viewModel.thoughtType.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val savedEvent by viewModel.savedEvent.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(savedEvent) {
        if (savedEvent) {
            viewModel.resetSavedEvent()
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录想法") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = content.isNotBlank() && !isSaving
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Type selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThoughtType.entries.forEach { type ->
                    FilterChip(
                        selected = thoughtType == type,
                        onClick = { viewModel.updateType(type) },
                        label = {
                            Text(
                                when (type) {
                                    ThoughtType.IDEA -> "💡 想法"
                                    ThoughtType.PREDICTION -> "🔮 预测"
                                    ThoughtType.REFLECTION -> "💭 反思"
                                }
                            )
                        }
                    )
                }
            }

            // Text input
            TextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        when (thoughtType) {
                            ThoughtType.IDEA -> "此刻你在想什么..."
                            ThoughtType.PREDICTION -> "你预测会发生什么..."
                            ThoughtType.REFLECTION -> "你在反思什么..."
                        }
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            // Voice input button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceInputButton(
                    isListening = isListening,
                    onClick = { viewModel.toggleVoiceInput() }
                )
                if (isListening) {
                    Text(
                        text = "正在聆听...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}
