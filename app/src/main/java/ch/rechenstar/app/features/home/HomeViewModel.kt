package ch.rechenstar.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.repository.ProgressRepository
import ch.rechenstar.app.data.repository.UserRepository
import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.Level
import ch.rechenstar.app.domain.service.ExerciseGenerator
import ch.rechenstar.app.domain.service.ExerciseMetrics
import ch.rechenstar.app.domain.service.MetricsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val totalStars: Int = 0,
    val dailyGoal: Int = 20,
    val dailyCompleted: Int = 0,
    val currentLevel: Level = Level.ANFAENGER,
    val sessionLength: Int = 10,
    val difficulty: Difficulty = Difficulty.EASY,
    val adaptiveDifficulty: Boolean = true,
    val gapFillEnabled: Boolean = true,
    val hideSkipButton: Boolean = false,
    val autoShowAnswerSeconds: Int = 0,
    val enabledCategories: List<ExerciseCategory> = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10),
    val metrics: ExerciseMetrics? = null,
    val userId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val userWithRelations = userRepository.getUserWithRelations(userId) ?: return@launch
            val user = userWithRelations.user
            val prefs = userWithRelations.preferences

            val dailyProgress = progressRepository.getForDateSync(userId, LocalDate.now())
            val completed = dailyProgress?.exercisesCompleted ?: 0

            // Compute metrics from recent records
            val metrics = computeMetrics(userId)
            val difficulty = if (prefs?.adaptiveDifficulty != false) {
                ExerciseGenerator.startingDifficulty(metrics)
            } else {
                Difficulty.fromRawValue(prefs?.difficultyLevel ?: 2)
            }

            val enabledCategories = parseCategories(prefs?.enabledCategoriesRaw)

            _uiState.value = HomeUiState(
                userName = user.name,
                totalStars = user.totalStars,
                dailyGoal = prefs?.dailyGoal ?: 20,
                dailyCompleted = completed,
                currentLevel = Level.current(user.totalExercises),
                sessionLength = prefs?.sessionLength ?: 10,
                difficulty = difficulty,
                adaptiveDifficulty = prefs?.adaptiveDifficulty ?: true,
                gapFillEnabled = prefs?.gapFillEnabled ?: true,
                hideSkipButton = prefs?.hideSkipButton ?: false,
                autoShowAnswerSeconds = prefs?.autoShowAnswerSeconds ?: 0,
                enabledCategories = enabledCategories,
                metrics = metrics,
                userId = userId,
                isLoading = false
            )
        }
    }

    private suspend fun computeMetrics(userId: String): ExerciseMetrics? {
        val records = userRepository.getRecentExerciseRecords(userId, 30)
        if (records.isEmpty()) return null

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

        return MetricsService.computeMetrics(recordData)
    }

    private fun parseCategories(raw: String?): List<ExerciseCategory> {
        if (raw.isNullOrBlank()) return listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10)
        val cats = raw.split(",").mapNotNull { ExerciseCategory.fromRawValue(it.trim()) }
        return cats.ifEmpty { listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10) }
    }
}
