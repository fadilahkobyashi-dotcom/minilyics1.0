package com.crintsoft.zikiplayer.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioUri: String? = null,
    val rawResId: Int? = null,
    val lrcContent: String? = null,
    val txtContent: String? = null,
    val isDemo: Boolean = false
) {
    val hasLrc: Boolean get() = !lrcContent.isNullOrBlank()
    val hasTxt: Boolean get() = !txtContent.isNullOrBlank()
    val hasLyrics: Boolean get() = hasLrc || hasTxt

    /**
     * Priority: LRC has priority as it contains timestamp data for synchronization.
     * TXT is used as fallback when no valid matching LRC is available.
     */
    fun getEffectiveLyrics(): Pair<LyricType, String>? {
        if (hasLrc) return Pair(LyricType.LRC, lrcContent!!)
        if (hasTxt) return Pair(LyricType.TXT, txtContent!!)
        return null
    }
}

data class Playlist(
    val id: String,
    val name: String,
    val trackIds: List<String> = emptyList()
)
