package ch.rechenstar.app.domain.service

import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseConstants
import ch.rechenstar.app.domain.model.OperationType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExerciseGeneratorTest {

    @Test
    fun `generate returns valid exercise`() {
        val exercise = ExerciseGenerator.generate(difficulty = Difficulty.EASY, category = ExerciseCategory.ADDITION_10)
        assertTrue(exercise.correctAnswer >= 0)
        assertTrue(exercise.firstNumber >= 1)
        assertTrue(exercise.secondNumber >= 1)
    }

    @Test
    fun `addition 10 never exceeds ten`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.ADDITION_10)
            assertTrue(exercise.firstNumber + exercise.secondNumber <= 10,
                "Addition ${exercise.firstNumber} + ${exercise.secondNumber} exceeds 10")
        }
    }

    @Test
    fun `addition 100 never exceeds 100`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.ADDITION_100)
            assertTrue(exercise.firstNumber + exercise.secondNumber <= 100,
                "Addition ${exercise.firstNumber} + ${exercise.secondNumber} exceeds 100")
            assertTrue(exercise.firstNumber >= 2)
            assertTrue(exercise.secondNumber >= 1)
        }
    }

    @Test
    fun `subtraction 10 never negative`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.SUBTRACTION_10)
            assertTrue(exercise.firstNumber - exercise.secondNumber >= 0,
                "Subtraction ${exercise.firstNumber} - ${exercise.secondNumber} is negative")
        }
    }

    @Test
    fun `subtraction 100 allows negative`() {
        var hasNegative = false
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.SUBTRACTION_100)
            if (exercise.firstNumber - exercise.secondNumber < 0) {
                hasNegative = true
                return@repeat
            }
        }
        assertTrue(hasNegative, "Subtraction 100 should allow negative results")
    }

    @Test
    fun `multiplication 10 factors in range`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.MULTIPLICATION_10)
            assertTrue(exercise.firstNumber >= 2)
            assertTrue(exercise.firstNumber <= 9)
            assertTrue(exercise.secondNumber >= 2)
            assertTrue(exercise.secondNumber <= 9)
            assertEquals(OperationType.MULTIPLICATION, exercise.type)
        }
    }

    @Test
    fun `multiplication 100 product not exceeds 400`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.MULTIPLICATION_100)
            assertTrue(exercise.firstNumber * exercise.secondNumber <= 400,
                "Multiplication ${exercise.firstNumber} x ${exercise.secondNumber} exceeds 400")
            assertTrue(exercise.firstNumber <= 20)
            assertTrue(exercise.secondNumber <= 20)
        }
    }

    @Test
    fun `difficulty ranges`() {
        val cases = listOf(
            Triple(Difficulty.VERY_EASY, 1, 3),
            Triple(Difficulty.EASY, 1, 5),
            Triple(Difficulty.MEDIUM, 2, 7),
            Triple(Difficulty.HARD, 2, 9)
        )
        for ((difficulty, lower, upper) in cases) {
            repeat(50) {
                val exercise = ExerciseGenerator.generate(difficulty = difficulty, category = ExerciseCategory.ADDITION_10)
                assertTrue(exercise.firstNumber >= lower,
                    "$difficulty firstNumber ${exercise.firstNumber} below min $lower")
                assertTrue(exercise.firstNumber <= upper)
                assertTrue(exercise.secondNumber >= lower,
                    "$difficulty secondNumber ${exercise.secondNumber} below min $lower")
                assertTrue(exercise.secondNumber <= upper)
            }
        }
    }

    @Test
    fun `generate session correct count`() {
        val exercises = ExerciseGenerator.generateSession(count = 10, difficulty = Difficulty.EASY, categories = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10))
        assertEquals(10, exercises.size)
    }

    @Test
    fun `generate session no duplicates`() {
        val exercises = ExerciseGenerator.generateSession(count = 10, difficulty = Difficulty.HARD, categories = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10))
        val signatures = exercises.map { it.signature }
        val unique = signatures.toSet()
        assertEquals(unique.size, signatures.size, "Duplicate signatures found")
    }

    @Test
    fun `excluding signatures respected`() {
        val excluded = setOf("addition_10_1_2_standard", "addition_10_3_4_standard")
        repeat(50) {
            val exercise = ExerciseGenerator.generate(
                difficulty = Difficulty.EASY,
                category = ExerciseCategory.ADDITION_10,
                excludingSignatures = excluded
            )
            assertFalse(excluded.contains(exercise.signature))
        }
    }

    @Test
    fun `adapt difficulty up when perfect and fast`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.EASY, recentAccuracy = 1.0, averageTime = 2.0)
        assertEquals(Difficulty.MEDIUM, result)
    }

    @Test
    fun `adapt difficulty stays when perfect but slow`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.EASY, recentAccuracy = 1.0, averageTime = 4.0)
        assertEquals(Difficulty.EASY, result)
    }

    @Test
    fun `adapt difficulty stays when partially correct`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.EASY, recentAccuracy = 0.5)
        assertEquals(Difficulty.EASY, result)
    }

    @Test
    fun `adapt difficulty down when all wrong`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.MEDIUM, recentAccuracy = 0.0)
        assertEquals(Difficulty.EASY, result)
    }

    @Test
    fun `adapt difficulty down when slow`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.MEDIUM, recentAccuracy = 1.0, averageTime = 8.0)
        assertEquals(Difficulty.EASY, result)
    }

    @Test
    fun `adapt difficulty hard ceiling`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.HARD, recentAccuracy = 1.0, averageTime = 2.0)
        assertEquals(Difficulty.HARD, result)
    }

    @Test
    fun `adapt difficulty very easy floor`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.VERY_EASY, recentAccuracy = 0.0)
        assertEquals(Difficulty.VERY_EASY, result)
    }

    @Test
    fun `adapt difficulty no turbo jump`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.VERY_EASY, recentAccuracy = 1.0, averageTime = 1.0)
        assertEquals(Difficulty.EASY, result)
    }

    @Test
    fun `adapt difficulty slow overrides accuracy`() {
        val result = ExerciseGenerator.adaptDifficulty(current = Difficulty.HARD, recentAccuracy = 1.0, averageTime = 9.0)
        assertEquals(Difficulty.MEDIUM, result)
    }

    @Test
    fun `addition 100 respects easy difficulty`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.EASY, category = ExerciseCategory.ADDITION_100)
            assertTrue(exercise.firstNumber <= 40,
                "Easy addition_100 first number ${exercise.firstNumber} exceeds 40")
            assertTrue(exercise.firstNumber + exercise.secondNumber <= 100)
        }
    }

    @Test
    fun `subtraction 100 respects easy difficulty`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.EASY, category = ExerciseCategory.SUBTRACTION_100)
            assertTrue(exercise.firstNumber <= 40,
                "Easy subtraction_100 first number ${exercise.firstNumber} exceeds 40")
            assertTrue(exercise.secondNumber <= 40)
        }
    }

    @Test
    fun `multiplication 100 respects easy difficulty`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.EASY, category = ExerciseCategory.MULTIPLICATION_100)
            assertTrue(exercise.firstNumber * exercise.secondNumber <= 100,
                "Easy multiplication_100 product ${exercise.firstNumber * exercise.secondNumber} exceeds 100")
        }
    }

    @Test
    fun `multiplication 100 hard allows full range`() {
        var hasLargeProduct = false
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.MULTIPLICATION_100)
            assertTrue(exercise.firstNumber * exercise.secondNumber <= 400)
            if (exercise.firstNumber * exercise.secondNumber > 200) {
                hasLargeProduct = true
            }
        }
        assertTrue(hasLargeProduct, "Hard multiplication_100 should produce products > 200")
    }

    @Test
    fun `generate session with multiple categories`() {
        val categories = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10, ExerciseCategory.MULTIPLICATION_10)
        val exercises = ExerciseGenerator.generateSession(count = 30, difficulty = Difficulty.EASY, categories = categories)
        assertEquals(30, exercises.size)
        val categorySet = exercises.map { it.category }.toSet()
        assertTrue(categorySet.size >= 2, "Expected at least 2 categories in session")
    }

    @Test
    fun `weighted category prefers weak categories`() {
        val metrics = ExerciseMetrics(
            categoryAccuracy = mapOf(ExerciseCategory.ADDITION_10 to 0.9, ExerciseCategory.SUBTRACTION_10 to 0.3),
            weakExercises = emptyMap()
        )
        val counts = mutableMapOf(ExerciseCategory.ADDITION_10 to 0, ExerciseCategory.SUBTRACTION_10 to 0)
        val categories = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10)
        repeat(1000) {
            val cat = ExerciseGenerator.weightedRandomCategory(categories, metrics)
            counts[cat] = counts.getOrDefault(cat, 0) + 1
        }
        assertTrue(counts[ExerciseCategory.SUBTRACTION_10]!! > counts[ExerciseCategory.ADDITION_10]!!,
            "Weak category should be chosen more often: sub=${counts[ExerciseCategory.SUBTRACTION_10]} add=${counts[ExerciseCategory.ADDITION_10]}")
    }

    @Test
    fun `weak exercises injected when available`() {
        val metrics = ExerciseMetrics(
            categoryAccuracy = emptyMap(),
            weakExercises = mapOf(ExerciseCategory.ADDITION_10 to listOf(2 to 3))
        )
        var weakCount = 0
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.EASY, category = ExerciseCategory.ADDITION_10, metrics = metrics)
            if (exercise.firstNumber == 2 && exercise.secondNumber == 3) weakCount++
        }
        assertTrue(weakCount > 10, "Weak exercise (2+3) should appear frequently, got $weakCount/200")
    }

    @Test
    fun `weak exercises marked as retry`() {
        val metrics = ExerciseMetrics(
            categoryAccuracy = emptyMap(),
            weakExercises = mapOf(ExerciseCategory.ADDITION_10 to listOf(2 to 3))
        )
        var retryCount = 0
        var nonRetryCount = 0
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.EASY, category = ExerciseCategory.ADDITION_10, metrics = metrics)
            if (exercise.isRetry) {
                retryCount++
                assertEquals(2, exercise.firstNumber)
                assertEquals(3, exercise.secondNumber)
            } else {
                nonRetryCount++
            }
        }
        assertTrue(retryCount > 10, "Weak exercises should be marked isRetry, got $retryCount/200")
        assertTrue(nonRetryCount > 50, "Non-weak exercises should NOT be marked isRetry")
    }

    @Test
    fun `nil metrics behaves like random`() {
        val exercises = ExerciseGenerator.generateSession(count = 10, difficulty = Difficulty.EASY, categories = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10), metrics = null)
        assertEquals(10, exercises.size)
        for (exercise in exercises) {
            assertTrue(exercise.category == ExerciseCategory.ADDITION_10 || exercise.category == ExerciseCategory.SUBTRACTION_10)
        }
    }

    @Test
    fun `starting difficulty from high accuracy`() {
        val metrics = ExerciseMetrics(categoryAccuracy = mapOf(ExerciseCategory.ADDITION_10 to 0.95, ExerciseCategory.SUBTRACTION_10 to 0.92), weakExercises = emptyMap())
        assertEquals(Difficulty.HARD, ExerciseGenerator.startingDifficulty(metrics))
    }

    @Test
    fun `starting difficulty from medium accuracy`() {
        val metrics = ExerciseMetrics(categoryAccuracy = mapOf(ExerciseCategory.ADDITION_10 to 0.75, ExerciseCategory.SUBTRACTION_10 to 0.8), weakExercises = emptyMap())
        assertEquals(Difficulty.MEDIUM, ExerciseGenerator.startingDifficulty(metrics))
    }

    @Test
    fun `starting difficulty from nil metrics`() {
        assertEquals(Difficulty.VERY_EASY, ExerciseGenerator.startingDifficulty(null))
    }

    @Test
    fun `medium difficulty excludes ones`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.MEDIUM, category = ExerciseCategory.MULTIPLICATION_10)
            assertTrue(exercise.firstNumber >= 2, "Medium multiplication should not have factor 1, got ${exercise.firstNumber}")
            assertTrue(exercise.secondNumber >= 2, "Medium multiplication should not have factor 1, got ${exercise.secondNumber}")
        }
    }

    @Test
    fun `hard multiplication 100 excludes ones`() {
        repeat(100) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.MULTIPLICATION_100)
            assertTrue(exercise.firstNumber >= 2, "Hard grosses 1x1 should not have factor 1")
            assertTrue(exercise.secondNumber >= 2, "Hard grosses 1x1 should not have factor 1")
        }
    }

    @Test
    fun `weighted category without metrics uses uniform`() {
        val categories = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10)
        repeat(100) {
            val cat = ExerciseGenerator.weightedRandomCategory(categories, null)
            assertTrue(cat in categories)
        }
    }

    @Test
    fun `multiplication 10 very easy allows ones`() {
        var sawOne = false
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.VERY_EASY, category = ExerciseCategory.MULTIPLICATION_10)
            assertTrue(exercise.firstNumber >= 1, "VeryEasy multiplication factor should be >= 1")
            assertTrue(exercise.secondNumber >= 1, "VeryEasy multiplication factor should be >= 1")
            if (exercise.firstNumber == 1 || exercise.secondNumber == 1) sawOne = true
        }
        assertTrue(sawOne, "VeryEasy multiplication should allow factor 1")
    }

    @Test
    fun `multiplication 10 easy allows ones`() {
        var sawOne = false
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.EASY, category = ExerciseCategory.MULTIPLICATION_10)
            assertTrue(exercise.firstNumber >= 1, "Easy multiplication factor should be >= 1")
            assertTrue(exercise.secondNumber >= 1, "Easy multiplication factor should be >= 1")
            if (exercise.firstNumber == 1 || exercise.secondNumber == 1) sawOne = true
        }
        assertTrue(sawOne, "Easy multiplication should allow factor 1")
    }

    @Test
    fun `multiplication 100 very easy allows ones`() {
        var sawOne = false
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.VERY_EASY, category = ExerciseCategory.MULTIPLICATION_100)
            assertTrue(exercise.firstNumber >= 1, "VeryEasy grosses 1x1 factor should be >= 1")
            assertTrue(exercise.secondNumber >= 1, "VeryEasy grosses 1x1 factor should be >= 1")
            if (exercise.firstNumber == 1 || exercise.secondNumber == 1) sawOne = true
        }
        assertTrue(sawOne, "VeryEasy grosses 1x1 should allow factor 1")
    }

    @Test
    fun `addition 100 hard second number respects lower bound`() {
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.ADDITION_100)
            assertTrue(exercise.secondNumber >= 2, "Hard addition_100 second number should be >=2, got ${exercise.firstNumber}+${exercise.secondNumber}")
            assertTrue(exercise.firstNumber + exercise.secondNumber <= 100, "Sum exceeds 100: ${exercise.firstNumber}+${exercise.secondNumber}")
        }
    }

    @Test
    fun `addition 100 medium second number respects lower bound`() {
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.MEDIUM, category = ExerciseCategory.ADDITION_100)
            assertTrue(exercise.secondNumber >= 2, "Medium addition_100 second number should be >=2, got ${exercise.firstNumber}+${exercise.secondNumber}")
            assertTrue(exercise.firstNumber + exercise.secondNumber <= 100)
        }
    }

    @Test
    fun `hard multiplication 100 excludes ten and twenty`() {
        repeat(200) {
            val exercise = ExerciseGenerator.generate(difficulty = Difficulty.HARD, category = ExerciseCategory.MULTIPLICATION_100)
            assertFalse(ExerciseConstants.EXCLUDED_HARD_MULTIPLICATION_FACTORS.contains(exercise.firstNumber),
                "Hard multiplication_100 should exclude factor ${exercise.firstNumber}")
            assertFalse(ExerciseConstants.EXCLUDED_HARD_MULTIPLICATION_FACTORS.contains(exercise.secondNumber),
                "Hard multiplication_100 should exclude factor ${exercise.secondNumber}")
        }
    }
}
