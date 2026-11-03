package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingColorTheme
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingColorTheme =
    compositionLocalOf<ReadingColorThemePreference> { ReadingColorThemePreference.default }

@Immutable
sealed class ReadingColorThemePreference(
    val value: Int,
    val previewSurface: Color,
    val previewOnSurface: Color,
) : Preference() {

    object Default : ReadingColorThemePreference(
        value = 0,
        previewSurface = Color(0xFFF7F9FC),
        previewOnSurface = Color(0xFF1B1B1F),
    )

    object Sepia : ReadingColorThemePreference(
        value = 1,
        previewSurface = Color(0xFFF4ECD8),
        previewOnSurface = Color(0xFF433422),
    )

    object Light : ReadingColorThemePreference(
        value = 2,
        previewSurface = Color(0xFFFFFFFF),
        previewOnSurface = Color(0xFF1A1A1A),
    )

    object Dark : ReadingColorThemePreference(
        value = 3,
        previewSurface = Color(0xFF202124),
        previewOnSurface = Color(0xFFE8EAED),
    )

    object Oled : ReadingColorThemePreference(
        value = 4,
        previewSurface = Color(0xFF000000),
        previewOnSurface = Color(0xFFF5F5F5),
    )

    object Mint : ReadingColorThemePreference(
        value = 5,
        previewSurface = Color(0xFFEAF2E8),
        previewOnSurface = Color(0xFF1E3520),
    )

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Default -> context.getString(R.string.color_theme_default)
            Sepia -> context.getString(R.string.color_theme_sepia)
            Light -> context.getString(R.string.color_theme_light)
            Dark -> context.getString(R.string.color_theme_dark)
            Oled -> context.getString(R.string.color_theme_oled)
            Mint -> context.getString(R.string.color_theme_mint)
        }

    fun getColorScheme(systemColorScheme: ColorScheme): ColorScheme =
        when (this) {
            Default -> systemColorScheme
            Sepia -> SepiaColorScheme
            Light -> LightColorScheme
            Dark -> DarkColorScheme
            Oled -> OledColorScheme
            Mint -> MintColorScheme
        }

    fun isDark(systemInDarkTheme: Boolean): Boolean =
        when (this) {
            Default -> systemInDarkTheme
            Sepia, Light, Mint -> false
            Dark, Oled -> true
        }

    companion object {
        val default = Default
        val values = listOf(Default, Sepia, Light, Dark, Oled, Mint)
        private val key = intPreferencesKey(readingColorTheme)

        fun fromPreferences(preferences: Preferences): ReadingColorThemePreference =
            when (preferences[key]) {
                0 -> Default
                1 -> Sepia
                2 -> Light
                3 -> Dark
                4 -> Oled
                5 -> Mint
                else -> default
            }

        private val SepiaColorScheme = lightColorScheme(
            primary = Color(0xFF8B5A2B),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE8DAC2),
            onPrimaryContainer = Color(0xFF332010),
            secondary = Color(0xFF705B44),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE8DAC2),
            onSecondaryContainer = Color(0xFF291C0E),
            tertiary = Color(0xFF5B6237),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFDFE7B1),
            onTertiaryContainer = Color(0xFF191E00),
            background = Color(0xFFF4ECD8),
            onBackground = Color(0xFF433422),
            surface = Color(0xFFF4ECD8),
            onSurface = Color(0xFF433422),
            surfaceVariant = Color(0xFFE6DCCE),
            onSurfaceVariant = Color(0xFF6B5845),
            surfaceContainer = Color(0xFFEFE6D1),
            surfaceContainerHighest = Color(0xFFE5DCB8),
            outline = Color(0xFFB5A795),
            outlineVariant = Color(0xFFD6C8B5),
        )

        private val LightColorScheme = lightColorScheme(
            primary = Color(0xFF1A73E8),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD2E3FC),
            onPrimaryContainer = Color(0xFF041E49),
            secondary = Color(0xFF5F6368),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF1F3F4),
            onSecondaryContainer = Color(0xFF202124),
            tertiary = Color(0xFF188038),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFCEEAD6),
            onTertiaryContainer = Color(0xFF0D652D),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1A1A1A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            surfaceVariant = Color(0xFFF1F3F4),
            onSurfaceVariant = Color(0xFF3C4043),
            surfaceContainer = Color(0xFFF8F9FA),
            surfaceContainerHighest = Color(0xFFE8EAED),
            outline = Color(0xFFDADCE0),
            outlineVariant = Color(0xFFE8EAED),
        )

        private val DarkColorScheme = darkColorScheme(
            primary = Color(0xFF8AB4F8),
            onPrimary = Color(0xFF041E49),
            primaryContainer = Color(0xFF174EA6),
            onPrimaryContainer = Color(0xFFD2E3FC),
            secondary = Color(0xFF9AA0A6),
            onSecondary = Color(0xFF202124),
            secondaryContainer = Color(0xFF303134),
            onSecondaryContainer = Color(0xFFE8EAED),
            tertiary = Color(0xFF81C995),
            onTertiary = Color(0xFF0D652D),
            tertiaryContainer = Color(0xFF137333),
            onTertiaryContainer = Color(0xFFCEEAD6),
            background = Color(0xFF202124),
            onBackground = Color(0xFFE8EAED),
            surface = Color(0xFF202124),
            onSurface = Color(0xFFE8EAED),
            surfaceVariant = Color(0xFF303134),
            onSurfaceVariant = Color(0xFFBDC1C6),
            surfaceContainer = Color(0xFF292A2D),
            surfaceContainerHighest = Color(0xFF3C4043),
            outline = Color(0xFF5F6368),
            outlineVariant = Color(0xFF3C4043),
        )

        private val OledColorScheme = darkColorScheme(
            primary = Color(0xFF90CAF9),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF1565C0),
            onPrimaryContainer = Color(0xFFE3F2FD),
            secondary = Color(0xFFB0BEC5),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFF1A1A1A),
            onSecondaryContainer = Color(0xFFECEFF1),
            tertiary = Color(0xFFA5D6A7),
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFF2E7D32),
            onTertiaryContainer = Color(0xFFE8F5E9),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF5F5F5),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFF5F5F5),
            surfaceVariant = Color(0xFF121212),
            onSurfaceVariant = Color(0xFFCCCCCC),
            surfaceContainer = Color(0xFF0A0A0A),
            surfaceContainerHighest = Color(0xFF222222),
            outline = Color(0xFF424242),
            outlineVariant = Color(0xFF212121),
        )

        private val MintColorScheme = lightColorScheme(
            primary = Color(0xFF2E6E34),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD2E8D2),
            onPrimaryContainer = Color(0xFF0C240F),
            secondary = Color(0xFF4A634B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD4E5D4),
            onSecondaryContainer = Color(0xFF1B2F1D),
            tertiary = Color(0xFF386567),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFBCEBEB),
            onTertiaryContainer = Color(0xFF002021),
            background = Color(0xFFEAF2E8),
            onBackground = Color(0xFF1E3520),
            surface = Color(0xFFEAF2E8),
            onSurface = Color(0xFF1E3520),
            surfaceVariant = Color(0xFFDBE8D9),
            onSurfaceVariant = Color(0xFF3B593E),
            surfaceContainer = Color(0xFFE2EEE0),
            surfaceContainerHighest = Color(0xFFD1E2CF),
            outline = Color(0xFFA2BFA0),
            outlineVariant = Color(0xFFC8DEC6),
        )
    }
}
