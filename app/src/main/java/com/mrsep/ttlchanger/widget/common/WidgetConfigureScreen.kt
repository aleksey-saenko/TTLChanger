package com.mrsep.ttlchanger.widget.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrsep.ttlchanger.R

data class WidgetConfigUiState(
    val widgetParams: WidgetParams,
    val editMode: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigureScreen(
    onBackPressed: () -> Unit,
    onCreateClicked: (widgetParams: WidgetParams) -> Unit,
    uiState: WidgetConfigUiState
) {
    var selectedTtl by rememberSaveable {
        mutableIntStateOf(uiState.widgetParams.selectedTtl)
    }
    var backgroundOpacity by rememberSaveable {
        mutableIntStateOf(uiState.widgetParams.backgroundOpacity)
    }
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding()
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = stringResource(R.string.widget_settings))
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                val ttlStringValue = "$selectedTtl".padStart(3, ' ')
                Text(
                    text = stringResource(R.string.format_selected_ttl, ttlStringValue),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalIconButton(
                    onClick = { selectedTtl = selectedTtl.dec().coerceAtLeast(1) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_remove_24),
                        contentDescription = stringResource(R.string.decrease_selected_ttl)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalIconButton(
                    onClick = { selectedTtl = selectedTtl.inc().coerceAtMost(255) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_24),
                        contentDescription = stringResource(R.string.increase_selected_ttl)
                    )
                }
            }
            Slider(
                value = selectedTtl.toFloat(),
                onValueChange = { selectedTtl = it.toInt() },
                valueRange = 1f..255f,
                steps = 255
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.background_color),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            val selectedBackgroundColorType by remember(backgroundOpacity) {
                mutableStateOf(ColorType.getClosest(backgroundOpacity))
            }
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ColorType.entries.forEach { buttonColorType ->
                    SegmentedButton(
                        selected = buttonColorType == selectedBackgroundColorType,
                        onClick = { backgroundOpacity = buttonColorType.opacity },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = buttonColorType.ordinal,
                            count = ColorType.entries.size
                        ),
                        icon = { }
                    ) {
                        Text(
                            text = buttonColorType.title(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val widgetParams = WidgetParams(
                        selectedTtl = selectedTtl,
                        backgroundOpacity = backgroundOpacity
                    )
                    onCreateClicked(widgetParams)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(if (uiState.editMode) R.string.apply_changes else R.string.add_widget)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

    }
}

private enum class ColorType(val opacity: Int) {
    Transparent(0),
    Translucent(50),
    Solid(100);

    companion object {
        fun getClosest(opacity: Int) = when {
            opacity <= 25 -> Transparent
            opacity <= 75 -> Translucent
            else -> Solid
        }
    }
}

@Composable
private fun ColorType.title() = when (this) {
    ColorType.Transparent -> stringResource(R.string.transparent)
    ColorType.Translucent -> stringResource(R.string.translucent)
    ColorType.Solid -> stringResource(R.string.solid)
}