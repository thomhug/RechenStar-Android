package ch.rechenstar.app.domain.service

import ch.rechenstar.app.domain.model.ExerciseCategory

object MetricsService {

    data class RecordData(
        val category: ExerciseCategory,
        val exerciseSignature: String,
        val firstNumber: Int,
        val secondNumber: Int,
        val isCorrect: Boolean,
        val date: Long
    )

    fun computeMetrics(records: List<RecordData>): ExerciseMetrics? {
        if (records.isEmpty()) return null

        // Category accuracy
        val categoryGroups = mutableMapOf<ExerciseCategory, Pair<Int, Int>>()
        for (record in records) {
            val (correct, total) = categoryGroups.getOrDefault(record.category, 0 to 0)
            categoryGroups[record.category] = (correct + if (record.isCorrect) 1 else 0) to (total + 1)
        }

        val categoryAccuracy = categoryGroups.mapValues { (_, group) ->
            group.first.toDouble() / group.second.toDouble()
        }

        // Weak exercises: last attempt was wrong AND overall accuracy < 0.6
        // Group by category + numbers (format-agnostic)
        data class ExerciseGroup(
            var correct: Int = 0,
            var total: Int = 0,
            val category: ExerciseCategory,
            val first: Int,
            val second: Int,
            var lastDate: Long = 0,
            var lastCorrect: Boolean = true
        )

        val exerciseGroups = mutableMapOf<String, ExerciseGroup>()
        for (record in records) {
            val key = "${record.category.rawValue}_${record.firstNumber}_${record.secondNumber}"
            val group = exerciseGroups.getOrPut(key) {
                ExerciseGroup(category = record.category, first = record.firstNumber, second = record.secondNumber)
            }
            group.total++
            if (record.isCorrect) group.correct++
            if (record.date >= group.lastDate) {
                group.lastDate = record.date
                group.lastCorrect = record.isCorrect
            }
        }

        val weakExercises = mutableMapOf<ExerciseCategory, MutableList<Pair<Int, Int>>>()
        for (group in exerciseGroups.values) {
            val accuracy = group.correct.toDouble() / group.total.toDouble()
            if (accuracy < 0.6 && !group.lastCorrect) {
                weakExercises.getOrPut(group.category) { mutableListOf() }
                    .add(group.first to group.second)
            }
        }

        return ExerciseMetrics(categoryAccuracy = categoryAccuracy, weakExercises = weakExercises)
    }
}
