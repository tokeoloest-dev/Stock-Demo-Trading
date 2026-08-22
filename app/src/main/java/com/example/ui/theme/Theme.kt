package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppTheme

private val SlateProDarkColorScheme = darkColorScheme(
    primary = SlateProPrimary,
    onPrimary = Color.White,
    primaryContainer = SlateProSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = SlateProAccent,
    onSecondary = Color.Black,
    background = SlateProBg,
    onBackground = Color(0xFFE6EDF3),
    surface = SlateProSurface,
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = SlateProSurfaceVariant,
    onSurfaceVariant = Color(0xFF8B949E),
    outline = Color(0xFF30363D)
)

private val PitchBlackColorScheme = darkColorScheme(
    primary = PitchBlackPrimary,
    onPrimary = Color.Black,
    primaryContainer = PitchBlackSurfaceVariant,
    onPrimaryContainer = PitchBlackPrimary,
    secondary = Color(0xFF00B0FF),
    onSecondary = Color.Black,
    background = PitchBlackBg,
    onBackground = PitchBlackText,
    surface = PitchBlackSurface,
    onSurface = PitchBlackText,
    surfaceVariant = PitchBlackSurfaceVariant,
    onSurfaceVariant = PitchBlackTextSecondary,
    outline = Color(0xFF2C2C2C)
)

private val PureWhiteColorScheme = lightColorScheme(
    primary = PureWhitePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = StockGreenDark,
    onSecondary = Color.White,
    background = PureWhiteBg,
    onBackground = PureWhiteText,
    surface = PureWhiteSurface,
    onSurface = PureWhiteText,
    surfaceVariant = PureWhiteSurfaceVariant,
    onSurfaceVariant = PureWhiteTextSecondary,
    outline = Color(0xFFE2E8F0)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF34D399),
    onSecondary = Color.Black,
    background = EmeraldBg,
    onBackground = Color(0xFFECFDF5),
    surface = EmeraldSurface,
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF064E3B)
)

private val SapphireColorScheme = darkColorScheme(
    primary = SapphirePrimary,
    onPrimary = Color.Black,
    primaryContainer = SapphireSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF90E0EF),
    onSecondary = Color.Black,
    background = SapphireBg,
    onBackground = Color(0xFFF0F8FF),
    surface = SapphireSurface,
    onSurface = Color(0xFFF0F8FF),
    surfaceVariant = SapphireSurfaceVariant,
    onSurfaceVariant = Color(0xFF8E9AAF),
    outline = Color(0xFF3A506B)
)

@Composable
fun StockTradingTheme(
    appTheme: AppTheme = AppTheme.SLATE_PRO,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (!darkTheme) {
        PureWhiteColorScheme
    } else {
        when (appTheme) {
            AppTheme.PITCH_BLACK -> PitchBlackColorScheme
            AppTheme.PURE_WHITE -> PureWhiteColorScheme
            AppTheme.EMERALD -> EmeraldColorScheme
            AppTheme.SAPPHIRE -> SapphireColorScheme
            AppTheme.SLATE_PRO -> SlateProDarkColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
