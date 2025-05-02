package com.turtlepaw.overlay

import android.content.Context
import androidx.core.content.edit

enum class UiMode {
    Dock,
    Transparent
}

class SharedPrefs(val context: Context) {
    companion object {
        private const val PREFS_NAME = "prefs"
        private const val API_TOKEN_KEY = "apiToken"
        private const val BASE_URL_KEY = "baseUrl"
        private const val SELECTED_MEDIA_PLAYER_KEY = "selectedMediaPlayer"
        private const val TRIGGER_ENTITY_ID_KEY = "triggerEntityId"
        private const val UI_MODE_KEY = "uiMode"
    }

    private fun setPref(prefName: String, value: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(prefName, value)
        }
    }

    private fun getPref(prefName: String): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(prefName, null)
    }

    fun setApiToken(apiToken: String): SharedPrefs {
        setPref(API_TOKEN_KEY, apiToken)
        return this
    }

    fun getApiToken(): String? {
        return getPref(API_TOKEN_KEY)
    }

    fun setBaseUrl(baseUrl: String): SharedPrefs {
        setPref(BASE_URL_KEY, baseUrl)
        return this
    }

    fun getBaseUrl(): String? {
        val baseUrl = getPref(BASE_URL_KEY)
        if (baseUrl != null && baseUrl.endsWith("/"))
            return baseUrl.substring(0, baseUrl.length - 1)
        return baseUrl
    }

    fun setSelectedMediaPlayer(id: String): SharedPrefs {
        setPref(SELECTED_MEDIA_PLAYER_KEY, id)
        return this
    }

    fun getSelectedMediaPlayer(): String? {
        val entityId = getPref(SELECTED_MEDIA_PLAYER_KEY) ?: return null
        return entityId
    }

    fun setTriggerEntityId(entityId: String): SharedPrefs {
        setPref(TRIGGER_ENTITY_ID_KEY, entityId)
        return this
    }

    fun getTriggerEntityId(): String? {
        return getPref(TRIGGER_ENTITY_ID_KEY)
    }

    fun setUiMode(uiMode: UiMode): SharedPrefs {
        setPref(UI_MODE_KEY, uiMode.name)
        return this
    }

    fun getUiMode(): UiMode {
        val uiModeName = getPref(UI_MODE_KEY) ?: return UiMode.Transparent
        return UiMode.valueOf(uiModeName)
    }
}