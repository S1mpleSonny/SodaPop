package com.sodapop.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "predictions",
    foreignKeys = [
        ForeignKey(
            entity = ThoughtEntity::class,
            parentColumns = ["id"],
            childColumns = ["thoughtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("thoughtId"), Index("reviewDate")]
)
data class PredictionEntity(
    @PrimaryKey val id: String,
    val thoughtId: String,
    val predictedOutcome: String,
    val reviewDate: Long,
    val actualOutcome: String?,
    val accuracy: Float?,
    val llmAnalysis: String?,
    val reviewedAt: Long?
)
