package com.crintsoft.zikiplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crintsoft.zikiplayer.model.Playlist
import com.crintsoft.zikiplayer.model.Track
import com.crintsoft.zikiplayer.ui.LibraryTab
import com.crintsoft.zikiplayer.ui.MainViewModel
import com.crintsoft.zikiplayer.ui.components.MiniPlayerBar
import com.crintsoft.zikiplayer.ui.theme.AccentCyan
import com.crintsoft.zikiplayer.ui.theme.AccentGold
import com.crintsoft.zikiplayer.ui.theme.DarkBackground
import com.crintsoft.zikiplayer.ui.theme.HeaderToolbarDark
import com.crintsoft.zikiplayer.ui.theme.HeaderToolbarGradientBottom
import com.crintsoft.zikiplayer.ui.theme.SurfaceCard
import com.crintsoft.zikiplayer.ui.theme.SurfaceDark
import com.crintsoft.zikiplayer.ui.theme.SurfaceVariantDark
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaLibraryScreen(
    viewModel: MainViewModel,
    onPickAudioFile: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentLibraryTab.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val searchQuery by viewModel.librarySearchQuery.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var trackToAddToPlaylist by remember { mutableStateOf<Track?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val songListState = rememberLazyListState()

    val alphabet = remember { listOf("#") + ('A'..'Z').map { it.toString() } }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Classic MiniLyrics Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(HeaderToolbarDark, HeaderToolbarGradientBottom)
                            )
                        )
                        .border(width = 0.5.dp, color = Color(0xFF33333E))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AccentCyan, AccentGold)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Logo",
                                    tint = Color(0xFF100E1C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "MiniLyrics",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.scanDeviceAudio() },
                                modifier = Modifier.testTag("btn_rescan_media")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Rescan Media",
                                    tint = AccentCyan
                                )
                            }
                            IconButton(
                                onClick = onPickAudioFile,
                                modifier = Modifier.testTag("btn_open_file")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Open Audio File",
                                    tint = AccentGold
                                )
                            }
                        }
                    }
                }
            }

            // Classic 5-Tab Bar (Artists, Albums, Songs, Playlists, Playback)
            // Exactly matching user-expected order:
            // 0: Artists, 1: Albums, 2: Songs, 3: Playlists, 4: Playback
            val tabs = listOf(
                Triple(LibraryTab.ARTISTS, "Artists", Icons.Default.Person),
                Triple(LibraryTab.ALBUMS, "Albums", Icons.Default.Album),
                Triple(LibraryTab.SONGS, "Songs", Icons.Default.MusicNote),
                Triple(LibraryTab.PLAYLISTS, "Playlists", Icons.AutoMirrored.Filled.QueueMusic)
            )

            val selectedTabIndex = tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceDark,
                contentColor = AccentCyan,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, (tab, label, icon) ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { viewModel.setLibraryTab(tab) },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.testTag("tab_${label.lowercase()}")
                    )
                }

                // 5th Tab: Playback (Now Playing)
                Tab(
                    selected = false,
                    onClick = onOpenNowPlaying,
                    text = {
                        Text("Playback", fontSize = 12.sp, color = AccentGold)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Playback", tint = AccentGold, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("tab_playback")
                )
            }

            // Search Bar for Songs tab
            if (currentTab == LibraryTab.SONGS) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setLibrarySearchQuery(it) },
                    placeholder = { Text("Search artists, albums, and tracks…", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceVariantDark,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("library_search_input")
                )
            }

            // Tab Content with horizontal expansion
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    LibraryTab.ARTISTS -> {
                        // 1. Artists Section: Grouped by artist with expandable tracks
                        val artistsGrouped = remember(allTracks) {
                            allTracks.groupBy { it.artist }
                        }

                        if (isWideScreen) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 300.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(artistsGrouped.keys.toList()) { artistName ->
                                    val artistTracks = artistsGrouped[artistName] ?: emptyList()
                                    ArtistCard(
                                        artistName = artistName,
                                        tracks = artistTracks,
                                        onPlayAll = {
                                            viewModel.playArtist(artistTracks, 0)
                                        },
                                        onPlayTrack = { track, index ->
                                            viewModel.playArtist(artistTracks, index)
                                        },
                                        onAddToPlaylist = { trackToAddToPlaylist = it }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(artistsGrouped.keys.toList()) { artistName ->
                                    val artistTracks = artistsGrouped[artistName] ?: emptyList()
                                    ArtistCard(
                                        artistName = artistName,
                                        tracks = artistTracks,
                                        onPlayAll = {
                                            viewModel.playArtist(artistTracks, 0)
                                        },
                                        onPlayTrack = { track, index ->
                                            viewModel.playArtist(artistTracks, index)
                                        },
                                        onAddToPlaylist = { trackToAddToPlaylist = it }
                                    )
                                }
                            }
                        }
                    }

                    LibraryTab.ALBUMS -> {
                        // 2. Albums Section: Grouped by album with expandable tracks
                        val albumsGrouped = remember(allTracks) {
                            allTracks.groupBy { it.album }
                        }

                        if (isWideScreen) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 300.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(albumsGrouped.keys.toList()) { albumName ->
                                    val albumTracks = albumsGrouped[albumName] ?: emptyList()
                                    AlbumCard(
                                        albumName = albumName,
                                        tracks = albumTracks,
                                        onPlayAll = {
                                            viewModel.playAlbum(albumTracks, 0)
                                        },
                                        onPlayTrack = { track, index ->
                                            viewModel.playAlbum(albumTracks, index)
                                        },
                                        onAddToPlaylist = { trackToAddToPlaylist = it }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(albumsGrouped.keys.toList()) { albumName ->
                                    val albumTracks = albumsGrouped[albumName] ?: emptyList()
                                    AlbumCard(
                                        albumName = albumName,
                                        tracks = albumTracks,
                                        onPlayAll = {
                                            viewModel.playAlbum(albumTracks, 0)
                                        },
                                        onPlayTrack = { track, index ->
                                            viewModel.playAlbum(albumTracks, index)
                                        },
                                        onAddToPlaylist = { trackToAddToPlaylist = it }
                                    )
                                }
                            }
                        }
                    }

                    LibraryTab.SONGS -> {
                        // 3. Songs Section: Full list of songs
                        val filteredTracks = remember(allTracks, searchQuery) {
                            if (searchQuery.isBlank()) allTracks else {
                                allTracks.filter {
                                    it.title.contains(searchQuery, ignoreCase = true) ||
                                            it.artist.contains(searchQuery, ignoreCase = true) ||
                                            it.album.contains(searchQuery, ignoreCase = true)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxSize()) {
                            // Main Songs List
                            LazyColumn(
                                state = songListState,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp, end = 4.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(filteredTracks, key = { _, track -> track.id }) { index, track ->
                                    TrackItemCard(
                                        track = track,
                                        onClick = {
                                            viewModel.playbackManager.setQueue(filteredTracks, index)
                                            viewModel.navigateTo(com.crintsoft.zikiplayer.ui.MainDestination.NOW_PLAYING_LYRICS)
                                        },
                                        onAddToPlaylist = { trackToAddToPlaylist = track }
                                    )
                                }
                            }

                            // Fast Scroll Alphabet Bar (Right edge)
                            Column(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.SpaceAround,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                alphabet.forEach { letter ->
                                    Text(
                                        text = letter,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .clickable {
                                                val targetIndex = filteredTracks.indexOfFirst {
                                                    if (letter == "#") !it.title.first().isLetter()
                                                    else it.title.startsWith(letter, ignoreCase = true)
                                                }
                                                if (targetIndex != -1) {
                                                    coroutineScope.launch {
                                                        songListState.animateScrollToItem(targetIndex)
                                                    }
                                                }
                                            }
                                            .padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    LibraryTab.PLAYLISTS -> {
                        // 4. Playlists Section
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Button(
                                onClick = { showCreatePlaylistDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("btn_create_playlist"),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Playlist",
                                    tint = Color(0xFF100E1C)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Playlist", color = Color(0xFF100E1C), fontWeight = FontWeight.Bold)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 90.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(playlists, key = { it.id }) { pl ->
                                    val plTracks = allTracks.filter { pl.trackIds.contains(it.id) }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (plTracks.isNotEmpty()) {
                                                    viewModel.playbackManager.setQueue(plTracks, 0)
                                                    onOpenNowPlaying()
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AccentCyan.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                                    contentDescription = "Playlist",
                                                    tint = AccentCyan
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = pl.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = "${plTracks.size} track(s)",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deletePlaylist(pl.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Playlist",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Persistent MiniPlayer floating on bottom
        MiniPlayerBar(
            playbackManager = viewModel.playbackManager,
            onClick = onOpenNowPlaying,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Dialogs
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createPlaylist(newPlaylistName)
                        newPlaylistName = ""
                        showCreatePlaylistDialog = false
                    }
                ) {
                    Text("Save", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    trackToAddToPlaylist?.let { targetTrack ->
        AlertDialog(
            onDismissRequest = { trackToAddToPlaylist = null },
            title = { Text("Add \"${targetTrack.title}\" to:") },
            text = {
                LazyColumn {
                    items(playlists) { pl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addTrackToPlaylist(pl.id, targetTrack.id)
                                    trackToAddToPlaylist = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = pl.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { trackToAddToPlaylist = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ArtistCard(
    artistName: String,
    tracks: List<Track>,
    onPlayAll: () -> Unit,
    onPlayTrack: (Track, Int) -> Unit,
    onAddToPlaylist: (Track) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("artist_card_${artistName.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Artist",
                        tint = AccentCyan
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${tracks.size} track(s)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Quick Play All Button
                IconButton(
                    onClick = onPlayAll,
                    modifier = Modifier.testTag("btn_play_artist_${artistName.replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play Artist Tracks",
                        tint = AccentGold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Expand/Collapse arrow
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    tracks.forEachIndexed { index, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayTrack(track, index) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.width(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.album,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (track.hasLrc) {
                                Surface(
                                    color = AccentCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text("LRC", fontSize = 8.sp, color = AccentCyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            } else if (track.hasTxt) {
                                Surface(
                                    color = AccentGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text("TXT", fontSize = 8.sp, color = AccentGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val sec = (track.durationMs / 1000).coerceAtLeast(0)
                            Text(
                                text = String.format(Locale.US, "%d:%02d", sec / 60, sec % 60),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumCard(
    albumName: String,
    tracks: List<Track>,
    onPlayAll: () -> Unit,
    onPlayTrack: (Track, Int) -> Unit,
    onAddToPlaylist: (Track) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("album_card_${albumName.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = "Album",
                        tint = AccentGold
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    val artist = tracks.firstOrNull()?.artist ?: "Various Artists"
                    Text(
                        text = "$artist • ${tracks.size} track(s)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Quick Play All Button
                IconButton(
                    onClick = onPlayAll,
                    modifier = Modifier.testTag("btn_play_album_${albumName.replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play Album Tracks",
                        tint = AccentGold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Expand/Collapse arrow
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    tracks.forEachIndexed { index, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayTrack(track, index) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.width(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (track.hasLrc) {
                                Surface(
                                    color = AccentCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text("LRC", fontSize = 8.sp, color = AccentCyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            } else if (track.hasTxt) {
                                Surface(
                                    color = AccentGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text("TXT", fontSize = 8.sp, color = AccentGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val sec = (track.durationMs / 1000).coerceAtLeast(0)
                            Text(
                                text = String.format(Locale.US, "%d:%02d", sec / 60, sec % 60),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackItemCard(
    track: Track,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("track_item_${track.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentCyan.copy(alpha = 0.25f), AccentGold.copy(alpha = 0.25f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (track.hasLrc) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AccentCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LRC",
                                color = AccentCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else if (track.hasTxt) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AccentGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "TXT",
                                color = AccentGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val seconds = (track.durationMs / 1000).coerceAtLeast(0)
            Text(
                text = String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60),
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                }
            }
        }
    }
}
