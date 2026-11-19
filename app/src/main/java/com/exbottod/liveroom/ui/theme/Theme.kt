package com.exbottod.liveroom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandPrimary = Color(0xFF7C4DFF)
val BrandAccent = Color(0xFFFF4D8D)
val SurfaceDark = Color(0xFF0E0B16)
val SurfaceCard = Color(0xFF1A1526)

private val DarkColors = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandAccent,
    background = SurfaceDark,
    surface = SurfaceCard,
    onPrimary = Color.White,
    onBackground = Color(0xFFF3EFFA),
    onSurface = Color(0xFFF3EFFA),
)

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandAccent,
)

@Composable
fun LiveRoomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
