package ch.rechenstar.app.features.exercise

import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.Exercise
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseConstants
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.service.ExerciseGenerator
import ch.rechenstar.app.domain.service.ExerciseMetrics
import kotlin.math.abs
import kotlin.math.min

class ExerciseViewModel(
    val sessionLength: Int = 10,
    difficulty: Difficulty = Difficulty.EASY,
    val categories: List<ExerciseCategory> = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10),
    val metrics: ExerciseMetrics? = null,
    val adaptiveDifficulty: Boolean = true,
    val gapFillEnabled: Boolean = true
) {

    sealed class FeedbackState {
        data object None : FeedbackState()
        data class Correct(val stars: Int) : FeedbackState()
        data class Revenge(val stars: Int) : FeedbackState()
        data object Incorrect : FeedbackState()
        data class WrongOperation(val correct: String, val wrong: String) : FeedbackState()
        data class ShowAnswer(val answer: Int) : FeedbackState()

        override fun equals(other: Any?): Boolean = when {
            this is None && other is None -> true
            this is Correct && other is Correct -> this.stars == other.stars
            this is Revenge && other is Revenge -> this.stars == other.stars
            this is Incorrect && other is Incorrect -> true
            this is WrongOperation && other is WrongOperation -> this.correct == other.correct && this.wrong == other.wrong
            this is ShowAnswer && other is ShowAnswer -> this.answer == other.answer
            else -> false
        }

        override fun hashCode(): Int = javaClass.hashCode()
    }

    enum class SessionState { NOT_STARTED, IN_PROGRESS, COMPLETED }

    var currentExercise: Exercise? = null
        private set
    var userAnswer: String = ""
    var isNegative: Boolean = false
    var exerciseIndex: Int = 0
        private set
    var sessionResults: MutableList<ExerciseResult> = mutableListOf()
        private set
    var feedbackState: FeedbackState = FeedbackState.None
        private set
    var sessionState: SessionState = SessionState.NOT_STARTED
        private set
    var currentDifficulty: Difficulty = difficulty
        private set
    var showEncouragement: Boolean = false
        private set

    private var exercises: MutableList<Exercise> = mutableListOf()
    private var currentAttempts: Int = 0
    private var exerciseStartTime: Long = System.currentTimeMillis()

    val progressText: String
        get() = "${exerciseIndex + 1} von $sessionLength"

    val progressFraction: Double
        get() = exerciseIndex.toDouble() / sessionLength.toDouble()

    val canSubmit: Boolean
        get() = userAnswer.isNotEmpty() && feedbackState is FeedbackState.None

    val totalStars: Int
        get() = sessionResults.sumOf { it.stars }

    val correctCount: Int
        get() = sessionResults.count { it.isCorrect }

    val accuracy: Double
        get() {
            if (sessionResults.isEmpty()) return 0.0
            return correctCount.toDouble() / sessionResults.size.toDouble()
        }

    val showNegativeToggle: Boolean
        get() = currentExercise?.category == ExerciseCategory.SUBTRACTION_100

    val displayAnswer: String
        get() {
            if (userAnswer.isEmpty()) return "_"
            return if (isNegative) "-$userAnswer" else userAnswer
        }

    fun startSession() {
        exercises = ExerciseGenerator.generateSession(
            count = sessionLength,
            difficulty = currentDifficulty,
            categories = categories,
            metrics = metrics,
            allowGapFill = gapFillEnabled
        ).toMutableList()
        sessionResults = mutableListOf()
        exerciseIndex = 0
        userAnswer = ""
        isNegative = false
        feedbackState = FeedbackState.None
        currentAttempts = 0
        showEncouragement = false
        sessionState = SessionState.IN_PROGRESS
        currentExercise = exercises.firstOrNull()
        exerciseStartTime = System.currentTimeMillis()
    }

    fun appendDigit(digit: Int) {
        if (feedbackState !is FeedbackState.None) return
        if (userAnswer.length >= 3) return
        userAnswer += "$digit"
    }

    fun deleteLastDigit() {
        if (feedbackState !is FeedbackState.None) return
        if (userAnswer.isEmpty()) return
        userAnswer = userAnswer.dropLast(1)
    }

    fun toggleNegative() {
        if (feedbackState !is FeedbackState.None) return
        isNegative = !isNegative
    }

    fun clearShowAnswer() {
        if (feedbackState !is FeedbackState.ShowAnswer) return
        nextExercise()
    }

    fun submitAnswer() {
        val exercise = currentExercise ?: return
        val absAnswer = userAnswer.toIntOrNull() ?: return
        if (feedbackState !is FeedbackState.None) return

        val answer = if (isNegative) -absAnswer else absAnswer
        currentAttempts++

        if (answer == exercise.correctAnswer) {
            val timeSpent = min(
                (System.currentTimeMillis() - exerciseStartTime) / 1000.0,
                ExerciseConstants.TIME_SPENT_CAP
            )
            val result = ExerciseResult(
                exercise = exercise,
                userAnswer = answer,
                isCorrect = true,
                attempts = currentAttempts,
                timeSpent = timeSpent
            )
            sessionResults.add(result)

            val isRevenge = exercise.isRetry || currentAttempts > 1 || isWeakExercise(exercise)
            feedbackState = if (isRevenge) FeedbackState.Revenge(result.stars) else FeedbackState.Correct(result.stars)
        } else if (currentAttempts >= ExerciseConstants.MAX_ATTEMPTS) {
            val timeSpent = min(
                (System.currentTimeMillis() - exerciseStartTime) / 1000.0,
                ExerciseConstants.TIME_SPENT_CAP
            )
            val result = ExerciseResult(
                exercise = exercise,
                userAnswer = answer,
                isCorrect = false,
                attempts = currentAttempts,
                timeSpent = timeSpent,
                wasRevealed = true
            )
            sessionResults.add(result)
            feedbackState = FeedbackState.ShowAnswer(exercise.correctAnswer)
        } else {
            // Check for +/- confusion (standard format only)
            if (exercise.format == ch.rechenstar.app.domain.model.ExerciseFormat.STANDARD) {
                val oppositeAnswer: Int? = when (exercise.type) {
                    ch.rechenstar.app.domain.model.OperationType.ADDITION ->
                        exercise.firstNumber - exercise.secondNumber
                    ch.rechenstar.app.domain.model.OperationType.SUBTRACTION ->
                        exercise.firstNumber + exercise.secondNumber
                    ch.rechenstar.app.domain.model.OperationType.MULTIPLICATION -> null
                }
                if (oppositeAnswer != null && answer == oppositeAnswer) {
                    val correctSymbol = exercise.type.symbol
                    val wrongSymbol = if (exercise.type == ch.rechenstar.app.domain.model.OperationType.ADDITION) "-" else "+"
                    feedbackState = FeedbackState.WrongOperation(correctSymbol, wrongSymbol)
                    return
                }
            }
            feedbackState = FeedbackState.Incorrect
        }
    }

    fun clearIncorrectFeedback() {
        when (feedbackState) {
            is FeedbackState.Incorrect, is FeedbackState.WrongOperation -> {
                feedbackState = FeedbackState.None
                userAnswer = ""
                isNegative = false
            }
            else -> {}
        }
    }

    fun nextExercise() {
        val nextIndex = exerciseIndex + 1

        if (nextIndex >= sessionLength) {
            sessionState = SessionState.COMPLETED
            return
        }

        // Adaptive difficulty check
        if (adaptiveDifficulty && nextIndex % ExerciseConstants.ADAPTATION_CHECK_INTERVAL == 0) {
            var frustrated = false
            if (sessionResults.size >= ExerciseConstants.FRUSTRATION_WINDOW_SIZE) {
                val window = sessionResults.takeLast(ExerciseConstants.FRUSTRATION_WINDOW_SIZE)
                val windowAccuracy = window.count { it.isCorrect }.toDouble() / window.size.toDouble()
                frustrated = windowAccuracy < ExerciseConstants.FRUSTRATION_ACCURACY_THRESHOLD
            }

            if (frustrated) {
                val lower = lowerDifficultyLevel(currentDifficulty)
                if (lower != currentDifficulty) {
                    currentDifficulty = lower
                    regenerateRemaining(nextIndex)
                    showEncouragement = true
                }
            } else {
                val last2 = sessionResults.takeLast(2)
                val recentAccuracy = last2.count { it.isCorrect }.toDouble() / last2.size.toDouble()
                val avgTime = last2.map { it.timeSpent }.average()
                val newDifficulty = ExerciseGenerator.adaptDifficulty(
                    current = currentDifficulty,
                    recentAccuracy = recentAccuracy,
                    averageTime = avgTime
                )
                if (newDifficulty != currentDifficulty) {
                    currentDifficulty = newDifficulty
                    regenerateRemaining(nextIndex)
                }
            }
        }

        exerciseIndex = nextIndex
        currentExercise = exercises[nextIndex]
        userAnswer = ""
        isNegative = false
        feedbackState = FeedbackState.None
        currentAttempts = 0
        exerciseStartTime = System.currentTimeMillis()
    }

    fun dismissEncouragement() {
        showEncouragement = false
    }

    fun skipExercise() {
        val exercise = currentExercise ?: return

        val timeSpent = min(
            (System.currentTimeMillis() - exerciseStartTime) / 1000.0,
            ExerciseConstants.TIME_SPENT_CAP
        )
        val result = ExerciseResult(
            exercise = exercise,
            userAnswer = 0,
            isCorrect = false,
            attempts = currentAttempts,
            timeSpent = timeSpent,
            wasSkipped = true
        )
        sessionResults.add(result)
        feedbackState = FeedbackState.ShowAnswer(exercise.correctAnswer)
    }

    fun autoRevealAnswer() {
        val exercise = currentExercise ?: return
        if (feedbackState !is FeedbackState.None) return

        val timeSpent = min(
            (System.currentTimeMillis() - exerciseStartTime) / 1000.0,
            ExerciseConstants.TIME_SPENT_CAP
        )
        val result = ExerciseResult(
            exercise = exercise,
            userAnswer = 0,
            isCorrect = false,
            attempts = currentAttempts,
            timeSpent = timeSpent,
            wasRevealed = true,
            wasSkipped = true
        )
        sessionResults.add(result)
        feedbackState = FeedbackState.ShowAnswer(exercise.correctAnswer)
    }

    private fun regenerateRemaining(nextIndex: Int) {
        val usedSignatures = exercises.take(nextIndex).map { it.signature }.toMutableSet()
        val remaining = sessionLength - nextIndex
        val newExercises = mutableListOf<Exercise>()
        repeat(remaining) {
            val category = ExerciseGenerator.weightedRandomCategory(categories, metrics)
            val ex = ExerciseGenerator.generate(
                difficulty = currentDifficulty,
                category = category,
                excludingSignatures = usedSignatures,
                metrics = metrics,
                allowGapFill = gapFillEnabled
            )
            newExercises.add(ex)
            usedSignatures.add(ex.signature)
        }
        exercises = (exercises.take(nextIndex) + newExercises).toMutableList()
    }

    private fun isWeakExercise(exercise: Exercise): Boolean {
        val weakPairs = metrics?.weakExercises?.get(exercise.category) ?: return false
        return weakPairs.any { it.first == exercise.firstNumber && it.second == exercise.secondNumber }
    }

    private fun lowerDifficultyLevel(difficulty: Difficulty): Difficulty = when (difficulty) {
        Difficulty.VERY_EASY -> Difficulty.VERY_EASY
        Difficulty.EASY -> Difficulty.VERY_EASY
        Difficulty.MEDIUM -> Difficulty.EASY
        Difficulty.HARD -> Difficulty.MEDIUM
    }
}
