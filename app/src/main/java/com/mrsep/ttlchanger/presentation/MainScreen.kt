package com.mrsep.ttlchanger.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrsep.ttlchanger.R
import com.mrsep.ttlchanger.data.TtlOperationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    val focusManager = LocalFocusManager.current
    val screenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiState = screenUiState
    uiState?.let {
        var preferencesDialogVisible by rememberSaveable { mutableStateOf(false) }
        if (preferencesDialogVisible) {
            PreferencesDialog(
                onDismissClick = { preferencesDialogVisible = false },
                autostartEnabled = uiState.autostartEnabled,
                onChangeAutostartEnabled = viewModel::toggleAutoStart,
                ipv6Enabled = uiState.ipv6Enabled,
                onChangeIPv6Enabled = viewModel::toggleIPv6
            )
        }
        Column {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { preferencesDialogVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.open_preferences)
                        )
                    }
                }
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
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
                                text = stringResource(R.string.prefix_enter_value),
                                modifier = Modifier
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FilledTonalIconButton(onClick = viewModel::decUserInput) {
                        Icon(
                            painter = painterResource(R.drawable.ic_remove_24),
                            contentDescription = stringResource(R.string.decrease_selected_ttl)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = viewModel::incUserInput) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_24),
                            contentDescription = stringResource(R.string.increase_selected_ttl)
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.writeTtl(uiState.ipv6Enabled) },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(text = stringResource(R.string.write_cap))
                    }
                    Button(
                        onClick = { viewModel.readTtl(uiState.ipv6Enabled) },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(text = stringResource(R.string.read_cap))
                    }
                    Crossfade(
                        targetState = uiState.inProgress,
                        animationSpec = tween(200),
                        label = "ProgressIndicator"
                    ) { inProgress ->
                        if (inProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(32.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
                val resultMessage = uiState.lastOperation?.getMessage(uiState.ipv6Enabled)
                Crossfade(
                    targetState = resultMessage,
                    animationSpec = tween(200),
                    label = "ResultCard"
                ) { message ->
                    message?.let {
                        ResultMessageCard(
                            message = message,
                            onDismiss = viewModel::resetLastOperation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = Dp.Unspecified)
                                .padding(top = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultMessageCard(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.operation_result),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dismiss_result_message)
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .horizontalScroll(rememberScrollState())
            )
        }
    }
}


@Composable
private fun TtlOperation.getMessage(ipv6Enabled: Boolean): String {
    val message = when (result) {
        is TtlOperationResult.Success -> buildString {
            append(stringResource(R.string.success))
            append("\nIPv4 = ${result.ipv4}")
            if (ipv6Enabled) append("\nIPv6 = ${result.ipv6}")
        }

        TtlOperationResult.InvalidValue -> buildString {
            append(stringResource(R.string.invalid_value))
        }

        TtlOperationResult.NoRootAccess -> buildString {
            append(stringResource(R.string.no_root_access))
        }

        is TtlOperationResult.ErrorReturnCode -> buildString {
            append(stringResource(R.string.format_error_return_code, result.code))
        }

        is TtlOperationResult.UnhandledError -> buildString {
            append(stringResource(R.string.unhandled_error))
            result.message?.let { append("\n$it") }
            result.t?.message?.let { append("\n$it") }
        }
    }
    val typeTitle = when (type) {
        TtlOperationType.WRITE -> stringResource(R.string.write_cap)
        TtlOperationType.READ -> stringResource(R.string.read_cap)
    }
    return "$typeTitle: $message"
}