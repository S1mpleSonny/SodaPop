package com.sodapop.app.domain.model

import java.util.UUID

data class Prediction(
    val id: String = UUID.randomUUID().toString(),
    val thoughtId: String,
    val predictedOutcome: String,
    val reviewDate: Long,
    val actualOutcome: String? = null,
    val accuracy: Float? = null,
    val llmAnalysis: String? = null,
    val reviewedAt: Long? = null
)
