package com.sodapop.app.domain.model

import java.util.UUID

data class Summary(
    val id: String = UUID.randomUUID().toString(),
    val type: SummaryType,
    val content: String,
    val themes: List<String> = emptyList(),
    val questions: List<String> = emptyList(),
    val periodStart: Long,
    val periodEnd: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val tokenUsage: Int? = null
)
