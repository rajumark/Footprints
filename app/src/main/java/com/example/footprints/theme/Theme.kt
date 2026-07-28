package com.example.footprints.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
  primary = Primary,
  onPrimary = OnPrimary,
  primaryContainer = Primary,
  onPrimaryContainer = OnPrimary,
  secondary = Primary,
  onSecondary = OnPrimary,
  secondaryContainer = Surface,
  onSecondaryContainer = OnSurface,
  background = PageBg,
  onBackground = OnSurface,
  surface = Surface,
  onSurface = OnSurface,
  surfaceVariant = Surface,
  onSurfaceVariant = OnSurface,
  outline = Border,
  outlineVariant = Border,
  inverseSurface = OnSurface,
  inverseOnSurface = Surface,
  error = Color(0xFFA73D3D),
)

private val DarkScheme = darkColorScheme(
  primary = DarkPrimary,
  onPrimary = DarkOnPrimary,
  primaryContainer = DarkPrimary,
  onPrimaryContainer = DarkOnPrimary,
  secondary = DarkPrimary,
  onSecondary = DarkOnPrimary,
  secondaryContainer = DarkSurface,
  onSecondaryContainer = DarkOnSurface,
  background = DarkPageBg,
  onBackground = DarkOnSurface,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurface,
  onSurfaceVariant = DarkOnSurface,
  outline = DarkBorder,
  outlineVariant = DarkBorder,
  inverseSurface = DarkOnSurface,
  inverseOnSurface = DarkSurface,
  error = Color(0xFFCF6679),
)

var AppDarkTheme by mutableStateOf(false)

@Composable
fun FootprintsTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val useDark = if (AppDarkTheme) true else darkTheme
  MaterialTheme(
    colorScheme = if (useDark) DarkScheme else LightScheme,
    typography = Typography,
    content = content,
  )
}
