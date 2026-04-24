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
class ChantCatalogViewModel @Inject constructor(
    private val talkRepository: TalkRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    val chants: StateFlow<List<DhammaTalk>> = talkRepository.getChants()
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
            talkRepository.importChantsFromAssets()
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
                talkRepository.importChantsFromAssets()
            } catch (e: Exception) {
                _error.value = "Failed to refresh: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun downloadChant(chant: DhammaTalk) {
        if (chant.downloadStatus == DownloadStatus.COMPLETE) {
            togglePlayPause(chant)
            return
        }
        if (chant.downloadStatus == DownloadStatus.IN_PROGRESS) return
        viewModelScope.launch {
            val ok = talkRepository.downloadTalk(chant)
            if (!ok) {
                _error.value = "Download failed for \"${chant.title}\""
            }
        }
    }

    private fun togglePlayPause(chant: DhammaTalk) {
        if (_currentlyPlayingId.value == chant.id) {
            if (_isPlaying.value) {
                exoPlayer?.pause()
            } else {
                exoPlayer?.play()
            }
        } else {
            playChant(chant)
        }
    }

    private fun playChant(chant: DhammaTalk) {
        val path = chant.localPath ?: return
        val uri = path.toUri()

        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(playerListener)
            }
        }

        exoPlayer?.apply {
            _currentlyPlayingId.value = chant.id
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
}
