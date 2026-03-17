package com.sodapop.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topic_clusters")
data class TopicClusterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val tags: String,
    val createdAt: Long,
    val updatedAt: Long
)
