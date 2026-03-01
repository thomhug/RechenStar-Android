package ch.rechenstar.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.local.dao.SessionDao
import ch.rechenstar.app.data.local.dao.UserPreferencesDao
import ch.rechenstar.app.data.local.entity.SessionEntity
import ch.rechenstar.app.data.repository.ProgressRepository
import ch.rechenstar.app.data.repository.UserRepository
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.Level
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class DashboardCategoryStat(
    val label: String,
    val correct: Int,
    val total: Int,
    val accuracy: Double
)

data class RecentSession(
    val label: String,
    val correctCount: Int,
    val totalCount: Int,
    val starsEarned: Int,
    val accuracy: Double
)

data class ExerciseDetail(
    val exercise: String,
    val correct: Int,
    val wrong: Int,
    val avgTime: String,
    val bestTime: String
)

data class FocusArea(
    val categoryLabel: String,
    val example: String
)

data class AdjustmentLogItem(
    val date: String,
    val summary: String
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
    val memberSince: String = "",
    val weeklyChartData: List<Int> = List(7) { 0 },
    val weeklyAccuracyData: List<Double?> = List(7) { null },
    val recentSessions: List<RecentSession> = emptyList(),
    val focusAreas: List<FocusArea> = emptyList(),
    val exerciseDetails: List<ExerciseDetail> = emptyList(),
    val breakReminderEnabled: Boolean = true,
    val breakIntervalMinutes: Int = 15,
    val currentLevel: Level = Level.ANFAENGER,
    val adjustStars: Int = 0,
    val adjustExercises: Int = 0,
    val adjustStreak: Int = 0,
    val adjustmentLogs: List<AdjustmentLogItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val sessionDao: SessionDao,
    private val preferencesDao: UserPreferencesDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    private var userId: String? = null

    fun loadDashboard(userId: String) {
        this.userId = userId
        viewModelScope.launch {
            val userWithRelations = userRepository.getUserWithRelations(userId) ?: return@launch
            val user = userWithRelations.user
            val prefs = userWithRelations.preferences

            val weeklyProgress = progressRepository.getLast7DaysSync(userId)
            val weeklyCorrect = weeklyProgress.sumOf { it.correctAnswers }
            val weeklyTotal = weeklyProgress.sumOf { it.exercisesCompleted }
            val weeklyAccuracy = if (weeklyTotal > 0) weeklyCorrect.toDouble() / weeklyTotal else 0.0
            val weeklyTime = weeklyProgress.sumOf { it.totalTime }
            val weeklySessions = weeklyProgress.sumOf { it.sessionsCount }

            // Weekly chart data (Mon-Sun)
            val weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weeklyChartData = (0..6).map { dayOffset ->
                val day = weekStart.plusDays(dayOffset.toLong())
                val dayEpoch = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dp = progressRepository.getForDate(userId, dayEpoch)
                dp?.exercisesCompleted ?: 0
            }

            // Weekly accuracy per day
            val weeklyAccuracyData = (0..6).map { dayOffset ->
                val day = weekStart.plusDays(dayOffset.toLong())
                val dayEpoch = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dp = progressRepository.getForDate(userId, dayEpoch)
                if (dp != null && dp.exercisesCompleted > 0) {
                    dp.correctAnswers.toDouble() / dp.exercisesCompleted
                } else null
            }

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

            // Focus areas (weakest categories)
            val focusAreas = categoryStats
                .filter { it.accuracy < 0.8 && it.total >= 2 }
                .sortedBy { it.accuracy }
                .take(3)
                .map { stat ->
                    val catRecords = records.filter {
                        val cat = ExerciseCategory.fromRawValue(it.category)
                        cat?.label == stat.label && !it.isCorrect
                    }
                    val example = if (catRecords.isNotEmpty()) {
                        val rec = catRecords.first()
                        "${rec.firstNumber} ${rec.operationType} ${rec.secondNumber}"
                    } else ""
                    FocusArea(categoryLabel = stat.label, example = example)
                }

            // Exercise details
            val exerciseDetails = if (records.isNotEmpty()) {
                records.groupBy { it.exerciseSignature }.map { (sig, recs) ->
                    val correct = recs.count { it.isCorrect }
                    val wrong = recs.count { !it.isCorrect }
                    val avgTime = recs.map { it.timeSpent }.average()
                    val bestTime = recs.minOf { it.timeSpent }
                    ExerciseDetail(
                        exercise = sig.ifEmpty { "${recs.first().firstNumber} ${recs.first().operationType} ${recs.first().secondNumber}" },
                        correct = correct,
                        wrong = wrong,
                        avgTime = "${avgTime.toInt()}s",
                        bestTime = "${bestTime.toInt()}s"
                    )
                }.take(15)
            } else emptyList()

            // Recent sessions
            val recentSessionEntities = sessionDao.getRecentForUser(userId, 5)
            val recentSessions = recentSessionEntities.map { session ->
                val time = Instant.ofEpochMilli(session.startTime)
                    .atZone(ZoneId.systemDefault())
                val today = LocalDate.now()
                val sessionDate = time.toLocalDate()
                val label = if (sessionDate == today) {
                    "Heute, ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                } else {
                    "${sessionDate.format(DateTimeFormatter.ofPattern("dd.MM"))}, ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                }
                val acc = if (session.totalCount > 0) session.correctCount.toDouble() / session.totalCount else 0.0
                RecentSession(
                    label = label,
                    correctCount = session.correctCount,
                    totalCount = session.totalCount,
                    starsEarned = session.starsEarned,
                    accuracy = acc
                )
            }

            // Adjustment logs
            val logEntities = userRepository.getAdjustmentLogsSync(userId)
            val adjustmentLogs = logEntities.map { log ->
                val date = Instant.ofEpochMilli(log.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                AdjustmentLogItem(date = date, summary = log.summary)
            }

            // Member since
            val memberSince = Instant.ofEpochMilli(user.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

            _uiState.value = ParentDashboardUiState(
                weeklyAccuracy = weeklyAccuracy,
                weeklyPlayTimeFormatted = formatPlayTime(weeklyTime),
                weeklySessions = weeklySessions,
                weeklyExercises = weeklyTotal,
                categoryStats = categoryStats,
                totalExercises = user.totalExercises,
                totalStars = user.totalStars,
                longestStreak = user.longestStreak,
                memberSince = memberSince,
                weeklyChartData = weeklyChartData,
                weeklyAccuracyData = weeklyAccuracyData,
                recentSessions = recentSessions,
                focusAreas = focusAreas,
                exerciseDetails = exerciseDetails,
                breakReminderEnabled = prefs?.breakReminder ?: true,
                breakIntervalMinutes = (prefs?.breakIntervalSeconds ?: 900) / 60,
                currentLevel = Level.current(user.totalExercises),
                adjustStars = user.totalStars,
                adjustExercises = user.totalExercises,
                adjustStreak = user.currentStreak,
                adjustmentLogs = adjustmentLogs,
                isLoading = false
            )
        }
    }

    fun updateBreakReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(breakReminderEnabled = enabled)
        val uid = userId ?: return
        viewModelScope.launch {
            val prefs = preferencesDao.getPreferencesSync(uid) ?: return@launch
            preferencesDao.updatePreferences(prefs.copy(breakReminder = enabled))
        }
    }

    fun updateBreakInterval(minutes: Int) {
        _uiState.value = _uiState.value.copy(breakIntervalMinutes = minutes)
        val uid = userId ?: return
        viewModelScope.launch {
            val prefs = preferencesDao.getPreferencesSync(uid) ?: return@launch
            preferencesDao.updatePreferences(prefs.copy(breakIntervalSeconds = minutes * 60))
        }
    }

    fun adjustStars(delta: Int) {
        val newVal = (_uiState.value.adjustStars + delta).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(adjustStars = newVal)
    }

    fun adjustExercises(delta: Int) {
        val newVal = (_uiState.value.adjustExercises + delta).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            adjustExercises = newVal,
            currentLevel = Level.current(newVal)
        )
    }

    fun adjustStreak(delta: Int) {
        val newVal = (_uiState.value.adjustStreak + delta).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(adjustStreak = newVal)
    }

    fun saveAdjustments() {
        val uid = userId ?: return
        val state = _uiState.value
        viewModelScope.launch {
            val user = userRepository.getUserById(uid) ?: return@launch
            val changes = mutableListOf<String>()
            if (user.totalStars != state.adjustStars) changes.add("Sterne: ${user.totalStars} → ${state.adjustStars}")
            if (user.totalExercises != state.adjustExercises) changes.add("Aufgaben: ${user.totalExercises} → ${state.adjustExercises}")
            if (user.currentStreak != state.adjustStreak) changes.add("Streak: ${user.currentStreak} → ${state.adjustStreak}")

            if (changes.isNotEmpty()) {
                userRepository.updateStats(uid, state.adjustExercises, state.adjustStars)
                val longest = maxOf(user.longestStreak, state.adjustStreak)
                userRepository.updateStreak(uid, state.adjustStreak, longest)
                userRepository.addAdjustmentLog(uid, changes.joinToString(", "))
                loadDashboard(uid)
            }
        }
    }

    fun resetAdjustments() {
        val state = _uiState.value
        _uiState.value = state.copy(
            adjustStars = state.totalStars,
            adjustExercises = state.totalExercises,
            adjustStreak = 0
        )
    }

    private fun formatPlayTime(seconds: Double): String {
        val minutes = (seconds / 60).toInt()
        return if (minutes < 60) "${minutes} Min"
        else "${minutes / 60}h ${minutes % 60}m"
    }
}
