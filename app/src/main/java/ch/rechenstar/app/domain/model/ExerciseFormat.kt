package ch.rechenstar.app.domain.model

enum class ExerciseFormat(val rawValue: String) {
    STANDARD("standard"),    // "3 + 4 = ?"  → Antwort: Ergebnis
    FIRST_GAP("firstGap"),   // "? + 4 = 7"  → Antwort: erster Operand
    SECOND_GAP("secondGap"); // "3 + ? = 7"  → Antwort: zweiter Operand

    companion object {
        fun fromRawValue(raw: String): ExerciseFormat =
            entries.first { it.rawValue == raw }
    }
}
