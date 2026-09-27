package me.ash.reader.ui.component

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.ash.reader.R
import me.ash.reader.ui.component.base.TextFieldDialog

@Composable
fun GeminiApiKeyDialog(
    visible: Boolean = false,
    value: String = "",
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    if (visible) {
        val textFieldState = rememberTextFieldState(value)
        TextFieldDialog(
            textFieldState = textFieldState,
            title = stringResource(R.string.gemini_api_key),
            icon = Icons.Rounded.AutoAwesome,
            placeholder = stringResource(R.string.gemini_api_key_placeholder),
            onDismissRequest = onDismissRequest,
            onConfirm = onConfirm,
        )
    }
}
