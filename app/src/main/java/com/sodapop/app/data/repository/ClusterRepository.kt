package com.sodapop.app.data.repository

import com.sodapop.app.data.local.dao.TopicClusterDao
import com.sodapop.app.data.local.entity.TopicClusterEntity
import com.sodapop.app.domain.model.TopicCluster
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClusterRepository @Inject constructor(
    private val dao: TopicClusterDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAll(): Flow<List<TopicCluster>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): TopicCluster? =
        dao.getById(id)?.toDomain()

    suspend fun save(cluster: TopicCluster) =
        dao.upsert(cluster.toEntity())

    suspend fun update(cluster: TopicCluster) =
        dao.upsert(cluster.toEntity())

    suspend fun delete(cluster: TopicCluster) =
        dao.delete(cluster.toEntity())

    private fun TopicClusterEntity.toDomain() = TopicCluster(
        id = id, name = name, description = description,
        tags = try { json.decodeFromString(tags) } catch (e: Exception) { emptyList() },
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun TopicCluster.toEntity() = TopicClusterEntity(
        id = id, name = name, description = description,
        tags = json.encodeToString(tags),
        createdAt = createdAt, updatedAt = updatedAt
    )
}
