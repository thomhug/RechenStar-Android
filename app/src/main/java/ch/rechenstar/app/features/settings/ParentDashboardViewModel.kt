package ch.rechenstar.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.repository.ProgressRepository
import ch.rechenstar.app.data.repository.UserRepository
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.service.MetricsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardCategoryStat(
    val label: String,
    val correct: Int,
    val total: Int,
    val accuracy: Double
)

data class ParentDashboardUiState(
    val weeklyAccuracy: Double = 0.0,
    val weeklyPlayTimeFormatted: String = "0 Min",
    val weeklySessions: Int = 0,
    val weeklyExercises: Int = 0,
    val categoryStats: List<DashboardCategoryStat> = emptyList(),
    val totalExercises: Int = 0,
    val totalStars: Int = 0,
    val longestStreak: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(userId: String) {
        viewModelScope.launch {
            val userWithRelations = userRepository.getUserWithRelations(userId) ?: return@launch
            val user = userWithRelations.user

            val weeklyProgress = progressRepository.getLast7DaysSync(userId)
            val weeklyCorrect = weeklyProgress.sumOf { it.correctAnswers }
            val weeklyTotal = weeklyProgress.sumOf { it.exercisesCompleted }
            val weeklyAccuracy = if (weeklyTotal > 0) weeklyCorrect.toDouble() / weeklyTotal else 0.0
            val weeklyTime = weeklyProgress.sumOf { it.totalTime }
            val weeklySessions = weeklyProgress.sumOf { it.sessionsCount }

            // Category stats
            val records = userRepository.getRecentExerciseRecords(userId, 7)
            val categoryStats = if (records.isNotEmpty()) {
                val grouped = records.groupBy { it.category }
                grouped.mapNotNull { (catRaw, catRecords) ->
                    val category = ExerciseCategory.fromRawValue(catRaw) ?: return@mapNotNull null
                    val correct = catRecords.count { it.isCorrect }
                    val total = catRecords.size
                    DashboardCategoryStat(
                        label = category.label,
                        correct = correct,
                        total = total,
                        accuracy = if (total > 0) correct.toDouble() / total else 0.0
                    )
                }.sortedBy { it.label }
            } else emptyList()

            _uiState.value = ParentDashboardUiState(
                weeklyAccuracy = weeklyAccuracy,
                weeklyPlayTimeFormatted = formatPlayTime(weeklyTime),
                weeklySessions = weeklySessions,
                weeklyExercises = weeklyTotal,
                categoryStats = categoryStats,
                totalExercises = user.totalExercises,
                totalStars = user.totalStars,
                longestStreak = user.longestStreak,
                isLoading = false
            )
        }
    }

    private fun formatPlayTime(seconds: Double): String {
        val minutes = (seconds / 60).toInt()
        return if (minutes < 60) "${minutes} Min"
        else "${minutes / 60}h ${minutes % 60}m"
    }
}
