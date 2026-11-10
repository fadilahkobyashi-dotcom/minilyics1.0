package com.crintsoft.zikiplayer.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.crintsoft.zikiplayer.R
import com.crintsoft.zikiplayer.lyrics.DefaultLyrics
import com.crintsoft.zikiplayer.lyrics.LrcParser
import com.crintsoft.zikiplayer.model.LyricType
import com.crintsoft.zikiplayer.model.LyricsData
import com.crintsoft.zikiplayer.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

class MusicPlaybackManager(private val context: Context) {
    private val tag = "MusicPlaybackManager"
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaPlayer: MediaPlayer? = null
    private var positionTickerJob: Job? = null

    // Provider to access the full music library when a sub-queue reaches boundaries
    var allTracksProvider: (() -> List<Track>)? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _syncOffsetMs = MutableStateFlow(0L)
    val syncOffsetMs: StateFlow<Long> = _syncOffsetMs.asStateFlow()

    private val _activeLyrics = MutableStateFlow(LyricsData())
    val activeLyrics: StateFlow<LyricsData> = _activeLyrics.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private var currentIndex = 0

    init {
        // Load default tracks into queue
        val defaultTracks = DefaultLyrics.getDefaultTracks()
        _queue.value = defaultTracks
        if (defaultTracks.isNotEmpty()) {
            setTrackWithoutPlaying(defaultTracks[0])
        }
    }

    fun setQueue(tracks: List<Track>, startPlayingIndex: Int = 0) {
        if (tracks.isEmpty()) return
        _queue.value = tracks
        currentIndex = startPlayingIndex.coerceIn(0, tracks.lastIndex)
        playTrack(tracks[currentIndex], tracks)
    }

    fun setTrackWithoutPlaying(track: Track) {
        _currentTrack.value = track
        _syncOffsetMs.value = 0L
        _activeLyrics.value = loadLyricsForTrack(track)
        _durationMs.value = track.durationMs
        _currentPositionMs.value = 0L
    }

    private fun loadLyricsForTrack(track: Track): LyricsData {
        val effective = track.getEffectiveLyrics()
        return if (effective != null) {
            LrcParser.parse(effective.second)
        } else {
            LyricsData(type = LyricType.NONE)
        }
    }

    /**
     * Plays the specified track safely.
     * Prevents MediaPlayer native file descriptor leaks, race conditions,
     * and IllegalStateExceptions during track transitions.
     */
    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        // Update queue if provided
        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
            val foundIdx = newQueue.indexOfFirst { it.id == track.id }
            currentIndex = if (foundIdx >= 0) foundIdx else 0
        } else {
            val q = _queue.value
            val foundIdx = q.indexOfFirst { it.id == track.id }
            if (foundIdx >= 0) {
                currentIndex = foundIdx
            } else {
                // If track is not in current queue, prepend to queue
                _queue.value = listOf(track) + q
                currentIndex = 0
            }
        }

        // Release previous player safely before creating a new one
        releasePlayer()

        _currentTrack.value = track
        _syncOffsetMs.value = 0L
        _activeLyrics.value = loadLyricsForTrack(track)
        _currentPositionMs.value = 0L
        _durationMs.value = track.durationMs

        try {
            val player = MediaPlayer()
            mediaPlayer = player

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            // Resolve audio source using standard Android resource URI or content URI
            if (track.isDemo) {
                val rawId = if (track.id.contains("cyber") || track.id.contains("neon")) {
                    R.raw.track_cyberpulse
                } else {
                    R.raw.track_starlight
                }
                val resourceUri = Uri.parse("android.resource://${context.packageName}/$rawId")
                player.setDataSource(context, resourceUri)
            } else if (!track.audioUri.isNullOrBlank()) {
                player.setDataSource(context, Uri.parse(track.audioUri))
            } else {
                Log.w(tag, "No audio URI or resource for track: ${track.id}, falling back to demo audio")
                val resourceUri = Uri.parse("android.resource://${context.packageName}/${R.raw.track_starlight}")
                player.setDataSource(context, resourceUri)
            }

            player.setOnPreparedListener { mp ->
                // Verify this callback belongs to the currently active player instance
                if (mp != mediaPlayer) {
                    try {
                        mp.reset()
                        mp.release()
                    } catch (_: Exception) {}
                    return@setOnPreparedListener
                }

                try {
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(track.durationMs)
                    mp.start()
                    _isPlaying.value = true
                    startTicker()
                } catch (e: Exception) {
                    Log.e(tag, "Error starting playback after prepared", e)
                    _isPlaying.value = false
                }
            }

            player.setOnCompletionListener { mp ->
                if (mp == mediaPlayer) {
                    handleTrackCompletion()
                }
            }

            player.setOnErrorListener { mp, what, extra ->
                Log.e(tag, "MediaPlayer error: what=$what, extra=$extra")
                if (mp == mediaPlayer) {
                    _isPlaying.value = false
                    stopTicker()
                }
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            Log.e(tag, "Failed to start playback for track: ${track.title}", e)
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player == null) {
            _currentTrack.value?.let { playTrack(it) }
            return
        }

        try {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopTicker()
            } else {
                player.start()
                _isPlaying.value = true
                startTicker()
            }
        } catch (e: Exception) {
            Log.w(tag, "Error toggling play/pause, restarting track", e)
            _currentTrack.value?.let { playTrack(it) }
        }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1L))
        try {
            mediaPlayer?.seekTo(bounded.toInt())
            _currentPositionMs.value = bounded
        } catch (e: Exception) {
            Log.w(tag, "Error seeking to $positionMs", e)
        }
    }

    /**
     * Reliable Next Track implementation across all sections (Songs, Artists, Albums, Playlists).
     * If the active queue has only 1 track, falls back to the full library so playback never gets stuck.
     */
    fun skipNext() {
        val q = _queue.value
        if (q.isEmpty()) {
            val all = allTracksProvider?.invoke() ?: emptyList()
            if (all.isNotEmpty()) {
                setQueue(all, 0)
            }
            return
        }

        // When queue has only 1 track, check fallback library so the Next button doesn't freeze or get stuck
        if (q.size == 1) {
            val all = allTracksProvider?.invoke() ?: emptyList()
            if (all.size > 1) {
                val curId = _currentTrack.value?.id ?: q[0].id
                val idxInAll = all.indexOfFirst { it.id == curId }
                val nextInAll = if (idxInAll >= 0) (idxInAll + 1) % all.size else 0
                // Switch to the full library queue from this point
                setQueue(all, nextInAll)
                return
            } else {
                // Replay from beginning
                seekTo(0L)
                mediaPlayer?.start()
                _isPlaying.value = true
                startTicker()
                return
            }
        }

        val nextIndex = if (_isShuffle.value && q.size > 1) {
            var candidate = (q.indices).random()
            if (candidate == currentIndex) {
                candidate = (currentIndex + 1) % q.size
            }
            candidate
        } else {
            (currentIndex + 1) % q.size
        }

        currentIndex = nextIndex
        playTrack(q[currentIndex])
    }

    /**
     * Reliable Previous Track implementation across all sections.
     * If played past 3 seconds, replays from beginning; otherwise goes to previous song.
     */
    fun skipPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        if (q.size == 1) {
            val all = allTracksProvider?.invoke() ?: emptyList()
            if (all.size > 1) {
                val curId = _currentTrack.value?.id ?: q[0].id
                val idxInAll = all.indexOfFirst { it.id == curId }
                val prevInAll = if (idxInAll - 1 < 0) all.lastIndex else idxInAll - 1
                setQueue(all, prevInAll)
                return
            } else {
                seekTo(0L)
                return
            }
        }

        val prevIndex = if (_isShuffle.value && q.size > 1) {
            var candidate = (q.indices).random()
            if (candidate == currentIndex) {
                candidate = if (currentIndex - 1 < 0) q.lastIndex else currentIndex - 1
            }
            candidate
        } else {
            if (currentIndex - 1 < 0) q.lastIndex else currentIndex - 1
        }

        currentIndex = prevIndex
        playTrack(q[currentIndex])
    }

    fun adjustSyncOffset(deltaMs: Long) {
        _syncOffsetMs.value += deltaMs
    }

    fun resetSyncOffset() {
        _syncOffsetMs.value = 0L
    }

    /**
     * Updates lyrics for current track. Supports both LRC and TXT formats.
     */
    fun updateLyricsForCurrentTrack(content: String) {
        val cur = _currentTrack.value ?: return
        val detectedType = LrcParser.detectLyricType(content)

        val updatedTrack = if (detectedType == LyricType.LRC) {
            cur.copy(lrcContent = content)
        } else {
            cur.copy(txtContent = content)
        }

        _currentTrack.value = updatedTrack
        _activeLyrics.value = LrcParser.parse(content)

        // Keep queue in sync
        _queue.value = _queue.value.map { if (it.id == cur.id) updatedTrack else it }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0L)
                try {
                    mediaPlayer?.start()
                    _isPlaying.value = true
                    startTicker()
                } catch (e: Exception) {
                    _currentTrack.value?.let { playTrack(it) }
                }
            }
            RepeatMode.ALL -> skipNext()
            RepeatMode.OFF -> {
                if (currentIndex < _queue.value.lastIndex) {
                    skipNext()
                } else {
                    _isPlaying.value = false
                    stopTicker()
                    seekTo(0L)
                }
            }
        }
    }

    private fun startTicker() {
        stopTicker()
        positionTickerJob = scope.launch {
            while (isActive) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            _currentPositionMs.value = mp.currentPosition.toLong()
                        }
                    }
                } catch (_: Exception) {
                    // Ignore transient playback state race conditions
                }
                delay(60) // smooth 60ms ticker for responsive lyrics
            }
        }
    }

    private fun stopTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    fun release() {
        releasePlayer()
    }

    /**
     * Safely tears down the MediaPlayer without throwing IllegalStateException.
     */
    private fun releasePlayer() {
        stopTicker()
        mediaPlayer?.let { player ->
            try {
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                player.reset()
                player.release()
            } catch (e: Exception) {
                Log.w(tag, "Error releasing player", e)
            }
        }
        mediaPlayer = null
        _isPlaying.value = false
    }
}
