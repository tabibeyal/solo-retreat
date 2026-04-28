package com.soloretreat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.soloretreat.data.local.entity.MeditationSession
import com.soloretreat.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface MeditationSessionDao {
    @Query("SELECT * FROM meditation_sessions WHERE blockId = :blockId ORDER BY actualStart DESC LIMIT 1")
    suspend fun getLatestForBlock(blockId: String): MeditationSession?

    @Query("SELECT * FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end ORDER BY actualStart")
    suspend fun getSessionsForRange(start: Instant, end: Instant): List<MeditationSession>

    @Query("SELECT * FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end ORDER BY actualStart")
    fun getSessionsForRangeFlow(start: Instant, end: Instant): Flow<List<MeditationSession>>

    @Query("SELECT COUNT(*) FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end AND interrupted = 0")
    suspend fun countCompletedSessions(start: Instant, end: Instant): Int

    @Query("SELECT COUNT(*) FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end AND interrupted = 1")
    suspend fun countInterruptedSessions(start: Instant, end: Instant): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: MeditationSession)

    @Update
    suspend fun update(session: MeditationSession)

    @Query("UPDATE meditation_sessions SET actualEnd = :endTime, interrupted = :wasInterrupted WHERE id = :id")
    suspend fun completeSession(id: String, endTime: Instant, wasInterrupted: Boolean)

    @Query("SELECT COALESCE(SUM(durationSeconds) / 60, 0) FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end")
    suspend fun getTotalMinutes(start: Instant, end: Instant): Long

    @Query("SELECT COUNT(*) FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end AND interrupted = 0 AND activityType = :type")
    suspend fun countCompletedByType(type: ActivityType, start: Instant, end: Instant): Int

    @Query("SELECT COUNT(*) FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end AND interrupted = 1 AND activityType = :type")
    suspend fun countInterruptedByType(type: ActivityType, start: Instant, end: Instant): Int

    @Query("SELECT COALESCE(SUM(durationSeconds) / 60, 0) FROM meditation_sessions WHERE actualStart BETWEEN :start AND :end AND activityType = :type")
    suspend fun getTotalMinutesByType(type: ActivityType, start: Instant, end: Instant): Long

    @Query("DELETE FROM meditation_sessions")
    suspend fun deleteAll()

    @Query("DELETE FROM meditation_sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}