package com.turtlepaw.overlay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.AuthenticationDestination
import com.ramcosta.composedestinations.generated.destinations.MediaPlayerSelectionDestination
import com.ramcosta.composedestinations.generated.destinations.TriggerSelectionDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.turtlepaw.overlay.Entity
import com.turtlepaw.overlay.LocalHomeAssistantViewModel
import com.turtlepaw.overlay.OverlayService
import com.turtlepaw.overlay.Page
import com.turtlepaw.overlay.SharedPrefs
import com.turtlepaw.overlay.UiMode
import com.turtlepaw.overlay.isServiceForegrounded
import com.turtlepaw.overlay.navigation.MainGraph
import com.turtlepaw.overlay.overlay.hideOverlay
import com.turtlepaw.overlay.overlay.showOverlay
import com.turtlepaw.overlay.overlay.startOverlayService
import com.turtlepaw.overlay.viewmodels.HomeAssistantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Destination<MainGraph>
@Composable
fun Home(
    navigator: DestinationsNavigator,
){
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    var isRunning by remember { mutableStateOf(false) }
    val viewModel = LocalHomeAssistantViewModel.current
    val context = LocalContext.current
    val cardWidth = getCardWidth()

    LaunchedEffect(Unit, state) {
        isRunning = context.isServiceForegrounded(OverlayService::class.java)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connected to Home Assistant",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = viewModel.serverUrl,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .width(cardWidth)
                .padding(vertical = 5.dp),
            onClick = {
                navigator.navigate(MediaPlayerSelectionDestination())
            },
        ) {
            Text(
                text = "Media Player${if(viewModel.selectedMediaPlayer != null) ": ${viewModel.selectedMediaPlayer!!.friendlyName}" else "" }",
                modifier = Modifier.padding(16.dp)
            )
        }

        Card(
            modifier = Modifier
                .width(cardWidth)
                .padding(vertical = 5.dp),
            onClick = {
                navigator.navigate(TriggerSelectionDestination())
            }
        ) {
            Text(
                text = "Trigger${if(viewModel.selectedTrigger != null) ": ${viewModel.selectedTrigger!!.friendlyName}" else "" }",
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
                    context.hideOverlay()
                    viewModel.clearData()
                    navigator.navigate(AuthenticationDestination)
                }
            ) {
                Text("Clear Data")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        context.hideOverlay()
                        delay(1000)
                        isRunning =
                            context.isServiceForegrounded(OverlayService::class.java)
                        delay(2000)
                        context.startOverlayService()
                        delay(1000)
                        isRunning =
                            context.isServiceForegrounded(OverlayService::class.java)
                    }
                }
            ) {
                Text("Restart Service")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        context.showOverlay()
                        isRunning =
                            context.isServiceForegrounded(OverlayService::class.java)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play Arrow"
                )
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

@Composable
fun getCardWidth(): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    return (screenWidth * 0.8f).coerceAtMost(500.dp) // 80% of screen width or max 400.dp
}