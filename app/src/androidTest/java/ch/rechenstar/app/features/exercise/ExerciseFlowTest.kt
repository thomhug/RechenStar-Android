package ch.rechenstar.app.features.exercise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.ui.theme.RechenStarTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI tests for the exercise flow.
 * Ported from iOS ExerciseFlowUITests.swift.
 */
class ExerciseFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchExerciseScreen(
        sessionLength: Int = 10,
        difficulty: Difficulty = Difficulty.EASY,
        categories: List<ExerciseCategory> = listOf(
            ExerciseCategory.ADDITION_10,
            ExerciseCategory.SUBTRACTION_10
        ),
        hideSkipButton: Boolean = false,
        gapFillEnabled: Boolean = false,
        onSessionComplete: (List<ExerciseResult>) -> Unit = {},
        onCancel: (List<ExerciseResult>) -> Unit = {}
    ) {
        composeTestRule.setContent {
            RechenStarTheme {
                ExerciseScreen(
                    sessionLength = sessionLength,
                    difficulty = difficulty,
                    categories = categories,
                    hideSkipButton = hideSkipButton,
                    gapFillEnabled = gapFillEnabled,
                    adaptiveDifficulty = false,
                    onSessionComplete = onSessionComplete,
                    onCancel = onCancel
                )
            }
        }
        // Wait for first exercise to appear
        composeTestRule.waitForIdle()
    }

    // MARK: - Tests

    @Test
    fun testCompleteExerciseFlow() {
        var completedResults: List<ExerciseResult>? = null
        launchExerciseScreen(
            sessionLength = 10,
            onSessionComplete = { completedResults = it }
        )

        repeat(10) {
            solveCurrentExercise()
        }

        composeTestRule.waitForIdle()
        assert(completedResults != null) { "Session should complete after 10 exercises" }
        assert(completedResults!!.size == 10) { "Should have 10 results" }
    }

    @Test
    fun testNumberPadInputAndDelete() {
        launchExerciseScreen()

        val answerDisplay = composeTestRule.onNodeWithContentDescription("answer-display")
        answerDisplay.assertIsDisplayed()

        // Type "42"
        composeTestRule.onNodeWithTag("number-pad-4").performClick()
        composeTestRule.onNodeWithTag("number-pad-2").performClick()
        composeTestRule.waitForIdle()

        answerDisplay.assertTextContains("42")

        // Delete last digit
        composeTestRule.onNodeWithTag("delete-button").performClick()
        composeTestRule.waitForIdle()

        answerDisplay.assertTextContains("4")
    }

    @Test
    fun testSkipExercise() {
        launchExerciseScreen()

        val exerciseCard = composeTestRule.onNodeWithTag("exercise-card")
        exerciseCard.assertIsDisplayed()

        // Get initial exercise description
        val initialDescription = getExerciseDescription()

        // Tap skip
        composeTestRule.onNodeWithTag("skip-button").performClick()

        // Wait for ShowAnswer feedback (2.5s) + auto-advance
        composeTestRule.mainClock.advanceTimeBy(3000)
        composeTestRule.waitForIdle()

        // Exercise should have changed
        val newDescription = getExerciseDescription()
        assert(initialDescription != newDescription) {
            "Exercise should change after skip: was '$initialDescription', still '$newDescription'"
        }
    }

    @Test
    fun testCancelSession() {
        var wasCancelled = false
        launchExerciseScreen(
            onCancel = { wasCancelled = true }
        )

        composeTestRule.onNodeWithTag("exercise-card").assertIsDisplayed()

        // Tap cancel button
        composeTestRule.onNodeWithContentDescription("cancel-button").performClick()
        composeTestRule.waitForIdle()

        assert(wasCancelled) { "Cancel callback should have been called" }
    }

    @Test
    fun testEasyDifficultyLimitsNumbers() {
        launchExerciseScreen(
            sessionLength = 10,
            difficulty = Difficulty.EASY,
            categories = listOf(ExerciseCategory.ADDITION_10),
            gapFillEnabled = false
        )

        // With EASY difficulty (range 1..5), all numbers must be <= 5
        repeat(10) { i ->
            val description = getExerciseDescription()
            val parts = description.split(" ")
            if (parts.size >= 5) {
                val first = parts[0].toIntOrNull()
                val second = parts[2].toIntOrNull()
                if (first != null) {
                    assert(first <= 5) {
                        "Exercise ${i + 1}: first number $first exceeds easy range (max 5) — $description"
                    }
                }
                if (second != null) {
                    assert(second <= 5) {
                        "Exercise ${i + 1}: second number $second exceeds easy range (max 5) — $description"
                    }
                }
            }
            solveCurrentExercise()
        }
    }

    @Test
    fun testHardDifficultyHasLargeNumbers() {
        launchExerciseScreen(
            sessionLength = 10,
            difficulty = Difficulty.HARD,
            categories = listOf(ExerciseCategory.ADDITION_10),
            gapFillEnabled = false
        )

        var sawLargeNumber = false

        repeat(10) {
            val description = getExerciseDescription()
            val parts = description.split(" ")
            if (parts.size >= 5) {
                val first = parts[0].toIntOrNull()
                val second = parts[2].toIntOrNull()
                if (first != null && first > 5) sawLargeNumber = true
                if (second != null && second > 5) sawLargeNumber = true
            }
            solveCurrentExercise()
        }

        assert(sawLargeNumber) {
            "With hard difficulty, at least one number should be > 5"
        }
    }

    @Test
    fun testRevengeOnSecondAttempt() {
        launchExerciseScreen(sessionLength = 10)

        val description = getExerciseDescription()
        val correctAnswer = parseAnswer(description)

        // Submit wrong answer first
        val wrongAnswer = if (correctAnswer == 0) 1 else 0
        typeOnNumberPad(wrongAnswer)
        composeTestRule.onNodeWithTag("submit-button").performClick()

        // Wait for incorrect feedback to clear (1s)
        composeTestRule.mainClock.advanceTimeBy(1500)
        composeTestRule.waitForIdle()

        // Now submit correct answer
        typeOnNumberPad(correctAnswer)
        composeTestRule.onNodeWithTag("submit-button").performClick()
        composeTestRule.waitForIdle()

        // Verify revenge feedback appears
        composeTestRule.onNodeWithTag("revenge-feedback").assertIsDisplayed()
    }

    @Test
    fun testRevengeVsNormalFeedback() {
        launchExerciseScreen(sessionLength = 10)

        // Exercise 1: Solve correctly on first attempt → NO revenge
        val answer1 = parseAnswer(getExerciseDescription())
        typeOnNumberPad(answer1)
        composeTestRule.onNodeWithTag("submit-button").performClick()
        composeTestRule.waitForIdle()

        // Revenge should NOT appear
        composeTestRule.onNodeWithTag("revenge-feedback").assertDoesNotExist()

        // Wait for auto-advance
        composeTestRule.mainClock.advanceTimeBy(1500)
        composeTestRule.waitForIdle()

        // Exercise 2: Wrong first, correct second → REVENGE
        val desc2 = getExerciseDescription()
        val answer2 = parseAnswer(desc2)
        val wrong2 = (answer2 + 1) % 100
        typeOnNumberPad(wrong2)
        composeTestRule.onNodeWithTag("submit-button").performClick()

        // Wait for incorrect feedback to clear
        composeTestRule.mainClock.advanceTimeBy(1500)
        composeTestRule.waitForIdle()

        typeOnNumberPad(answer2)
        composeTestRule.onNodeWithTag("submit-button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("revenge-feedback").assertIsDisplayed()
    }

    // MARK: - Helpers

    private fun getExerciseDescription(): String {
        val node = composeTestRule
            .onNodeWithTag("exercise-card")
            .fetchSemanticsNode()
        return node.config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription]
            .joinToString(" ")
    }

    private fun parseAnswer(description: String): Int {
        // Description format: "3 + 4 gleich unbekannt" or "? + 4 gleich 7"
        val parts = description.split(" ")
        if (parts.size < 5) return 0

        val leftStr = parts[0]
        val op = parts[1]
        val rightStr = parts[2]
        // parts[3] == "gleich"
        val resultStr = parts[4]

        val left = leftStr.toIntOrNull()
        val right = rightStr.toIntOrNull()
        val result = resultStr.toIntOrNull()

        // Standard format: left and right are numbers, result is "unbekannt"
        if (left != null && right != null) {
            return when (op) {
                "+" -> left + right
                "-" -> left - right
                "×" -> left * right
                else -> 0
            }
        }

        // Gap-fill: one side is "?", result is a number
        if (right != null && result != null && leftStr == "?") {
            return when (op) {
                "+" -> result - right
                "-" -> result + right
                "×" -> if (right == 0) 0 else result / right
                else -> 0
            }
        }

        if (left != null && result != null && rightStr == "?") {
            return when (op) {
                "+" -> result - left
                "-" -> left - result
                "×" -> if (left == 0) 0 else result / left
                else -> 0
            }
        }

        return 0
    }

    private fun typeOnNumberPad(number: Int) {
        if (number < 0) {
            composeTestRule.onNodeWithTag("negative-button").performClick()
        }
        val digits = kotlin.math.abs(number).toString()
        for (char in digits) {
            composeTestRule.onNodeWithTag("number-pad-$char").performClick()
        }
    }

    private fun solveCurrentExercise() {
        val description = getExerciseDescription()
        val answer = parseAnswer(description)
        typeOnNumberPad(answer)
        composeTestRule.onNodeWithTag("submit-button").performClick()

        // Wait for star animation + auto-advance (1.0s for correct)
        composeTestRule.mainClock.advanceTimeBy(1500)
        composeTestRule.waitForIdle()
    }
}
