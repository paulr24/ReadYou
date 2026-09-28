package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingPullDownFullContent
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingPullDownFullContent =
    compositionLocalOf<ReadingPullDownFullContentPreference> { ReadingPullDownFullContentPreference.default }

sealed class ReadingPullDownFullContentPreference(val value: Boolean) : Preference() {
    object ON : ReadingPullDownFullContentPreference(true)
    object OFF : ReadingPullDownFullContentPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingPullDownFullContent, value)
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingPullDownFullContent]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingPullDownFullContentPreference.not(): ReadingPullDownFullContentPreference =
    when (value) {
        true -> ReadingPullDownFullContentPreference.OFF
        false -> ReadingPullDownFullContentPreference.ON
    }
