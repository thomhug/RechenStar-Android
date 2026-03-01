package ch.rechenstar.app.domain.service

import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.Exercise
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseConstants
import ch.rechenstar.app.domain.model.ExerciseFormat
import ch.rechenstar.app.domain.model.OperationType
import kotlin.random.Random

data class ExerciseMetrics(
    val categoryAccuracy: Map<ExerciseCategory, Double>,
    val weakExercises: Map<ExerciseCategory, List<Pair<Int, Int>>>
)

object ExerciseGenerator {

    fun generate(
        difficulty: Difficulty = Difficulty.EASY,
        category: ExerciseCategory,
        excludingSignatures: Set<String> = emptySet(),
        metrics: ExerciseMetrics? = null,
        allowGapFill: Boolean = false
    ): Exercise {
        val format = randomFormat(category, allowGapFill)

        // Chance to use a weak exercise if available
        if (metrics != null) {
            val weakPairs = metrics.weakExercises[category]
            if (!weakPairs.isNullOrEmpty() && Random.nextDouble() < ExerciseConstants.WEAK_EXERCISE_CHANCE) {
                val pair = weakPairs.random()
                val exercise = Exercise(
                    type = category.operationType,
                    category = category,
                    firstNumber = pair.first,
                    secondNumber = pair.second,
                    difficulty = difficulty,
                    format = format,
                    isRetry = true
                )
                if (exercise.signature !in excludingSignatures) {
                    return exercise
                }
            }
        }

        repeat(50) {
            val exercise = randomExercise(category, difficulty, format)
            if (exercise.signature !in excludingSignatures) {
                return exercise
            }
        }

        return randomExercise(category, difficulty, format)
    }

    fun generateSession(
        count: Int = 10,
        difficulty: Difficulty = Difficulty.EASY,
        categories: List<ExerciseCategory> = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10),
        metrics: ExerciseMetrics? = null,
        allowGapFill: Boolean = false
    ): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        val usedSignatures = mutableSetOf<String>()

        repeat(count) {
            val category = weightedRandomCategory(categories, metrics)
            val exercise = generate(difficulty, category, usedSignatures, metrics, allowGapFill)
            exercises.add(exercise)
            usedSignatures.add(exercise.signature)
        }

        return exercises
    }

    fun adaptDifficulty(
        current: Difficulty,
        recentAccuracy: Double,
        averageTime: Double = Double.POSITIVE_INFINITY
    ): Difficulty {
        // Slow -> down (child is struggling, even if answers are correct)
        if (averageTime.isFinite() && averageTime > ExerciseConstants.SLOW_TIME_THRESHOLD) {
            return nextLower(current)
        }
        // Perfect AND fast -> up (automated knowledge)
        if (recentAccuracy >= 1.0 && averageTime < ExerciseConstants.FAST_TIME_THRESHOLD) {
            return nextHigher(current)
        }
        // All wrong -> down
        if (recentAccuracy < 0.01) {
            return nextLower(current)
        }
        // Otherwise stay
        return current
    }

    fun startingDifficulty(metrics: ExerciseMetrics?): Difficulty {
        if (metrics == null || metrics.categoryAccuracy.isEmpty()) return Difficulty.VERY_EASY
        val avgAccuracy = metrics.categoryAccuracy.values.sum() / metrics.categoryAccuracy.size
        return when {
            avgAccuracy >= ExerciseConstants.START_HARD_THRESHOLD -> Difficulty.HARD
            avgAccuracy >= ExerciseConstants.START_MEDIUM_THRESHOLD -> Difficulty.MEDIUM
            avgAccuracy >= ExerciseConstants.START_EASY_THRESHOLD -> Difficulty.EASY
            else -> Difficulty.VERY_EASY
        }
    }

    fun weightedRandomCategory(
        categories: List<ExerciseCategory>,
        metrics: ExerciseMetrics?
    ): ExerciseCategory {
        if (metrics == null || categories.isEmpty()) {
            return categories.random()
        }

        val weights = categories.map { category ->
            val accuracy = metrics.categoryAccuracy[category]
            if (accuracy != null) 1.0 + (1.0 - accuracy) else 1.0
        }

        val totalWeight = weights.sum()
        var random = Random.nextDouble() * totalWeight

        for ((index, weight) in weights.withIndex()) {
            random -= weight
            if (random <= 0) {
                return categories[index]
            }
        }

        return categories.last()
    }

    private fun randomExercise(
        category: ExerciseCategory,
        difficulty: Difficulty,
        format: ExerciseFormat = ExerciseFormat.STANDARD
    ): Exercise {
        return when (category) {
            ExerciseCategory.ADDITION_10 -> {
                val range = difficulty.range
                val maxFirst = minOf(range.last, 10 - range.first)
                val first = Random.nextInt(range.first, maxFirst + 1)
                val maxSecond = minOf(range.last, 10 - first)
                val second = Random.nextInt(range.first, maxSecond + 1)
                Exercise(type = OperationType.ADDITION, category = category, firstNumber = first, secondNumber = second, difficulty = difficulty, format = format)
            }

            ExerciseCategory.ADDITION_100 -> {
                val range100 = difficulty.range100
                val maxFirst = 100 - range100.first
                val first = Random.nextInt(range100.first, minOf(range100.last, maxFirst) + 1)
                val maxSecond = minOf(range100.last, 100 - first)
                val second = Random.nextInt(range100.first, maxOf(range100.first, maxSecond) + 1)
                Exercise(type = OperationType.ADDITION, category = category, firstNumber = first, secondNumber = second, difficulty = difficulty, format = format)
            }

            ExerciseCategory.SUBTRACTION_10 -> {
                val range = difficulty.range
                val first = Random.nextInt(range.first, range.last + 1)
                val second = Random.nextInt(range.first, first + 1)
                Exercise(type = OperationType.SUBTRACTION, category = category, firstNumber = first, secondNumber = second, difficulty = difficulty, format = format)
            }

            ExerciseCategory.SUBTRACTION_100 -> {
                val range100 = difficulty.range100
                val first = Random.nextInt(range100.first, range100.last + 1)
                val second = Random.nextInt(range100.first, range100.last + 1)
                Exercise(type = OperationType.SUBTRACTION, category = category, firstNumber = first, secondNumber = second, difficulty = difficulty, format = format)
            }

            ExerciseCategory.MULTIPLICATION_10 -> {
                val range = difficulty.range
                val minFactor = if (difficulty <= Difficulty.EASY) range.first else maxOf(range.first, ExerciseConstants.MINIMUM_MULTIPLICATION_FACTOR)
                val first = Random.nextInt(minFactor, range.last + 1)
                val second = Random.nextInt(minFactor, range.last + 1)
                Exercise(type = OperationType.MULTIPLICATION, category = category, firstNumber = first, secondNumber = second, difficulty = difficulty, format = format)
            }

            ExerciseCategory.MULTIPLICATION_100 -> {
                val maxProduct = difficulty.maxProduct
                val minFactor = if (difficulty <= Difficulty.EASY) difficulty.range.first else maxOf(difficulty.range.first, ExerciseConstants.MINIMUM_MULTIPLICATION_FACTOR)
                val excludedFactors: Set<Int> = if (difficulty == Difficulty.HARD) ExerciseConstants.EXCLUDED_HARD_MULTIPLICATION_FACTORS else emptySet()
                var first: Int
                var second: Int
                do {
                    first = Random.nextInt(minFactor, 21)
                    val maxSecond = minOf(20, maxProduct / maxOf(first, 1))
                    second = Random.nextInt(minFactor, maxOf(minFactor, maxSecond) + 1)
                } while (first in excludedFactors || second in excludedFactors)
                Exercise(type = OperationType.MULTIPLICATION, category = category, firstNumber = first, secondNumber = second, difficulty = difficulty, format = format)
            }
        }
    }

    private fun randomFormat(category: ExerciseCategory, allowGapFill: Boolean): ExerciseFormat {
        if (!allowGapFill) return ExerciseFormat.STANDARD
        // Only addition/subtraction bis 10
        if (category != ExerciseCategory.ADDITION_10 && category != ExerciseCategory.SUBTRACTION_10) {
            return ExerciseFormat.STANDARD
        }
        if (Random.nextDouble() >= ExerciseConstants.GAP_FILL_CHANCE) return ExerciseFormat.STANDARD
        return if (Random.nextBoolean()) ExerciseFormat.FIRST_GAP else ExerciseFormat.SECOND_GAP
    }

    private fun nextHigher(difficulty: Difficulty): Difficulty = when (difficulty) {
        Difficulty.VERY_EASY -> Difficulty.EASY
        Difficulty.EASY -> Difficulty.MEDIUM
        Difficulty.MEDIUM -> Difficulty.HARD
        Difficulty.HARD -> Difficulty.HARD
    }

    private fun nextLower(difficulty: Difficulty): Difficulty = when (difficulty) {
        Difficulty.VERY_EASY -> Difficulty.VERY_EASY
        Difficulty.EASY -> Difficulty.VERY_EASY
        Difficulty.MEDIUM -> Difficulty.EASY
        Difficulty.HARD -> Difficulty.MEDIUM
    }
}
