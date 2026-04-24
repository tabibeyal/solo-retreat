package com.soloretreat.data.repository

import com.soloretreat.data.local.dao.ScheduleBlockDao
import com.soloretreat.data.local.dao.ScheduleTemplateDao
import com.soloretreat.data.local.entity.ScheduleBlock
import com.soloretreat.data.local.entity.ScheduleTemplate
import com.soloretreat.data.local.entity.ScheduleTemplateBlock
import com.soloretreat.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleBlockDao: ScheduleBlockDao,
    private val templateDao: ScheduleTemplateDao
) {
    fun getAllBlocks(): Flow<List<ScheduleBlock>> = scheduleBlockDao.getAllBlocks()

    suspend fun getBlocksForDay(dayOffset: Int): List<ScheduleBlock> =
        scheduleBlockDao.getBlocksForDay(dayOffset)

    fun getBlocksForDayFlow(dayOffset: Int): Flow<List<ScheduleBlock>> =
        scheduleBlockDao.getBlocksForDayFlow(dayOffset)

    suspend fun addBlock(block: ScheduleBlock): Boolean {
        if (!validateBlock(block)) return false
        scheduleBlockDao.insert(block)
        return true
    }

    suspend fun addBlocks(blocks: List<ScheduleBlock>) {
        scheduleBlockDao.insertAll(blocks.filter { validateBlock(it) })
    }

    suspend fun updateBlock(block: ScheduleBlock) {
        scheduleBlockDao.update(block)
    }

    suspend fun deleteBlock(block: ScheduleBlock) {
        scheduleBlockDao.delete(block)
    }

    suspend fun deleteAllBlocks() {
        scheduleBlockDao.deleteAll()
    }

    suspend fun hasBlocks(): Boolean = scheduleBlockDao.count() > 0

    suspend fun getCurrentBlock(dayOffset: Int, currentTime: LocalTime): ScheduleBlock? {
        val blocks = scheduleBlockDao.getBlocksForDay(dayOffset)
        return blocks.find { block ->
            !currentTime.isBefore(block.startTime) && currentTime.isBefore(block.endTime)
        }
    }

    suspend fun getNextBlock(dayOffset: Int, currentTime: LocalTime): ScheduleBlock? {
        val blocks = scheduleBlockDao.getBlocksForDay(dayOffset)
        return blocks.filter { it.startTime.isAfter(currentTime) }
            .minByOrNull { it.startTime }
    }

    suspend fun validateSchedule(): ScheduleValidationResult {
        val blocks = scheduleBlockDao.getAllBlocks().firstOrNull() ?: emptyList()

        if (blocks.isEmpty()) return ScheduleValidationResult.NoBlocks

        val grouped = blocks.groupBy { it.dayOffset }

        for ((_, dayBlocks) in grouped) {
            val sorted = dayBlocks.sortedBy { it.startTime }

            for (i in 0 until sorted.size - 1) {
                val current = sorted[i]
                val next = sorted[i + 1]
                if (next.startTime.isBefore(current.endTime)) {
                    return ScheduleValidationResult.Overlap(
                        dayOffset = current.dayOffset,
                        block1 = "${current.activityType.displayName} (${current.startTime}-${current.endTime})",
                        block2 = "${next.activityType.displayName} (${next.startTime}-${next.endTime})"
                    )
                }
            }

            val mealBlocks = sorted.filter { it.activityType == ActivityType.MEAL }
            for (meal in mealBlocks) {
                if (meal.endTime.isAfter(LocalTime.of(12, 0))) {
                    return ScheduleValidationResult.MealTooLate(
                        dayOffset = meal.dayOffset,
                        endTime = meal.endTime
                    )
                }
            }
        }

        return ScheduleValidationResult.Valid
    }

    private suspend fun validateBlock(block: ScheduleBlock): Boolean {
        val existing = scheduleBlockDao.getBlocksForDay(block.dayOffset)
        val hasOverlap = existing.any { existingBlock ->
            existingBlock.id != block.id &&
            block.startTime.isBefore(existingBlock.endTime) &&
            block.endTime.isAfter(existingBlock.startTime)
        }
        return !hasOverlap && block.startTime.isBefore(block.endTime)
    }

    fun getTemplates(): Flow<List<ScheduleTemplate>> = templateDao.getAll()

    suspend fun saveCurrentAsTemplate(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val current = scheduleBlockDao.getAllBlocks().firstOrNull() ?: emptyList()
        if (current.isEmpty()) return false
        val template = ScheduleTemplate(name = trimmed)
        val templateBlocks = current.map { block ->
            ScheduleTemplateBlock(
                templateId = template.id,
                dayOffset = block.dayOffset,
                startTime = block.startTime,
                endTime = block.endTime,
                activityType = block.activityType,
                notes = block.notes
            )
        }
        templateDao.save(template, templateBlocks)
        return true
    }

    suspend fun applyTemplate(templateId: String) {
        val blocks = templateDao.getBlocksFor(templateId)
        scheduleBlockDao.deleteAll()
        val newBlocks = blocks.map { tb ->
            ScheduleBlock(
                dayOffset = tb.dayOffset,
                startTime = tb.startTime,
                endTime = tb.endTime,
                activityType = tb.activityType,
                notes = tb.notes
            )
        }
        scheduleBlockDao.insertAll(newBlocks)
    }

    suspend fun deleteTemplate(id: String) = templateDao.delete(id)

    sealed class ScheduleValidationResult {
        data object Valid : ScheduleValidationResult()
        data object NoBlocks : ScheduleValidationResult()
        data class Overlap(val dayOffset: Int, val block1: String, val block2: String) : ScheduleValidationResult()
        data class MealTooLate(val dayOffset: Int, val endTime: LocalTime) : ScheduleValidationResult()
    }
}