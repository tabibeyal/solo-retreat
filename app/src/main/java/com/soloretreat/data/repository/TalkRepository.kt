package com.soloretreat.data.repository

import android.content.Context
import android.os.Environment
import com.soloretreat.data.local.dao.DhammaTalkDao
import com.soloretreat.data.local.entity.DhammaTalk
import com.soloretreat.data.model.DownloadStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

private val catalogJson = Json { 
    ignoreUnknownKeys = true 
    coerceInputValues = true
    encodeDefaults = true
}

@Singleton
class TalkRepository @Inject constructor(
    private val talkDao: DhammaTalkDao,
    @ApplicationContext private val context: Context
) {
    fun getAllTalks(): Flow<List<DhammaTalk>> = talkDao.getAllTalks()

    fun getRevealedTalks(): Flow<List<DhammaTalk>> = talkDao.getRevealedTalks()

    fun getChants(): Flow<List<DhammaTalk>> = talkDao.getTalksByCategory("Chants")

    suspend fun countRevealed(): Int = talkDao.countRevealed()

    suspend fun revealNextBatch(count: Int): Int {
        val ids = talkDao.getUnrevealedIds(count)
        if (ids.isEmpty()) return 0
        talkDao.markRevealed(ids, System.currentTimeMillis())
        return ids.size
    }

    suspend fun getDownloadedTalks(): List<DhammaTalk> = talkDao.getDownloadedTalks()

    suspend fun getTalkById(id: String): DhammaTalk? = talkDao.getTalkById(id)

    suspend fun areAllTalksDownloaded(): Boolean {
        return talkDao.countPendingDownloads() == 0
    }

    suspend fun refreshCatalog() = withContext(kotlinx.coroutines.Dispatchers.IO) {
        android.util.Log.d("TalkRepository", "Refreshing catalog...")
        importCatalogFromAssets()
        importChantsFromAssets()
        // Artificial delay to show the refresh animation
        kotlinx.coroutines.delay(1000)
    }

    suspend fun importCatalogFromAssets() {
        android.util.Log.d("TalkRepository", "Importing talks from assets...")
        importFromAsset("talks_catalog.json")
    }

    suspend fun importChantsFromAssets() {
        android.util.Log.d("TalkRepository", "Importing chants from assets...")
        importFromAsset("chants_catalog.json")
    }

    private suspend fun importFromAsset(fileName: String) {
        try {
            val jsonString = context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val catalog = catalogJson.decodeFromString<List<DhammaTalk>>(jsonString)
            android.util.Log.d("TalkRepository", "Decoded ${catalog.size} items from $fileName")
            
            talkDao.upsertPreservingStatus(catalog)
        } catch (e: Exception) {
            android.util.Log.e("TalkRepository", "Asset import failed for $fileName", e)
        }
    }

    suspend fun updateDownloadStatus(id: String, status: DownloadStatus, localPath: String?) {
        talkDao.updateDownloadStatus(id, status, localPath)
    }

    fun getTalksDirectory(): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "talks")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun downloadTalk(talk: DhammaTalk): Boolean = withContext(Dispatchers.IO) {
        android.util.Log.d("TalkRepository", "downloadTalk id=${talk.id} url=${talk.remoteUrl}")
        updateDownloadStatus(talk.id, DownloadStatus.IN_PROGRESS, null)
        try {
            val localPath = performDownload(talk.remoteUrl, talk.title)
            if (localPath != null) {
                android.util.Log.d("TalkRepository", "Download success: $localPath")
                updateDownloadStatus(talk.id, DownloadStatus.COMPLETE, localPath)
                true
            } else {
                android.util.Log.e("TalkRepository", "Download failed: ${talk.title}")
                updateDownloadStatus(talk.id, DownloadStatus.FAILED, null)
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("TalkRepository", "Download exception", e)
            updateDownloadStatus(talk.id, DownloadStatus.FAILED, null)
            false
        }
    }

    private fun performDownload(rawUrl: String, title: String): String? {
        val dir = getTalksDirectory()
        val fileName = resolveFileName(rawUrl, title)
        val outFile = File(dir, fileName)
        val tempFile = File(dir, "$fileName.part")
        if (tempFile.exists()) tempFile.delete()

        var url = URL(rawUrl)
        var redirects = 0
        while (true) {
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.instanceFollowRedirects = false
            conn.requestMethod = "GET"
            try {
                val code = conn.responseCode
                android.util.Log.d("TalkRepository", "HTTP $code for $url")

                if (code in 300..399) {
                    if (redirects++ >= 5) {
                        android.util.Log.e("TalkRepository", "Too many redirects")
                        return null
                    }
                    val loc = conn.getHeaderField("Location") ?: return null
                    url = URL(url, loc)
                    continue
                }
                if (code !in 200..299) {
                    android.util.Log.e("TalkRepository", "HTTP error $code")
                    return null
                }

                conn.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buf = ByteArray(8 * 1024)
                        var total = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n == -1) break
                            output.write(buf, 0, n)
                            total += n
                        }
                        android.util.Log.d("TalkRepository", "Wrote $total bytes")
                    }
                }

                if (!tempFile.renameTo(outFile)) {
                    android.util.Log.e("TalkRepository", "Rename failed")
                    tempFile.delete()
                    return null
                }
                return outFile.absolutePath
            } finally {
                conn.disconnect()
            }
        }
        @Suppress("UNREACHABLE_CODE")
        return null
    }

    private fun resolveFileName(url: String, title: String): String {
        val last = url.substringAfterLast('/').substringBefore('?')
        val decoded = try {
            java.net.URLDecoder.decode(last, "UTF-8")
        } catch (_: Exception) {
            last
        }
        return if (decoded.endsWith(".mp3", ignoreCase = true)) {
            decoded.replace(Regex("[^A-Za-z0-9._-]"), "_")
        } else {
            "${title.replace(Regex("[^A-Za-z0-9._-]"), "_")}.mp3"
        }
    }

    private suspend fun insertDefaultCatalog() {
        android.util.Log.d("TalkRepository", "Inserting default catalog...")
        val defaults = listOf(
            DhammaTalk(
                id = "thanissaro_basics_01",
                title = "Strength from the Basics",
                teacher = "Thanissaro Bhikkhu",
                remoteUrl = "https://www.dhammatalks.org/Archive/basics_collection/01%20Strength%20from%20the%20Basics.mp3",
                durationMinutes = 15,
                category = "Basics",
                description = "First talk in the Basics collection, remastered for clarity."
            ),
            DhammaTalk(
                id = "thanissaro_basics_02",
                title = "Basics",
                teacher = "Thanissaro Bhikkhu",
                remoteUrl = "https://www.dhammatalks.org/Archive/basics_collection/02%20Basics.mp3",
                durationMinutes = 12,
                category = "Basics",
                description = "Foundational instructions for meditation practice."
            )
        )
        talkDao.upsertPreservingStatus(defaults)
    }
}
