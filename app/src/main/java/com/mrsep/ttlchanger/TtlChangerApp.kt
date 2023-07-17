package com.mrsep.ttlchanger

import android.app.Application

class TtlChangerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DiContainer.provideContext(this)
    }

}