package com.mrsep.ttlchanger

import android.content.Context
import com.mrsep.ttlchanger.data.AutostartManager
import com.mrsep.ttlchanger.data.AutostartManagerImpl
import com.mrsep.ttlchanger.data.TtlManager
import com.mrsep.ttlchanger.data.TtlManagerImpl
import com.mrsep.ttlchanger.data.preferences.PreferencesRepository
import com.mrsep.ttlchanger.data.preferences.PreferencesRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object DiContainer {

    private lateinit var applicationContext: Context

    val mainDispatcher = Dispatchers.Main
    val defaultDispatcher = Dispatchers.Default
    val ioDispatcher = Dispatchers.IO

    val appScope = CoroutineScope(ioDispatcher + SupervisorJob())

    val ttlManager: TtlManager by lazy {
        TtlManagerImpl()
    }

    val autostartManager: AutostartManager by lazy {
        AutostartManagerImpl(applicationContext)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(
            appContext = applicationContext,
            appScope = appScope,
            ioDispatcher = ioDispatcher
        )
    }

    fun provideContext(appContext: Context) {
        applicationContext = appContext
    }

}