package com.mrsep.ttlchanger.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mrsep.ttlchanger.DiContainer
import com.mrsep.ttlchanger.R
import com.mrsep.ttlchanger.data.TtlOperationResult
import com.mrsep.ttlchanger.widget.SetTtlAction.Companion.ttlKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

val keySelectedTtl = intPreferencesKey("selected_ttl")
val keyEventCode = intPreferencesKey("event_code")

@Suppress("unused")
private const val TAG = "OneValueWidget"

/**
 * Event codes: 0 for no event, -1 for TTL changing failed, 1 for TTL changing successful.
 * */
class OneValueWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(id)
            }
        }
    }

    @Composable
    private fun WidgetContent(glanceId: GlanceId) {
        val context = LocalContext.current
        val preferences = currentState<Preferences>()
        val selectedTtl = preferences[keySelectedTtl] ?: 64
        val eventCode = preferences[keyEventCode] ?: 0

        if (eventCode != 0) {
            LaunchedEffect(Unit) {
                delay(3000)
                updateAppWidgetState(context, glanceId) { preferences ->
                    preferences[keyEventCode] = 0
                }
                OneValueWidget().update(context, glanceId)
            }
        }

        val size = LocalSize.current
        val minDimension = min(size.width, size.height)

        Box(
            modifier = GlanceModifier
                .width(minDimension)
                .height(minDimension)
                .background(GlanceTheme.colors.background)
                .clickable(
                    onClick = actionRunCallback<SetTtlAction>(
                        actionParametersOf(ttlKey to selectedTtl)
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (eventCode == 0) {
                Text(
                    text = selectedTtl.toString(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                )
            } else {
                Image(
                    provider = ImageProvider(
                        if (eventCode == 1) R.drawable.ic_done_24 else R.drawable.ic_cancel_24
                    ),
                    contentDescription = context.getString(
                        if (eventCode == 1) R.string.changing_success_message else R.string.changing_failure_message
                    ),
                    modifier = GlanceModifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(getIconColor(eventCode))
                )
            }
        }
    }

    @Stable
    @Composable
    private fun getIconColor(eventCode: Int) = when (eventCode) {
        1 -> ColorProvider(Color.Green)
        -1 -> ColorProvider(Color.Red)
        else -> GlanceTheme.colors.onBackground
    }

}

class SetTtlAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        parameters[ttlKey]?.let { ttlValue ->
            val userPref = DiContainer.preferencesRepository.userPreferencesFlow.first()
            val writeResult = DiContainer.ttlManager.writeValue(ttlValue, userPref.ipv6Enabled)
            val codeResult = if (writeResult is TtlOperationResult.Success) 1 else -1
            updateAppWidgetState(context, glanceId) { preferences ->
                preferences[keyEventCode] = codeResult
            }
            OneValueWidget().update(context, glanceId)
        }
    }

    companion object {
        val ttlKey = ActionParameters.Key<Int>("TTL_KEY")
    }

}