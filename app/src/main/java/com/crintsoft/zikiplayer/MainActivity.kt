package com.crintsoft.zikiplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.crintsoft.zikiplayer.ui.MainDestination
import com.crintsoft.zikiplayer.ui.MainViewModel
import com.crintsoft.zikiplayer.ui.screens.MediaLibraryScreen
import com.crintsoft.zikiplayer.ui.screens.PreferencesScreen
import com.crintsoft.zikiplayer.ui.screens.SearchLyricsScreen
import com.crintsoft.zikiplayer.ui.screens.SyncedLyricsScreen
import com.crintsoft.zikiplayer.ui.theme.MiniLyricsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            MiniLyricsTheme {
                val destination by viewModel.currentDestination.collectAsState()
                val preferences by viewModel.lyricsPreferences.collectAsState()

                // Keep screen on management
                LaunchedEffect(preferences.keepScreenOn) {
                    if (preferences.keepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                // Audio file picker launcher
                val audioPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { handlePickedAudio(it) }
                }

                // Audio permission launcher for Android 13-16+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.scanDeviceAudio()
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.READ_MEDIA_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                }

                // Handle system back navigation
                if (destination != MainDestination.LIBRARY) {
                    BackHandler {
                        viewModel.navigateTo(MainDestination.LIBRARY)
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (destination) {
                            MainDestination.LIBRARY -> {
                                MediaLibraryScreen(
                                    viewModel = viewModel,
                                    onPickAudioFile = { audioPickerLauncher.launch("audio/*") },
                                    onOpenNowPlaying = { viewModel.navigateTo(MainDestination.NOW_PLAYING_LYRICS) }
                                )
                            }
                            MainDestination.NOW_PLAYING_LYRICS -> {
                                SyncedLyricsScreen(
                                    playbackManager = viewModel.playbackManager,
                                    preferences = preferences,
                                    onBack = { viewModel.navigateTo(MainDestination.LIBRARY) },
                                    onOpenSearch = { viewModel.navigateTo(MainDestination.ONLINE_LYRICS_SEARCH) },
                                    onOpenPreferences = { viewModel.navigateTo(MainDestination.PREFERENCES) }
                                )
                            }
                            MainDestination.ONLINE_LYRICS_SEARCH -> {
                                SearchLyricsScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(MainDestination.NOW_PLAYING_LYRICS) }
                                )
                            }
                            MainDestination.PREFERENCES -> {
                                PreferencesScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(MainDestination.NOW_PLAYING_LYRICS) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                handlePickedAudio(uri)
                viewModel.navigateTo(MainDestination.NOW_PLAYING_LYRICS)
            }
        }
    }

    private fun handlePickedAudio(uri: Uri) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Imported Song"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            retriever.release()

            viewModel.addCustomTrack(
                uri = uri,
                title = title,
                artist = artist,
                durationMs = duration
            )
        } catch (e: Exception) {
            viewModel.addCustomTrack(
                uri = uri,
                title = "Imported Audio",
                artist = "Unknown Artist",
                durationMs = 0L
            )
        }
    }
}
