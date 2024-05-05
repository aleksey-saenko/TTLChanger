package com.mrsep.ttlchanger.widget.onevalue

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
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
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mrsep.ttlchanger.R
import com.mrsep.ttlchanger.presentation.theme.DarkGreen
import com.mrsep.ttlchanger.presentation.theme.DarkRed
import com.mrsep.ttlchanger.presentation.theme.LightGreen
import com.mrsep.ttlchanger.presentation.theme.LightRed
import com.mrsep.ttlchanger.presentation.theme.darkColorScheme
import com.mrsep.ttlchanger.presentation.theme.lightColorScheme
import com.mrsep.ttlchanger.widget.common.WidgetState
import com.mrsep.ttlchanger.widget.common.WriteTtlActionBase
import com.mrsep.ttlchanger.widget.common.actionKeyTtl
import com.mrsep.ttlchanger.widget.common.backgroundCompat
import com.mrsep.ttlchanger.widget.common.prefKeyBackgroundOpacity
import com.mrsep.ttlchanger.widget.common.prefKeySelectedTtl
import com.mrsep.ttlchanger.widget.common.prefKeyWidgetState

class OneValueWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(
                // enable dynamic colors if available
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    GlanceTheme.colors
                else
                    ColorProviders(light = lightColorScheme, dark = darkColorScheme)
            ) {
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
        Box(
            modifier = GlanceModifier
                .backgroundCompat(backgroundOpacity)
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            when (widgetState) {
                WidgetState.Ready -> Box(
                    modifier = GlanceModifier
                        .size(56.dp)
                        .clickable(
                            onClick = actionRunCallback<WriteTtlActionOneValue>(
                                actionParametersOf(actionKeyTtl to selectedTtl)
                            ),
                            rippleOverride = -1
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedTtl.toString(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                WidgetState.Success -> Image(
                    provider = ImageProvider(R.drawable.ic_done_24),
                    contentDescription = context.getString(R.string.changing_success_message),
                    modifier = GlanceModifier.size(40.dp),
                    colorFilter = ColorFilter.tint(
                        ColorProvider(
                            day = DarkGreen,
                            night = LightGreen
                        )
                    )
                )

                WidgetState.Error -> Image(
                    provider = ImageProvider(R.drawable.ic_cancel_24),
                    contentDescription = context.getString(R.string.changing_failure_message),
                    modifier = GlanceModifier.size(40.dp),
                    colorFilter = ColorFilter.tint(
                        ColorProvider(
                            day = DarkRed,
                            night = LightRed
                        )
                    )
                )
            }
        }
    }

}

class WriteTtlActionOneValue : WriteTtlActionBase() {
    override val glanceAppWidget = OneValueWidget()
}