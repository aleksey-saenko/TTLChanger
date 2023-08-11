package com.mrsep.ttlchanger.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.ttlchanger.R

@Composable
internal fun PreferencesDialog(
    onDismissClick: () -> Unit,
    autostartEnabled: Boolean,
    onChangeAutostartEnabled: (Boolean) -> Unit,
    ipv6Enabled: Boolean,
    onChangeIPv6Enabled: (Boolean) -> Unit
) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.preferences),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(R.string.close))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.enable_support_ipv6),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = ipv6Enabled,
                        onCheckedChange = onChangeIPv6Enabled
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.apply_ttl_on_boot),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = autostartEnabled,
                        onCheckedChange = onChangeAutostartEnabled
                    )
                }
//                Text(
//                    text = "If disabled, values will be reset to default 64",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier
//                        .padding(top = 8.dp)
//                        .alpha(0.8f)
//                )

            }
        },
        textContentColor = MaterialTheme.colorScheme.onSurface,
        onDismissRequest = onDismissClick
    )
}