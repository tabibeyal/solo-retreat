package com.soloretreat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soloretreat.data.model.ActivityType
import java.time.Instant
import java.util.UUID

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val blockId: String,
    val actualStart: Instant,
    val actualEnd: Instant? = null,
    val interrupted: Boolean = false,
    val activityType: ActivityType? = null
)