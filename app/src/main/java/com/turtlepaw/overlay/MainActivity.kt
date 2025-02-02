package com.turtlepaw.overlay

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextField
import androidx.tv.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.skydoves.cloudy.cloudy
import com.turtlepaw.overlay.ui.theme.OverlayTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL

@Suppress("DEPRECATION") // Deprecated for third party Services.
fun <T> Context.isServiceForegrounded(service: Class<T>) =
    (getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
        ?.getRunningServices(Integer.MAX_VALUE)
        ?.find { it.service.className == service.name }
        ?.foreground == true

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE = 1234
    }

    var overlayVisible by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OverlayTheme {
                MainScreen(
                    onShowOverlay = { checkPermissionsAndShowOverlay() },
                    onHideOverlay = { hideOverlay() },
                    overlayVisible,
                    context = this
                )
            }
        }

        checkPermissionsAndShowOverlay()
    }

    private fun checkPermissionsAndShowOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        } else {
            showOverlay()
            overlayVisible = isServiceForegrounded(OverlayService::class.java)
        }
    }

    private fun showOverlay() {
        val intent = Intent(this, OverlayService::class.java)
        startForegroundService(intent)
    }

    private fun hideOverlay() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }
}

// First, let's define our pages as a sealed class
sealed class Page {
    object MediaPlayerSelection : Page()
    object TriggerSelection : Page()
    object Loading : Page()
    object Authentication : Page()
    object UiModeSelection : Page()
    data class Connected(val entity: Entity, val triggerEntity: Entity) : Page()
}


fun isValidUrl(url: String): Boolean {
    return try {
        URL(url) // Attempt to create a URL object
        true     // If successful, the URL is valid
    } catch (e: MalformedURLException) {
        false    // If an exception occurs, the URL is not valid
    }
}

@Composable
fun MainScreen(
    onShowOverlay: () -> Unit,
    onHideOverlay: () -> Unit,
    overlayVisible: Boolean,
    context: Context
) {
    var currentPage by remember { mutableStateOf<Page>(Page.Loading) }
    var selectedEntity by remember { mutableStateOf<Entity?>(null) }
    var selectedTrigger by remember { mutableStateOf<Entity?>(null) }
    val sharedPrefs = SharedPrefs(context)
    var isConnected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var serverUrl by remember { mutableStateOf("") }
    var apiToken by remember { mutableStateOf("") }
    var mediaPlayers by remember { mutableStateOf<List<Entity>>(emptyList()) }
    var triggerEntities by remember { mutableStateOf<List<Entity>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var uiMode by remember { mutableStateOf<UiMode>(UiMode.Transparent) }
    LaunchedEffect(Unit) {
        val sharedPrefs = SharedPrefs(context)
        apiToken = sharedPrefs.getApiToken() ?: ""
        serverUrl = sharedPrefs.getBaseUrl() ?: ""

        // Try to restore previous selections from SharedPrefs
        val savedMediaPlayer = sharedPrefs.getSelectedMediaPlayer()
        val savedTrigger = sharedPrefs.getTriggerEntityId() // You'll need to add this method
        val isValid = apiToken.isNotBlank() && serverUrl.isNotBlank()
        val statesValid = savedMediaPlayer != null && savedTrigger != null

        if (isValid) {
            isAuthenticated = true
            isLoading = true
            currentPage = Page.Loading
            try {
                mediaPlayers = HomeAssistantClient(serverUrl, apiToken).getMediaPlayers()
                triggerEntities = HomeAssistantClient(serverUrl, apiToken).getTriggerEntities()
            } catch (e: Exception) {
                // log the full error
                Log.e("MediaPlayerSelector", "Failed to connect: ${e.message}", e)
                error = "Failed to connect: ${e.message}"
            } finally {
                if (!statesValid) {
                    currentPage = Page.MediaPlayerSelection
                }
                // This prevents it from delaying other tasks
                scope.launch {
                    delay(3000)
                    isLoading = false
                }
            }
        }

        if (isValid && statesValid) {
            try {
                val client =
                    HomeAssistantClient(sharedPrefs.getBaseUrl()!!, sharedPrefs.getApiToken()!!)
                val mediaPlayers = client.getMediaPlayers()
                val triggers = client.getTriggerEntities() // You'll need to implement this method

                selectedEntity = mediaPlayers.find { it.entityId == savedMediaPlayer }
                selectedTrigger = triggers.find { it.entityId == savedTrigger }

                if (selectedEntity != null && selectedTrigger != null) {
                    currentPage = Page.Connected(selectedEntity!!, selectedTrigger!!)
                    isConnected = true
                } else {
                    isConnected = false
                }
            } catch (e: Exception) {
                Log.e("MainScreen", "Failed to connect: ${e.message}", e)
            }
        } else {
            isConnected = false
        }

        if(apiToken.isBlank() || serverUrl.isBlank()){
            currentPage = Page.Authentication
        }
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth =
        (screenWidth * 0.8f).coerceAtMost(500.dp) // 80% of screen width or max 400.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentPage) {
            is Page.UiModeSelection -> {
                InterfacePreview(cardWidth, uiMode, {
                    // Go back to Connected page with the previously selected entities
                    if (selectedEntity != null && selectedTrigger != null) {
                        currentPage = Page.Connected(selectedEntity!!, selectedTrigger!!)
                    }
                }) {
                    uiMode = it
                    sharedPrefs.setUiMode(it)
                }
            }

            is Page.Authentication -> {
                TextField(
                    value = apiToken,
                    onValueChange = { apiToken = it },
                    label = { Text("Enter Home Assistant API Token") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                TextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("https://myhomeassistantserver.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        if(!isValidUrl(serverUrl)){
                            Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            if (apiToken.isNotBlank() && serverUrl.isNotBlank()) {
                                isAuthenticated = true
                                isLoading = true
                                currentPage = Page.Loading
                                SharedPrefs(context)
                                    .setApiToken(apiToken)
                                    .setBaseUrl(serverUrl)

                                try {
                                    mediaPlayers =
                                        HomeAssistantClient(serverUrl, apiToken).getMediaPlayers()
                                } catch (e: Exception) {
                                    Log.e("MediaPlayerSelector", "Failed to connect: ${e}")
                                    error = "Failed to connect: ${e}"
                                    isAuthenticated = false  // Reset authentication on failure
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        "Connect to Home Assistant",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is Page.Loading -> {
                if (isLoading) {
                    Text(
                        "Connecting to Home Assistant...",
                        modifier = Modifier.padding(16.dp),
                        color = androidx.tv.material3.MaterialTheme.colorScheme.onBackground
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Server URL: ${HomeAssistantClient.resolveBaseUrl(serverUrl)}",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            is Page.MediaPlayerSelection -> {
                MediaPlayerSelector(
                    entities = mediaPlayers,
                    onSelected = { mediaPlayer ->
                        selectedEntity = mediaPlayer
                        sharedPrefs.setSelectedMediaPlayer(mediaPlayer)
                        currentPage = Page.TriggerSelection
                    },
                    onBack = {
                        if (isConnected) {
                            currentPage = Page.Connected(selectedEntity!!, selectedTrigger!!)
                        } else {
                            currentPage = Page.Authentication
                        }
                    }
                )
            }

            is Page.TriggerSelection -> {
                TriggerEntitySelector(
                    triggers = triggerEntities,
                    onSelected = { trigger ->
                        selectedTrigger = trigger
                        sharedPrefs.setTriggerEntityId(trigger.entityId) // You'll need to add this method
                        currentPage = Page.Connected(selectedEntity!!, trigger)
                        onShowOverlay()
                    },
                    onBack = {
                        currentPage = Page.MediaPlayerSelection
                    }
                )
            }

            is Page.Connected -> {
                val page = currentPage as Page.Connected
                val lifecycleOwner = LocalLifecycleOwner.current
                val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
                var isRunning by remember { mutableStateOf(false) }

                LaunchedEffect(Unit, state) {
                    isRunning = context.isServiceForegrounded(OverlayService::class.java)
                }

                Text(
                    text = "Connected to Home Assistant",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = serverUrl,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .width(cardWidth)
                        .padding(vertical = 5.dp),
                    onClick = {
                        currentPage = Page.MediaPlayerSelection
                    },
                ) {
                    Text(
                        text = "Media Player: ${page.entity.friendlyName}",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .width(cardWidth)
                        .padding(vertical = 5.dp),
                    onClick = {
                        currentPage = Page.TriggerSelection
                    }
                ) {
                    Text(
                        text = "Trigger: ${page.triggerEntity.friendlyName}",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .width(cardWidth)
                        .padding(vertical = 5.dp),
                    onClick = {
                        currentPage = Page.UiModeSelection
                    }
                ) {
                    Text(
                        text = "UI Mode: ${uiMode.name}",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val coroutineScope = rememberCoroutineScope()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        20.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onHideOverlay()
                            selectedEntity = null
                            selectedTrigger = null
                            sharedPrefs
                                .setSelectedMediaPlayer(Entity("", "", ""))
                                .setTriggerEntityId("")
                                .setUiMode(UiMode.Transparent)
                                .setBaseUrl("")
                                .setApiToken("")
                            serverUrl = ""
                            apiToken = ""
                            uiMode = UiMode.Transparent
                            error = null
                            isLoading = false
                            isAuthenticated = false
                            currentPage = Page.Authentication
                        }
                    ) {
                        Text("Clear Data")
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onHideOverlay()
                                delay(1000)
                                isRunning =
                                    context.isServiceForegrounded(OverlayService::class.java)
                                delay(2000)
                                onShowOverlay()
                                delay(1000)
                                isRunning =
                                    context.isServiceForegrounded(OverlayService::class.java)
                            }
                        }
                    ) {
                        Text("Restart Service")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Background service is ${if (isRunning) "running" else "not running"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            )
                            .size(10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerEntitySelector(
    triggers: List<Entity>,
    onSelected: (Entity) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
            if (triggers.isNotEmpty()) {
                item {
                    Text(
                        text = "Select a Trigger",
                        modifier = Modifier.padding(top = 16.dp),
                        style = androidx.tv.material3.MaterialTheme.typography.titleLarge,
                        color = androidx.tv.material3.MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    Text(
                        text = "This will control when the media dock is visible",
                        modifier = Modifier.padding(bottom = 16.dp, top = 8.dp),
                        style = androidx.tv.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.tv.material3.MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(triggers) { player ->
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

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}