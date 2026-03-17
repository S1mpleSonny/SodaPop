package com.sodapop.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "thoughts",
    indices = [
        Index("createdAt"),
        Index("type"),
        Index("layer"),
        Index("clusterId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = TopicClusterEntity::class,
            parentColumns = ["id"],
            childColumns = ["clusterId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ThoughtEntity(
    @PrimaryKey val id: String,
    val content: String,
    val rawContent: String,
    val type: String,
    val tags: String,
    val mood: String?,
    val confidence: Float?,
    val createdAt: Long,
    val updatedAt: Long,
    val clusterId: String?,
    val layer: String
)
