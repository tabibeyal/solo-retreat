package com.soloretreat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.soloretreat.data.local.entity.DhammaTalk
import com.soloretreat.data.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DhammaTalkDao {
    @Query("SELECT * FROM dhamma_talks ORDER BY category, title")
    fun getAllTalks(): Flow<List<DhammaTalk>>

    @Query("SELECT * FROM dhamma_talks WHERE revealedAt IS NOT NULL ORDER BY revealedAt DESC")
    fun getRevealedTalks(): Flow<List<DhammaTalk>>

    @Query("SELECT COUNT(*) FROM dhamma_talks WHERE revealedAt IS NOT NULL")
    suspend fun countRevealed(): Int

    @Query("SELECT id FROM dhamma_talks WHERE revealedAt IS NULL ORDER BY id LIMIT :limit")
    suspend fun getUnrevealedIds(limit: Int): List<String>

    @Query("UPDATE dhamma_talks SET revealedAt = :timestamp WHERE id IN (:ids)")
    suspend fun markRevealed(ids: List<String>, timestamp: Long)

    @Query("SELECT * FROM dhamma_talks WHERE downloadStatus = 'COMPLETE'")
    suspend fun getDownloadedTalks(): List<DhammaTalk>

    @Query("SELECT * FROM dhamma_talks WHERE id = :id")
    suspend fun getTalkById(id: String): DhammaTalk?

    @Query("SELECT COUNT(*) FROM dhamma_talks WHERE downloadStatus != 'COMPLETE'")
    suspend fun countPendingDownloads(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(talks: List<DhammaTalk>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(talk: DhammaTalk): Long

    @Transaction
    suspend fun upsertPreservingStatus(talks: List<DhammaTalk>) {
        for (talk in talks) {
            val id = insertIgnore(talk)
            if (id == -1L) {
                val existing = getTalkById(talk.id)
                if (existing != null) {
                    update(talk.copy(
                        downloadStatus = existing.downloadStatus,
                        localPath = existing.localPath,
                        revealedAt = existing.revealedAt
                    ))
                }
            }
        }
    }

    @Update
    suspend fun update(talk: DhammaTalk)

    @Query("UPDATE dhamma_talks SET downloadStatus = :status, localPath = :path WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: DownloadStatus, path: String?)

    @Query("DELETE FROM dhamma_talks")
    suspend fun deleteAll()
}