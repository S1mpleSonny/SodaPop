package com.sodapop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sodapop.app.data.local.entity.TopicClusterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicClusterDao {

    @Query("SELECT * FROM topic_clusters ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<TopicClusterEntity>>

    @Query("SELECT * FROM topic_clusters WHERE id = :id")
    suspend fun getById(id: String): TopicClusterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TopicClusterEntity)

    @Update
    suspend fun update(entity: TopicClusterEntity)

    @Delete
    suspend fun delete(entity: TopicClusterEntity)
}
