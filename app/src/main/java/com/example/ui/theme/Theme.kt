package com.example.ui.theme

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

private val GlobalWhiteColorScheme =
  lightColorScheme(
    primary = WarmMahogany,
    secondary = GoldAccent,
    tertiary = AccentSuccess,
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = TextDark,
    onTertiary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = TextMuted,
    outline = CardBorderGray,
    outlineVariant = CardBorderGray,
    primaryContainer = PureWhite,
    onPrimaryContainer = TextDark,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = TextDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = GlobalWhiteColorScheme, typography = Typography, content = content)
}

