package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.aiApiEndpoint
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalAiApiEndpoint = compositionLocalOf { AiApiEndpointPreference.default }

object AiApiEndpointPreference {
    const val default = ""
    private val key = stringPreferencesKey(aiApiEndpoint)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch {
            context.dataStore.put(DataStoreKey.aiApiEndpoint, value)
        }
    }

    fun fromPreferences(preferences: Preferences): String =
        preferences[key] ?: default
}
