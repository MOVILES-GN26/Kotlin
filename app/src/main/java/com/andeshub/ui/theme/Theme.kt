package com.andeshub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import java.util.Calendar

private val LightColorScheme = lightColorScheme(
    primary = Yellow,
    secondary = MutedOlive,
    tertiary = Yellow,
    background = SoftCream,
    surface = LightNeutral,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    error = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = Yellow,
    secondary = MutedOlive,
    tertiary = Yellow,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Black,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White
)

fun isNightTime(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour >= 19 || hour < 6  // 7pm a 6am
}


@Composable
fun AndesHubTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (isNightTime()) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}