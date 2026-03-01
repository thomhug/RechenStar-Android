package ch.rechenstar.app.domain.model

enum class Difficulty(val level: Int) {
    VERY_EASY(1),
    EASY(2),
    MEDIUM(3),
    HARD(4);

    val range: IntRange
        get() = when (this) {
            VERY_EASY -> 1..3
            EASY -> 1..5
            MEDIUM -> 2..7
            HARD -> 2..9
        }

    val range100: IntRange
        get() = when (this) {
            VERY_EASY -> 1..20
            EASY -> 1..40
            MEDIUM -> 2..70
            HARD -> 2..99
        }

    val maxProduct: Int
        get() = when (this) {
            VERY_EASY -> 50
            EASY -> 100
            MEDIUM -> 200
            HARD -> 400
        }

    val label: String
        get() = when (this) {
            VERY_EASY -> "Sehr leicht"
            EASY -> "Leicht"
            MEDIUM -> "Mittel"
            HARD -> "Schwer"
        }

    val skillTitle: String
        get() = when (this) {
            VERY_EASY -> "Entdecker"
            EASY -> "Kenner"
            MEDIUM -> "Könner"
            HARD -> "Meister"
        }

    val skillImageName: String
        get() = when (this) {
            VERY_EASY -> "skill_entdecker"
            EASY -> "skill_kenner"
            MEDIUM -> "skill_könner"
            HARD -> "skill_meister"
        }

    companion object {
        fun fromLevel(level: Int): Difficulty =
            entries.first { it.level == level }

        fun fromRawValue(rawValue: Int): Difficulty =
            entries.find { it.level == rawValue } ?: EASY
    }
}
