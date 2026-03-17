package com.sodapop.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sodapop.app.data.local.converter.Converters
import com.sodapop.app.data.local.dao.DialogueDao
import com.sodapop.app.data.local.dao.PredictionDao
import com.sodapop.app.data.local.dao.SummaryDao
import com.sodapop.app.data.local.dao.ThoughtDao
import com.sodapop.app.data.local.dao.TopicClusterDao
import com.sodapop.app.data.local.entity.DialogueEntity
import com.sodapop.app.data.local.entity.PredictionEntity
import com.sodapop.app.data.local.entity.SummaryEntity
import com.sodapop.app.data.local.entity.ThoughtEntity
import com.sodapop.app.data.local.entity.TopicClusterEntity

@Database(
    entities = [
        ThoughtEntity::class,
        PredictionEntity::class,
        DialogueEntity::class,
        SummaryEntity::class,
        TopicClusterEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SodaPopDatabase : RoomDatabase() {
    abstract fun thoughtDao(): ThoughtDao
    abstract fun predictionDao(): PredictionDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun summaryDao(): SummaryDao
    abstract fun topicClusterDao(): TopicClusterDao
}
