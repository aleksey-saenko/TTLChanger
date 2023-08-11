package com.mrsep.ttlchanger.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.mrsep.ttlchanger.UserPreferencesProto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

private const val USER_PREFERENCES_STORE = "USER_PREFERENCES_STORE"
private const val TAG = "PreferencesRepository"

class PreferencesRepositoryImpl(
    appContext: Context,
    appScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher
) : PreferencesRepository {

    private val dataStore: DataStore<UserPreferencesProto> = DataStoreFactory.create(
        serializer = UserPreferencesProtoSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { UserPreferencesProto.getDefaultInstance() }
        ),
        scope = CoroutineScope(appScope.coroutineContext + ioDispatcher),
        produceFile = { appContext.dataStoreFile(USER_PREFERENCES_STORE) }
    )

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .ioExceptionCatcherOnRead()
        .map(::mapToUserPreferences)
        .flowOn(ioDispatcher)

    override suspend fun setDefaultTtl(value: Int) {
        safeWriter { setDefaultTtl(value) }
    }

    override suspend fun setSavedTtl(value: Int) {
        safeWriter { setSavedTtl(value) }
    }

    override suspend fun setAutostartEnabled(enabled: Boolean) {
        safeWriter { setAutostartEnabled(enabled) }
    }

    override suspend fun setIPv6Enabled(enabled: Boolean) {
        safeWriter { setIpv6Enabled(enabled) }
    }

    private fun mapToUserPreferences(preferences: UserPreferencesProto) = UserPreferences(
        defaultTtl = preferences.defaultTtl,
        savedTtl = preferences.savedTtl,
        autostartEnabled = preferences.autostartEnabled,
        ipv6Enabled = preferences.ipv6Enabled
    )

    private fun Flow<UserPreferencesProto>.ioExceptionCatcherOnRead(): Flow<UserPreferencesProto> {
        return this.catch { e ->
            Log.e(TAG, "Failed to read user preferences", e)
            when (e) {
                is IOException -> emit(UserPreferencesProto.getDefaultInstance())
                else -> throw e
            }
        }
    }

    private suspend inline fun safeWriter(
        crossinline action: UserPreferencesProto.Builder.() -> UserPreferencesProto.Builder
    ) {
        withContext(ioDispatcher) {
            try {
                dataStore.updateData { currentPreferences ->
                    currentPreferences.toBuilder()
                        .action()
                        .build()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to update user preferences", e)
            }
        }
    }

}