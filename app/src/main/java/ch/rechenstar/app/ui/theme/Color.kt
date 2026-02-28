package ch.rechenstar.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors (constant across themes)
val AppSkyBlue = Color(0xFF4A90E2)
val AppSunYellow = Color(0xFFF5D547)
val AppGrassGreen = Color(0xFF7ED321)
val AppCoral = Color(0xFFFF6B6B)
val AppPurple = Color(0xFF9B59B6)
val AppOrange = Color(0xFFFFA500)

// Neutral Colors
val AppDarkGray = Color(0xFF2C3E50)
val AppLightGray = Color(0xFFECF0F1)
val AppWhite = Color(0xFFFFFFFF)

// Semantic aliases
val AppSuccess = AppGrassGreen
val AppWarning = AppSunYellow
val AppError = AppCoral
val AppInfo = AppSkyBlue

// Light Mode
val LightBackground = Color(0xFFF7F9FC)
val LightBackgroundBottom = Color(0xFFFFFFFF)
val LightCardBackground = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF2C3E50)
val LightTextSecondary = Color(0xFF7F8C8D)
val LightBorder = Color(0xFFE1E4E8)

// Dark Mode
val DarkBackground = Color(0xFF1A1A2E)
val DarkBackgroundBottom = Color(0xFF16213E)
val DarkCardBackground = Color(0xFF1E2A45)
val DarkTextPrimary = Color(0xFFE8E8E8)
val DarkTextSecondary = Color(0xFFA0A0B0)
val DarkBorder = Color(0xFF2E3A52)

// Color Blind Safe Palettes
object ProtanopiaColors {
    val primary = Color(0xFF0072B2)
    val success = Color(0xFF56B4E9)
    val warning = Color(0xFFE69F00)
    val error = Color(0xFFCC79A7)
}

object DeuteranopiaColors {
    val primary = Color(0xFF0072B2)
    val success = Color(0xFF009E73)
    val warning = Color(0xFFE69F00)
    val error = Color(0xFFCC79A7)
}

object TritanopiaColors {
    val primary = Color(0xFFD55E00)
    val success = Color(0xFF009E73)
    val warning = Color(0xFFF0E442)
    val error = Color(0xFFCC79A7)
}

// Star colors
val StarGold = Color(0xFFFFD700)
val StarEmpty = Color(0xFFE0E0E0)
