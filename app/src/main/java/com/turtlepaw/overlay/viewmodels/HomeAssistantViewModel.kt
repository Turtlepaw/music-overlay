package com.turtlepaw.overlay.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.turtlepaw.overlay.Entity
import com.turtlepaw.overlay.HomeAssistantClient
import com.turtlepaw.overlay.SharedPrefs
import kotlinx.coroutines.launch

class HomeAssistantViewModel(
    private val sharedPrefs: SharedPrefs
) : ViewModel() {
    var serverUrl by mutableStateOf(sharedPrefs.getBaseUrl() ?: "")
        private set

    var apiToken by mutableStateOf(sharedPrefs.getApiToken() ?: "")
        private set

    var mediaPlayers by mutableStateOf<List<Entity>>(emptyList())
        private set

    var triggerEntities by mutableStateOf<List<Entity>>(emptyList())
        private set

    var selectedMediaPlayer by mutableStateOf<Entity?>(null)
        private set

    var selectedTrigger by mutableStateOf<Entity?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val client: HomeAssistantClient?
        get() = if (serverUrl.isNotBlank() && apiToken.isNotBlank()) {
            HomeAssistantClient(serverUrl, apiToken)
        } else null

    init {
        if (serverUrl.isNotBlank() && apiToken.isNotBlank()) {
            loadEntities()
        }
    }

    fun updateCredentials(newServerUrl: String, newApiToken: String) {
        serverUrl = newServerUrl
        apiToken = newApiToken
        sharedPrefs
            .setBaseUrl(newServerUrl)
            .setApiToken(newApiToken)
        loadEntities()
    }

    fun updateSelectedMediaPlayer(id: String) {
        selectedMediaPlayer = mediaPlayers.find { it.entityId == id }
        sharedPrefs.setSelectedMediaPlayer(id)
    }

    fun updateSelectedTrigger(id: String) {
        selectedTrigger = triggerEntities.find { it.entityId == id }
        sharedPrefs.setTriggerEntityId(id)
    }

    private fun loadEntities() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                client?.let { client ->
                    mediaPlayers = client.getMediaPlayers()
                    triggerEntities = client.getTriggerEntities()
                    selectedMediaPlayer = mediaPlayers.find { it.entityId == sharedPrefs.getSelectedMediaPlayer() }
                    selectedTrigger = triggerEntities.find { it.entityId == sharedPrefs.getTriggerEntityId() }
                }
            } catch (e: Exception) {
                error = "Failed to load entities: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearData() {
        serverUrl = ""
        apiToken = ""
        mediaPlayers = emptyList()
        triggerEntities = emptyList()
        error = null
        sharedPrefs
            .setBaseUrl("")
            .setApiToken("")
            .setSelectedMediaPlayer("")
            .setTriggerEntityId("")
    }
}

class HomeAssistantViewModelFactory(
    private val sharedPrefs: SharedPrefs
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeAssistantViewModel::class.java)) {
            return HomeAssistantViewModel(sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
