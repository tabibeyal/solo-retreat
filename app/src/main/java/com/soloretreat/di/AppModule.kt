package com.soloretreat.di

import android.content.Context
import androidx.room.Room
import com.soloretreat.data.local.HomeRetreatDatabase
import com.soloretreat.data.repository.JournalRepository
import com.soloretreat.data.repository.PreceptRepository
import com.soloretreat.data.repository.RetreatRepository
import com.soloretreat.data.repository.ScheduleRepository
import com.soloretreat.data.repository.TalkRepository
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
    fun provideDatabase(@ApplicationContext context: Context): HomeRetreatDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HomeRetreatDatabase::class.java,
            "home_retreat_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideRetreatConfigDao(db: HomeRetreatDatabase) = db.retreatConfigDao()

    @Provides
    @Singleton
    fun provideScheduleBlockDao(db: HomeRetreatDatabase) = db.scheduleBlockDao()

    @Provides
    @Singleton
    fun provideDhammaTalkDao(db: HomeRetreatDatabase) = db.dhammaTalkDao()

    @Provides
    @Singleton
    fun providePreceptLogDao(db: HomeRetreatDatabase) = db.preceptLogDao()

    @Provides
    @Singleton
    fun provideMeditationSessionDao(db: HomeRetreatDatabase) = db.meditationSessionDao()

    @Provides
    @Singleton
    fun provideMealLogDao(db: HomeRetreatDatabase) = db.mealLogDao()

    @Provides
    @Singleton
    fun provideJournalEntryDao(db: HomeRetreatDatabase) = db.journalEntryDao()

    @Provides
    @Singleton
    fun provideChecklistItemDao(db: HomeRetreatDatabase) = db.checklistItemDao()

    @Provides
    @Singleton
    fun provideScheduleTemplateDao(db: HomeRetreatDatabase) = db.scheduleTemplateDao()

    @Provides
    @Singleton
    fun provideRetreatRepository(
        retreatConfigDao: com.soloretreat.data.local.dao.RetreatConfigDao,
        checklistItemDao: com.soloretreat.data.local.dao.ChecklistItemDao,
        scheduleBlockDao: com.soloretreat.data.local.dao.ScheduleBlockDao,
        meditationSessionDao: com.soloretreat.data.local.dao.MeditationSessionDao,
        journalEntryDao: com.soloretreat.data.local.dao.JournalEntryDao,
        mealLogDao: com.soloretreat.data.local.dao.MealLogDao,
        preceptLogDao: com.soloretreat.data.local.dao.PreceptLogDao,
        dhammaTalkDao: com.soloretreat.data.local.dao.DhammaTalkDao
    ): RetreatRepository = RetreatRepository(
        retreatConfigDao,
        checklistItemDao,
        scheduleBlockDao,
        meditationSessionDao,
        journalEntryDao,
        mealLogDao,
        preceptLogDao,
        dhammaTalkDao
    )

    @Provides
    @Singleton
    fun provideScheduleRepository(
        scheduleBlockDao: com.soloretreat.data.local.dao.ScheduleBlockDao,
        scheduleTemplateDao: com.soloretreat.data.local.dao.ScheduleTemplateDao
    ): ScheduleRepository = ScheduleRepository(scheduleBlockDao, scheduleTemplateDao)

    @Provides
    @Singleton
    fun provideTalkRepository(
        dhammaTalkDao: com.soloretreat.data.local.dao.DhammaTalkDao,
        @ApplicationContext context: Context
    ): TalkRepository = TalkRepository(dhammaTalkDao, context)

    @Provides
    @Singleton
    fun providePreceptRepository(
        preceptLogDao: com.soloretreat.data.local.dao.PreceptLogDao
    ): PreceptRepository = PreceptRepository(preceptLogDao)

    @Provides
    @Singleton
    fun provideJournalRepository(
        journalEntryDao: com.soloretreat.data.local.dao.JournalEntryDao
    ): JournalRepository = JournalRepository(journalEntryDao)

    @Provides
    @Singleton
    fun provideMealLogRepository(
        mealLogDao: com.soloretreat.data.local.dao.MealLogDao
    ): com.soloretreat.data.repository.MealLogRepository =
        com.soloretreat.data.repository.MealLogRepository(mealLogDao)

    @Provides
    @Singleton
    fun provideMeditationSessionRepository(
        meditationSessionDao: com.soloretreat.data.local.dao.MeditationSessionDao
    ): com.soloretreat.data.repository.MeditationSessionRepository =
        com.soloretreat.data.repository.MeditationSessionRepository(meditationSessionDao)
}