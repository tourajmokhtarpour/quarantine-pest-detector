package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ElegantColorScheme = darkColorScheme(
    primary = ElegantAccent,
    onPrimary = ElegantBackground,
    primaryContainer = ElegantPrimaryForest,
    onPrimaryContainer = ElegantText,
    secondary = ElegantPrimaryForest,
    onSecondary = ElegantText,
    secondaryContainer = ElegantSurface,
    onSecondaryContainer = ElegantText,
    tertiary = ElegantMutedText,
    onTertiary = ElegantBackground,
    background = ElegantBackground,
    onBackground = ElegantText,
    surface = ElegantSurface,
    onSurface = ElegantText,
    onSurfaceVariant = ElegantMutedText,
    outline = ElegantBorder,
    error = ElegantAlert,
    onError = ElegantBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for the Elegant Dark aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // We always use the Elegant Dark Color Scheme to satisfy "Apply the Elegant Dark design theme to the app"
    val colorScheme = ElegantColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
