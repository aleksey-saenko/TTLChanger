package com.mrsep.ttlchanger.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrsep.ttlchanger.R
import com.mrsep.ttlchanger.presentation.components.triangleShape
import com.mrsep.ttlchanger.presentation.theme.TTLChangerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    val screenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiState = screenUiState
    uiState?.let {
        Column {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.open_settings)
                        )
                    }
                }
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.userInput,
                        onValueChange = viewModel::updateUserInput,
                        prefix = {
                            Text(
                                text = "Enter value: ",
                                modifier = Modifier
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        shape = RectangleShape,
                    )
                    Column {
                        Button(
                            onClick = viewModel::incUserInput,
                            shape = triangleShape,
                            content = { }
                        )
                        Button(
                            onClick = viewModel::decUserInput,
                            shape = triangleShape,
                            modifier = Modifier.rotate(180f),
                            content = { }
                        )
                    }
                }
                Text(
                    text = "Last operation:\n${uiState.lastOperation?.getMessage() ?: ""}",
                    modifier = Modifier.padding(top = 16.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = viewModel::writeTtl, shape = RectangleShape) {
                        Text(text = "WRITE")
                    }
                    Button(onClick = viewModel::readTtl, shape = RectangleShape) {
                        Text(text = "READ")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Apply selected TTL on device boot",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.autostartEnabled,
                        onCheckedChange = viewModel::toggleAutoStart
                    )
                }
                Text(
                    text = "If disabled, the value will be reset to default 64 on reboot",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp).alpha(0.8f)
                )
            }
        }
    }

}

@Composable
private fun TtlOperation.getMessage(): String {
    return "${type.name}: $result"
}


@Preview(widthDp = 400, heightDp = 800)
@Composable
fun MainScreenPreview() {
    TTLChangerTheme(darkTheme = true) {
        Surface {
            MainScreen()
        }
    }
}