package com.sodapop.app.domain.model

import java.util.UUID

data class Dialogue(
    val id: String = UUID.randomUUID().toString(),
    val thoughtId: String,
    val messages: List<ChatMessage> = emptyList(),
    val mode: DialogueMode = DialogueMode.EXPANSION,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)

data class ChatMessage(
    val role: String,
    val content: String
)
