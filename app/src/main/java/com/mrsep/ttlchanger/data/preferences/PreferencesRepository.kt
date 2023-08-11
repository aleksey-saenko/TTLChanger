package com.mrsep.ttlchanger.data.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setDefaultTtl(value: Int)
    suspend fun setSavedTtl(value: Int)
    suspend fun setAutostartEnabled(enabled: Boolean)
    suspend fun setIPv6Enabled(enabled: Boolean)

}