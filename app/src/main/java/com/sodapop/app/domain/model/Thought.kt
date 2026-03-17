package com.sodapop.app.domain.model

import java.util.UUID

data class Thought(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val rawContent: String = content,
    val type: ThoughtType = ThoughtType.IDEA,
    val tags: List<String> = emptyList(),
    val mood: String? = null,
    val confidence: Float? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val clusterId: String? = null,
    val layer: MemoryLayer = MemoryLayer.FRAGMENT
)
