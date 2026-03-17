package com.sodapop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sodapop.app.data.local.entity.PredictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {

    @Query("SELECT * FROM predictions ORDER BY reviewDate ASC")
    fun getAllFlow(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE reviewedAt IS NULL ORDER BY reviewDate ASC")
    fun getPendingFlow(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE reviewedAt IS NULL AND reviewDate <= :now ORDER BY reviewDate ASC")
    suspend fun getOverdueReviews(now: Long): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE reviewedAt IS NULL AND reviewDate BETWEEN :now AND :tomorrow")
    suspend fun getUpcomingReviews(now: Long, tomorrow: Long): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE reviewedAt IS NOT NULL ORDER BY reviewedAt DESC")
    fun getReviewedFlow(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE id = :id")
    suspend fun getById(id: String): PredictionEntity?

    @Query("SELECT * FROM predictions WHERE thoughtId = :thoughtId")
    suspend fun getByThoughtId(thoughtId: String): PredictionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PredictionEntity)

    @Update
    suspend fun update(entity: PredictionEntity)

    @Delete
    suspend fun delete(entity: PredictionEntity)

    @Query("SELECT AVG(accuracy) FROM predictions WHERE accuracy IS NOT NULL")
    suspend fun getAverageAccuracy(): Float?
}
