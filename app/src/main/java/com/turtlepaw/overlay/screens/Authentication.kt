package com.turtlepaw.overlay.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.MediaPlayerSelectionDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.turtlepaw.overlay.HomeAssistantClient
import com.turtlepaw.overlay.LocalHomeAssistantViewModel
import com.turtlepaw.overlay.Page
import com.turtlepaw.overlay.SharedPrefs
import com.turtlepaw.overlay.isValidUrl
import com.turtlepaw.overlay.navigation.MainGraph
import com.turtlepaw.overlay.viewmodels.HomeAssistantViewModel
import kotlinx.coroutines.launch

@Destination<MainGraph>(start = true)
@Composable
fun Authentication(
    navigator: DestinationsNavigator,
) {
    val viewModel = LocalHomeAssistantViewModel.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var serverUrl by remember { mutableStateOf("") }
    var apiToken by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = apiToken,
            onValueChange = {
                apiToken = it
            },
            label = { Text("Enter Home Assistant API Token") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        TextField(
            value = serverUrl,
            onValueChange = {
                serverUrl = it
            },
            label = { Text("https://example.com") },
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
                        viewModel.updateCredentials(
                            serverUrl,
                            apiToken
                        )
                        navigator.navigate(MediaPlayerSelectionDestination(isOnboarding = true))
                        SharedPrefs(context)
                            .setApiToken(apiToken)
                            .setBaseUrl(serverUrl)
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
}