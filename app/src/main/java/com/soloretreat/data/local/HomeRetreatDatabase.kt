package com.soloretreat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.soloretreat.data.local.dao.ChecklistItemDao
import com.soloretreat.data.local.dao.DhammaTalkDao
import com.soloretreat.data.local.dao.JournalEntryDao
import com.soloretreat.data.local.dao.MealLogDao
import com.soloretreat.data.local.dao.MeditationSessionDao
import com.soloretreat.data.local.dao.PreceptLogDao
import com.soloretreat.data.local.dao.RetreatConfigDao
import com.soloretreat.data.local.dao.ScheduleBlockDao
import com.soloretreat.data.local.dao.ScheduleTemplateDao
import com.soloretreat.data.local.entity.ChecklistItem
import com.soloretreat.data.local.entity.DhammaTalk
import com.soloretreat.data.local.entity.JournalEntry
import com.soloretreat.data.local.entity.MealLog
import com.soloretreat.data.local.entity.MeditationSession
import com.soloretreat.data.local.entity.PreceptLog
import com.soloretreat.data.local.entity.RetreatConfig
import com.soloretreat.data.local.entity.ScheduleBlock
import com.soloretreat.data.local.entity.ScheduleTemplate
import com.soloretreat.data.local.entity.ScheduleTemplateBlock

@Database(
    entities = [
        RetreatConfig::class,
        ScheduleBlock::class,
        DhammaTalk::class,
        PreceptLog::class,
        MeditationSession::class,
        MealLog::class,
        JournalEntry::class,
        ChecklistItem::class,
        ScheduleTemplate::class,
        ScheduleTemplateBlock::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HomeRetreatDatabase : RoomDatabase() {
    abstract fun retreatConfigDao(): RetreatConfigDao
    abstract fun scheduleBlockDao(): ScheduleBlockDao
    abstract fun dhammaTalkDao(): DhammaTalkDao
    abstract fun preceptLogDao(): PreceptLogDao
    abstract fun meditationSessionDao(): MeditationSessionDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun checklistItemDao(): ChecklistItemDao
    abstract fun scheduleTemplateDao(): ScheduleTemplateDao
}