package com.mrsep.ttlchanger.widget.common

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val prefKeySelectedTtl = intPreferencesKey("selected_ttl")
val prefKeyBackgroundOpacity = intPreferencesKey("background_opacity")
val prefKeyWidgetState = stringPreferencesKey("widget_state")

enum class WidgetState { Ready, Success, Error }