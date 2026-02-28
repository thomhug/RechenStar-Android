package ch.rechenstar.app.domain.model

enum class ExerciseCategory(val rawValue: String) {
    ADDITION_10("addition_10"),
    ADDITION_100("addition_100"),
    SUBTRACTION_10("subtraction_10"),
    SUBTRACTION_100("subtraction_100"),
    MULTIPLICATION_10("multiplication_10"),
    MULTIPLICATION_100("multiplication_100");

    val label: String
        get() = when (this) {
            ADDITION_10 -> "Addition bis 10"
            ADDITION_100 -> "Addition bis 100"
            SUBTRACTION_10 -> "Subtraktion bis 10"
            SUBTRACTION_100 -> "Subtraktion bis 100"
            MULTIPLICATION_10 -> "Kleines 1\u00D71"
            MULTIPLICATION_100 -> "Grosses 1\u00D71"
        }

    val operationType: OperationType
        get() = when (this) {
            ADDITION_10, ADDITION_100 -> OperationType.ADDITION
            SUBTRACTION_10, SUBTRACTION_100 -> OperationType.SUBTRACTION
            MULTIPLICATION_10, MULTIPLICATION_100 -> OperationType.MULTIPLICATION
        }

    val groupLabel: String
        get() = when (this) {
            ADDITION_10, ADDITION_100 -> "Addition"
            SUBTRACTION_10, SUBTRACTION_100 -> "Subtraktion"
            MULTIPLICATION_10, MULTIPLICATION_100 -> "Multiplikation"
        }

    companion object {
        fun fromRawValue(raw: String): ExerciseCategory? =
            entries.find { it.rawValue == raw }
    }
}
