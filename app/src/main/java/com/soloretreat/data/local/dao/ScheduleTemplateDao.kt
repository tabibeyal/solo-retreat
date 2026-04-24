package com.soloretreat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.soloretreat.data.local.entity.ScheduleTemplate
import com.soloretreat.data.local.entity.ScheduleTemplateBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleTemplateDao {
    @Query("SELECT * FROM schedule_templates ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ScheduleTemplate>>

    @Query("SELECT * FROM schedule_template_blocks WHERE templateId = :templateId ORDER BY dayOffset, startTime")
    suspend fun getBlocksFor(templateId: String): List<ScheduleTemplateBlock>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ScheduleTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<ScheduleTemplateBlock>)

    @Query("DELETE FROM schedule_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)

    @Query("DELETE FROM schedule_template_blocks WHERE templateId = :templateId")
    suspend fun deleteBlocksFor(templateId: String)

    @Transaction
    suspend fun save(template: ScheduleTemplate, blocks: List<ScheduleTemplateBlock>) {
        insertTemplate(template)
        insertBlocks(blocks)
    }

    @Transaction
    suspend fun delete(id: String) {
        deleteBlocksFor(id)
        deleteTemplate(id)
    }
}
