package com.example.dailyquotes.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Soft pastel palette: lavender primary, mint secondary, peach tertiary.
private val LightColors = lightColorScheme(
    primary = Color(0xFF7B68C2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEAE4F8),
    onPrimaryContainer = Color(0xFF2D2352),
    secondary = Color(0xFF6FAE98),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDF2EA),
    onSecondaryContainer = Color(0xFF17392F),
    tertiary = Color(0xFFE9A88F),
    tertiaryContainer = Color(0xFFFBE3DA),
    background = Color(0xFFF8F6FC),
    onBackground = Color(0xFF1F1D27),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1D27),
    surfaceVariant = Color(0xFFF0EDF6),
    onSurfaceVariant = Color(0xFF5C5770),
    outline = Color(0xFFCBC4DB),
    error = Color(0xFFBA3D4F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC6B9F2),
    onPrimary = Color(0xFF2B2050),
    primaryContainer = Color(0xFF3D3462),
    onPrimaryContainer = Color(0xFFEAE4F8),
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

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun DailyQuotesTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )

    // Keep status and navigation bar icons readable when the app theme differs from the system theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
