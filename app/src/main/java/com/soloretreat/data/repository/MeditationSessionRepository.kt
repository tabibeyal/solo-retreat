package com.soloretreat.data.repository

import com.soloretreat.data.local.dao.MeditationSessionDao
import com.soloretreat.data.local.entity.MeditationSession
import com.soloretreat.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeditationSessionRepository @Inject constructor(
    private val meditationSessionDao: MeditationSessionDao
) {
    suspend fun getLatestForBlock(blockId: String): MeditationSession? = meditationSessionDao.getLatestForBlock(blockId)
    suspend fun getSessionsForRange(start: Instant, end: Instant): List<MeditationSession> = meditationSessionDao.getSessionsForRange(start, end)
    fun getSessionsForRangeFlow(start: Instant, end: Instant): Flow<List<MeditationSession>> = meditationSessionDao.getSessionsForRangeFlow(start, end)
    suspend fun countCompletedSessions(start: Instant, end: Instant): Int = meditationSessionDao.countCompletedSessions(start, end)
    suspend fun countInterruptedSessions(start: Instant, end: Instant): Int = meditationSessionDao.countInterruptedSessions(start, end)
    suspend fun insert(session: MeditationSession) = meditationSessionDao.insert(session)
    suspend fun update(session: MeditationSession) = meditationSessionDao.update(session)
    suspend fun completeSession(id: String, endTime: Instant, wasInterrupted: Boolean) = meditationSessionDao.completeSession(id, endTime, wasInterrupted)
    suspend fun getTotalMinutes(start: Instant, end: Instant): Long = meditationSessionDao.getTotalMinutes(start, end)
    suspend fun countCompletedByType(type: ActivityType, start: Instant, end: Instant): Int = meditationSessionDao.countCompletedByType(type, start, end)
    suspend fun countInterruptedByType(type: ActivityType, start: Instant, end: Instant): Int = meditationSessionDao.countInterruptedByType(type, start, end)
    suspend fun getTotalMinutesByType(type: ActivityType, start: Instant, end: Instant): Long = meditationSessionDao.getTotalMinutesByType(type, start, end)
}