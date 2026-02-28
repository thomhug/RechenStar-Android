package ch.rechenstar.app.domain.model

import java.time.Instant
import java.util.UUID

data class Exercise(
    val id: UUID = UUID.randomUUID(),
    val type: OperationType,
    val category: ExerciseCategory,
    val firstNumber: Int,
    val secondNumber: Int,
    val difficulty: Difficulty = Difficulty.EASY,
    val format: ExerciseFormat = ExerciseFormat.STANDARD,
    val isRetry: Boolean = false,
    val createdAt: Instant = Instant.now()
) {
    val correctAnswer: Int
        get() = when (format) {
            ExerciseFormat.STANDARD -> when (type) {
                OperationType.ADDITION -> firstNumber + secondNumber
                OperationType.SUBTRACTION -> firstNumber - secondNumber
                OperationType.MULTIPLICATION -> firstNumber * secondNumber
            }
            ExerciseFormat.FIRST_GAP -> firstNumber
            ExerciseFormat.SECOND_GAP -> secondNumber
        }

    val operationResult: Int
        get() = when (type) {
            OperationType.ADDITION -> firstNumber + secondNumber
            OperationType.SUBTRACTION -> firstNumber - secondNumber
            OperationType.MULTIPLICATION -> firstNumber * secondNumber
        }

    data class DisplayNumbers(val left: String, val right: String, val result: String)

    val displayNumbers: DisplayNumbers
        get() = when (format) {
            ExerciseFormat.STANDARD ->
                DisplayNumbers("$firstNumber", "$secondNumber", "?")
            ExerciseFormat.FIRST_GAP ->
                DisplayNumbers("?", "$secondNumber", "$operationResult")
            ExerciseFormat.SECOND_GAP ->
                DisplayNumbers("$firstNumber", "?", "$operationResult")
        }

    val displayText: String
        get() {
            val d = displayNumbers
            return "${d.left} ${type.symbol} ${d.right} = ${d.result}"
        }

    val signature: String
        get() = "${category.rawValue}_${firstNumber}_${secondNumber}_${format.rawValue}"
}
