package com.sk.gallery.photo.album.ui.theme

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

enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

private val DarkColorScheme =
  darkColorScheme(
    primary = GalleryBluePrimaryDark,
    onPrimary = GalleryBlueOnPrimaryDark,
    primaryContainer = GalleryBlueContainerDark,
    onPrimaryContainer = GalleryBlueOnContainerDark,
    secondary = GallerySecondaryDark,
    secondaryContainer = GallerySecondaryContainerDark,
    background = GalleryBackgroundDark,
    surface = GallerySurfaceDark,
    surfaceVariant = GallerySurfaceVariantDark,
    onSurface = GalleryOnSurfaceDark,
    outline = GalleryOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GalleryBluePrimaryLight,
    onPrimary = GalleryBlueOnPrimaryLight,
    primaryContainer = GalleryBlueContainerLight,
    onPrimaryContainer = GalleryBlueOnContainerLight,
    secondary = GallerySecondaryLight,
    secondaryContainer = GallerySecondaryContainerLight,
    background = GalleryBackgroundLight,
    surface = GallerySurfaceLight,
    surfaceVariant = GallerySurfaceVariantLight,
    onSurface = GalleryOnSurfaceLight,
    outline = GalleryOutlineLight
  )

@Composable
fun GalleryTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val darkTheme = when (themeMode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
