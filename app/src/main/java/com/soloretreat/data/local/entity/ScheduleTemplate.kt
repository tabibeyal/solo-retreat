package com.soloretreat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soloretreat.data.model.ActivityType
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "schedule_templates")
data class ScheduleTemplate(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedule_template_blocks")
data class ScheduleTemplateBlock(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val dayOffset: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val activityType: ActivityType,
    val notes: String? = null
)
