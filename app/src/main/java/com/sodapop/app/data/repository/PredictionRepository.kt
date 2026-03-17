package com.sodapop.app.data.repository

import com.sodapop.app.data.local.dao.PredictionDao
import com.sodapop.app.data.local.entity.PredictionEntity
import com.sodapop.app.domain.model.Prediction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepository @Inject constructor(
    private val dao: PredictionDao
) {
    fun getAll(): Flow<List<Prediction>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    fun getPending(): Flow<List<Prediction>> =
        dao.getPendingFlow().map { list -> list.map { it.toDomain() } }

    fun getReviewed(): Flow<List<Prediction>> =
        dao.getReviewedFlow().map { list -> list.map { it.toDomain() } }

    suspend fun getOverdueReviews(now: Long): List<Prediction> =
        dao.getOverdueReviews(now).map { it.toDomain() }

    suspend fun getUpcomingReviews(now: Long, tomorrow: Long): List<Prediction> =
        dao.getUpcomingReviews(now, tomorrow).map { it.toDomain() }

    suspend fun getById(id: String): Prediction? =
        dao.getById(id)?.toDomain()

    suspend fun getByThoughtId(thoughtId: String): Prediction? =
        dao.getByThoughtId(thoughtId)?.toDomain()

    suspend fun save(prediction: Prediction) =
        dao.upsert(prediction.toEntity())

    suspend fun update(prediction: Prediction) =
        dao.upsert(prediction.toEntity())

    suspend fun delete(prediction: Prediction) =
        dao.delete(prediction.toEntity())

    suspend fun getAverageAccuracy(): Float? =
        dao.getAverageAccuracy()

    private fun PredictionEntity.toDomain() = Prediction(
        id = id, thoughtId = thoughtId, predictedOutcome = predictedOutcome,
        reviewDate = reviewDate, actualOutcome = actualOutcome,
        accuracy = accuracy, llmAnalysis = llmAnalysis, reviewedAt = reviewedAt
    )

    private fun Prediction.toEntity() = PredictionEntity(
        id = id, thoughtId = thoughtId, predictedOutcome = predictedOutcome,
        reviewDate = reviewDate, actualOutcome = actualOutcome,
        accuracy = accuracy, llmAnalysis = llmAnalysis, reviewedAt = reviewedAt
    )
}
