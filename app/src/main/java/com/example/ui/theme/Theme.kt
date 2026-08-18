package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class CustomColors(val warning: Color)
val LocalCustomColors = staticCompositionLocalOf { CustomColors(Color.Unspecified) }

val ColorScheme.warning: Color
    @Composable
    get() = LocalCustomColors.current.warning

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = PrimaryVariant,
    tertiary = AccentPink,
    background = DarkAppBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorBackground,
    onErrorContainer = ErrorRed,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderColor
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = PrimaryVariant,
    tertiary = AccentPink,
    background = AppBackground,
    surface = Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorBackground,
    onErrorContainer = ErrorRed,
    surfaceVariant = BorderColor,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val customColors = CustomColors(warning = WarningOrange)

  CompositionLocalProvider(LocalCustomColors provides customColors) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
