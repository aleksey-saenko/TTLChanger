package com.mrsep.ttlchanger.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.mrsep.ttlchanger.DiContainer
import com.mrsep.ttlchanger.presentation.theme.TTLChangerTheme
import kotlinx.coroutines.launch

class AppWidgetConfigActivity : ComponentActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
        setContent {
            TTLChangerTheme {
                Surface {
                    ConfigureScreen(
                        onBackPressed = ::finish,
                        onCreateClicked = ::onWidgetCreate
                    )
                }
            }
        }
    }

    private fun onWidgetCreate(selectedTtl: Int) {
        saveWidgetState(selectedTtl)
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private fun saveWidgetState(selectedTtl: Int) = DiContainer.appScope.launch {
        val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
        updateAppWidgetState(applicationContext, glanceId) { preferences ->
            preferences[keySelectedTtl] = selectedTtl
        }
        OneValueWidget().update(applicationContext, glanceId)
    }


    private fun setupActivity() {
        setResult(RESULT_CANCELED)
        // Find the widget id from the intent.
        val extras = intent.extras
        if (extras != null) {
            widgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

}