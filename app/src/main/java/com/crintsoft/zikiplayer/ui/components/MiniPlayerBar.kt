package com.crintsoft.zikiplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.crintsoft.zikiplayer.lyrics.LrcParser
import com.crintsoft.zikiplayer.player.MusicPlaybackManager
import com.crintsoft.zikiplayer.ui.theme.AccentCyan
import com.crintsoft.zikiplayer.ui.theme.AccentGold
import com.crintsoft.zikiplayer.ui.theme.SurfaceCard

@Composable
fun MiniPlayerBar(
    playbackManager: MusicPlaybackManager,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPos by playbackManager.currentPositionMs.collectAsState()
    val duration by playbackManager.durationMs.collectAsState()
    val syncOffset by playbackManager.syncOffsetMs.collectAsState()
    val lyrics by playbackManager.activeLyrics.collectAsState()

    AnimatedVisibility(
        visible = track != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val currentTrack = track ?: return@AnimatedVisibility

        val activeIndex = remember(lyrics.lines, currentPos, syncOffset) {
            LrcParser.findActiveIndex(lyrics.lines, currentPos, syncOffset)
        }
        val currentLyricText = if (activeIndex in lyrics.lines.indices) {
            lyrics.lines[activeIndex].text
        } else if (lyrics.isTxt) {
            val firstLine = lyrics.rawText.lines().firstOrNull { it.isNotBlank() } ?: "Plain Lyrics"
            "TXT • $firstLine"
        } else {
            "MiniLyrics • Tap for lyrics"
        }

        val progress = if (duration > 0) {
            (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .testTag("mini_player_bar"),
            color = SurfaceCard,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = AccentCyan,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art / Music icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AccentCyan.copy(alpha = 0.3f), AccentGold.copy(alpha = 0.3f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Track Art",
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title, Artist, & Live Lyric line
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentLyricText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (activeIndex != -1) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (activeIndex != -1) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Play/Pause button
                    IconButton(
                        onClick = { playbackManager.togglePlayPause() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.15f))
                            .testTag("mini_play_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = AccentCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Next button
                    IconButton(
                        onClick = { playbackManager.skipNext() },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("mini_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
