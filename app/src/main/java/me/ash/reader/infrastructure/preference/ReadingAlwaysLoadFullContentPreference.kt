package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingAlwaysLoadFullContent
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingAlwaysLoadFullContent =
    compositionLocalOf<ReadingAlwaysLoadFullContentPreference> { ReadingAlwaysLoadFullContentPreference.default }

sealed class ReadingAlwaysLoadFullContentPreference(val value: Boolean) : Preference() {
    object ON : ReadingAlwaysLoadFullContentPreference(true)
    object OFF : ReadingAlwaysLoadFullContentPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingAlwaysLoadFullContent, value)
        }
    }

    companion object {
        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingAlwaysLoadFullContent]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingAlwaysLoadFullContentPreference.not(): ReadingAlwaysLoadFullContentPreference =
    when (value) {
        true -> ReadingAlwaysLoadFullContentPreference.OFF
        false -> ReadingAlwaysLoadFullContentPreference.ON
    }
