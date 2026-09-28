package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.aiSummaryPrompt
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalAiSummaryPrompt = compositionLocalOf { AiSummaryPromptPreference.default }

object AiSummaryPromptPreference {
    const val default = "Summarize the following article in 2-3 concise sentences. Article title: {title}\n\nArticle content:\n{content}"

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch {
            context.dataStore.put(DataStoreKey.aiSummaryPrompt, value)
        }
    }

    fun fromPreferences(preferences: Preferences): String =
        preferences[DataStoreKey.keys[aiSummaryPrompt]?.key as Preferences.Key<String>] ?: default
}
