package com.mrsep.ttlchanger.widget.onevalue

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mrsep.ttlchanger.R
import com.mrsep.ttlchanger.widget.common.WidgetState
import com.mrsep.ttlchanger.widget.common.WriteTtlActionBase
import com.mrsep.ttlchanger.widget.common.actionKeyTtl
import com.mrsep.ttlchanger.widget.common.prefKeyBackgroundOpacity
import com.mrsep.ttlchanger.widget.common.prefKeySelectedTtl
import com.mrsep.ttlchanger.widget.common.prefKeyWidgetState

class OneValueWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()
        val selectedTtl = prefs[prefKeySelectedTtl] ?: 64
        val backgroundOpacity = prefs[prefKeyBackgroundOpacity] ?: 100
        val widgetState = prefs[prefKeyWidgetState]?.run(WidgetState::valueOf) ?: WidgetState.Ready
        val backgroundColor = GlanceTheme.colors.background.getColor(context).copy(
            alpha = backgroundOpacity / 100f
        )
        Box(
            modifier = GlanceModifier
                .background(backgroundColor)
                .cornerRadius(8.dp)
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            when (widgetState) {
                WidgetState.Ready -> Box(
                    modifier = GlanceModifier
                        .cornerRadius(8.dp)
                        .size(56.dp)
                        .clickable(
                            actionRunCallback<WriteTtlActionOneValue>(
                                actionParametersOf(actionKeyTtl to selectedTtl)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedTtl.toString(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                WidgetState.Success -> Image(
                    provider = ImageProvider(R.drawable.ic_done_24),
                    contentDescription = context.getString(R.string.changing_success_message),
                    modifier = GlanceModifier.size(40.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(Color.Green))
                )

                WidgetState.Error -> Image(
                    provider = ImageProvider(R.drawable.ic_cancel_24),
                    contentDescription = context.getString(R.string.changing_failure_message),
                    modifier = GlanceModifier.size(40.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(Color.Red))
                )
            }
        }
    }

}

class WriteTtlActionOneValue : WriteTtlActionBase() {
    override val glanceAppWidget = OneValueWidget()
}