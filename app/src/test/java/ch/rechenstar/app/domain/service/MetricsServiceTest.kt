package ch.rechenstar.app.domain.service

import ch.rechenstar.app.domain.model.ExerciseCategory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MetricsServiceTest {

    private val now = System.currentTimeMillis()

    private fun record(
        category: ExerciseCategory,
        sig: String,
        first: Int,
        second: Int,
        correct: Boolean,
        minutesAgo: Int = 0
    ) = MetricsService.RecordData(
        category = category,
        exerciseSignature = sig,
        firstNumber = first,
        secondNumber = second,
        isCorrect = correct,
        date = now - (minutesAgo * 60 * 1000L)
    )

    @Test
    fun `empty records returns null`() {
        assertNull(MetricsService.computeMetrics(emptyList()))
    }

    @Test
    fun `category accuracy correct`() {
        val records = listOf(
            record(ExerciseCategory.ADDITION_10, "addition_10_3_4", 3, 4, true),
            record(ExerciseCategory.ADDITION_10, "addition_10_5_2", 5, 2, true),
            record(ExerciseCategory.ADDITION_10, "addition_10_1_6", 1, 6, false),
            record(ExerciseCategory.SUBTRACTION_10, "subtraction_10_8_3", 8, 3, true),
            record(ExerciseCategory.SUBTRACTION_10, "subtraction_10_7_2", 7, 2, false),
        )
        val metrics = MetricsService.computeMetrics(records)
        assertNotNull(metrics)
        assertEquals(2.0 / 3.0, metrics!!.categoryAccuracy[ExerciseCategory.ADDITION_10]!!, 0.001)
        assertEquals(0.5, metrics.categoryAccuracy[ExerciseCategory.SUBTRACTION_10]!!, 0.001)
    }

    @Test
    fun `weak exercise last attempt wrong clears after revenge`() {
        val records = listOf(
            record(ExerciseCategory.ADDITION_10, "addition_10_3_7", 3, 7, false, minutesAgo = 30),
            record(ExerciseCategory.ADDITION_10, "addition_10_3_7", 3, 7, false, minutesAgo = 20),
            record(ExerciseCategory.ADDITION_10, "addition_10_3_7", 3, 7, true, minutesAgo = 10),
        )
        val metrics = MetricsService.computeMetrics(records)!!
        val weak = metrics.weakExercises[ExerciseCategory.ADDITION_10] ?: emptyList()
        assertTrue(weak.isEmpty(), "Exercise should NOT be weak after most recent attempt was correct")
    }

    @Test
    fun `weak exercise last attempt wrong still weak`() {
        val records = listOf(
            record(ExerciseCategory.ADDITION_10, "addition_10_3_7", 3, 7, false, minutesAgo = 30),
            record(ExerciseCategory.ADDITION_10, "addition_10_3_7", 3, 7, true, minutesAgo = 20),
            record(ExerciseCategory.ADDITION_10, "addition_10_3_7", 3, 7, false, minutesAgo = 10),
        )
        val metrics = MetricsService.computeMetrics(records)!!
        val weak = metrics.weakExercises[ExerciseCategory.ADDITION_10] ?: emptyList()
        assertEquals(1, weak.size, "Exercise should still be weak when most recent attempt is wrong")
    }

    @Test
    fun `single wrong attempt is weak`() {
        val records = listOf(
            record(ExerciseCategory.SUBTRACTION_10, "subtraction_10_8_5", 8, 5, false),
        )
        val metrics = MetricsService.computeMetrics(records)!!
        val weak = metrics.weakExercises[ExerciseCategory.SUBTRACTION_10] ?: emptyList()
        assertEquals(1, weak.size)
    }

    @Test
    fun `strong exercises not in weak exercises`() {
        val records = listOf(
            record(ExerciseCategory.ADDITION_10, "addition_10_2_3", 2, 3, true, minutesAgo = 30),
            record(ExerciseCategory.ADDITION_10, "addition_10_2_3", 2, 3, true, minutesAgo = 20),
            record(ExerciseCategory.ADDITION_10, "addition_10_2_3", 2, 3, false, minutesAgo = 10),
        )
        val metrics = MetricsService.computeMetrics(records)!!
        val weak = metrics.weakExercises[ExerciseCategory.ADDITION_10] ?: emptyList()
        assertTrue(weak.isEmpty())
    }

    @Test
    fun `exercise drops from weak after revenge`() {
        val beforeRevenge = listOf(
            record(ExerciseCategory.ADDITION_10, "addition_10_4_5", 4, 5, false, minutesAgo = 60),
        )
        val metricsB = MetricsService.computeMetrics(beforeRevenge)!!
        assertEquals(1, metricsB.weakExercises[ExerciseCategory.ADDITION_10]?.size, "Should be weak before revenge")

        val afterRevenge = beforeRevenge + listOf(
            record(ExerciseCategory.ADDITION_10, "addition_10_4_5", 4, 5, true, minutesAgo = 10),
        )
        val metricsA = MetricsService.computeMetrics(afterRevenge)!!
        val weak = metricsA.weakExercises[ExerciseCategory.ADDITION_10] ?: emptyList()
        assertTrue(weak.isEmpty(), "Should NOT be weak after successful revenge (last attempt correct)")
    }

    @Test
    fun `revenge in different format clears weak`() {
        val records = listOf(
            record(ExerciseCategory.ADDITION_10, "addition_10_3_2_standard", 3, 2, false, minutesAgo = 60),
            record(ExerciseCategory.ADDITION_10, "addition_10_3_2_firstGap", 3, 2, true, minutesAgo = 10),
        )
        val metrics = MetricsService.computeMetrics(records)!!
        val weak = metrics.weakExercises[ExerciseCategory.ADDITION_10] ?: emptyList()
        assertTrue(weak.isEmpty(), "Exercise solved in different format should clear weak status")
    }
}
