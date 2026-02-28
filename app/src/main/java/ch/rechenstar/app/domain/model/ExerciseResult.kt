package ch.rechenstar.app.domain.model

import java.util.UUID

data class ExerciseResult(
    val id: UUID = UUID.randomUUID(),
    val exercise: Exercise,
    val userAnswer: Int,
    val isCorrect: Boolean,
    val attempts: Int,
    val timeSpent: Double,
    val wasRevealed: Boolean = false,
    val wasSkipped: Boolean = false
) {
    val stars: Int
        get() {
            if (!isCorrect) return 0
            return when (attempts) {
                1 -> 2
                else -> 1
            }
        }
}
