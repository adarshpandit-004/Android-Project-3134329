
package ie.example.spendmap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// SpendMap light theme
private val LightColorScheme = lightColorScheme(
    primary = SpendMapGreen,
    onPrimary = SpendMapSurface,

    secondary = SpendMapGreenDark,
    onSecondary = SpendMapSurface,

    tertiary = SpendMapOrange,
    onTertiary = SpendMapText,

    background = SpendMapBackground,
    onBackground = SpendMapText,

    surface = SpendMapSurface,
    onSurface = SpendMapText,

    surfaceVariant = SpendMapGreenLight,
    onSurfaceVariant = SpendMapTextSecondary,

    error = SpendMapRed,
    onError = SpendMapSurface
)

// SpendMap dark theme
private val DarkColorScheme = darkColorScheme(
    primary = SpendMapGreen,
    onPrimary = SpendMapSurface,

    secondary = SpendMapGreenLight,
    onSecondary = SpendMapDarkBackground,

    tertiary = SpendMapOrange,
    onTertiary = SpendMapDarkBackground,

    background = SpendMapDarkBackground,
    onBackground = SpendMapDarkText,

    surface = SpendMapDarkSurface,
    onSurface = SpendMapDarkText,

    surfaceVariant = SpendMapGreenDark,
    onSurfaceVariant = SpendMapGreenLight,

    error = SpendMapRed,
    onError = SpendMapSurface
)

@Composable
fun SpendMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
