package com.sodapop.app.data.repository

import com.sodapop.app.data.local.dao.ThoughtDao
import com.sodapop.app.data.local.entity.ThoughtEntity
import com.sodapop.app.domain.model.MemoryLayer
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.domain.model.ThoughtType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThoughtRepository @Inject constructor(
    private val dao: ThoughtDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllThoughts(): Flow<List<Thought>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    fun getByLayer(layer: MemoryLayer): Flow<List<Thought>> =
        dao.getByLayerFlow(layer.name).map { list -> list.map { it.toDomain() } }

    fun getByType(type: ThoughtType): Flow<List<Thought>> =
        dao.getByTypeFlow(type.name).map { list -> list.map { it.toDomain() } }

    fun getByCluster(clusterId: String): Flow<List<Thought>> =
        dao.getByClusterFlow(clusterId).map { list -> list.map { it.toDomain() } }

    suspend fun getByDateRange(start: Long, end: Long): List<Thought> =
        dao.getByDateRange(start, end).map { it.toDomain() }

    suspend fun getById(id: String): Thought? =
        dao.getById(id)?.toDomain()

    suspend fun getRandom(count: Int): List<Thought> =
        dao.getRandom(count).map { it.toDomain() }

    suspend fun save(thought: Thought) =
        dao.upsert(thought.toEntity())

    suspend fun update(thought: Thought) =
        dao.upsert(thought.toEntity())

    suspend fun delete(thought: Thought) =
        dao.delete(thought.toEntity())

    fun search(query: String): Flow<List<Thought>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    private fun ThoughtEntity.toDomain() = Thought(
        id = id,
        content = content,
        rawContent = rawContent,
        type = ThoughtType.valueOf(type),
        tags = try { json.decodeFromString(tags) } catch (e: Exception) { emptyList() },
        mood = mood,
        confidence = confidence,
        createdAt = createdAt,
        updatedAt = updatedAt,
        clusterId = clusterId,
        layer = MemoryLayer.valueOf(layer)
    )

    private fun Thought.toEntity() = ThoughtEntity(
        id = id,
        content = content,
        rawContent = rawContent,
        type = type.name,
        tags = json.encodeToString(tags),
        mood = mood,
        confidence = confidence,
        createdAt = createdAt,
        updatedAt = updatedAt,
        clusterId = clusterId,
        layer = layer.name
    )
}
