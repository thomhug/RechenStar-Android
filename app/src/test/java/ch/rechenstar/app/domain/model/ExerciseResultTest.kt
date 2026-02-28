package ch.rechenstar.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExerciseResultTest {

    private fun makeExercise(
        type: OperationType = OperationType.ADDITION,
        first: Int = 3,
        second: Int = 4
    ) = Exercise(
        type = type,
        category = ExerciseCategory.ADDITION_10,
        firstNumber = first,
        secondNumber = second
    )

    @Test
    fun `first try correct gives 2 stars`() {
        val result = ExerciseResult(
            exercise = makeExercise(),
            userAnswer = 7,
            isCorrect = true,
            attempts = 1,
            timeSpent = 2.0
        )
        assertEquals(2, result.stars)
    }

    @Test
    fun `second try correct gives 1 star`() {
        val result = ExerciseResult(
            exercise = makeExercise(),
            userAnswer = 7,
            isCorrect = true,
            attempts = 2,
            timeSpent = 5.0
        )
        assertEquals(1, result.stars)
    }

    @Test
    fun `incorrect gives 0 stars`() {
        val result = ExerciseResult(
            exercise = makeExercise(),
            userAnswer = 5,
            isCorrect = false,
            attempts = 2,
            timeSpent = 8.0
        )
        assertEquals(0, result.stars)
    }

    @Test
    fun `skipped gives 0 stars`() {
        val result = ExerciseResult(
            exercise = makeExercise(),
            userAnswer = 0,
            isCorrect = false,
            attempts = 0,
            timeSpent = 1.0,
            wasSkipped = true
        )
        assertEquals(0, result.stars)
    }

    @Test
    fun `revealed gives 0 stars`() {
        val result = ExerciseResult(
            exercise = makeExercise(),
            userAnswer = 7,
            isCorrect = false,
            attempts = 2,
            timeSpent = 10.0,
            wasRevealed = true
        )
        assertEquals(0, result.stars)
    }
}
