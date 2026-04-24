package com.soloretreat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soloretreat.data.model.DownloadStatus
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "dhamma_talks")
data class DhammaTalk(
    @PrimaryKey val id: String,
    val title: String,
    val teacher: String,
    val remoteUrl: String,
    val transcriptionUrl: String? = null,
    val localPath: String? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_STARTED,
    val durationMinutes: Int,
    val category: String,
    val description: String = "",
    val revealedAt: Long? = null
)