package com.sodapop.app.di

import android.content.Context
import com.sodapop.app.data.local.DatabaseFactory
import com.sodapop.app.data.local.SodaPopDatabase
import com.sodapop.app.data.local.dao.DialogueDao
import com.sodapop.app.data.local.dao.PredictionDao
import com.sodapop.app.data.local.dao.SummaryDao
import com.sodapop.app.data.local.dao.ThoughtDao
import com.sodapop.app.data.local.dao.TopicClusterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SodaPopDatabase =
        DatabaseFactory.create(context)

    @Provides
    fun provideThoughtDao(db: SodaPopDatabase): ThoughtDao = db.thoughtDao()

    @Provides
    fun providePredictionDao(db: SodaPopDatabase): PredictionDao = db.predictionDao()

    @Provides
    fun provideDialogueDao(db: SodaPopDatabase): DialogueDao = db.dialogueDao()

    @Provides
    fun provideSummaryDao(db: SodaPopDatabase): SummaryDao = db.summaryDao()

    @Provides
    fun provideTopicClusterDao(db: SodaPopDatabase): TopicClusterDao = db.topicClusterDao()
}
