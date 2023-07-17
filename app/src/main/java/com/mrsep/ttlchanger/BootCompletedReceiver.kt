package com.mrsep.ttlchanger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    private val appScope = DiContainer.appScope
    private val ttlManager = DiContainer.ttlManager
    private val preferencesRepository = DiContainer.preferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            appScope.launch {
                val preferences = preferencesRepository.userPreferencesFlow.first()
                ttlManager.writeValue(preferences.savedTtl)
            }
        }
    }

}