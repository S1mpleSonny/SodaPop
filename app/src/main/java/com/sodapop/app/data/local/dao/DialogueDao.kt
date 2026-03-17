package com.sodapop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sodapop.app.data.local.entity.DialogueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DialogueDao {

    @Query("SELECT * FROM dialogues ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<DialogueEntity>>

    @Query("SELECT * FROM dialogues WHERE thoughtId = :thoughtId ORDER BY createdAt DESC")
    fun getByThoughtIdFlow(thoughtId: String): Flow<List<DialogueEntity>>

    @Query("SELECT * FROM dialogues WHERE id = :id")
    suspend fun getById(id: String): DialogueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DialogueEntity)

    @Update
    suspend fun update(entity: DialogueEntity)

    @Delete
    suspend fun delete(entity: DialogueEntity)
}
