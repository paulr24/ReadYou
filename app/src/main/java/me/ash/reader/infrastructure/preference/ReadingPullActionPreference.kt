package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingPullDownAction
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingPullUpAction
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

data object ReadingPullGestureActions {
    const val None = 0
    const val AiSummary = 1
    const val FullContent = 2
    const val PreviousArticle = 3
    const val NextArticle = 3
}

val LocalReadingPullDownAction = compositionLocalOf { ReadingPullDownActionPreference.default }

sealed class ReadingPullDownActionPreference(val action: Int) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingPullDownAction, action)
        }
    }

    data object None : ReadingPullDownActionPreference(ReadingPullGestureActions.None)
    data object AiSummary : ReadingPullDownActionPreference(ReadingPullGestureActions.AiSummary)
    data object FullContent : ReadingPullDownActionPreference(ReadingPullGestureActions.FullContent)
    data object PreviousArticle : ReadingPullDownActionPreference(ReadingPullGestureActions.PreviousArticle)

    val desc: String
        @Composable get() = when (this) {
            None -> stringResource(id = R.string.none)
            AiSummary -> stringResource(id = R.string.ai_summary)
            FullContent -> stringResource(id = R.string.load_full_content)
            PreviousArticle -> stringResource(id = R.string.previous_article)
        }

    companion object {
        val default: ReadingPullDownActionPreference = AiSummary
        val values = listOf(
            None,
            AiSummary,
            FullContent,
            PreviousArticle,
        )

        fun fromPreferences(preferences: Preferences): ReadingPullDownActionPreference {
            return when (preferences[DataStoreKey.keys[readingPullDownAction]?.key as Preferences.Key<Int>]) {
                ReadingPullGestureActions.None -> None
                ReadingPullGestureActions.AiSummary -> AiSummary
                ReadingPullGestureActions.FullContent -> FullContent
                ReadingPullGestureActions.PreviousArticle -> PreviousArticle
                else -> default
            }
        }
    }
}

val LocalReadingPullUpAction = compositionLocalOf { ReadingPullUpActionPreference.default }

sealed class ReadingPullUpActionPreference(val action: Int) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingPullUpAction, action)
        }
    }

    data object None : ReadingPullUpActionPreference(ReadingPullGestureActions.None)
    data object FullContent : ReadingPullUpActionPreference(ReadingPullGestureActions.FullContent)
    data object AiSummary : ReadingPullUpActionPreference(ReadingPullGestureActions.AiSummary)
    data object NextArticle : ReadingPullUpActionPreference(ReadingPullGestureActions.NextArticle)

    val desc: String
        @Composable get() = when (this) {
            None -> stringResource(id = R.string.none)
            FullContent -> stringResource(id = R.string.load_full_content)
            AiSummary -> stringResource(id = R.string.ai_summary)
            NextArticle -> stringResource(id = R.string.next_article)
        }

    companion object {
        val default: ReadingPullUpActionPreference = FullContent
        val values = listOf(
            None,
            FullContent,
            AiSummary,
            NextArticle,
        )

        fun fromPreferences(preferences: Preferences): ReadingPullUpActionPreference {
            return when (preferences[DataStoreKey.keys[readingPullUpAction]?.key as Preferences.Key<Int>]) {
                ReadingPullGestureActions.None -> None
                ReadingPullGestureActions.FullContent -> FullContent
                ReadingPullGestureActions.AiSummary -> AiSummary
                ReadingPullGestureActions.NextArticle -> NextArticle
                else -> default
            }
        }
    }
}
