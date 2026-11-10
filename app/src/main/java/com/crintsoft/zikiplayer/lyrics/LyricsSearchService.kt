package com.crintsoft.zikiplayer.lyrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class LyricsSearchResult(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Int,
    val syncedLyrics: String?,
    val plainLyrics: String?
) {
    val isSynced: Boolean get() = !syncedLyrics.isNullOrBlank()
}

object LyricsSearchService {
    private const val BASE_URL = "https://lrclib.net/api"

    suspend fun searchLyrics(query: String): Result<List<LyricsSearchResult>> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("$BASE_URL/search?q=$encodedQuery")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "MiniLyrics-Android/1.0")
            }

            if (connection.responseCode != 200) {
                return@withContext Result.failure(Exception("Server returned code ${connection.responseCode}"))
            }

            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val jsonArray = JSONArray(response)
            val results = mutableListOf<LyricsSearchResult>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optLong("id")
                val trackName = item.optString("trackName", item.optString("name", "Unknown Track"))
                val artistName = item.optString("artistName", "Unknown Artist")
                val albumName = item.optString("albumName", "")
                val duration = item.optInt("duration", 0)
                val syncedLyrics = if (item.isNull("syncedLyrics")) null else item.optString("syncedLyrics")
                val plainLyrics = if (item.isNull("plainLyrics")) null else item.optString("plainLyrics")

                results.add(
                    LyricsSearchResult(
                        id = id,
                        trackName = trackName,
                        artistName = artistName,
                        albumName = albumName,
                        durationSeconds = duration,
                        syncedLyrics = syncedLyrics,
                        plainLyrics = plainLyrics
                    )
                )
            }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExactLyrics(trackName: String, artistName: String): Result<LyricsSearchResult?> = withContext(Dispatchers.IO) {
        try {
            val encTrack = URLEncoder.encode(trackName, "UTF-8")
            val encArtist = URLEncoder.encode(artistName, "UTF-8")
            val url = URL("$BASE_URL/get?track_name=$encTrack&artist_name=$encArtist")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "MiniLyrics-Android/1.0")
            }

            if (connection.responseCode == 404) {
                return@withContext Result.success(null)
            }
            if (connection.responseCode != 200) {
                return@withContext Result.failure(Exception("HTTP error ${connection.responseCode}"))
            }

            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val item = JSONObject(response)
            val id = item.optLong("id")
            val tName = item.optString("trackName", item.optString("name", trackName))
            val aName = item.optString("artistName", artistName)
            val alName = item.optString("albumName", "")
            val duration = item.optInt("duration", 0)
            val syncedLyrics = if (item.isNull("syncedLyrics")) null else item.optString("syncedLyrics")
            val plainLyrics = if (item.isNull("plainLyrics")) null else item.optString("plainLyrics")

            Result.success(
                LyricsSearchResult(
                    id = id,
                    trackName = tName,
                    artistName = aName,
                    albumName = alName,
                    durationSeconds = duration,
                    syncedLyrics = syncedLyrics,
                    plainLyrics = plainLyrics
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
