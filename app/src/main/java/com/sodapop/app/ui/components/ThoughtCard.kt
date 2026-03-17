package com.sodapop.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sodapop.app.domain.model.MemoryLayer
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.domain.model.ThoughtType
import com.sodapop.app.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThoughtCard(
    thought: Thought,
    onStartDialogue: () -> Unit = {},
    onPromote: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: type badge + time + menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LayerBadge(thought.layer)
                    if (thought.type != ThoughtType.IDEA) {
                        Text(
                            text = when (thought.type) {
                                ThoughtType.PREDICTION -> "🔮"
                                ThoughtType.REFLECTION -> "💭"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = DateUtils.formatRelativeDate(thought.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onStartDialogue) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "开始对话",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (thought.layer != MemoryLayer.BELIEF) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (thought.layer == MemoryLayer.FRAGMENT) "提升为主题"
                                        else "提升为信念"
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onPromote()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Content
            Text(
                text = thought.content,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Tags
            if (thought.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    thought.tags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }
        }
    }
}
