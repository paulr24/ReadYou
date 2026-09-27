package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingHorizontalScroll
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingHorizontalScroll =
    compositionLocalOf<ReadingHorizontalScrollPreference> { ReadingHorizontalScrollPreference.default }

sealed class ReadingHorizontalScrollPreference(val value: Boolean) : Preference() {
    object ON : ReadingHorizontalScrollPreference(true)
    object OFF : ReadingHorizontalScrollPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingHorizontalScroll, value)
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingHorizontalScroll]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingHorizontalScrollPreference.not(): ReadingHorizontalScrollPreference =
    when (value) {
        true -> ReadingHorizontalScrollPreference.OFF
        false -> ReadingHorizontalScrollPreference.ON
    }
