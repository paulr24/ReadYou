package me.ash.reader.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.R

@Composable
fun AiApiEndpointDialog(
    visible: Boolean = false,
    value: String = "",
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    if (visible) {
        var endpointText by remember(value) { mutableStateOf(value) }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = { Icon(Icons.Outlined.Language, contentDescription = null) },
            title = { Text(text = stringResource(R.string.ai_api_endpoint_title)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.ai_api_endpoint_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = endpointText,
                        onValueChange = { endpointText = it },
                        placeholder = { Text(text = "Default (Google Gemini) or e.g. https://api.openai.com/v1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(endpointText.trim())
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
