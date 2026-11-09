package com.example.dailyquotes.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.dailyquotes.data.Accent
import com.example.dailyquotes.data.ThemeMode

/** The colours that change with the accent choice. */
data class AccentPalette(
    val lightPrimary: Color,
    val lightContainer: Color,
    val lightOnContainer: Color,
    val darkPrimary: Color,
    val darkContainer: Color,
    val darkOnContainer: Color
)

fun accentPalette(accent: Accent): AccentPalette = when (accent) {
    Accent.LAVENDER -> AccentPalette(
        lightPrimary = Color(0xFF7B68C2),
        lightContainer = Color(0xFFEAE4F8),
        lightOnContainer = Color(0xFF2D2352),
        darkPrimary = Color(0xFFC6B9F2),
        darkContainer = Color(0xFF3D3462),
        darkOnContainer = Color(0xFFEAE4F8)
    )
    Accent.MINT -> AccentPalette(
        lightPrimary = Color(0xFF3E8E74),
        lightContainer = Color(0xFFDDF2EA),
        lightOnContainer = Color(0xFF17392F),
        darkPrimary = Color(0xFF9ED6BE),
        darkContainer = Color(0xFF244A3E),
        darkOnContainer = Color(0xFFDDF2EA)
    )
    Accent.PEACH -> AccentPalette(
        lightPrimary = Color(0xFFC0684D),
        lightContainer = Color(0xFFFBE3DA),
        lightOnContainer = Color(0xFF5A2A1A),
        darkPrimary = Color(0xFFF0BBA8),
        darkContainer = Color(0xFF5A3D35),
        darkOnContainer = Color(0xFFFBE3DA)
    )
}

private fun lightScheme(accent: Accent): ColorScheme {
    val palette = accentPalette(accent)
    return lightColorScheme(
        primary = palette.lightPrimary,
        onPrimary = Color.White,
        primaryContainer = palette.lightContainer,
        onPrimaryContainer = palette.lightOnContainer,
        secondary = Color(0xFF6FAE98),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFDDF2EA),
        onSecondaryContainer = Color(0xFF17392F),
        tertiary = Color(0xFFE9A88F),
        tertiaryContainer = Color(0xFFFBE3DA),
        background = Color(0xFFF8F6FC),
        onBackground = Color(0xFF1F1D27),
        surface = Color.White,
        onSurface = Color(0xFF1F1D27),
        surfaceVariant = Color(0xFFF0EDF6),
        onSurfaceVariant = Color(0xFF5C5770),
        outline = Color(0xFFCBC4DB),
        error = Color(0xFFBA3D4F)
    )
}

private fun darkScheme(accent: Accent): ColorScheme {
    val palette = accentPalette(accent)
    return darkColorScheme(
        primary = palette.darkPrimary,
        onPrimary = Color(0xFF2B2050),
        primaryContainer = palette.darkContainer,
        onPrimaryContainer = palette.darkOnContainer,
        secondary = Color(0xFF9ED6BE),
        onSecondary = Color(0xFF103127),
        secondaryContainer = Color(0xFF244A3E),
        onSecondaryContainer = Color(0xFFDDF2EA),
        tertiary = Color(0xFFF0BBA8),
        tertiaryContainer = Color(0xFF5A3D35),
        background = Color(0xFF141319),
        onBackground = Color(0xFFE8E5EE),
        surface = Color(0xFF1D1B25),
        onSurface = Color(0xFFE8E5EE),
        surfaceVariant = Color(0xFF2A2734),
        onSurfaceVariant = Color(0xFFBDB7CC),
        outline = Color(0xFF4A465C),
        error = Color(0xFFFF8A98)
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun DailyQuotesTheme(themeMode: ThemeMode, accent: Accent, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (dark) darkScheme(accent) else lightScheme(accent),
        shapes = AppShapes,
        content = content
    )

    // Keep status and navigation bar icons readable when the app theme differs from the system theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
}
