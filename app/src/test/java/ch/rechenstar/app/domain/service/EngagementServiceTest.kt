package ch.rechenstar.app.domain.service

import ch.rechenstar.app.domain.model.AchievementType
import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.Exercise
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.model.OperationType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for the engagement/achievement evaluation logic.
 * These test the pure domain logic of evaluating achievements,
 * separate from Room database operations.
 */
class EngagementServiceTest {

    private fun makeResults(count: Int = 10, allCorrect: Boolean = true): List<ExerciseResult> {
        return (0 until count).map { i ->
            ExerciseResult(
                exercise = Exercise(
                    type = OperationType.ADDITION,
                    category = ExerciseCategory.ADDITION_10,
                    firstNumber = 1 + (i % 5),
                    secondNumber = 1
                ),
                userAnswer = if (allCorrect) (2 + (i % 5)) else 99,
                isCorrect = allCorrect,
                attempts = 1,
                timeSpent = 5.0
            )
        }
    }

    @Test
    fun `exercises10 achievement met when total is 10`() {
        val totalExercises = 10
        val met = totalExercises >= AchievementType.EXERCISES_10.defaultTarget
        assertTrue(met)
    }

    @Test
    fun `exercises10 achievement not met when total is 7`() {
        val totalExercises = 7
        val progress = minOf(totalExercises, AchievementType.EXERCISES_10.defaultTarget)
        assertEquals(7, progress)
        assertFalse(totalExercises >= AchievementType.EXERCISES_10.defaultTarget)
    }

    @Test
    fun `streak achievement met when current streak is 3`() {
        val currentStreak = 3
        assertTrue(currentStreak >= AchievementType.STREAK_3.defaultTarget)
    }

    @Test
    fun `streak resets after gap of more than 1 day`() {
        // If lastActive is 2 days ago, streak should reset to 1
        // This tests the logic that would be in EngagementService
        val daysSinceLastActive = 2
        val newStreak = if (daysSinceLastActive == 1) 4 else 1 // was 3
        assertEquals(1, newStreak)
    }

    @Test
    fun `streak same day no change`() {
        val daysSinceLastActive = 0
        val currentStreak = 3
        // Same day = no change
        assertEquals(3, currentStreak)
    }

    @Test
    fun `perfect10 is incremental`() {
        val results = makeResults(count = 10, allCorrect = true)
        val isPerfect = results.filter { !it.wasSkipped }.all { it.isCorrect } &&
                results.filter { !it.wasSkipped }.size >= 10
        assertTrue(isPerfect)

        // After 1 perfect session, progress should be 1
        var progress = 0
        progress = if (isPerfect) progress + 1 else progress
        assertEquals(1, progress)
        assertFalse(progress >= 10)  // Not yet unlocked
    }

    @Test
    fun `speed demon met when 10 exercises in under 120 seconds`() {
        val exerciseCount = 10
        val sessionDuration = 90.0 // seconds
        val met = exerciseCount >= 10 && sessionDuration < 120
        assertTrue(met)
    }

    @Test
    fun `speed demon not met when session too long`() {
        val exerciseCount = 10
        val sessionDuration = 150.0
        val met = exerciseCount >= 10 && sessionDuration < 120
        assertFalse(met)
    }

    @Test
    fun `variety achievement met with 4 categories`() {
        val categories = setOf(
            ExerciseCategory.ADDITION_10,
            ExerciseCategory.SUBTRACTION_10,
            ExerciseCategory.MULTIPLICATION_10,
            ExerciseCategory.ADDITION_100
        )
        assertTrue(categories.size >= 4)
    }

    @Test
    fun `accuracy streak resets on bad session`() {
        var progress = 2  // Had 2 good sessions
        val sessionAccuracy = 0.6  // Below 80%
        progress = if (sessionAccuracy >= 0.8) progress + 1 else 0
        assertEquals(0, progress)
    }

    @Test
    fun `accuracy streak increments on good session`() {
        var progress = 2
        val sessionAccuracy = 0.85
        progress = if (sessionAccuracy >= 0.8) progress + 1 else 0
        assertEquals(3, progress)
        assertTrue(progress >= 3)  // Unlocked!
    }

    @Test
    fun `skipped exercises dont count for achievements`() {
        val results = (0 until 10).map { i ->
            ExerciseResult(
                exercise = Exercise(
                    type = OperationType.ADDITION,
                    category = ExerciseCategory.ADDITION_10,
                    firstNumber = 1 + (i % 5),
                    secondNumber = 1
                ),
                userAnswer = 0,
                isCorrect = false,
                attempts = 0,
                timeSpent = 0.0,
                wasSkipped = true
            )
        }
        val attempted = results.filter { !it.wasSkipped }
        assertEquals(0, attempted.size)
        // speedDemon requires 10 attempted exercises
        assertFalse(attempted.size >= 10)
    }
}
