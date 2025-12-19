package com.turtlepaw.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import com.turtlepaw.overlay.overlays.OverlayDock
import com.turtlepaw.overlay.overlays.OverlayTransparent
import okhttp3.Response
import androidx.core.net.toUri
import androidx.tv.material3.MaterialTheme
import com.turtlepaw.nearby_settings.tv_core.SettingsManager
import com.turtlepaw.nearby_settings.tv_core.getTypedValue
import com.turtlepaw.overlay.ui.theme.OverlayTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class MediaInfo(
    val title: String?,
    val artist: String?,
    val albumArtUri: String?,
)

class OverlayService : Service() {

    private var overlayView: FrameLayout? = null
    private val CHANNEL_ID = "OverlayServiceChannel"
    private lateinit var serviceLifecycleOwner: ServiceLifecycleOwner
    private var overlayMessage = mutableStateOf<MediaInfo?>(null)
    private var entityId: String? = null
    private var triggerId: String? = null

    private inner class ServiceLifecycleOwner : LifecycleOwner, ViewModelStoreOwner,
        SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val _viewModelStore = ViewModelStore()
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle = lifecycleRegistry
        override val viewModelStore: ViewModelStore
            get() = _viewModelStore
        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun init() {
            savedStateRegistryController.performRestore(Bundle())
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        entityId = SharedPrefs(this).getSelectedMediaPlayer()
        triggerId = SharedPrefs(this).getTriggerEntityId()
        val play = intent?.getBooleanExtra("play", false) ?: false
        if (play) {
            CoroutineScope(Dispatchers.Default).launch {
                launchYoutube()
            }
            Handler(Looper.getMainLooper()).post {
                showOverlay()
            }
        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the lifecycle owner first
        serviceLifecycleOwner = ServiceLifecycleOwner()
        serviceLifecycleOwner.init()
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

        createNotificationChannel()

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Overlay Service")
            .setContentText("Overlay is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
        startWebSocketServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        removeOverlay()
    }

    private fun startWebSocketServer() {
        val url = SharedPrefs(this).getBaseUrl()?.replace("https://", "")?.replace("http://", "")?.trim()
        val webSocketClient = OkHttpClient().newWebSocket(
            Request.Builder()
                .url("wss://$url/api/websocket")
                .build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("WebSocket", "Connection opened")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val message = JSONObject(text)

                    when (message.getString("type")) {
                        "auth_required" -> {
                            val auth = SharedPrefs(this@OverlayService).getApiToken()
                            // Send the auth message
                            webSocket.send(
                                JSONObject(
                                    mapOf(
                                        "type" to "auth",
                                        "access_token" to auth
                                    )
                                ).toString()
                            )
                            Log.d("WebSocket", "Sent auth message")
                        }

                        "auth_ok" -> {
                            Log.d("WebSocket", "Authentication succeeded")

                            // Send the subscription message
                            val subscribeMessage = JSONObject(
                                mapOf(
                                    "id" to 18,
                                    "type" to "subscribe_events",
                                    "event_type" to "state_changed"
                                )
                            )
                            webSocket.send(subscribeMessage.toString())
                            Log.d("WebSocket", "Sent subscription message")
                        }

                        "auth_invalid" -> {
                            Log.e(
                                "WebSocket",
                                "Authentication failed: ${message.getString("message")}"
                            )
                        }

                        "event" -> {
                            try {
                                val event = message.getJSONObject("event")
                                val newState =
                                    event.getJSONObject("data")
                                        .getJSONObject("new_state")
                                if (triggerId != null && newState.getString("entity_id") == triggerId) {
                                    Log.d("WebSocket", "Received event: $newState - $event")
                                    val state = newState.getString("state") == "on"
                                    if (state) {
                                        launchYoutube()
                                        Handler(Looper.getMainLooper()).post {
                                            showOverlay()
                                        }
                                    } else {
                                        Log.d("WebSocket", "Removing overlay")
                                            removeOverlay()
                                    }
                                } else if (entityId != null && newState.getString("entity_id") == entityId) {
                                    val attributes = newState.getJSONObject("attributes")

                                    val mediaTitle =
                                        attributes.optString("media_title", "Unknown Title")
                                    val mediaArtist =
                                        attributes.optString("media_artist", "Unknown Artist")
                                    val mediaAlbumName =
                                        attributes.optString(
                                            "media_album_name",
                                            "Unknown Album"
                                        )
                                    val albumCoverUrl =
                                        attributes.optString("entity_picture", null)

                                    println("Now Playing:")
                                    println("Title: $mediaTitle")
                                    println("Artist: $mediaArtist")
                                    println("Album: $mediaAlbumName")
                                    println("Album Cover URL: $albumCoverUrl")
                                    overlayMessage.value = MediaInfo(
                                        title = mediaTitle,
                                        artist = mediaArtist,
                                        albumArtUri = if (albumCoverUrl == null) null else "https://$url$albumCoverUrl"
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    Log.e("WebSocket", "Connection failed", t)
                }
            }
        )
    }

    private fun launchYoutube() {
        val settings = SettingsManager(this).loadSettings(defaultSchema, true)
        if (settings == null) return
        val playlistId = settings.getTypedValue<String>("youtube_playlist_id")
        val apiKey = settings.getTypedValue<String>("youtube_api_key")
        if(playlistId == null || apiKey == null) return Toast.makeText(
            this,
            "Youtube not configured",
            Toast.LENGTH_SHORT
        ).show()

        val playlist = fetchRandomVideoId(
            playlistId,
            apiKey
        )

// Check if the video ID is valid
        if (playlist != null && playlist.isNotEmpty()) {
            Log.d("Playlist", "Video ID: $playlist")

            val videoUri = "vnd.youtube://video/$playlist".toUri()
            val intent = Intent(Intent.ACTION_VIEW, videoUri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            // Check if there is an activity to handle this intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Log.e("Playlist", "No activity found to handle the intent")
            }
        } else {
            Log.e("Playlist", "Invalid or empty video ID")
        }

    }

    private fun showOverlay() {
        if (overlayView != null) {
            Log.d("OverlayService", "Overlay already exists, skipping")
            return
        }

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = FrameLayout(this).apply {
            // Set up all three tree owners
            setViewTreeLifecycleOwner(serviceLifecycleOwner)
            setViewTreeViewModelStoreOwner(serviceLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(serviceLifecycleOwner)

            val layoutFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_TOAST
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            val composeView = ComposeView(context).apply {
                // Also set the owners for the ComposeView itself
                setViewTreeLifecycleOwner(serviceLifecycleOwner)
                setViewTreeViewModelStoreOwner(serviceLifecycleOwner)
                setViewTreeSavedStateRegistryOwner(serviceLifecycleOwner)

                setContent {
                        OverlayComposable(
                            overlayMessage.value, this@OverlayService
                        )
                }
            }

            addView(
                composeView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER
                })

            windowManager.addView(this, params)
        }
    }

    private fun removeOverlay() {
        Handler(Looper.getMainLooper()).post {
            try {
                overlayView?.let { view ->
                    val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                    if (view.parent != null) {
                        windowManager.removeView(view)
                        Log.d("OverlayService", "Overlay removed successfully")
                    } else {
                        Log.d("OverlayService", "Overlay view has no parent")
                    }
                } ?: Log.d("OverlayService", "Overlay view is null")
                overlayView = null
            } catch (e: Exception) {
                Log.e("OverlayService", "Error removing overlay", e)
                overlayView = null
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}

@Composable
fun OverlayComposable(data: MediaInfo?, context: Context) {
    var preferredLayout by remember { mutableStateOf<UiMode?>(null) }

    LaunchedEffect(Unit) {
        preferredLayout = SharedPrefs(context).getUiMode()
    }

    if (preferredLayout == UiMode.Transparent) {
        OverlayTransparent(data)
    } else if (preferredLayout == UiMode.Dock) {
        OverlayDock(data)
    }
}
