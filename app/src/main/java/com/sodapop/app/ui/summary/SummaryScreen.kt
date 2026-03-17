package com.sodapop.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sodapop.app.domain.model.Summary
import com.sodapop.app.domain.model.SummaryType
import com.sodapop.app.ui.components.StreamingText
import com.sodapop.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val summaries by viewModel.summaries.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingContent by viewModel.streamingContent.collectAsState()
    val promotionSuggestions by viewModel.promotionSuggestions.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.error.value = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI 总结") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.generateDailySummary() },
                modifier = Modifier
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "生成今日总结")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Streaming card at top while generating
            if (isGenerating && streamingContent.isNotEmpty()) {
                item(key = "streaming") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📅 正在生成今日总结...",
                                style = MaterialTheme.typography.titleMedium
                            )
                            StreamingText(
                                text = streamingContent,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }

            // Promotion suggestions
            if (promotionSuggestions.isNotEmpty()) {
                item(key = "promotion_header") {
                    Text(
                        text = "✨ AI 建议提升",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(promotionSuggestions, key = { "promo_${it.thought.id}" }) { item ->
                    PromotionSuggestionCard(
                        item = item,
                        onAccept = { viewModel.acceptPromotion(item) },
                        onDismiss = { viewModel.dismissPromotion(item) }
                    )
                }
            }

            if (summaries.isEmpty() && !isGenerating) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", style = MaterialTheme.typography.headlineLarge)
                            Text(
                                text = "点击右下角按钮生成今日总结",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                items(summaries, key = { it.id }) { summary ->
                    SummaryCard(summary = summary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCard(summary: Summary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (summary.type) {
                    SummaryType.DAILY -> "📅 每日总结"
                    SummaryType.WEEKLY -> "📆 每周总结"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${DateUtils.formatDate(summary.periodStart)} ~ ${DateUtils.formatDate(summary.periodEnd)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = com.sodapop.app.util.SimpleMarkdown.parse(summary.content),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )

            if (summary.themes.isNotEmpty()) {
                Text(
                    text = "主题",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    summary.themes.forEach { theme ->
                        AssistChip(
                            onClick = {},
                            label = { Text(theme, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            if (summary.questions.isNotEmpty()) {
                Text(
                    text = "深入思考",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
                summary.questions.forEach { question ->
                    Text(
                        text = "• $question",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionSuggestionCard(
    item: PromotionItem,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.targetLayer == com.sodapop.app.domain.model.MemoryLayer.BELIEF)
                MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (item.targetLayer == com.sodapop.app.domain.model.MemoryLayer.BELIEF)
                    "💎 建议提升为信念" else "📌 建议提升为主题",
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = item.thought.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 3
            )

            Text(
                text = "理由: ${item.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("忽略")
                }
                TextButton(onClick = onAccept) {
                    Text(
                        if (item.targetLayer == com.sodapop.app.domain.model.MemoryLayer.BELIEF)
                            "确认为信念" else "确认为主题"
                    )
                }
            }
        }
    }
}
