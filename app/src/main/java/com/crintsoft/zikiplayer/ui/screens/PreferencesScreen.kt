package com.crintsoft.zikiplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crintsoft.zikiplayer.ui.MainViewModel
import com.crintsoft.zikiplayer.ui.theme.AccentCyan
import com.crintsoft.zikiplayer.ui.theme.AccentGold
import com.crintsoft.zikiplayer.ui.theme.DarkBackground
import com.crintsoft.zikiplayer.ui.theme.HighlightCyan
import com.crintsoft.zikiplayer.ui.theme.HighlightGold
import com.crintsoft.zikiplayer.ui.theme.HighlightNeonGreen
import com.crintsoft.zikiplayer.ui.theme.HighlightPink
import com.crintsoft.zikiplayer.ui.theme.HighlightWhite
import com.crintsoft.zikiplayer.ui.theme.LowlightGray
import com.crintsoft.zikiplayer.ui.theme.LowlightSlate
import com.crintsoft.zikiplayer.ui.theme.LowlightWhite
import com.crintsoft.zikiplayer.ui.theme.SurfaceCard
import com.crintsoft.zikiplayer.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.lyricsPreferences.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Lyrics Display Options",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("preferences_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceDark,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Preview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = prefs.backgroundColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = when (prefs.textAlign) {
                        TextAlign.Start -> Alignment.Start
                        TextAlign.End -> Alignment.End
                        else -> Alignment.CenterHorizontally
                    }
                ) {
                    Text(
                        text = "PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentCyan, letterSpacing = 2.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "♪ Gentle starlight in the sky ♪",
                        fontSize = prefs.fontSize,
                        color = prefs.lowlightColor,
                        textAlign = prefs.textAlign
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MiniLyrics karaoke line in focus",
                        fontSize = (prefs.fontSize.value + 2).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = prefs.highlightColor,
                        textAlign = prefs.textAlign
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Synchronized to your favorite song",
                        fontSize = prefs.fontSize,
                        color = prefs.lowlightColor,
                        textAlign = prefs.textAlign
                    )
                }
            }

            // Font Size Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lyrics Font Size",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val sizes = listOf(14.sp to "Small", 18.sp to "Medium", 22.sp to "Large", 26.sp to "XL")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        sizes.forEachIndexed { index, (size, label) ->
                            SegmentedButton(
                                selected = prefs.fontSize == size,
                                onClick = { viewModel.updatePreferences { it.copy(fontSize = size) } },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = sizes.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = AccentCyan,
                                    activeContentColor = Color(0xFF100E1C),
                                    inactiveContainerColor = SurfaceDark,
                                    inactiveContentColor = MaterialTheme.colorScheme.onBackground
                                )
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Alignment Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Text Alignment",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val alignments = listOf(
                        TextAlign.Start to ("Left" to Icons.Default.FormatAlignLeft),
                        TextAlign.Center to ("Center" to Icons.Default.FormatAlignCenter),
                        TextAlign.End to ("Right" to Icons.Default.FormatAlignRight)
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        alignments.forEachIndexed { index, (align, info) ->
                            val (label, icon) = info
                            SegmentedButton(
                                selected = prefs.textAlign == align,
                                onClick = { viewModel.updatePreferences { it.copy(textAlign = align) } },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = alignments.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = AccentCyan,
                                    activeContentColor = Color(0xFF100E1C),
                                    inactiveContainerColor = SurfaceDark,
                                    inactiveContentColor = MaterialTheme.colorScheme.onBackground
                                )
                            ) {
                                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Highlight Color Chips
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Highlight Lyric Color",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val highlightColors = listOf(
                        HighlightGold to "Gold",
                        HighlightCyan to "Cyan",
                        HighlightNeonGreen to "Green",
                        HighlightPink to "Pink",
                        HighlightWhite to "White"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        highlightColors.forEach { (color, _) ->
                            val isSelected = prefs.highlightColor == color
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { viewModel.updatePreferences { it.copy(highlightColor = color) } }
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                            )
                        }
                    }
                }
            }

            // Lowlight Color Chips
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Normal Text Color",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val lowlightColors = listOf(
                        LowlightGray to "Muted Gray",
                        LowlightSlate to "Cool Slate",
                        LowlightWhite to "Soft White"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        lowlightColors.forEach { (color, label) ->
                            val isSelected = prefs.lowlightColor == color
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updatePreferences { it.copy(lowlightColor = color) } }
                                    .background(if (isSelected) SurfaceDark else Color.Transparent)
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) Modifier.border(2.dp, AccentCyan, CircleShape)
                                            else Modifier
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
            }

            // Background Theme
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Background Theme",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val bgThemes = listOf(
                        Color(0xFF100E1C) to "Midnight",
                        Color(0xFF000000) to "Pitch Black",
                        Color(0xFF0D1B2A) to "Deep Ocean",
                        Color(0xFF1E1E24) to "Studio Dark"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        bgThemes.forEach { (bgColor, name) ->
                            val isSelected = prefs.backgroundColor == bgColor
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    viewModel.updatePreferences { it.copy(backgroundColor = bgColor) }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bgColor)
                                        .then(
                                            if (isSelected) Modifier.border(2.dp, AccentCyan, RoundedCornerShape(10.dp))
                                            else Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Keep Screen On Switch
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keep Screen On",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Prevent screen from dimming while lyrics scroll",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = prefs.keepScreenOn,
                        onCheckedChange = { checked ->
                            viewModel.updatePreferences { it.copy(keepScreenOn = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = AccentCyan.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("switch_keep_screen_on")
                    )
                }
            }

            // Lyrics Search and Download Category (Original pf_cat_lyrics_search_download)
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lyrics Search & Download",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Save Downloaded Lyrics In",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Music directory (alongside audio tracks)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Extra Lyrics Search Directory",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Internal / Download / LRC storage enabled",
                        style = MaterialTheme.typography.bodySmall.copy(color = AccentCyan)
                    )
                }
            }

            // About Footer
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MiniLyrics for Android",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Synchronized scrolling lyrics & audio player",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentGold)
                    )
                }
            }
        }
    }
}
