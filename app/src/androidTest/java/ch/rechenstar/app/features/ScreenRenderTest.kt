package ch.rechenstar.app.features

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import ch.rechenstar.app.domain.model.AchievementType
import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.Exercise
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.model.Level
import ch.rechenstar.app.domain.model.OperationType
import ch.rechenstar.app.features.exercise.EngagementResult
import ch.rechenstar.app.features.exercise.ExerciseScreen
import ch.rechenstar.app.features.exercise.SessionCompleteScreen
import ch.rechenstar.app.ui.theme.RechenStarTheme
import org.junit.Rule
import org.junit.Test

class ScreenRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
    fun testSessionCompleteScreenRenders() {
        val results = makeResults()

        composeTestRule.setContent {
            RechenStarTheme {
                SessionCompleteScreen(
                    results = results,
                    sessionLength = 10,
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Geschafft!").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("done-button").assertIsDisplayed()
    }

    @Test
    fun testSessionCompleteWithEngagement() {
        val results = makeResults()
        val engagement = EngagementResult(
            newlyUnlockedAchievements = listOf(AchievementType.EXERCISES_10),
            currentStreak = 5,
            isNewStreak = true,
            dailyGoalReached = true,
            newLevel = Level.RECHENKIND
        )

        composeTestRule.setContent {
            RechenStarTheme {
                SessionCompleteScreen(
                    results = results,
                    sessionLength = 10,
                    engagement = engagement,
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Geschafft!").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 Tage am Stück!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tagesziel geschafft!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Erste Schritte").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level Up!").assertIsDisplayed()
    }

    @Test
    fun testExerciseScreenRenders() {
        composeTestRule.setContent {
            RechenStarTheme {
                ExerciseScreen(
                    sessionLength = 10,
                    difficulty = Difficulty.EASY,
                    categories = listOf(ExerciseCategory.ADDITION_10),
                    gapFillEnabled = false,
                    adaptiveDifficulty = false,
                    onSessionComplete = {},
                    onCancel = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("exercise-card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("submit-button").assertIsDisplayed()
    }
}
