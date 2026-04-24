package com.soloretreat.ui.preparation

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.soloretreat.data.local.entity.DhammaTalk
import com.soloretreat.data.model.DownloadStatus
import com.soloretreat.data.repository.TalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TalkCatalogViewModel @Inject constructor(
    private val talkRepository: TalkRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    val talks: StateFlow<List<DhammaTalk>> = talkRepository.getRevealedTalks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var exoPlayer: ExoPlayer? = null

    init {
        viewModelScope.launch {
            talkRepository.importCatalogFromAssets()
            if (talkRepository.countRevealed() == 0) {
                talkRepository.revealNextBatch(INITIAL_BATCH_SIZE)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _currentlyPlayingId.value = null
            }
        }
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                talkRepository.refreshCatalog()
                val revealed = talkRepository.revealNextBatch(REFRESH_BATCH_SIZE)
                if (revealed == 0) {
                    _error.value = "No more talks available"
                }
            } catch (e: Exception) {
                _error.value = "Failed to refresh: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun downloadTalk(talk: DhammaTalk) {
        if (talk.downloadStatus == DownloadStatus.COMPLETE) {
            togglePlayPause(talk)
            return
        }
        if (talk.downloadStatus == DownloadStatus.IN_PROGRESS) return
        viewModelScope.launch {
            val ok = talkRepository.downloadTalk(talk)
            if (!ok) {
                _error.value = "Download failed for \"${talk.title}\""
            }
        }
    }

    private fun togglePlayPause(talk: DhammaTalk) {
        if (_currentlyPlayingId.value == talk.id) {
            if (_isPlaying.value) {
                exoPlayer?.pause()
            } else {
                exoPlayer?.play()
            }
        } else {
            playTalk(talk)
        }
    }

    private fun playTalk(talk: DhammaTalk) {
        val path = talk.localPath ?: return
        val uri = path.toUri()

        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(playerListener)
            }
        }

        exoPlayer?.apply {
            _currentlyPlayingId.value = talk.id
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        private const val INITIAL_BATCH_SIZE = 5
        private const val REFRESH_BATCH_SIZE = 3
    }
}