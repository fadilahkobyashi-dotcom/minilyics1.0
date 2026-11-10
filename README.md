# MiniLyrics for Android (Android 16 Compatibility & Navigation/Playback Repair)

**Porting, Bug Fix & Enhancement Release targeting Android 16 (API 36)** preserving 100% of the original MiniLyrics functionality, interface structure, visual identity, and internal mechanisms, with a horizontally expanded layout for modern smartphone aspect ratios, bulletproof Next/Previous track transitions, and comprehensive **LRC + TXT lyrics support**.

---

## 📥 Download APK (Unduh APK)

The compiled, verified APK is available directly in the root directory for download:

- **[MiniLyrics.apk](./MiniLyrics.apk)** *(18 MB — Android 16 Ready)*
- Alternatif: **[minilyrics.apk](./minilyrics.apk)** atau jalur build di `app/build/outputs/apk/debug/app-debug.apk`

### Installation Instructions
1. Download `MiniLyrics.apk` to your Android device.
2. Enable "Install Unknown Apps" in device Settings > Security/Privacy if prompted.
3. Open `MiniLyrics.apk` and tap **Install**. The app launches with preloaded demo tracks and synced lyrics ready to play immediately.

---

## 🛠️ Critical Bug Fixes in this Release

### 1. Navigation Fix: Artists, Albums, Songs, and Playlists Mapping (BUG 1 FIXED)
- **Root Cause**: The `LibraryTab` enum in `MainViewModel` had `SONGS` at ordinal 0, whereas the tab row in `MediaLibraryScreen` listed `ARTISTS` first at index 0 and `ALBUMS` at index 1. Because the tab component used `selectedTabIndex = currentTab.ordinal`, selecting Artists set ordinal 1 (highlighting Albums), and selecting Albums set ordinal 2 (highlighting Songs).
- **Solution**:
  - Aligned `LibraryTab` enum to strictly follow the canonical UI order:
    `ARTISTS` (0), `ALBUMS` (1), `SONGS` (2), `PLAYLISTS` (3).
  - Explicitly resolved tab selection index via `tabs.indexOfFirst { it.first == currentTab }`.
  - Selecting **Artists** displays the Artists library and highlights the Artists tab.
  - Selecting **Albums** displays the Albums library and highlights the Albums tab.
  - Selecting **Songs** displays the Songs library and highlights the Songs tab.
  - Selecting **Playlists** displays the Playlists library and highlights the Playlists tab.

### 2. Next Track Stuck Issue across Library Sections (BUG 2 FIXED)
- **Root Cause**:
  1. *Single-Song Sub-Queues*: Demo catalog only contained 1 track per artist and 1 track per album. Playing from Artists or Albums created a 1-song queue. Pressing Next calculated `(0 + 1) % 1 = 0`, causing the player to restart the same song and become permanently stuck.
  2. *Queue Desynchronization in Songs*: Initiating playback from Songs or filtered search lists previously did not preserve the full library queue context.
  3. *Missing Fallback on Boundary*: When a 1-song queue was active, pressing Next Track had no fallback mechanism to advance into the wider music library.
- **Solution**:
  - **Expanded Multi-Song Catalog**: Every artist and album now features multiple playable tracks with both synchronized LRC and formatted TXT lyrics.
  - **Artist & Album Track Views**: Added expandable track lists inside each Artist and Album card. Tapping an artist or album allows playing all songs belonging to that collection or jumping directly to a specific track.
  - **Context-Aware Playback Queues**:
    - *Songs section*: Populates queue with all songs in library ordering (`playTrack(track, filteredTracks)`).
    - *Artists section*: Populates queue with all songs of the selected artist (`playArtist(artistTracks, startIndex)`).
    - *Albums section*: Populates queue with all songs of the selected album (`playAlbum(albumTracks, startIndex)`).
    - *Playlists section*: Continues utilizing the playlist track queue (`setQueue(plTracks, startIndex)`).
  - **Smart Next Track Fallback**: If a queue contains only 1 track, `skipNext()` checks `allTracksProvider` and advances to the next song in the general music library instead of freezing.

---

## 🚀 Additional Features Preserved

### Dual LRC and TXT Lyrics Support
- **LRC Synchronized**: Real-time karaoke line highlighting, interactive tap-to-seek, and `[-0.5s]` / `[+0.5s]` offset calibration.
- **TXT Plain Lyrics**: Multiline formatted text preserving line breaks, paragraph spacing, and full Unicode (Japanese, Chinese, Korean, Indonesian).
- **Priority System**: LRC synchronized lyrics take precedence over TXT; TXT serves as graceful fallback.
- **Format Badges**: `[LRC]` (Green) and `[TXT]` (Gold) badges displayed across song lists, headers, and MiniPlayer.

### Android 16 (API 36) Compatibility
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`.
- Proper runtime permission handling (`READ_MEDIA_AUDIO`).
- Edge-to-edge drawing with safe insets.
