package com.mrsep.ttlchanger.widget.common

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.mrsep.ttlchanger.DiContainer
import com.mrsep.ttlchanger.data.TtlOperationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

val actionKeyTtl = ActionParameters.Key<Int>("key_ttl")

abstract class WriteTtlActionBase : ActionCallback {

    abstract val glanceAppWidget: GlanceAppWidget

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val ttlValue = parameters[actionKeyTtl] ?: return
        val ipv6Enabled = DiContainer.preferencesRepository.userPreferencesFlow.first().ipv6Enabled
        val resultState = when (DiContainer.ttlManager.writeValue(ttlValue, ipv6Enabled)) {
            is TtlOperationResult.Success -> WidgetState.Success
            else -> WidgetState.Error
        }
        updateWidgetState(context, glanceId, resultState)
        delay(RESULT_RESET_DELAY)
        updateWidgetState(context, glanceId, WidgetState.Ready)
    }

    private suspend fun updateWidgetState(
        context: Context,
        glanceId: GlanceId,
        state: WidgetState
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[prefKeyWidgetState] = state.name
        }
        glanceAppWidget.update(context, glanceId)
    }

    companion object {
        private const val RESULT_RESET_DELAY = 2_000L
    }

}

abstract class SelectTtlActionBase : ActionCallback {

    abstract val glanceAppWidget: GlanceAppWidget

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val ttlValue = parameters[actionKeyTtl] ?: return
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[prefKeySelectedTtl] = ttlValue
        }
        glanceAppWidget.update(context, glanceId)
    }

}