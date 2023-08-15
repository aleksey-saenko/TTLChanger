package com.mrsep.ttlchanger

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.mrsep.ttlchanger.widget.onevalue.OneValueWidget
import com.mrsep.ttlchanger.widget.adjustable.AdjustableValueWidget
import kotlinx.coroutines.launch

class TtlChangerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DiContainer.provideContext(this)
        DiContainer.appScope.launch {
            OneValueWidget().updateAll(applicationContext)
            AdjustableValueWidget().updateAll(applicationContext)
        }
    }

}