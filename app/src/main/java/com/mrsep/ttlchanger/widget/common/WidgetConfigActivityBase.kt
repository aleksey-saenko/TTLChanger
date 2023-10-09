package com.mrsep.ttlchanger.widget.common

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.mrsep.ttlchanger.DiContainer
import com.mrsep.ttlchanger.presentation.theme.TTLChangerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class WidgetConfigActivityBase : ComponentActivity() {

    abstract val glanceAppWidget: GlanceAppWidget

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)
        val extras = intent.extras
        if (extras != null) {
            widgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val uiState = mutableStateOf<WidgetConfigUiState?>(null)
        lifecycleScope.launch(Dispatchers.IO) {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
            val preferences = glanceAppWidget.getAppWidgetState<Preferences>(
                this@WidgetConfigActivityBase,
                glanceId
            )
            uiState.value = preferences[prefKeySelectedTtl]?.let { savedTtl ->
                WidgetConfigUiState(
                    widgetParams = WidgetParams(
                        selectedTtl = savedTtl,
                        backgroundOpacity = preferences[prefKeyBackgroundOpacity] ?: 100
                    ),
                    editMode = true
                )
            } ?: WidgetConfigUiState(
                widgetParams = WidgetParams(
                    selectedTtl = 64,
                    backgroundOpacity = 100
                ),
                editMode = false
            )
        }

        setContent {
            TTLChangerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    uiState.value?.let { state ->
                        WidgetConfigureScreen(
                            onBackPressed = ::finish,
                            onCreateClicked = ::onWidgetCreate,
                            uiState = state
                        )
                    }
                }
            }
        }
    }

    private fun onWidgetCreate(widgetParams: WidgetParams) {
        saveWidgetState(widgetParams)
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private fun saveWidgetState(widgetParams: WidgetParams) = DiContainer.appScope.launch {
        val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
        updateAppWidgetState(applicationContext, glanceId) { preferences ->
            preferences[prefKeySelectedTtl] = widgetParams.selectedTtl
            preferences[prefKeyBackgroundOpacity] = widgetParams.backgroundOpacity
        }
        glanceAppWidget.update(applicationContext, glanceId)
    }

}