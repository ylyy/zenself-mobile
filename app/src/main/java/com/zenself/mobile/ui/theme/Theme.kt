package com.zenself.mobile.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Zen / calm palette — light theme only. */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B4C8A),        // Dark purple accent
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8D5F5),
    onPrimaryContainer = Color(0xFF2E1538),

    secondary = Color(0xFF9B7DB8),       // Lighter purple
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0E4F7),
    onSecondaryContainer = Color(0xFF341048),

    tertiary = Color(0xFFB8A0C8),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFF7F3FA),      // Soft, warm lavender-white
    onBackground = Color(0xFF1E1A24),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1A24),
    surfaceVariant = Color(0xFFEDE5F2),
    onSurfaceVariant = Color(0xFF494458),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF7B7489),
    outlineVariant = Color(0xFFCDC4D9),
)

/** Dark variant (not used by default, but available). */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4B8EA),
    onPrimary = Color(0xFF3F2852),
    primaryContainer = Color(0xFF563F70),
    onPrimaryContainer = Color(0xFFF0DDFF),

    secondary = Color(0xFFD0BDE3),
    onSecondary = Color(0xFF3F2852),
    secondaryContainer = Color(0xFF563F70),
    onSecondaryContainer = Color(0xFFF0DDFF),

    background = Color(0xFF141218),
    onBackground = Color(0xFFECE0F4),

    surface = Color(0xFF141218),
    onSurface = Color(0xFFECE0F4),
    surfaceVariant = Color(0xFF494458),
    onSurfaceVariant = Color(0xFFCDC4D9),

    outline = Color(0xFF9690A5),
    outlineVariant = Color(0xFF494458),
)

@Composable
fun ZenSelfTheme(
    content: @Composable () -> Unit,
) {
    // Light-only as specified. Dark fallback kept for composability.
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
