package com.turtlepaw.overlay

import com.turtlepaw.nearby_settings.tv_core.GroupData
import com.turtlepaw.nearby_settings.tv_core.SettingConstraints
import com.turtlepaw.nearby_settings.tv_core.SettingParent
import com.turtlepaw.nearby_settings.tv_core.SettingSchema
import com.turtlepaw.nearby_settings.tv_core.SettingType
import com.turtlepaw.nearby_settings.tv_core.SettingsSchema

val youtubeGroup = GroupData(
    key = "youtube",
    label = "YouTube Plugin",
    description = "[Learn how to setup the YouTube plugin](https://github.com/Turtlepaw/Overlay/wiki/YouTube-Playlist)"
)

val defaultSchema = SettingsSchema(
    schemaItems = listOf(
        SettingSchema(
            key = "ui_mode",
            label = "UI Mode",
            type = SettingType.SELECT,
            constraints = SettingConstraints(
                options = UiMode.entries.map { it.name },
            )
        ),
        SettingSchema(
            key = "youtube",
            label = "Enable YouTube Plugin",
            type = SettingType.TOGGLE,
            group = youtubeGroup
        ),
        SettingSchema(
            key = "youtube_api_key",
            label = "API Key",
            type = SettingType.TEXT,
            group = youtubeGroup,
            parent = SettingParent(
                key = "youtube",
                requiredBoolValue = true
            )
        ),
        SettingSchema(
            key = "youtube_playlist_id",
            label = "Playlist ID",
            type = SettingType.TEXT,
            group = youtubeGroup,
            parent = SettingParent(
                key = "youtube",
                requiredBoolValue = true
            )
        ),
    )
);