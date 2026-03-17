package com.sodapop.app.ui.prediction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sodapop.app.domain.model.Prediction
import com.sodapop.app.ui.components.StreamingText
import com.sodapop.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val pending by viewModel.pendingPredictions.collectAsState()
    val reviewed by viewModel.reviewedPredictions.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val streamingContent by viewModel.streamingContent.collectAsState()
    val avgAccuracy by viewModel.averageAccuracy.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var reviewingPrediction by remember { mutableStateOf<Prediction?>(null) }
    var outcomeText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预测追踪") },
                actions = {
                    avgAccuracy?.let {
                        Text(
                            text = "准确率: ${(it * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("待验证 (${pending.size})", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("已验证 (${reviewed.size})", modifier = Modifier.padding(16.dp))
                }
            }

            val items = if (selectedTab == 0) pending else reviewed

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) "暂无待验证的预测" else "暂无已验证的预测",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { prediction ->
                        PredictionCard(
                            prediction = prediction,
                            isPending = selectedTab == 0,
                            onReview = {
                                reviewingPrediction = prediction
                                outcomeText = ""
                            }
                        )
                    }
                }
            }
        }

        // Review dialog
        reviewingPrediction?.let { prediction ->
            AlertDialog(
                onDismissRequest = {
                    if (!isAnalyzing) reviewingPrediction = null
                },
                title = { Text("验证预测") },
                text = {
                    Column {
                        Text(
                            text = "预测内容: ${prediction.predictedOutcome}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = outcomeText,
                            onValueChange = { outcomeText = it },
                            label = { Text("实际结果") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            minLines = 3,
                            enabled = !isAnalyzing
                        )
                        if (streamingContent.isNotEmpty()) {
                            Text(
                                text = "AI 分析:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            StreamingText(
                                text = streamingContent,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.reviewPrediction(prediction.id, outcomeText)
                        },
                        enabled = outcomeText.isNotBlank() && !isAnalyzing
                    ) { Text("提交") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { reviewingPrediction = null },
                        enabled = !isAnalyzing
                    ) { Text("取消") }
                }
            )
        }
    }
}

@Composable
private fun PredictionCard(
    prediction: Prediction,
    isPending: Boolean,
    onReview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🔮 预测", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = if (isPending) "回顾日期: ${DateUtils.formatDate(prediction.reviewDate)}"
                    else "准确度: ${((prediction.accuracy ?: 0f) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = prediction.predictedOutcome,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (!isPending && prediction.llmAnalysis != null) {
                Text(
                    text = com.sodapop.app.util.SimpleMarkdown.parse(prediction.llmAnalysis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (isPending && prediction.reviewDate <= System.currentTimeMillis()) {
                TextButton(onClick = onReview, modifier = Modifier.align(Alignment.End)) {
                    Text("验证")
                }
            }
        }
    }
}
