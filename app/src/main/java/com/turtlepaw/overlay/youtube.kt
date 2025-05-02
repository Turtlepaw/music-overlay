package com.turtlepaw.overlay

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.Random

// Data classes for parsing JSON (adjust based on your API response structure)
data class PlaylistResponse(val items: List<PlaylistItem>)
data class PlaylistItem(val id: String, val snippet: Snippet)
data class Snippet(val title: String)

val PLAYLIST_ID = ""
val API_KEY = ""

fun fetchRandomVideoId(playlistId: String, apiKey: String): String? {
    val client = OkHttpClient()

    // URL to get the playlist items (videos) from YouTube, with your API key
    val url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=$playlistId&maxResults=50&key=$apiKey"

    // Build the request with the API key
    val request = Request.Builder()
        .url(url)
        .build()

    // Execute the request
    val response: Response = client.newCall(request).execute()

    if (!response.isSuccessful) {
        println("Request failed: ${response.message}")
        return null
    }

    // Parse the response
    val jsonResponse = response.body?.string()
    val jsonObject = JSONObject(jsonResponse)
    val items = jsonObject.getJSONArray("items")

    // If there are no items in the playlist, return null
    if (items.length() == 0) {
        println("No videos found in this playlist")
        return null
    }

    // Pick a random video ID from the playlist
    val randomIndex = kotlin.random.Random.nextInt(items.length())
    val randomItem = items.getJSONObject(randomIndex)
    val videoId = randomItem.getJSONObject("snippet").getJSONObject("resourceId").getString("videoId")

    // Return the random video ID
    return videoId
}