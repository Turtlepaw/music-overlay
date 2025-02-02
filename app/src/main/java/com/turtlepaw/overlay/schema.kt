package com.turtlepaw.overlay

import com.turtlepaw.nearby_settings.tv_core.GroupData
import com.turtlepaw.nearby_settings.tv_core.SettingConstraints
import com.turtlepaw.nearby_settings.tv_core.SettingParent
import com.turtlepaw.nearby_settings.tv_core.SettingSchema
import com.turtlepaw.nearby_settings.tv_core.SettingType
import com.turtlepaw.nearby_settings.tv_core.SettingsSchema

val youtubeGroup = GroupData(
    key = "youtube",
    label = "YouTube",
    description = "Custom input group with custom input"
)

val defaultSchema = SettingsSchema(
    schemaItems = listOf(
        SettingSchema(
            key = "text_input",
            label = "Text Input",
            description = "**Markdown** is fully supported thanks to [flutter_markdown](https://pub.dev/packages/flutter_markdown)!",
            type = SettingType.TEXT,
            constraints = SettingConstraints(
                min = 5,
                max = 10
            )
        ),
        SettingSchema(
            key = "number_input",
            label = "Number Input",
            type = SettingType.NUMBER,
            constraints = SettingConstraints(
                min = 1,
                max = 10
            )
        ),
        SettingSchema(
            key = "toggle_input",
            description = "This is a toggle input",
            label = "Toggle Input",
            type = SettingType.TOGGLE,
        ),
        SettingSchema(
            key = "select_input",
            label = "Select Input",
            type = SettingType.SELECT,
            constraints = SettingConstraints(
                options = listOf("option1", "option2", "option3"),
            )
        ),
        SettingSchema(
            key = "multiselect_input",
            label = "Multiselect Input",
            type = SettingType.MULTI_SELECT,
            constraints = SettingConstraints(
                options = listOf("option1", "option2", "option3"),
                max = 2,
                min = 1
            )
        ),
        SettingSchema(
            key = "toggle_parent",
            label = "Custom Input",
            type = SettingType.TOGGLE,
            group = customInputGroup
        ),
        SettingSchema(
            key = "custom_input",
            label = "Custom Input",
            type = SettingType.TEXT,
            group = customInputGroup,
            parent = SettingParent(
                key = "toggle_parent",
                requiredBoolValue = true
            )
        )
    )
);