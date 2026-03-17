package com.sodapop.app.data.repository

import com.sodapop.app.data.local.dao.DialogueDao
import com.sodapop.app.data.local.entity.DialogueEntity
import com.sodapop.app.domain.model.ChatMessage
import com.sodapop.app.domain.model.Dialogue
import com.sodapop.app.domain.model.DialogueMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class SerializableMessage(val role: String, val content: String)

@Singleton
class DialogueRepository @Inject constructor(
    private val dao: DialogueDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAll(): Flow<List<Dialogue>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    fun getByThoughtId(thoughtId: String): Flow<List<Dialogue>> =
        dao.getByThoughtIdFlow(thoughtId).map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): Dialogue? =
        dao.getById(id)?.toDomain()

    suspend fun save(dialogue: Dialogue) =
        dao.upsert(dialogue.toEntity())

    suspend fun update(dialogue: Dialogue) =
        dao.upsert(dialogue.toEntity())

    suspend fun delete(dialogue: Dialogue) =
        dao.delete(dialogue.toEntity())

    private fun DialogueEntity.toDomain() = Dialogue(
        id = id, thoughtId = thoughtId,
        messages = try {
            json.decodeFromString<List<SerializableMessage>>(messages)
                .map { ChatMessage(it.role, it.content) }
        } catch (e: Exception) { emptyList() },
        mode = DialogueMode.valueOf(mode),
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun Dialogue.toEntity() = DialogueEntity(
        id = id, thoughtId = thoughtId,
        messages = json.encodeToString(
            messages.map { SerializableMessage(it.role, it.content) }
        ),
        mode = mode.name,
        createdAt = createdAt, updatedAt = updatedAt
    )
}
