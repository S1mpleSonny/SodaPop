package com.sodapop.app.domain.model

import java.util.UUID

data class TopicCluster(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)
