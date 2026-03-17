package com.sodapop.app.ui.dialogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sodapop.app.domain.model.ChatMessage
import com.sodapop.app.domain.model.DialogueMode
import com.sodapop.app.ui.components.LoadingDots
import com.sodapop.app.ui.components.StreamingText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueScreen(
    onBack: () -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val thought by viewModel.thought.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val streamingContent by viewModel.streamingContent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mode by viewModel.mode.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showModeMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom
    LaunchedEffect(messages.size, streamingContent) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "思维对话",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = thought?.content?.take(30) ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { showModeMenu = true }) {
                            Text(
                                when (mode) {
                                    DialogueMode.DEVILS_ADVOCATE -> "反面推敲"
                                    DialogueMode.EXPANSION -> "联想拓展"
                                    DialogueMode.FEASIBILITY -> "可行评估"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            DialogueMode.entries.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (m) {
                                                DialogueMode.DEVILS_ADVOCATE -> "🔍 反面推敲"
                                                DialogueMode.EXPANSION -> "💡 联想拓展"
                                                DialogueMode.FEASIBILITY -> "📋 可行评估"
                                            }
                                        )
                                    },
                                    onClick = {
                                        showModeMenu = false
                                        viewModel.changeMode(m)
                                    }
                                )
                            }
                        }
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
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter out system messages for display
                val displayMessages = messages.filter { it.role != "system" }
                items(displayMessages) { message ->
                    MessageBubble(message = message)
                }

                // Streaming content
                if (streamingContent.isNotEmpty()) {
                    item {
                        MessageBubble(
                            message = ChatMessage("assistant", ""),
                            streamingText = streamingContent
                        )
                    }
                }

                // Loading indicator
                if (isLoading && streamingContent.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            LoadingDots()
                        }
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入你的想法...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !isLoading
                ) {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    streamingText: String? = null
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            if (streamingText != null) {
                StreamingText(text = streamingText)
            } else if (!isUser) {
                // AI messages: render lightweight Markdown
                val parsed = remember(message.content) {
                    com.sodapop.app.util.SimpleMarkdown.parse(message.content)
                }
                Text(
                    text = parsed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // User messages: plain text
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
