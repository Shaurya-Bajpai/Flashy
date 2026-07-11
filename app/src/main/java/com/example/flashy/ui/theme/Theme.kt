package com.dsb.flashy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlashDarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1A0D00),
    primaryContainer = AmberDark,
    onPrimaryContainer = AmberLight,
    secondary = ColorCall,
    onSecondary = Color(0xFF001F14),
    tertiary = ColorSms,
    onTertiary = Color(0xFF1A0040),
    background = BgVoid,
    onBackground = TextWarm,
    surface = BgDark,
    onSurface = TextWarm,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextMuted,
    error = ColorDanger,
    onError = Color.White,
    outline = GlassBorderTop,
    outlineVariant = GlassBorderBot,
)

@Composable
fun FlashyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlashDarkColors,
        typography = AppTypography,
        content = content
    )
}
