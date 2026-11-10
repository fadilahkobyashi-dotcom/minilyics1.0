package com.crintsoft.zikiplayer.ui

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crintsoft.zikiplayer.lyrics.DefaultLyrics
import com.crintsoft.zikiplayer.lyrics.LyricsSearchResult
import com.crintsoft.zikiplayer.lyrics.LyricsSearchService
import com.crintsoft.zikiplayer.lyrics.LrcParser
import com.crintsoft.zikiplayer.model.LyricType
import com.crintsoft.zikiplayer.model.Playlist
import com.crintsoft.zikiplayer.model.Track
import com.crintsoft.zikiplayer.player.MusicPlaybackManager
import com.crintsoft.zikiplayer.ui.theme.LyricsDisplayPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

enum class MainDestination {
    LIBRARY,
    NOW_PLAYING_LYRICS,
    ONLINE_LYRICS_SEARCH,
    PREFERENCES
}

/**
 * Navigation tabs matching the exact 4 library sections in MiniLyrics:
 * 1. Artists (index 0)
 * 2. Albums (index 1)
 * 3. Songs (index 2)
 * 4. Playlists (index 3)
 */
enum class LibraryTab {
    ARTISTS,
    ALBUMS,
    SONGS,
    PLAYLISTS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val playbackManager = MusicPlaybackManager(application)

    private val _currentDestination = MutableStateFlow(MainDestination.LIBRARY)
    val currentDestination: StateFlow<MainDestination> = _currentDestination.asStateFlow()

    private val _currentLibraryTab = MutableStateFlow(LibraryTab.SONGS)
    val currentLibraryTab: StateFlow<LibraryTab> = _currentLibraryTab.asStateFlow()

    private val _allTracks = MutableStateFlow<List<Track>>(DefaultLyrics.getDefaultTracks())
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(
        listOf(
            Playlist(id = "pl_favorites", name = "Favorites", trackIds = listOf("demo_starlight", "demo_cyberpulse")),
            Playlist(
                id = "pl_recent",
                name = "Recently Added",
                trackIds = listOf("demo_starlight", "demo_cosmic_lullaby", "demo_cyberpulse", "demo_txt_horizon")
            )
        )
    )
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    private val _lyricsPreferences = MutableStateFlow(LyricsDisplayPreferences())
    val lyricsPreferences: StateFlow<LyricsDisplayPreferences> = _lyricsPreferences.asStateFlow()

    // Online lyrics search state
    private val _onlineSearchQuery = MutableStateFlow("")
    val onlineSearchQuery: StateFlow<String> = _onlineSearchQuery.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    private val _onlineResults = MutableStateFlow<List<LyricsSearchResult>>(emptyList())
    val onlineResults: StateFlow<List<LyricsSearchResult>> = _onlineResults.asStateFlow()

    private val _onlineError = MutableStateFlow<String?>(null)
    val onlineError: StateFlow<String?> = _onlineError.asStateFlow()

    init {
        // Link allTracks provider to playback manager so Next Track never gets stuck
        playbackManager.allTracksProvider = { _allTracks.value }
        scanDeviceAudio()
    }

    fun navigateTo(dest: MainDestination) {
        _currentDestination.value = dest
    }

    fun setLibraryTab(tab: LibraryTab) {
        _currentLibraryTab.value = tab
    }

    fun setLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    fun updatePreferences(transform: (LyricsDisplayPreferences) -> LyricsDisplayPreferences) {
        _lyricsPreferences.value = transform(_lyricsPreferences.value)
    }

    /**
     * Plays a track with an explicit queue context (Songs, Artists, Albums, or Playlists).
     */
    fun playTrack(track: Track, currentQueue: List<Track>? = _allTracks.value) {
        val q = currentQueue ?: _allTracks.value
        playbackManager.playTrack(track, q)
        navigateTo(MainDestination.NOW_PLAYING_LYRICS)
    }

    /**
     * Plays from an Artist collection with the correct queue and starting index.
     */
    fun playArtist(artistTracks: List<Track>, startIndex: Int = 0) {
        if (artistTracks.isEmpty()) return
        playbackManager.setQueue(artistTracks, startIndex)
        navigateTo(MainDestination.NOW_PLAYING_LYRICS)
    }

    /**
     * Plays from an Album collection with the correct queue and starting index.
     */
    fun playAlbum(albumTracks: List<Track>, startIndex: Int = 0) {
        if (albumTracks.isEmpty()) return
        playbackManager.setQueue(albumTracks, startIndex)
        navigateTo(MainDestination.NOW_PLAYING_LYRICS)
    }

    fun addCustomTrack(uri: Uri, title: String, artist: String, durationMs: Long) {
        val newTrack = Track(
            id = "user_${System.currentTimeMillis()}",
            title = title.ifBlank { "Custom Track" },
            artist = artist.ifBlank { "Unknown Artist" },
            album = "Local Storage",
            durationMs = durationMs,
            audioUri = uri.toString(),
            lrcContent = null,
            txtContent = null,
            isDemo = false
        )
        val updatedList = listOf(newTrack) + _allTracks.value
        _allTracks.value = updatedList
        playTrack(newTrack, updatedList)
    }

    /**
     * Imports a .lrc or .txt file directly from device storage and assigns it to current song.
     */
    fun importLyricsFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                val content = reader.readText()
                reader.close()
                inputStream.close()

                if (content.isNotBlank()) {
                    playbackManager.updateLyricsForCurrentTrack(content)
                    // Also update in allTracks
                    val cur = playbackManager.currentTrack.value
                    if (cur != null) {
                        _allTracks.value = _allTracks.value.map {
                            if (it.id == cur.id) cur else it
                        }
                    }
                }
            } catch (e: Exception) {
                _onlineError.value = "Failed to read lyrics file: ${e.message}"
            }
        }
    }

    fun scanDeviceAudio() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val resolver = context.contentResolver
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION
                )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val cursor = resolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    "${MediaStore.Audio.Media.TITLE} ASC"
                )

                val scanned = mutableListOf<Track>()
                cursor?.use { c ->
                    val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                    while (c.moveToNext()) {
                        val id = c.getLong(idCol)
                        val title = c.getString(titleCol) ?: "Unknown"
                        val artist = c.getString(artistCol) ?: "Unknown Artist"
                        val album = c.getString(albumCol) ?: "Unknown Album"
                        val duration = c.getLong(durCol)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        scanned.add(
                            Track(
                                id = "ms_$id",
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                audioUri = contentUri.toString(),
                                isDemo = false
                            )
                        )
                    }
                }

                if (scanned.isNotEmpty()) {
                    _allTracks.value = DefaultLyrics.getDefaultTracks() + scanned
                }
            } catch (e: Exception) {
                // MediaStore permission might not be granted yet, graceful fallback
            }
        }
    }

    fun createPlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val newPl = Playlist(
            id = "pl_${System.currentTimeMillis()}",
            name = trimmed,
            trackIds = emptyList()
        )
        _playlists.value = _playlists.value + newPl
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filter { it.id != playlistId }
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId) it.copy(name = trimmed) else it
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId && !it.trackIds.contains(trackId)) {
                it.copy(trackIds = it.trackIds + trackId)
            } else it
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId) {
                it.copy(trackIds = it.trackIds.filter { id -> id != trackId })
            } else it
        }
    }

    fun searchOnlineLyrics(query: String) {
        _onlineSearchQuery.value = query
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        _isSearchingOnline.value = true
        _onlineError.value = null
        _onlineResults.value = emptyList()

        viewModelScope.launch {
            val result = LyricsSearchService.searchLyrics(trimmed)
            _isSearchingOnline.value = false
            result.onSuccess { list ->
                _onlineResults.value = list
                if (list.isEmpty()) {
                    _onlineError.value = "No lyrics found for \"$trimmed\""
                }
            }.onFailure { err ->
                _onlineError.value = "Network error: ${err.message ?: "Unable to reach server"}"
            }
        }
    }

    fun applyLyricsResult(result: LyricsSearchResult) {
        val text = result.syncedLyrics ?: result.plainLyrics ?: return
        playbackManager.updateLyricsForCurrentTrack(text)
        navigateTo(MainDestination.NOW_PLAYING_LYRICS)
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.release()
    }
}
