package com.turtlepaw.overlay

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

data class Entity(
    val entityId: String,
    val friendlyName: String,
    val state: String
)

class HomeAssistantClient(
    private val baseUrl: String,
    private val apiToken: String
) {
    private val client = OkHttpClient()

    companion object {
        fun resolveBaseUrl(_baseUrl: String): String {
            val baseUrl = _baseUrl.trim()
            return if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
                baseUrl
            } else {
                "http://$baseUrl"
            }
        }
    }

    suspend fun getMediaPlayers(): List<Entity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${resolveBaseUrl(baseUrl)}/api/states")
            .addHeader("Authorization", "Bearer $apiToken")
            .build()

        runBlocking {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Server returned ${response.code}")

                    val responseBody = response.body
                        ?.string()
                        ?: throw Exception("Empty response from server")

                    val jsonArray = JSONArray(responseBody)

                    val entities = (0 until jsonArray.length())
                        .map { i -> jsonArray.getJSONObject(i) }
                        .filter { obj -> obj.getString("entity_id").startsWith("media_player.") }
                        .map { obj ->
                            val attributes = obj.getJSONObject("attributes")
                            Entity(
                                entityId = obj.getString("entity_id"),
                                friendlyName = attributes.optString(
                                    "friendly_name",
                                    obj.getString("entity_id")
                                ),
                                state = obj.getString("state")
                            )
                        }

                    if (entities.isEmpty()) {
                        throw Exception("No media players found in your Home Assistant instance")
                    }

                    return@use entities
                }
            }

            return@runBlocking response
        }
    }

    suspend fun getTriggerEntities(): List<Entity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${resolveBaseUrl(baseUrl)}/api/states")
            .addHeader("Authorization", "Bearer $apiToken")
            .build()

        runBlocking {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Server returned ${response.code}")

                    val responseBody = response.body
                        ?.string()
                        ?: throw Exception("Empty response from server")

                    val jsonArray = JSONArray(responseBody)

                    val entities = (0 until jsonArray.length())
                        .map { i -> jsonArray.getJSONObject(i) }
                        .filter { obj -> obj.getString("entity_id").startsWith("input_boolean.") }
                        .map { obj ->
                            val attributes = obj.getJSONObject("attributes")
                            Entity(
                                entityId = obj.getString("entity_id"),
                                friendlyName = attributes.optString(
                                    "friendly_name",
                                    obj.getString("entity_id")
                                ),
                                state = obj.getString("state")
                            )
                        }

                    if (entities.isEmpty()) {
                        throw Exception("No input_boolean entities found in your Home Assistant instance")
                    }

                    return@use entities
                }
            }

            return@runBlocking response
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaPlayerSelector(
    entities: List<Entity>,
    onSelected: (Entity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val cardWidth =
            (screenWidth * 0.8f).coerceAtMost(500.dp) // 80% of screen width or max 400.dp

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }

            if (entities.isNotEmpty()) {
                item {
                    Text(
                        text = "Select a Media Player",
                        modifier = Modifier.padding(16.dp),
                        style = androidx.tv.material3.MaterialTheme.typography.titleLarge,
                        color = androidx.tv.material3.MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(entities) { player ->
                Card(
                    modifier = Modifier
                        .width(cardWidth)
                        .padding(vertical = 8.dp),
                    onClick = { onSelected(player) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = player.friendlyName)
                        Text(text = "State: ${player.state}")
                    }
                }
            }
        }
    }
}