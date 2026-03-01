package ch.rechenstar.app.domain.service

import ch.rechenstar.app.domain.model.AchievementType
import ch.rechenstar.app.domain.model.Exercise
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.model.OperationType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for the engagement/achievement evaluation logic.
 * These test the pure domain logic via EngagementService methods directly.
 */
class EngagementServiceTest {

    private fun makeResults(
        count: Int = 10,
        allCorrect: Boolean = true,
        categories: List<ExerciseCategory> = listOf(ExerciseCategory.ADDITION_10)
    ): List<ExerciseResult> {
        return (0 until count).map { i ->
            val category = categories[i % categories.size]
            ExerciseResult(
                exercise = Exercise(
                    type = category.operationType,
                    category = category,
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

    // --- Exercise count achievements ---

    @Test
    fun `exercises10 achievement met when total is 10`() {
        val (met, progress) = EngagementService.evaluateAchievement(
            type = AchievementType.EXERCISES_10,
            totalExercises = 10
        )
        assertTrue(met)
        assertEquals(10, progress)
    }

    @Test
    fun `exercises10 achievement not met when total is 7`() {
        val (met, progress) = EngagementService.evaluateAchievement(
            type = AchievementType.EXERCISES_10,
            totalExercises = 7
        )
        assertFalse(met)
        assertEquals(7, progress)
    }

    // --- Streak achievements ---

    @Test
    fun `streak achievement met when current streak is 3`() {
        val (met, _) = EngagementService.evaluateAchievement(
            type = AchievementType.STREAK_3,
            currentStreak = 3
        )
        assertTrue(met)
    }

    @Test
    fun `streak resets after gap of more than 1 day`() {
        val twoDaysAgo = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000
        val (newStreak, _, isNew) = EngagementService.updateStreak(
            currentStreak = 3,
            longestStreak = 5,
            lastActiveAt = twoDaysAgo
        )
        assertEquals(1, newStreak)
        assertTrue(isNew)
    }

    @Test
    fun `streak same day no change`() {
        val now = System.currentTimeMillis()
        val (newStreak, _, isNew) = EngagementService.updateStreak(
            currentStreak = 3,
            longestStreak = 5,
            lastActiveAt = now
        )
        assertEquals(3, newStreak)
        assertFalse(isNew)
    }

    // --- Perfect 10 ---

    @Test
    fun `perfect10 is incremental`() {
        val results = makeResults(count = 10, allCorrect = true)

        val (met, progress) = EngagementService.evaluateAchievement(
            type = AchievementType.PERFECT_10,
            results = results,
            currentProgress = 0
        )
        assertFalse(met)
        assertEquals(1, progress)
    }

    // --- Speed demon ---

    @Test
    fun `speed demon met when 10 exercises in under 120 seconds`() {
        val results = makeResults(count = 10)
        val (met, _) = EngagementService.evaluateAchievement(
            type = AchievementType.SPEED_DEMON,
            results = results,
            sessionDuration = 90.0
        )
        assertTrue(met)
    }

    @Test
    fun `speed demon not met when session too long`() {
        val results = makeResults(count = 10)
        val (met, _) = EngagementService.evaluateAchievement(
            type = AchievementType.SPEED_DEMON,
            results = results,
            sessionDuration = 150.0
        )
        assertFalse(met)
    }

    // --- Variety ---

    @Test
    fun `variety achievement met with 4 categories`() {
        val results = makeResults(
            count = 8,
            categories = listOf(
                ExerciseCategory.ADDITION_10,
                ExerciseCategory.SUBTRACTION_10,
                ExerciseCategory.MULTIPLICATION_10,
                ExerciseCategory.ADDITION_100
            )
        )
        val (met, _) = EngagementService.evaluateAchievement(
            type = AchievementType.VARIETY,
            results = results
        )
        assertTrue(met)
    }

    // --- Accuracy streak ---

    @Test
    fun `accuracy streak resets on bad session`() {
        val results = makeResults(count = 10, allCorrect = false)
        val (met, progress) = EngagementService.evaluateAchievement(
            type = AchievementType.ACCURACY_STREAK,
            results = results,
            currentProgress = 2
        )
        assertFalse(met)
        assertEquals(0, progress)
    }

    @Test
    fun `accuracy streak increments on good session`() {
        val results = makeResults(count = 10, allCorrect = true)
        val (met, progress) = EngagementService.evaluateAchievement(
            type = AchievementType.ACCURACY_STREAK,
            results = results,
            currentProgress = 2
        )
        assertTrue(met)
        assertEquals(3, progress)
    }

    // --- Skipped exercises ---

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
        val (met, _) = EngagementService.evaluateAchievement(
            type = AchievementType.SPEED_DEMON,
            results = results,
            sessionDuration = 60.0
        )
        assertFalse(met)
    }
}
