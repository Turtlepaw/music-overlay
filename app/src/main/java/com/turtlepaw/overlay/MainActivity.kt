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
import androidx.activity.viewModels
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextField
import androidx.tv.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.HomeDestination
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.skydoves.cloudy.cloudy
import com.turtlepaw.nearby_settings.tv_core.AppDetails
import com.turtlepaw.nearby_settings.tv_core.NearbySettingsDiscoveryDialog
import com.turtlepaw.nearby_settings.tv_core.NearbySettingsHost
import com.turtlepaw.nearby_settings.tv_core.SettingsSchema
import com.turtlepaw.nearby_settings.tv_core.getTypedValue
import com.turtlepaw.nearby_settings.tv_core.rememberNearbyPermissions
import com.turtlepaw.overlay.navigation.MainGraph
import com.turtlepaw.overlay.overlay.startOverlayService
import com.turtlepaw.overlay.ui.theme.OverlayTheme
import com.turtlepaw.overlay.viewmodels.HomeAssistantViewModel
import com.turtlepaw.overlay.viewmodels.HomeAssistantViewModelFactory
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
            var schema by remember { mutableStateOf(defaultSchema) }
            var showDiscoveryDialog by remember { mutableStateOf(false) }

            val settingsHost = remember {
                NearbySettingsHost(
                    settingsSchema = defaultSchema,
                    onSettingsChanged = { newSettings ->
                        Log.d("SettingsHost", "New settings: $newSettings")
                        schema = newSettings
                    },
                    context = this,
                    enablePersistence = true,
                    automaticallyStart = true,
                    appDetails = AppDetails(
                        label = "Music Overlay",
                        developer = "Beaverfy",
                        contact = "https://discord.com/invite/4CUkgTEmnr",
                        website = "https://github.com/Turtlepaw/Overlay"
                    )
                )
            }

            val isAdvertising by settingsHost.isAdvertising
            var isAdvertisingLoading by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            DisposableEffect(isAdvertising) {
                Log.d("MainActivity", "isAdvertising: $isAdvertising")
                if (isAdvertising) {
                    isAdvertisingLoading = false
                }
                onDispose { }
            }

            OverlayTheme {
                settingsHost.AuthScreen()

                if (showDiscoveryDialog) {
                    NearbySettingsDiscoveryDialog {
                        showDiscoveryDialog = false
                    }
                }

                val permissions = rememberNearbyPermissions(launchPermissionsOnStart = true)

                val homeAssistantViewModel by viewModels<HomeAssistantViewModel> {
                    HomeAssistantViewModelFactory(SharedPrefs(applicationContext))
                }

                CompositionLocalProvider(
                    LocalHomeAssistantViewModel provides homeAssistantViewModel
                ) {
                    val navHostEngine = rememberNavHostEngine()
                    val navController = navHostEngine.rememberNavController()
                    DestinationsNavHost(
                        navGraph = NavGraphs.preferredRoute,
                        navController = navController,
                        engine = navHostEngine,
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                    )

                    LaunchedEffect(Unit) {
                        if(homeAssistantViewModel.serverUrl != "" && homeAssistantViewModel.apiToken != ""){
                            navController.navigate(HomeDestination.route)
                        }
                    }
                }
            }
        }

        startOverlayService()
    }
}

val LocalHomeAssistantViewModel = staticCompositionLocalOf<HomeAssistantViewModel> {
    error("HomeAssistantViewModel not provided")
}

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