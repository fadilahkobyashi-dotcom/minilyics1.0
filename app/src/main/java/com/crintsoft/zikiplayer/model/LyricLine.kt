package com.crintsoft.zikiplayer.model

enum class LyricType {
    NONE,
    LRC,    // Synchronized lyrics with timestamps [mm:ss.xx]
    TXT     // Plain text lyrics preserving line breaks, paragraphs, and Unicode
}

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class LyricsData(
    val type: LyricType = LyricType.NONE,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val by: String = "",
    val offsetMs: Long = 0L,
    val lines: List<LyricLine> = emptyList(),
    val rawText: String = ""
) {
    val isEmpty: Boolean get() = when (type) {
        LyricType.NONE -> true
        LyricType.LRC -> lines.isEmpty()
        LyricType.TXT -> rawText.isBlank()
    }

    val isSynced: Boolean get() = type == LyricType.LRC && lines.isNotEmpty()
    val isLrc: Boolean get() = type == LyricType.LRC
    val isTxt: Boolean get() = type == LyricType.TXT
}

typealias SyncedLyrics = LyricsData
