package com.crintsoft.zikiplayer.lyrics

import com.crintsoft.zikiplayer.model.Track

object DefaultLyrics {

    val TRACK_STARLIGHT_LRC = """
        [ti:Starlight Reverie]
        [ar:Luna Eclipse]
        [al:Celestial Echoes]
        [by:MiniLyrics Studio]
        [00:00.00]♪ Floating through the cosmic sea ♪
        [00:03.50]Whispers in the quiet night
        [00:07.20]Stars align to guide the way
        [00:11.00]Shadows fading into light
        [00:15.30]Every heartbeat is a rhythm in time
        [00:19.40]Dancing along the electric skyline
        [00:23.50]Echoes of dreams we used to know
        [00:27.20]Where the gentle starlight glows
        [00:30.80]♪ Music carries us away... ♪
    """.trimIndent()

    val TRACK_COSMIC_LULLABY_LRC = """
        [ti:Cosmic Lullaby]
        [ar:Luna Eclipse]
        [al:Celestial Echoes]
        [by:MiniLyrics Studio]
        [00:00.00]♪ Drifting into gentle slumber ♪
        [00:04.00]Comets dancing across the sky
        [00:08.50]Silver clouds softly wandering by
        [00:13.00]Close your eyes and breathe the peace
        [00:17.50]All your earthly worries cease
        [00:22.00]Harmonies from distant spheres
        [00:26.50]Washing away all our fears
        [00:30.00]♪ Sleep beneath the starry dome... ♪
    """.trimIndent()

    val TRACK_SOLAR_FLARE_LRC = """
        [ti:Solar Flare]
        [ar:Luna Eclipse]
        [al:Nocturne Dreams]
        [by:MiniLyrics Studio]
        [00:00.00]♪ Blazing through the stratosphere ♪
        [00:03.80]Golden light burning bright and clear
        [00:07.50]Fires of dawn begin to rise
        [00:11.20]Painting colors across the skies
        [00:15.50]Feel the warmth upon your face
        [00:19.80]Accelerating through time and space
        [00:24.00]Nothing can hold our spirits down
        [00:28.00]As we soar above the ground
        [00:31.00]♪ Reaching for the blazing sun ♪
    """.trimIndent()

    val TRACK_CYBERPULSE_LRC = """
        [ti:Cybernetic Pulse]
        [ar:Neon Skyline]
        [al:Retro Future]
        [by:MiniLyrics Studio]
        [00:00.00]♪ Synthwave bassline kicking in ♪
        [00:03.00]Digital frequencies rising high
        [00:06.50]Neon glow beneath the purple sky
        [00:10.20]Feel the speed through the fiber wire
        [00:14.00]City lights reflect our golden fire
        [00:18.00]Synchronized in perfect time
        [00:21.80]MiniLyrics scrolling line by line
        [00:25.50]Can you feel the pulse tonight?
        [00:29.00]♪ Electric dreams until sunrise ♪
    """.trimIndent()

    val TRACK_NEON_DRIVE_LRC = """
        [ti:Midnight Drive]
        [ar:Neon Skyline]
        [al:Retro Future]
        [by:MiniLyrics Studio]
        [00:00.00]♪ Cruising down the highway lanes ♪
        [00:04.00]Reflections in the evening rain
        [00:08.00]Analog synthesizers in the air
        [00:12.00]Wind running through our hair
        [00:16.50]Midnight shadows racing past
        [00:20.50]Moments that were made to last
        [00:25.00]Speeding toward the neon dawn
        [00:29.00]♪ Driving till the darkness is gone ♪
    """.trimIndent()

    val TRACK_TXT_SAMPLE = """
        Midnight Horizon (TXT Plain Lyrics)
        Artist: Echoes of Aurora
        Album: Acoustic Serenade
        
        [Stanza 1]
        Beneath the velvet midnight dome
        The wanderer finds a place called home
        Silent breeze across the sea
        Unlocking every memory
        
        [Refrain / 繰り返し / Reff]
        Di bawah langit malam berbintang
        Harapan baru kan menjelang
        夜空に輝く星のように
        光り続ける、永遠に
        
        [Stanza 2]
        Pages turned through chapters old
        Stories that remain untold
        MiniLyrics plays the song
        Where timeless melodies belong.
    """.trimIndent()

    val TRACK_WHISPERING_WIND_TXT = """
        Whispering Wind (TXT Plain Lyrics)
        Artist: Echoes of Aurora
        Album: Acoustic Serenade
        
        [Verse 1]
        Autumn leaves fall on the ground
        Without making any sound
        Golden amber everywhere
        Cooling breezes in the air
        
        [Chorus]
        Listen to the whispering wind
        A brand new chapter will begin
        Kemanapun langkahmu pergi
        Damai kan selalu di hati
        风声低语诉说着过往
        心中的光芒永不迷茫
        
        [Outro]
        Rest your head and hear the song
        MiniLyrics all night long.
    """.trimIndent()

    fun getDefaultTracks(): List<Track> {
        return listOf(
            Track(
                id = "demo_starlight",
                title = "Starlight Reverie",
                artist = "Luna Eclipse",
                album = "Celestial Echoes",
                durationMs = 30000L,
                lrcContent = TRACK_STARLIGHT_LRC,
                txtContent = null,
                isDemo = true
            ),
            Track(
                id = "demo_cosmic_lullaby",
                title = "Cosmic Lullaby",
                artist = "Luna Eclipse",
                album = "Celestial Echoes",
                durationMs = 30000L,
                lrcContent = TRACK_COSMIC_LULLABY_LRC,
                txtContent = null,
                isDemo = true
            ),
            Track(
                id = "demo_solar_flare",
                title = "Solar Flare",
                artist = "Luna Eclipse",
                album = "Nocturne Dreams",
                durationMs = 31000L,
                lrcContent = TRACK_SOLAR_FLARE_LRC,
                txtContent = null,
                isDemo = true
            ),
            Track(
                id = "demo_cyberpulse",
                title = "Cybernetic Pulse",
                artist = "Neon Skyline",
                album = "Retro Future",
                durationMs = 32000L,
                lrcContent = TRACK_CYBERPULSE_LRC,
                txtContent = null,
                isDemo = true
            ),
            Track(
                id = "demo_neon_drive",
                title = "Midnight Drive",
                artist = "Neon Skyline",
                album = "Retro Future",
                durationMs = 32000L,
                lrcContent = TRACK_NEON_DRIVE_LRC,
                txtContent = null,
                isDemo = true
            ),
            Track(
                id = "demo_txt_horizon",
                title = "Midnight Horizon",
                artist = "Echoes of Aurora",
                album = "Acoustic Serenade",
                durationMs = 30000L,
                lrcContent = null,
                txtContent = TRACK_TXT_SAMPLE,
                isDemo = true
            ),
            Track(
                id = "demo_whispering_wind",
                title = "Whispering Wind",
                artist = "Echoes of Aurora",
                album = "Acoustic Serenade",
                durationMs = 30000L,
                lrcContent = null,
                txtContent = TRACK_WHISPERING_WIND_TXT,
                isDemo = true
            )
        )
    }
}
