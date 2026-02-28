package ch.rechenstar.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.local.dao.UserPreferencesDao
import ch.rechenstar.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val sessionLength: Int = 10,
    val difficultyLevel: Int = 2,
    val adaptiveDifficulty: Boolean = true,
    val dailyGoal: Int = 20,
    val gapFillEnabled: Boolean = true,
    val hideSkipButton: Boolean = false,
    val autoShowAnswerSeconds: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val enabledCategories: List<String> = listOf("addition_10", "subtraction_10"),
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesDao: UserPreferencesDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var userId: String? = null

    fun loadSettings(userId: String) {
        this.userId = userId
        viewModelScope.launch {
            val userWithRelations = userRepository.getUserWithRelations(userId) ?: return@launch
            val prefs = userWithRelations.preferences

            _uiState.value = SettingsUiState(
                sessionLength = prefs?.sessionLength ?: 10,
                difficultyLevel = prefs?.difficultyLevel ?: 2,
                adaptiveDifficulty = prefs?.adaptiveDifficulty ?: true,
                dailyGoal = prefs?.dailyGoal ?: 20,
                gapFillEnabled = prefs?.gapFillEnabled ?: true,
                hideSkipButton = prefs?.hideSkipButton ?: false,
                autoShowAnswerSeconds = prefs?.autoShowAnswerSeconds ?: 0,
                soundEnabled = prefs?.soundEnabled ?: true,
                hapticEnabled = prefs?.hapticEnabled ?: true,
                reducedMotion = prefs?.reducedMotion ?: false,
                enabledCategories = prefs?.enabledCategoriesRaw?.split(",")?.filter { it.isNotBlank() }
                    ?: listOf("addition_10", "subtraction_10"),
                isLoading = false
            )
        }
    }

    fun updateSessionLength(value: Int) {
        _uiState.value = _uiState.value.copy(sessionLength = value)
        savePrefs { it.copy(sessionLength = value) }
    }

    fun updateDifficulty(value: Int) {
        if (value == 0) {
            _uiState.value = _uiState.value.copy(adaptiveDifficulty = true)
            savePrefs { it.copy(adaptiveDifficulty = true) }
        } else {
            _uiState.value = _uiState.value.copy(adaptiveDifficulty = false, difficultyLevel = value)
            savePrefs { it.copy(adaptiveDifficulty = false, difficultyLevel = value) }
        }
    }

    fun updateDailyGoal(value: Int) {
        _uiState.value = _uiState.value.copy(dailyGoal = value)
        savePrefs { it.copy(dailyGoal = value) }
    }

    fun updateHideSkipButton(value: Boolean) {
        _uiState.value = _uiState.value.copy(hideSkipButton = value)
        savePrefs { it.copy(hideSkipButton = value) }
    }

    fun updateAutoShowAnswer(value: Int) {
        _uiState.value = _uiState.value.copy(autoShowAnswerSeconds = value)
        savePrefs { it.copy(autoShowAnswerSeconds = value) }
    }

    fun updateGapFillEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(gapFillEnabled = value)
        savePrefs { it.copy(gapFillEnabled = value) }
    }

    fun updateSoundEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(soundEnabled = value)
        savePrefs { it.copy(soundEnabled = value) }
    }

    fun updateHapticEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(hapticEnabled = value)
        savePrefs { it.copy(hapticEnabled = value) }
    }

    fun updateReducedMotion(value: Boolean) {
        _uiState.value = _uiState.value.copy(reducedMotion = value)
        savePrefs { it.copy(reducedMotion = value) }
    }

    fun toggleCategory(categoryRaw: String, enabled: Boolean) {
        val current = _uiState.value.enabledCategories.toMutableList()
        if (enabled && !current.contains(categoryRaw)) {
            current.add(categoryRaw)
        } else if (!enabled) {
            current.remove(categoryRaw)
        }
        _uiState.value = _uiState.value.copy(enabledCategories = current)
        savePrefs { it.copy(enabledCategoriesRaw = current.joinToString(",")) }
    }

    private fun savePrefs(update: (ch.rechenstar.app.data.local.entity.UserPreferencesEntity) -> ch.rechenstar.app.data.local.entity.UserPreferencesEntity) {
        val uid = userId ?: return
        viewModelScope.launch {
            val prefs = preferencesDao.getPreferencesSync(uid) ?: return@launch
            preferencesDao.updatePreferences(update(prefs))
        }
    }
}
