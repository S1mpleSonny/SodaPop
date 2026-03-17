package com.sodapop.app.ui.memory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sodapop.app.ui.components.ThoughtCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    onNavigateToDialogue: (String) -> Unit,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val fragments by viewModel.fragments.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val beliefs by viewModel.beliefs.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "碎片 (${fragments.size})",
        "主题 (${topics.size})",
        "信念 (${beliefs.size})"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("记忆系统") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    ) {
                        Text(title, modifier = Modifier.padding(16.dp))
                    }
                }
            }

            val items = when (selectedTab) {
                0 -> fragments
                1 -> topics
                2 -> beliefs
                else -> emptyList()
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "暂无碎片想法"
                            1 -> "长按碎片想法可提升为主题"
                            2 -> "长按主题想法可提升为信念"
                            else -> ""
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { thought ->
                        ThoughtCard(
                            thought = thought,
                            onStartDialogue = { onNavigateToDialogue(thought.id) },
                            onPromote = { viewModel.promoteThought(thought) },
                            onDelete = { viewModel.deleteThought(thought) }
                        )
                    }
                }
            }
        }
    }
}
