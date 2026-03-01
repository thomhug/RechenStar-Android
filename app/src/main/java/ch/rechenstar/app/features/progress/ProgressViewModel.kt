package ch.rechenstar.app.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.repository.ProgressRepository
import ch.rechenstar.app.data.repository.UserRepository
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.Level
import ch.rechenstar.app.domain.service.MetricsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    fun loadProgress(userId: String) {
        viewModelScope.launch {
            val userWithRelations = userRepository.getUserWithRelations(userId) ?: return@launch
            val user = userWithRelations.user
            val prefs = userWithRelations.preferences

            val level = Level.current(user.totalExercises)
            val progress = Level.progress(user.totalExercises)

            val dailyProgress = progressRepository.getForDateSync(userId, LocalDate.now())

            // Category stats from last 7 days
            val records = userRepository.getRecentExerciseRecords(userId, 7)
            val categoryStats = if (records.isNotEmpty()) {
                val recordData = records.mapNotNull { record ->
                    val category = ExerciseCategory.fromRawValue(record.category) ?: return@mapNotNull null
                    MetricsService.RecordData(
                        category = category,
                        exerciseSignature = record.exerciseSignature,
                        firstNumber = record.firstNumber,
                        secondNumber = record.secondNumber,
                        isCorrect = record.isCorrect,
                        date = record.date
                    )
                }
                val metrics = MetricsService.computeMetrics(recordData)
                metrics?.categoryAccuracy?.map { (cat, acc) ->
                    CategoryStat(category = cat, accuracy = acc)
                }?.sortedBy { it.category.rawValue } ?: emptyList()
            } else emptyList()

            // Weekly data (Mon-Sun)
            val weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weeklyData = (0..6).map { dayOffset ->
                val day = weekStart.plusDays(dayOffset.toLong())
                val dayEpoch = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dp = progressRepository.getForDate(userId, dayEpoch)
                dp?.exercisesCompleted ?: 0
            }

            _uiState.value = ProgressUiState(
                totalExercises = user.totalExercises,
                totalStars = user.totalStars,
                currentStreak = user.currentStreak,
                currentLevel = level,
                levelProgress = progress,
                nextLevelExercises = level.nextLevelExercises,
                dailyGoal = prefs?.dailyGoal ?: 20,
                dailyCompleted = dailyProgress?.exercisesCompleted ?: 0,
                categoryStats = categoryStats,
                weeklyData = weeklyData,
                isLoading = false
            )
        }
    }
}
