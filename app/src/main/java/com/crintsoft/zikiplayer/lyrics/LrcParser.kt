package com.crintsoft.zikiplayer.lyrics

import com.crintsoft.zikiplayer.model.LyricLine
import com.crintsoft.zikiplayer.model.LyricType
import com.crintsoft.zikiplayer.model.LyricsData
import java.util.regex.Pattern

object LrcParser {
    private val TIME_TAG_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]")
    private val META_TAG_PATTERN = Pattern.compile("^\\[(ti|ar|al|by|offset):([^\\]]*)\\]$", Pattern.CASE_INSENSITIVE)

    /**
     * Detects whether given content is synchronized LRC or plain TXT.
     */
    fun detectLyricType(content: String): LyricType {
        if (content.isBlank()) return LyricType.NONE
        return if (TIME_TAG_PATTERN.matcher(content).find()) {
            LyricType.LRC
        } else {
            LyricType.TXT
        }
    }

    /**
     * Automatic format detection:
     * - If content contains timestamp tags [mm:ss.xx], parses as synchronized LRC.
     * - Otherwise, parses as plain text TXT lyrics preserving original line breaks, paragraphs, and Unicode.
     */
    fun parse(content: String): LyricsData {
        if (content.isBlank()) {
            return LyricsData()
        }

        val hasTimeTags = TIME_TAG_PATTERN.matcher(content).find()
        return if (hasTimeTags) {
            parseLrc(content)
        } else {
            parseTxt(content)
        }
    }

    /**
     * Parses standard LRC formatted lyrics with timestamps and metadata tags.
     */
    fun parseLrc(lrcText: String): LyricsData {
        if (lrcText.isBlank()) return LyricsData()

        var title = ""
        var artist = ""
        var album = ""
        var by = ""
        var fileOffsetMs = 0L
        val parsedLines = mutableListOf<LyricLine>()

        val rawLines = lrcText.lines()

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Check meta tags first
            val metaMatcher = META_TAG_PATTERN.matcher(trimmed)
            if (metaMatcher.matches()) {
                val tag = metaMatcher.group(1)?.lowercase()
                val value = metaMatcher.group(2)?.trim() ?: ""
                when (tag) {
                    "ti" -> title = value
                    "ar" -> artist = value
                    "al" -> album = value
                    "by" -> by = value
                    "offset" -> fileOffsetMs = value.toLongOrNull() ?: 0L
                }
                continue
            }

            // Extract all time tags in this line
            val timeMatcher = TIME_TAG_PATTERN.matcher(trimmed)
            val timestamps = mutableListOf<Long>()
            var lastEnd = 0

            while (timeMatcher.find()) {
                val minutes = timeMatcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = timeMatcher.group(2)?.toLongOrNull() ?: 0L
                val fractionStr = timeMatcher.group(3)

                val millis = when {
                    fractionStr == null -> 0L
                    fractionStr.length == 1 -> fractionStr.toLong() * 100L
                    fractionStr.length == 2 -> fractionStr.toLong() * 10L
                    else -> fractionStr.take(3).toLong()
                }

                val totalMs = (minutes * 60L + seconds) * 1000L + millis
                timestamps.add(totalMs)
                lastEnd = timeMatcher.end()
            }

            if (timestamps.isNotEmpty()) {
                val text = trimmed.substring(lastEnd).trim()
                for (ts in timestamps) {
                    parsedLines.add(LyricLine(timestampMs = ts, text = text))
                }
            }
        }

        val sortedLines = parsedLines.sortedBy { it.timestampMs }

        return LyricsData(
            type = if (sortedLines.isNotEmpty()) LyricType.LRC else LyricType.TXT,
            title = title,
            artist = artist,
            album = album,
            by = by,
            offsetMs = fileOffsetMs,
            lines = sortedLines,
            rawText = lrcText
        )
    }

    /**
     * Parses ordinary TXT plain text lyrics.
     * Preserves exact line breaks, empty lines for stanza/paragraph separation,
     * and full Unicode (UTF-8, Asian languages, European accents, etc.) without timestamps.
     */
    fun parseTxt(txtText: String): LyricsData {
        if (txtText.isBlank()) return LyricsData()

        return LyricsData(
            type = LyricType.TXT,
            lines = emptyList(),
            rawText = txtText.trim()
        )
    }

    /**
     * Finds the index of the currently playing lyric line for synchronized LRC format.
     */
    fun findActiveIndex(
        lines: List<LyricLine>,
        currentPositionMs: Long,
        syncOffsetMs: Long
    ): Int {
        if (lines.isEmpty()) return -1
        val adjustedPosition = currentPositionMs + syncOffsetMs
        if (adjustedPosition < lines.first().timestampMs) {
            return -1
        }
        return lines.indexOfLast { it.timestampMs <= adjustedPosition }
    }
}
