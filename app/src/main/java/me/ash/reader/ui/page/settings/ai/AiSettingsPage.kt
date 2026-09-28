package me.ash.reader.ui.page.settings.ai

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Segment
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.BuildConfig
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.AiApiEndpointPreference
import me.ash.reader.infrastructure.preference.AiSummaryPromptPreference
import me.ash.reader.infrastructure.preference.GeminiApiKeyPreference
import me.ash.reader.infrastructure.preference.GeminiModelPreference
import me.ash.reader.infrastructure.preference.LocalAiApiEndpoint
import me.ash.reader.infrastructure.preference.LocalAiSummaryPrompt
import me.ash.reader.infrastructure.preference.LocalGeminiApiKey
import me.ash.reader.infrastructure.preference.LocalGeminiModel
import me.ash.reader.infrastructure.preference.LocalReadingPullUpAiSummary
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.ui.component.AiApiEndpointDialog
import me.ash.reader.ui.component.AiSummaryPromptDialog
import me.ash.reader.ui.component.GeminiApiKeyDialog
import me.ash.reader.ui.component.GeminiModelDialog
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun AiSettingsPage(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiApiKey = LocalGeminiApiKey.current
    val geminiModel = LocalGeminiModel.current
    val aiApiEndpoint = LocalAiApiEndpoint.current
    val aiSummaryPrompt = LocalAiSummaryPrompt.current
    val pullUpAiSummary = LocalReadingPullUpAiSummary.current

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showEndpointDialog by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.ai_summary_settings), desc = "")
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.general),
                    )
                    SettingItem(
                        title = stringResource(R.string.gemini_model),
                        desc = geminiModel.ifBlank { GeminiModelPreference.default },
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = { showModelDialog = true }
                    )
                    SettingItem(
                        title = stringResource(R.string.ai_api_key_dialog_title),
                        desc = if (geminiApiKey.isNotBlank()) stringResource(R.string.custom_api_key_set)
                               else if (BuildConfig.DEFAULT_GEMINI_API_KEY.isNotBlank()) stringResource(R.string.default_api_key_active)
                               else stringResource(R.string.api_key_not_configured),
                        icon = Icons.Default.Key,
                        onClick = { showApiKeyDialog = true }
                    )
                    SettingItem(
                        title = stringResource(R.string.ai_api_endpoint),
                        desc = if (aiApiEndpoint.isNotBlank()) aiApiEndpoint else stringResource(R.string.ai_api_endpoint_default),
                        icon = Icons.Outlined.Language,
                        onClick = { showEndpointDialog = true }
                    )
                    SettingItem(
                        title = stringResource(R.string.ai_summary_prompt),
                        desc = if (aiSummaryPrompt.isNotBlank() && aiSummaryPrompt != AiSummaryPromptPreference.default) {
                            aiSummaryPrompt.replace("\n", " ").let { if (it.length > 50) it.take(50) + "..." else it }
                        } else {
                            stringResource(R.string.ai_summary_prompt_default)
                        },
                        icon = Icons.AutoMirrored.Rounded.Segment,
                        onClick = { showPromptDialog = true }
                    )

                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.reading_page),
                    )
                    SettingItem(
                        title = stringResource(R.string.pull_up_for_ai_summary),
                        desc = stringResource(R.string.pull_up_for_ai_summary_desc),
                        onClick = { (!pullUpAiSummary).put(context, scope) },
                    ) {
                        RYSwitch(activated = pullUpAiSummary.value) {
                            (!pullUpAiSummary).put(context, scope)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    GeminiModelDialog(
        visible = showModelDialog,
        value = geminiModel,
        onDismissRequest = { showModelDialog = false },
        onConfirm = {
            GeminiModelPreference.put(context, scope, it.trim())
            showModelDialog = false
        }
    )

    GeminiApiKeyDialog(
        visible = showApiKeyDialog,
        value = geminiApiKey,
        onDismissRequest = { showApiKeyDialog = false },
        onConfirm = {
            GeminiApiKeyPreference.put(context, scope, it.trim())
            showApiKeyDialog = false
        }
    )

    AiApiEndpointDialog(
        visible = showEndpointDialog,
        value = aiApiEndpoint,
        onDismissRequest = { showEndpointDialog = false },
        onConfirm = {
            AiApiEndpointPreference.put(context, scope, it.trim())
            showEndpointDialog = false
        }
    )

    AiSummaryPromptDialog(
        visible = showPromptDialog,
        value = aiSummaryPrompt,
        onDismissRequest = { showPromptDialog = false },
        onConfirm = {
            AiSummaryPromptPreference.put(context, scope, it.trim())
            showPromptDialog = false
        }
    )
}
