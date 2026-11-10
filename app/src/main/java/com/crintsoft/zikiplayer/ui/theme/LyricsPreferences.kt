package com.crintsoft.zikiplayer.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class LyricsDisplayPreferences(
    val fontSize: TextUnit = 18.sp,
    val textAlign: TextAlign = TextAlign.Center,
    val highlightColor: Color = HighlightGold,
    val lowlightColor: Color = LowlightGray,
    val backgroundColor: Color = DarkBackground,
    val keepScreenOn: Boolean = true,
    val showTimestamps: Boolean = false
)
