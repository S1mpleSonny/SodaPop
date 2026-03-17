package com.sodapop.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dialogues",
    foreignKeys = [
        ForeignKey(
            entity = ThoughtEntity::class,
            parentColumns = ["id"],
            childColumns = ["thoughtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("thoughtId")]
)
data class DialogueEntity(
    @PrimaryKey val id: String,
    val thoughtId: String,
    val messages: String,
    val mode: String,
    val createdAt: Long,
    val updatedAt: Long
)
