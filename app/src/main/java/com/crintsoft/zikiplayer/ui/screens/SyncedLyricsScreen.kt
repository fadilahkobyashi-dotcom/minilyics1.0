package com.crintsoft.zikiplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crintsoft.zikiplayer.lyrics.LrcParser
import com.crintsoft.zikiplayer.model.LyricType
import com.crintsoft.zikiplayer.model.LyricsData
import com.crintsoft.zikiplayer.player.MusicPlaybackManager
import com.crintsoft.zikiplayer.player.RepeatMode
import com.crintsoft.zikiplayer.ui.theme.AccentCyan
import com.crintsoft.zikiplayer.ui.theme.AccentGold
import com.crintsoft.zikiplayer.ui.theme.AccentGreen
import com.crintsoft.zikiplayer.ui.theme.AccentOrange
import com.crintsoft.zikiplayer.ui.theme.HeaderToolbarDark
import com.crintsoft.zikiplayer.ui.theme.HeaderToolbarGradientBottom
import com.crintsoft.zikiplayer.ui.theme.LyricsDisplayPreferences
import com.crintsoft.zikiplayer.ui.theme.SurfaceCard
import com.crintsoft.zikiplayer.ui.theme.SurfaceDark
import com.crintsoft.zikiplayer.ui.theme.SurfaceVariantDark
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncedLyricsScreen(
    playbackManager: MusicPlaybackManager,
    preferences: LyricsDisplayPreferences,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenPreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val syncOffsetMs by playbackManager.syncOffsetMs.collectAsState()
    val lyrics by playbackManager.activeLyrics.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()
    val queue by playbackManager.queue.collectAsState()

    val listState = rememberLazyListState()
    var currentViewMode by remember { mutableIntStateOf(0) } // 0: Lyrics, 1: Album Art
    var showQueueSheet by remember { mutableStateOf(false) }

    // Active line index for LRC synchronized lyrics
    val activeIndex = remember(lyrics, currentPositionMs, syncOffsetMs) {
        if (lyrics.isLrc) {
            LrcParser.findActiveIndex(lyrics.lines, currentPositionMs, syncOffsetMs)
        } else {
            -1
        }
    }

    // Auto-scroll when activeIndex changes in LRC mode
    LaunchedEffect(activeIndex) {
        if (lyrics.isLrc && activeIndex in lyrics.lines.indices) {
            val targetScroll = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0f) }

    val sliderValue = if (isUserScrubbing) scrubPosition else {
        if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f
    }

    // Vinyl rotation animation for album art mode
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val vinylAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        preferences.backgroundColor,
                        Color(0xFF0C0C10)
                    )
                )
            )
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Classic MiniLyrics Header Bar (buttonbarbackground)
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
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFF33333E)
                        )
                ) {
                    TopAppBar(
                        title = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = track?.title ?: "MiniLyrics",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )

                                    // Lyrics format badge (LRC vs TXT)
                                    if (lyrics.isLrc) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = AccentGreen.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "LRC",
                                                color = AccentGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else if (lyrics.isTxt) {
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
                                    text = track?.artist ?: "Unknown Artist",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            // btn_ng_medialib: switches back to media library
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("btn_ng_medialib")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = "Media Library",
                                    tint = AccentCyan
                                )
                            }
                        },
                        actions = {
                            // View Switcher (Lyrics vs Album Art)
                            IconButton(
                                onClick = { currentViewMode = if (currentViewMode == 0) 1 else 0 },
                                modifier = Modifier.testTag("btn_toggle_view_mode")
                            ) {
                                Icon(
                                    imageVector = if (currentViewMode == 0) Icons.Default.Album else Icons.Default.MusicNote,
                                    contentDescription = "Toggle View",
                                    tint = AccentGold
                                )
                            }
                            // Search Lyrics Online
                            IconButton(
                                onClick = onOpenSearch,
                                modifier = Modifier.testTag("search_lyrics_top_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Lyrics Online",
                                    tint = AccentCyan
                                )
                            }
                            // Preferences
                            IconButton(
                                onClick = onOpenPreferences,
                                modifier = Modifier.testTag("preferences_top_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Preferences",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // btn_ng_cur_playlist: open queue bottom sheet
                            IconButton(
                                onClick = { showQueueSheet = true },
                                modifier = Modifier.testTag("btn_ng_cur_playlist")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = "Current Playlist",
                                    tint = AccentOrange
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            // Sync Offset Adjust Bar / Lyrics Status Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                color = SurfaceVariantDark.copy(alpha = 0.9f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333342))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (lyrics.isTxt) {
                        // Plain TXT Lyrics Mode
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "TXT Lyrics",
                                tint = AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TXT Plain Lyrics (Scroll mode)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // Badge indicating no artificial timing required
                        Surface(
                            color = SurfaceCard,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Full Text",
                                fontSize = 11.sp,
                                color = AccentGold,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        // LRC Synchronized Mode: Calibration controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { playbackManager.resetSyncOffset() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatPaint,
                                contentDescription = "Sync",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sync Offset:",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val offsetSeconds = syncOffsetMs / 1000.0
                            val offsetText = String.format(Locale.US, "%+.1fs", offsetSeconds)
                            Text(
                                text = offsetText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncOffsetMs != 0L) AccentGold else AccentCyan
                                ),
                                modifier = Modifier.testTag("offset_badge")
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Backward 0.5s (btn_backward_lyrics)
                            Button(
                                onClick = { playbackManager.adjustSyncOffset(-500L) },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("offset_minus_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "Backward Lyrics",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("-0.5s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Forward 0.5s (btn_forward_lyrics)
                            Button(
                                onClick = { playbackManager.adjustSyncOffset(500L) },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("offset_plus_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+0.5s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Forward Lyrics",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            // Main Display Area: Expands horizontally on modern Android 16 screens
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isWideScreen) {
                    // Two-pane horizontal layout on wide screens / landscape / tablets
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Pane: Album Art / Now Playing Details (40% width)
                        Box(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            AlbumArtDisplay(
                                track = track,
                                isPlaying = isPlaying,
                                vinylAngle = vinylAngle
                            )
                        }

                        // Right Pane: Scrolling Lyrics (58% width)
                        Box(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight()
                        ) {
                            LyricsContainerView(
                                lyrics = lyrics,
                                activeIndex = activeIndex,
                                preferences = preferences,
                                listState = listState,
                                onLineClick = { playbackManager.seekTo(it) },
                                onOpenSearch = onOpenSearch
                            )
                        }
                    }
                } else {
                    // Standard Portrait Layout with View Toggle
                    if (currentViewMode == 0) {
                        LyricsContainerView(
                            lyrics = lyrics,
                            activeIndex = activeIndex,
                            preferences = preferences,
                            listState = listState,
                            onLineClick = { playbackManager.seekTo(it) },
                            onOpenSearch = onOpenSearch
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AlbumArtDisplay(
                                track = track,
                                isPlaying = isPlaying,
                                vinylAngle = vinylAngle
                            )
                        }
                    }
                }
            }

            // Bottom Audio Controls Bar (res/drawable/play_ctrl_bg)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                color = SurfaceDark.copy(alpha = 0.98f),
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF333342))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    // Seek Bar
                    Slider(
                        value = sliderValue.coerceIn(0f, 1f),
                        onValueChange = { newValue ->
                            isUserScrubbing = true
                            scrubPosition = newValue
                        },
                        onValueChangeFinished = {
                            val newPositionMs = (scrubPosition * durationMs).toLong()
                            playbackManager.seekTo(newPositionMs)
                            isUserScrubbing = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color(0xFF333344)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playback_seek_bar")
                    )

                    // Timestamps: Current & Total Duration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val displayPos = if (isUserScrubbing) (scrubPosition * durationMs).toLong() else currentPositionMs
                        Text(
                            text = formatDuration(displayPos),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = formatDuration(durationMs),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons Row (Shuffle, Previous, Play/Pause, Next, Repeat)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Repeat Button (btn_repeat)
                        IconButton(
                            onClick = { playbackManager.cycleRepeatMode() },
                            modifier = Modifier.testTag("btn_repeat")
                        ) {
                            val (icon, tint) = when (repeatMode) {
                                RepeatMode.OFF -> Icons.Default.Repeat to MaterialTheme.colorScheme.onSurfaceVariant
                                RepeatMode.ALL -> Icons.Default.Repeat to AccentCyan
                                RepeatMode.ONE -> Icons.Default.RepeatOne to AccentGold
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Repeat Mode",
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Previous Track (btn_prev)
                        IconButton(
                            onClick = { playbackManager.skipPrevious() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_prev")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                modifier = Modifier.size(34.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Play/Pause Center Button (btn_play / btn_pause) - Classic Circular Media Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF3E3E4C), Color(0xFF1E1E28))
                                    )
                                )
                                .border(2.dp, Brush.verticalGradient(listOf(Color(0xFF6B6B80), Color(0xFF252532))), CircleShape)
                                .clickable { playbackManager.togglePlayPause() }
                                .testTag("btn_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = if (isPlaying) AccentCyan else AccentGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Next Track (btn_next)
                        IconButton(
                            onClick = { playbackManager.skipNext() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_next")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                modifier = Modifier.size(34.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Shuffle Button (btn_shuffle)
                        IconButton(
                            onClick = { playbackManager.toggleShuffle() },
                            modifier = Modifier.testTag("btn_shuffle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Current Queue Bottom Sheet
    if (showQueueSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Current Playlist / Queue (${queue.size} songs)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(queue) { index, item ->
                        val isCurrent = item.id == track?.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playbackManager.setQueue(queue, index)
                                    showQueueSheet = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) SurfaceCard else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isCurrent) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrent) AccentCyan else MaterialTheme.colorScheme.onBackground
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (item.hasLrc) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AccentGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(3.dp)
                                            ) {
                                                Text(
                                                    "LRC",
                                                    fontSize = 8.sp,
                                                    color = AccentGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                            }
                                        } else if (item.hasTxt) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AccentGold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(3.dp)
                                            ) {
                                                Text(
                                                    "TXT",
                                                    fontSize = 8.sp,
                                                    color = AccentGold,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.artist,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

/**
 * Universal lyrics container supporting both:
 * 1. Synchronized LRC lyrics (karaoke highlight + auto-scroll)
 * 2. Plain TXT lyrics (multiline formatted text with full Unicode support)
 */
@Composable
private fun LyricsContainerView(
    lyrics: LyricsData,
    activeIndex: Int,
    preferences: LyricsDisplayPreferences,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onLineClick: (Long) -> Unit,
    onOpenSearch: () -> Unit
) {
    if (lyrics.isEmpty) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AccentCyan,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No lyrics loaded",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Supports both LRC synchronized & TXT plain lyrics",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("search_lyrics_empty_button")
                    ) {
                        Text("Search or Load Lyrics", color = Color(0xFF100E1C), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else if (lyrics.isTxt) {
        // Plain TXT Lyrics View: Clean scrollable formatted text
        // Preserves line breaks, paragraph spacing, and full Unicode without fake timing
        val lines = remember(lyrics.rawText) {
            lyrics.rawText.lines()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .testTag("txt_lyrics_lazy_column"),
            contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(lines) { _, lineText ->
                if (lineText.isBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        contentAlignment = when (preferences.textAlign) {
                            TextAlign.Start -> Alignment.CenterStart
                            TextAlign.End -> Alignment.CenterEnd
                            else -> Alignment.Center
                        }
                    ) {
                        Text(
                            text = lineText,
                            fontSize = preferences.fontSize,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = preferences.textAlign,
                            lineHeight = (preferences.fontSize.value * 1.42).sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    } else {
        // Synchronized LRC Lyrics View: Auto-scroller with highlight
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .testTag("lyrics_lazy_column"),
            contentPadding = PaddingValues(top = 90.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(lyrics.lines) { index, line ->
                val isActive = index == activeIndex

                val textColor by animateColorAsState(
                    targetValue = if (isActive) preferences.highlightColor else preferences.lowlightColor,
                    label = "textColor"
                )
                val textScale by animateFloatAsState(
                    targetValue = if (isActive) 1.08f else 1.0f,
                    animationSpec = spring(),
                    label = "textScale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(textScale)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLineClick(line.timestampMs) }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    contentAlignment = when (preferences.textAlign) {
                        TextAlign.Start -> Alignment.CenterStart
                        TextAlign.End -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                ) {
                    Text(
                        text = line.text,
                        fontSize = preferences.fontSize,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                        color = textColor,
                        textAlign = preferences.textAlign,
                        lineHeight = (preferences.fontSize.value * 1.36).sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumArtDisplay(
    track: com.crintsoft.zikiplayer.model.Track?,
    isPlaying: Boolean,
    vinylAngle: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Vinyl Record visual
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2E2E38),
                            Color(0xFF14141A),
                            Color(0xFF0A0A0E)
                        )
                    )
                )
                .border(6.dp, Color(0xFF1F1F28), CircleShape)
                .rotate(if (isPlaying) vinylAngle else 0f),
            contentAlignment = Alignment.Center
        ) {
            // Grooves
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            )

            // Center Label
            Box(
                modifier = Modifier
                    .size(80.dp)
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
                    contentDescription = null,
                    tint = Color(0xFF100E1C),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = track?.title ?: "No Song Selected",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${track?.artist ?: "Unknown Artist"} • ${track?.album ?: "Unknown Album"}",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
