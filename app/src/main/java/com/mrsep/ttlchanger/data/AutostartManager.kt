package com.mrsep.ttlchanger.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.mrsep.ttlchanger.BootCompletedReceiver

interface AutostartManager {
    fun toggleAutostart(enabled: Boolean)
}

class AutostartManagerImpl(private val appContext: Context) : AutostartManager {

    override fun toggleAutostart(enabled: Boolean) {
        val packageManager = appContext.packageManager
        val componentName = ComponentName(appContext, BootCompletedReceiver::class.java)
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        packageManager.setComponentEnabledSetting(
            componentName,
            state,
            PackageManager.DONT_KILL_APP
        )
    }

}