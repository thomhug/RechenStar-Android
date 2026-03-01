package ch.rechenstar.app.domain.model

enum class Level(val order: Int, val requiredExercises: Int) {
    ANFAENGER(1, 0),
    RECHENKIND(2, 25),
    ZAHLENFUCHS(3, 75),
    RECHENPROFI(4, 150),
    MATHE_HELD(5, 300),
    ZAHLENKOENIG(6, 500),
    RECHENSTAR(7, 1000),
    RECHENSTAR_2(8, 1500),
    RECHENSTAR_3(9, 2000),
    RECHENSTAR_4(10, 3000),
    RECHENSTAR_5(11, 5000);

    val title: String
        get() = when (this) {
            ANFAENGER -> "Anfänger"
            RECHENKIND -> "Rechenkind"
            ZAHLENFUCHS -> "Zahlenfuchs"
            RECHENPROFI -> "Rechenprofi"
            MATHE_HELD -> "Mathe-Held"
            ZAHLENKOENIG -> "Zahlenkönig"
            RECHENSTAR -> "RechenStar"
            RECHENSTAR_2 -> "RechenStar 2"
            RECHENSTAR_3 -> "RechenStar 3"
            RECHENSTAR_4 -> "RechenStar 4"
            RECHENSTAR_5 -> "RechenStar 5"
        }

    val imageName: String
        get() = when (this) {
            ANFAENGER -> "level_anfaenger"
            RECHENKIND -> "level_rechenkind"
            ZAHLENFUCHS -> "level_zahlenfuchs"
            RECHENPROFI -> "level_rechenprofi"
            MATHE_HELD -> "level_mathe_held"
            ZAHLENKOENIG -> "level_zahlenkoenig"
            RECHENSTAR, RECHENSTAR_2, RECHENSTAR_3, RECHENSTAR_4, RECHENSTAR_5 -> "level_rechenstar"
        }

    val nextLevelExercises: Int?
        get() {
            val allLevels = entries
            val currentIndex = allLevels.indexOf(this)
            return if (currentIndex + 1 < allLevels.size) {
                allLevels[currentIndex + 1].requiredExercises
            } else null
        }

    companion object {
        fun current(totalExercises: Int): Level =
            entries.lastOrNull { totalExercises >= it.requiredExercises } ?: ANFAENGER

        fun progress(totalExercises: Int): Double {
            val level = current(totalExercises)
            val nextRequired = level.nextLevelExercises ?: return 1.0
            val levelStart = level.requiredExercises
            return (totalExercises - levelStart).toDouble() / (nextRequired - levelStart).toDouble()
        }
    }
}
