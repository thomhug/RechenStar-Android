package ch.rechenstar.app.features.exercise

import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.service.ExerciseMetrics
import ch.rechenstar.app.domain.service.MetricsService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

class ExerciseViewModelTest {

    private fun makeSUT(
        sessionLength: Int = 10,
        categories: List<ExerciseCategory> = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10),
        metrics: ExerciseMetrics? = null
    ): ExerciseViewModel {
        return ExerciseViewModel(
            sessionLength = sessionLength,
            difficulty = Difficulty.EASY,
            categories = categories,
            metrics = metrics
        )
    }

    @Test
    fun `initial state`() {
        val vm = makeSUT()
        assertEquals(ExerciseViewModel.SessionState.NOT_STARTED, vm.sessionState)
        assertNull(vm.currentExercise)
        assertEquals("", vm.userAnswer)
    }

    @Test
    fun `start session`() {
        val vm = makeSUT()
        vm.startSession()
        assertEquals(ExerciseViewModel.SessionState.IN_PROGRESS, vm.sessionState)
        assertNotNull(vm.currentExercise)
    }

    @Test
    fun `append digit`() {
        val vm = makeSUT()
        vm.startSession()
        vm.appendDigit(5)
        assertEquals("5", vm.userAnswer)
        vm.appendDigit(3)
        assertEquals("53", vm.userAnswer)
    }

    @Test
    fun `append digit max three digits`() {
        val vm = makeSUT()
        vm.startSession()
        vm.appendDigit(1)
        vm.appendDigit(2)
        vm.appendDigit(3)
        assertEquals("123", vm.userAnswer)
        vm.appendDigit(4)
        assertEquals("123", vm.userAnswer)
    }

    @Test
    fun `delete last digit`() {
        val vm = makeSUT()
        vm.startSession()
        vm.appendDigit(5)
        vm.appendDigit(3)
        vm.deleteLastDigit()
        assertEquals("5", vm.userAnswer)
        vm.deleteLastDigit()
        assertEquals("", vm.userAnswer)
    }

    @Test
    fun `submit correct answer`() {
        val vm = makeSUT()
        vm.startSession()
        val exercise = vm.currentExercise ?: fail("No current exercise")
        val answer = exercise.correctAnswer
        for (digit in answer.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        assertEquals(ExerciseViewModel.FeedbackState.Correct(stars = 2), vm.feedbackState)
        assertEquals(1, vm.sessionResults.size)
        assertTrue(vm.sessionResults.first().isCorrect)
    }

    @Test
    fun `submit incorrect answer`() {
        val vm = makeSUT()
        vm.startSession()
        val exercise = vm.currentExercise ?: fail("No current exercise")
        val wrong = (exercise.correctAnswer + 1) % 100
        for (digit in wrong.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        assertEquals(ExerciseViewModel.FeedbackState.Incorrect, vm.feedbackState)
    }

    @Test
    fun `second attempt correct gives revenge`() {
        val vm = makeSUT()
        vm.startSession()
        val exercise = vm.currentExercise ?: fail("No current exercise")

        // First attempt: wrong
        val wrong = (exercise.correctAnswer + 1) % 100
        for (digit in wrong.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        assertEquals(ExerciseViewModel.FeedbackState.Incorrect, vm.feedbackState)

        vm.clearIncorrectFeedback()

        // Second attempt: correct
        val correct = exercise.correctAnswer
        for (digit in correct.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()

        val feedback = vm.feedbackState
        assertTrue(feedback is ExerciseViewModel.FeedbackState.Revenge, "Expected .revenge on 2nd attempt, got $feedback")
        assertEquals(1, (feedback as ExerciseViewModel.FeedbackState.Revenge).stars)
    }

    @Test
    fun `can submit`() {
        val vm = makeSUT()
        vm.startSession()
        assertFalse(vm.canSubmit)
        vm.appendDigit(5)
        assertTrue(vm.canSubmit)
    }

    @Test
    fun `next exercise advances`() {
        val vm = makeSUT()
        vm.startSession()
        val exercise = vm.currentExercise ?: fail("No current exercise")
        for (digit in exercise.correctAnswer.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        vm.nextExercise()
        assertEquals(1, vm.exerciseIndex)
        assertEquals("", vm.userAnswer)
        assertEquals(ExerciseViewModel.FeedbackState.None, vm.feedbackState)
    }

    @Test
    fun `session completes at end`() {
        val vm = makeSUT(sessionLength = 2)
        vm.startSession()

        // Exercise 1
        val ex1 = vm.currentExercise!!
        for (digit in ex1.correctAnswer.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        vm.nextExercise()

        // Exercise 2
        val ex2 = vm.currentExercise!!
        for (digit in ex2.correctAnswer.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        vm.nextExercise()

        assertEquals(ExerciseViewModel.SessionState.COMPLETED, vm.sessionState)
    }

    @Test
    fun `progress text`() {
        val vm = makeSUT(sessionLength = 10)
        vm.startSession()
        assertEquals("1 von 10", vm.progressText)
    }

    @Test
    fun `accuracy`() {
        val vm = makeSUT(sessionLength = 2)
        vm.startSession()

        // Correct answer for exercise 1
        val ex1 = vm.currentExercise!!
        for (digit in ex1.correctAnswer.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        vm.nextExercise()

        // Skip exercise 2
        vm.skipExercise()
        vm.clearShowAnswer()

        assertEquals(0.5, vm.accuracy)
    }

    @Test
    fun `skip exercise`() {
        val vm = makeSUT(sessionLength = 3)
        vm.startSession()
        val exercise = vm.currentExercise!!
        vm.skipExercise()

        assertEquals(1, vm.sessionResults.size)
        assertFalse(vm.sessionResults.first().isCorrect)
        val feedback = vm.feedbackState
        assertTrue(feedback is ExerciseViewModel.FeedbackState.ShowAnswer)
        assertEquals(exercise.correctAnswer, (feedback as ExerciseViewModel.FeedbackState.ShowAnswer).answer)
        vm.clearShowAnswer()
        assertEquals(1, vm.exerciseIndex)
    }

    @Test
    fun `negative toggle`() {
        val vm = makeSUT(categories = listOf(ExerciseCategory.SUBTRACTION_100))
        vm.startSession()
        assertFalse(vm.isNegative)
        vm.toggleNegative()
        assertTrue(vm.isNegative)
        vm.toggleNegative()
        assertFalse(vm.isNegative)
    }

    @Test
    fun `negative answer submission`() {
        val vm = makeSUT(categories = listOf(ExerciseCategory.SUBTRACTION_100))
        vm.startSession()
        val exercise = vm.currentExercise ?: fail("No current exercise")
        val answer = exercise.correctAnswer
        if (answer < 0) {
            vm.toggleNegative()
            for (digit in abs(answer).toString()) {
                vm.appendDigit(digit.toString().toInt())
            }
        } else {
            for (digit in answer.toString()) {
                vm.appendDigit(digit.toString().toInt())
            }
        }
        vm.submitAnswer()
        assertEquals(ExerciseViewModel.FeedbackState.Correct(stars = 2), vm.feedbackState)
    }

    @Test
    fun `display answer with negative`() {
        val vm = makeSUT(categories = listOf(ExerciseCategory.SUBTRACTION_100))
        vm.startSession()
        vm.appendDigit(4)
        vm.appendDigit(2)
        assertEquals("42", vm.displayAnswer)
        vm.toggleNegative()
        assertEquals("-42", vm.displayAnswer)
    }

    @Test
    fun `show negative toggle only for subtraction 100`() {
        val vm1 = makeSUT(categories = listOf(ExerciseCategory.SUBTRACTION_100))
        vm1.startSession()
        assertTrue(vm1.showNegativeToggle)

        val vm2 = makeSUT(categories = listOf(ExerciseCategory.ADDITION_10))
        vm2.startSession()
        assertFalse(vm2.showNegativeToggle)
    }

    @Test
    fun `no duplicate exercises after difficulty change`() {
        val vm = makeSUT(sessionLength = 10, categories = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10))
        vm.startSession()

        // Answer first 3 exercises correctly
        repeat(3) {
            val exercise = vm.currentExercise ?: fail("No current exercise")
            for (digit in exercise.correctAnswer.toString()) {
                vm.appendDigit(digit.toString().toInt())
            }
            vm.submitAnswer()
            vm.nextExercise()
        }

        val signatures = mutableListOf<String>()
        for (result in vm.sessionResults) {
            signatures.add(result.exercise.signature)
        }
        var idx = vm.exerciseIndex
        while (idx < vm.sessionLength && vm.sessionState == ExerciseViewModel.SessionState.IN_PROGRESS) {
            vm.currentExercise?.let { signatures.add(it.signature) }
            vm.skipExercise()
            vm.clearShowAnswer()
            idx++
        }

        val unique = signatures.toSet()
        assertEquals(unique.size, signatures.size, "Duplicate exercises found after difficulty change")
    }

    @Test
    fun `revenge feedback on retry exercise`() {
        val metrics = ExerciseMetrics(
            categoryAccuracy = emptyMap(),
            weakExercises = mapOf(
                ExerciseCategory.ADDITION_10 to listOf(1 to 2, 2 to 3, 3 to 4),
                ExerciseCategory.SUBTRACTION_10 to listOf(5 to 2, 4 to 1)
            )
        )

        var sawRevenge = false
        var sawNormalCorrect = false

        repeat(20) {
            val vm = makeSUT(sessionLength = 10, metrics = metrics)
            vm.startSession()

            repeat(10) {
                val exercise = vm.currentExercise ?: return@repeat
                if (vm.sessionState != ExerciseViewModel.SessionState.IN_PROGRESS) return@repeat

                for (digit in exercise.correctAnswer.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()

                if (exercise.isRetry) {
                    if (vm.feedbackState is ExerciseViewModel.FeedbackState.Revenge) {
                        sawRevenge = true
                    } else {
                        fail("isRetry exercise answered correctly should give .revenge, got ${vm.feedbackState}")
                    }
                } else {
                    if (vm.feedbackState is ExerciseViewModel.FeedbackState.Correct) {
                        sawNormalCorrect = true
                    }
                }

                vm.nextExercise()
            }

            if (sawRevenge && sawNormalCorrect) return
        }

        assertTrue(sawRevenge, "Should have seen .revenge feedback for a retry exercise")
        assertTrue(sawNormalCorrect, "Should have seen .correct feedback for a normal exercise")
    }

    @Test
    fun `frustration detection lowers difficulty`() {
        val vm = ExerciseViewModel(
            sessionLength = 10,
            difficulty = Difficulty.HARD,
            categories = listOf(ExerciseCategory.ADDITION_10),
            metrics = null,
            adaptiveDifficulty = true,
            gapFillEnabled = false
        )
        vm.startSession()

        for (i in 0 until 4) {
            val exercise = vm.currentExercise ?: fail("No exercise at index $i")
            assertTrue(vm.sessionState == ExerciseViewModel.SessionState.IN_PROGRESS, "Session not in progress")

            if (i == 0) {
                for (digit in exercise.correctAnswer.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.nextExercise()
            } else {
                val wrong = (exercise.correctAnswer + 1) % 100
                for (digit in wrong.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.clearIncorrectFeedback()
                val wrong2 = (exercise.correctAnswer + 2) % 100
                for (digit in wrong2.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.clearShowAnswer()
            }
        }

        assertEquals(Difficulty.MEDIUM, vm.currentDifficulty,
            "Frustration (1/4 correct) should lower from hard to medium")
    }

    @Test
    fun `no frustration when accuracy above threshold`() {
        val vm = ExerciseViewModel(
            sessionLength = 10,
            difficulty = Difficulty.HARD,
            categories = listOf(ExerciseCategory.ADDITION_10),
            metrics = null,
            adaptiveDifficulty = true,
            gapFillEnabled = false
        )
        vm.startSession()

        for (i in 0 until 4) {
            val exercise = vm.currentExercise ?: fail("No exercise at index $i")
            assertTrue(vm.sessionState == ExerciseViewModel.SessionState.IN_PROGRESS, "Session not in progress")

            if (i < 2) {
                for (digit in exercise.correctAnswer.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.nextExercise()
            } else {
                val wrong = (exercise.correctAnswer + 1) % 100
                for (digit in wrong.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.clearIncorrectFeedback()
                val wrong2 = (exercise.correctAnswer + 2) % 100
                for (digit in wrong2.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.clearShowAnswer()
            }
        }

        assertEquals(Difficulty.MEDIUM, vm.currentDifficulty,
            "2/4 correct (50%) should not trigger frustration, but normal 0/2 check lowers 1 step")
        assertFalse(vm.showEncouragement, "No frustration -> no encouragement message")
    }

    @Test
    fun `encouragement shown on frustration`() {
        val vm = ExerciseViewModel(
            sessionLength = 10,
            difficulty = Difficulty.HARD,
            categories = listOf(ExerciseCategory.ADDITION_10),
            metrics = null,
            adaptiveDifficulty = true,
            gapFillEnabled = false
        )
        vm.startSession()

        repeat(4) { i ->
            val exercise = vm.currentExercise ?: return@repeat
            if (vm.sessionState != ExerciseViewModel.SessionState.IN_PROGRESS) return@repeat
            if (i == 0) {
                for (digit in exercise.correctAnswer.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.nextExercise()
            } else {
                val wrong = (exercise.correctAnswer + 1) % 100
                for (digit in wrong.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.clearIncorrectFeedback()
                val wrong2 = (exercise.correctAnswer + 2) % 100
                for (digit in wrong2.toString()) {
                    vm.appendDigit(digit.toString().toInt())
                }
                vm.submitAnswer()
                vm.clearShowAnswer()
            }
        }

        assertTrue(vm.showEncouragement, "Frustration should trigger encouragement message")
    }

    @Test
    fun `auto reveal answer records incorrect skipped result`() {
        val vm = makeSUT(sessionLength = 3)
        vm.startSession()
        val exercise = vm.currentExercise ?: fail("No current exercise")

        vm.autoRevealAnswer()

        assertEquals(1, vm.sessionResults.size)
        val result = vm.sessionResults.first()
        assertFalse(result.isCorrect)
        assertTrue(result.wasRevealed)
        assertTrue(result.wasSkipped)
        assertEquals(0, result.userAnswer)
        assertEquals(0, result.stars)

        val feedback = vm.feedbackState
        assertTrue(feedback is ExerciseViewModel.FeedbackState.ShowAnswer)
        assertEquals(exercise.correctAnswer, (feedback as ExerciseViewModel.FeedbackState.ShowAnswer).answer)
    }

    @Test
    fun `auto reveal after partial input records result`() {
        val vm = makeSUT(sessionLength = 3)
        vm.startSession()

        vm.appendDigit(9)
        vm.appendDigit(9)

        vm.autoRevealAnswer()

        assertEquals(1, vm.sessionResults.size)
        val result = vm.sessionResults.first()
        assertFalse(result.isCorrect)
        assertTrue(result.wasRevealed)
        assertEquals(0, result.userAnswer)
    }

    @Test
    fun `auto reveal does nothing during feedback`() {
        val vm = makeSUT(sessionLength = 3)
        vm.startSession()
        val exercise = vm.currentExercise ?: fail("No current exercise")

        for (digit in exercise.correctAnswer.toString()) {
            vm.appendDigit(digit.toString().toInt())
        }
        vm.submitAnswer()
        assertEquals(1, vm.sessionResults.size)

        vm.autoRevealAnswer()
        assertEquals(1, vm.sessionResults.size,
            "Auto-reveal should not add a second result when feedback is already showing")
    }
}
