package com.sodapop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sodapop.app.data.local.entity.ThoughtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThoughtDao {

    @Query("SELECT * FROM thoughts ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts WHERE layer = :layer ORDER BY createdAt DESC")
    fun getByLayerFlow(layer: String): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<ThoughtEntity>

    @Query("SELECT * FROM thoughts WHERE id = :id")
    suspend fun getById(id: String): ThoughtEntity?

    @Query("SELECT * FROM thoughts WHERE clusterId = :clusterId ORDER BY createdAt DESC")
    fun getByClusterFlow(clusterId: String): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandom(count: Int): List<ThoughtEntity>

    @Query("SELECT * FROM thoughts WHERE type = :type ORDER BY createdAt DESC")
    fun getByTypeFlow(type: String): Flow<List<ThoughtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ThoughtEntity)

    @Update
    suspend fun update(entity: ThoughtEntity)

    @Delete
    suspend fun delete(entity: ThoughtEntity)

    @Query("SELECT * FROM thoughts WHERE content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<ThoughtEntity>>

    @Query("SELECT COUNT(*) FROM thoughts WHERE clusterId = :clusterId")
    suspend fun countByCluster(clusterId: String): Int
}
