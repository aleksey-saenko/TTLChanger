package com.mrsep.ttlchanger.data.preferences

data class UserPreferences(
    val defaultTtl: Int,
    val savedTtl: Int,
    val autostartEnabled: Boolean,
    val ipv6Enabled: Boolean
)