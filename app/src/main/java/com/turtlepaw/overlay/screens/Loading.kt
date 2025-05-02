package com.turtlepaw.overlay.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.turtlepaw.overlay.HomeAssistantClient

@Composable
fun Loading(isLoading: Boolean, error: String?, serverUrl: String){
    if (isLoading) {
        Text(
            "Connecting to Home Assistant...",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
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