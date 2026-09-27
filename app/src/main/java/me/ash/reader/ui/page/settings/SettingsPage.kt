package me.ash.reader.ui.page.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.BuildConfig
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.AiApiEndpointPreference
import me.ash.reader.infrastructure.preference.GeminiApiKeyPreference
import me.ash.reader.infrastructure.preference.GeminiModelPreference
import me.ash.reader.infrastructure.preference.LocalAiApiEndpoint
import me.ash.reader.infrastructure.preference.LocalGeminiApiKey
import me.ash.reader.infrastructure.preference.LocalGeminiModel
import me.ash.reader.infrastructure.preference.LocalNewVersionNumber
import me.ash.reader.infrastructure.preference.LocalSkipVersionNumber
import me.ash.reader.infrastructure.preference.toDisplayName
import me.ash.reader.ui.component.AiApiEndpointDialog
import me.ash.reader.ui.component.GeminiApiKeyDialog
import me.ash.reader.ui.component.GeminiModelDialog
import me.ash.reader.ui.component.base.Banner
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.getCurrentVersion
import me.ash.reader.ui.page.settings.tips.UpdateDialog
import me.ash.reader.ui.page.settings.tips.UpdateViewModel
import me.ash.reader.ui.theme.palette.onLight
import java.util.Locale

@Composable
fun SettingsPage(
    updateViewModel: UpdateViewModel = hiltViewModel(),
    onBack: () -> Unit,
    navigateToAccounts: () -> Unit,
    navigateToColorAndStyle: () -> Unit,
    navigateToInteraction: () -> Unit,
    navigateToLanguages: () -> Unit,
    navigateToTroubleshooting: () -> Unit,
    navigateToTipsAndSupport: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val newVersion = LocalNewVersionNumber.current
    val skipVersion = LocalSkipVersionNumber.current
    val geminiApiKey = LocalGeminiApiKey.current
    val geminiModel = LocalGeminiModel.current
    val aiApiEndpoint = LocalAiApiEndpoint.current
    val currentVersion by remember { mutableStateOf(context.getCurrentVersion()) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showEndpointDialog by remember { mutableStateOf(false) }

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
                    DisplayText(text = stringResource(R.string.settings), desc = "")
                }
                item {
                    Box {
                        if (newVersion.whetherNeedUpdate(currentVersion, skipVersion)) {
                            Banner(
                                modifier = Modifier.zIndex(1f),
                                title = stringResource(R.string.get_new_updates),
                                desc = stringResource(
                                    R.string.get_new_updates_desc,
                                    newVersion.toString(),
                                ),
                                icon = Icons.Outlined.Lightbulb,
                                action = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.close),
                                    )
                                },
                            ) {
                                updateViewModel.showDialog()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        // Banner(
                        //     title = stringResource(R.string.in_coding),
                        //     desc = stringResource(R.string.coming_soon),
                        //     icon = Icons.Outlined.Lightbulb,
                        // )
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.accounts),
                        desc = stringResource(R.string.accounts_desc),
                        icon = Icons.Outlined.AccountCircle,
                        onClick = navigateToAccounts
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.color_and_style),
                        desc = stringResource(R.string.color_and_style_desc),
                        icon = Icons.Outlined.Palette,
                        onClick = navigateToColorAndStyle
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.interaction),
                        desc = stringResource(R.string.interaction_desc),
                        icon = Icons.Outlined.TouchApp,
                        onClick = navigateToInteraction
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.gemini_model),
                        desc = geminiModel.ifBlank { GeminiModelPreference.default },
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = { showModelDialog = true }
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.ai_api_key_dialog_title),
                        desc = if (geminiApiKey.isNotBlank()) stringResource(R.string.custom_api_key_set)
                               else if (BuildConfig.DEFAULT_GEMINI_API_KEY.isNotBlank()) stringResource(R.string.default_api_key_active)
                               else stringResource(R.string.api_key_not_configured),
                        icon = Icons.Default.Key,
                        onClick = { showApiKeyDialog = true }
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.ai_api_endpoint),
                        desc = if (aiApiEndpoint.isNotBlank()) aiApiEndpoint else stringResource(R.string.ai_api_endpoint_default),
                        icon = Icons.Outlined.Language,
                        onClick = { showEndpointDialog = true }
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.languages),
                        desc = Locale.getDefault().toDisplayName(),
                        icon = Icons.Outlined.Language,
                        onClick = navigateToLanguages
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.troubleshooting),
                        desc = stringResource(R.string.troubleshooting_desc),
                        icon = Icons.Outlined.BugReport,
                        onClick = navigateToTroubleshooting
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.tips_and_support),
                        desc = stringResource(R.string.tips_and_support_desc),
                        icon = Icons.Outlined.TipsAndUpdates,
                        onClick = navigateToTipsAndSupport
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    UpdateDialog()

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
}
