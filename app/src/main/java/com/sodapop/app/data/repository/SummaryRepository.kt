package com.sodapop.app.data.repository

import com.sodapop.app.data.local.dao.SummaryDao
import com.sodapop.app.data.local.entity.SummaryEntity
import com.sodapop.app.domain.model.Summary
import com.sodapop.app.domain.model.SummaryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor(
    private val dao: SummaryDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAll(): Flow<List<Summary>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    fun getByType(type: SummaryType): Flow<List<Summary>> =
        dao.getByTypeFlow(type.name).map { list -> list.map { it.toDomain() } }

    suspend fun getByTypeAndPeriod(type: SummaryType, periodStart: Long): Summary? =
        dao.getByTypeAndPeriod(type.name, periodStart)?.toDomain()

    suspend fun getById(id: String): Summary? =
        dao.getById(id)?.toDomain()

    suspend fun save(summary: Summary) =
        dao.upsert(summary.toEntity())

    suspend fun delete(summary: Summary) =
        dao.delete(summary.toEntity())

    private fun SummaryEntity.toDomain() = Summary(
        id = id, type = SummaryType.valueOf(type), content = content,
        themes = try { json.decodeFromString(themes) } catch (e: Exception) { emptyList() },
        questions = try { json.decodeFromString(questions) } catch (e: Exception) { emptyList() },
        periodStart = periodStart, periodEnd = periodEnd,
        createdAt = createdAt, tokenUsage = tokenUsage
    )

    private fun Summary.toEntity() = SummaryEntity(
        id = id, type = type.name, content = content,
        themes = json.encodeToString(themes),
        questions = json.encodeToString(questions),
        periodStart = periodStart, periodEnd = periodEnd,
        createdAt = createdAt, tokenUsage = tokenUsage
    )
}
