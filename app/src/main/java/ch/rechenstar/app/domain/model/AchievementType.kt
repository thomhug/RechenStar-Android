package ch.rechenstar.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.ui.graphics.vector.ImageVector

enum class AchievementType(val rawValue: String) {
    EXERCISES_10("first_10"),
    EXERCISES_50("half_century"),
    EXERCISES_100("century"),
    EXERCISES_500("master_500"),

    STREAK_3("streak_3"),
    STREAK_7("week_warrior"),
    STREAK_30("month_master"),

    PERFECT_10("perfect_10"),
    ALL_STARS("star_collector"),

    SPEED_DEMON("speed_demon"),
    EARLY_BIRD("early_bird"),
    NIGHT_OWL("night_owl"),

    CATEGORY_MASTER("category_master"),
    VARIETY("variety"),
    ACCURACY_STREAK("accuracy_streak"),
    DAILY_CHAMPION("daily_champion");

    val title: String
        get() = when (this) {
            EXERCISES_10 -> "Erste Schritte"
            EXERCISES_50 -> "Halbes Hundert"
            EXERCISES_100 -> "Hunderter-Held"
            EXERCISES_500 -> "Mathe-Meister"
            STREAK_3 -> "3 Tage am Stueck"
            STREAK_7 -> "Wochen-Krieger"
            STREAK_30 -> "Monats-Meister"
            PERFECT_10 -> "Perfekte 10"
            ALL_STARS -> "Sterne-Sammler"
            SPEED_DEMON -> "Blitzrechner"
            EARLY_BIRD -> "Fruehaufsteher"
            NIGHT_OWL -> "Nachteule"
            CATEGORY_MASTER -> "Kategorie-Profi"
            VARIETY -> "Vielseitig"
            ACCURACY_STREAK -> "Treffsicher"
            DAILY_CHAMPION -> "Tages-Champion"
        }

    val description: String
        get() = when (this) {
            EXERCISES_10 -> "Loese 10 Aufgaben"
            EXERCISES_50 -> "Loese 50 Aufgaben"
            EXERCISES_100 -> "Loese 100 Aufgaben"
            EXERCISES_500 -> "Loese 500 Aufgaben"
            STREAK_3 -> "Uebe 3 Tage hintereinander"
            STREAK_7 -> "Uebe 7 Tage hintereinander"
            STREAK_30 -> "Uebe 30 Tage hintereinander"
            PERFECT_10 -> "10 perfekte Runden spielen"
            ALL_STARS -> "Sammle 100 Sterne"
            SPEED_DEMON -> "10 Aufgaben in 2 Minuten"
            EARLY_BIRD -> "Uebe vor 8 Uhr morgens"
            NIGHT_OWL -> "Uebe nach 20 Uhr abends"
            CATEGORY_MASTER -> "90%+ in einer Kategorie (min 20 Aufgaben)"
            VARIETY -> "4+ Kategorien in einer Runde"
            ACCURACY_STREAK -> "3 Runden mit 80%+ Genauigkeit"
            DAILY_CHAMPION -> "Loese 100 Aufgaben an einem Tag"
        }

    val defaultTarget: Int
        get() = when (this) {
            EXERCISES_10 -> 10
            EXERCISES_50 -> 50
            EXERCISES_100 -> 100
            EXERCISES_500 -> 500
            STREAK_3 -> 3
            STREAK_7 -> 7
            STREAK_30 -> 30
            PERFECT_10 -> 10
            ALL_STARS -> 100
            SPEED_DEMON -> 1
            EARLY_BIRD -> 1
            NIGHT_OWL -> 1
            CATEGORY_MASTER -> 1
            VARIETY -> 1
            ACCURACY_STREAK -> 3
            DAILY_CHAMPION -> 100
        }

    val icon: ImageVector
        get() = when (this) {
            EXERCISES_10 -> Icons.Filled.CheckCircle
            EXERCISES_50 -> Icons.Filled.CheckCircle
            EXERCISES_100 -> Icons.Filled.EmojiEvents
            EXERCISES_500 -> Icons.Filled.MilitaryTech
            STREAK_3 -> Icons.Filled.Favorite
            STREAK_7 -> Icons.Filled.Favorite
            STREAK_30 -> Icons.Filled.Favorite
            PERFECT_10 -> Icons.Filled.Star
            ALL_STARS -> Icons.Filled.Star
            SPEED_DEMON -> Icons.Filled.FlashOn
            EARLY_BIRD -> Icons.Filled.WbSunny
            NIGHT_OWL -> Icons.Filled.NightsStay
            CATEGORY_MASTER -> Icons.Filled.School
            VARIETY -> Icons.Filled.GridView
            ACCURACY_STREAK -> Icons.AutoMirrored.Filled.TrendingUp
            DAILY_CHAMPION -> Icons.Filled.EmojiEvents
        }

    companion object {
        fun fromRawValue(raw: String): AchievementType? =
            entries.find { it.rawValue == raw }
    }
}
