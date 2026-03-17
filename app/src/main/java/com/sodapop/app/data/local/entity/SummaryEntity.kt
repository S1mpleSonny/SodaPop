package com.sodapop.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "summaries",
    indices = [Index("type"), Index("periodStart")]
)
data class SummaryEntity(
    @PrimaryKey val id: String,
    val type: String,
    val content: String,
    val themes: String,
    val questions: String,
    val periodStart: Long,
    val periodEnd: Long,
    val createdAt: Long,
    val tokenUsage: Int?
)
