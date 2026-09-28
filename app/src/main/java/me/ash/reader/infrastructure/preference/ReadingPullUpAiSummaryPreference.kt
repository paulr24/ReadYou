package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingPullUpAiSummary
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingPullUpAiSummary =
    compositionLocalOf<ReadingPullUpAiSummaryPreference> { ReadingPullUpAiSummaryPreference.default }

sealed class ReadingPullUpAiSummaryPreference(val value: Boolean) : Preference() {
    object ON : ReadingPullUpAiSummaryPreference(true)
    object OFF : ReadingPullUpAiSummaryPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingPullUpAiSummary, value)
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingPullUpAiSummary]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingPullUpAiSummaryPreference.not(): ReadingPullUpAiSummaryPreference =
    when (value) {
        true -> ReadingPullUpAiSummaryPreference.OFF
        false -> ReadingPullUpAiSummaryPreference.ON
    }
