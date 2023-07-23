package com.mrsep.ttlchanger.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.mrsep.ttlchanger.DiContainer
import com.mrsep.ttlchanger.presentation.theme.TTLChangerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OneValueWidgetConfigActivity : ComponentActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWidgetId()

        val uiState = mutableStateOf<OvwUiState?>(null)
        lifecycleScope.launch(Dispatchers.IO) {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
            val preferences = OneValueWidget().getAppWidgetState<Preferences>(
                this@OneValueWidgetConfigActivity,
                glanceId
            )
            uiState.value = preferences[keySelectedTtl]?.let { savedTtl ->
                OvwUiState(initialTtl = savedTtl, editMode = true)
            } ?: OvwUiState(initialTtl = 64, editMode = false)
        }

        setContent {
            TTLChangerTheme {
                Surface {
                    uiState.value?.let { state ->
                        OneValueConfigureScreen(
                            onBackPressed = ::finish,
                            onCreateClicked = ::onWidgetCreate,
                            uiState = state
                        )
                    }
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


    private fun initWidgetId() {
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

data class OvwUiState(
    val initialTtl: Int,
    val editMode: Boolean
)