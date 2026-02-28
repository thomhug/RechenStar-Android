package ch.rechenstar.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AppSkyBlue,
    secondary = AppPurple,
    tertiary = AppOrange,
    background = LightBackground,
    surface = LightCardBackground,
    error = AppCoral,
    onPrimary = AppWhite,
    onSecondary = AppWhite,
    onTertiary = AppWhite,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onError = AppWhite,
    outline = LightBorder,
    surfaceVariant = AppLightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = AppSkyBlue,
    secondary = AppPurple,
    tertiary = AppOrange,
    background = DarkBackground,
    surface = DarkCardBackground,
    error = AppCoral,
    onPrimary = AppWhite,
    onSecondary = AppWhite,
    onTertiary = AppWhite,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onError = AppWhite,
    outline = DarkBorder,
    surfaceVariant = DarkCardBackground
)

@Composable
fun RechenStarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RechenStarTypography,
        content = content
    )
}
