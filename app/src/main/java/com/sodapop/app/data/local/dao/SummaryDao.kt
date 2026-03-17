package com.sodapop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sodapop.app.data.local.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    @Query("SELECT * FROM summaries ORDER BY periodEnd DESC")
    fun getAllFlow(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries WHERE type = :type ORDER BY periodEnd DESC")
    fun getByTypeFlow(type: String): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries WHERE type = :type AND periodStart = :periodStart")
    suspend fun getByTypeAndPeriod(type: String, periodStart: Long): SummaryEntity?

    @Query("SELECT * FROM summaries WHERE id = :id")
    suspend fun getById(id: String): SummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SummaryEntity)

    @Delete
    suspend fun delete(entity: SummaryEntity)
}
