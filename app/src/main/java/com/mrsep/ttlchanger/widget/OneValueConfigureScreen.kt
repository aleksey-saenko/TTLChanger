package com.mrsep.ttlchanger.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.ttlchanger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneValueConfigureScreen(
    onBackPressed: () -> Unit,
    onCreateClicked: (selectedTtl: Int) -> Unit,
    uiState: OvwUiState
) {
    var selectedTtl by rememberSaveable { mutableStateOf(uiState.initialTtl) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = stringResource(R.string.widget_settings))
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(
                            R.string.navigate_back
                        )
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
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalIconButton(onClick = { selectedTtl -= 1 }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_remove_24),
                        contentDescription = stringResource(R.string.decrease_selected_ttl)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalIconButton(onClick = { selectedTtl += 1 }) {
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
            Button(
                onClick = { onCreateClicked(selectedTtl) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(
                        if (uiState.editMode) R.string.apply_changes else R.string.add_widget
                    )
                )
            }
        }

    }
}