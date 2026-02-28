package ch.rechenstar.app.features.achievements

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.local.dao.AchievementDao
import ch.rechenstar.app.domain.model.AchievementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementItem(
    val type: AchievementType,
    val progress: Int,
    val target: Int,
    val isUnlocked: Boolean,
    val progressFraction: Float
)

data class AchievementsUiState(
    val achievements: List<AchievementItem> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementDao: AchievementDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    fun loadAchievements(userId: String) {
        viewModelScope.launch {
            val entities = achievementDao.getAllForUserSync(userId)

            val items = entities.mapNotNull { entity ->
                val type = AchievementType.fromRawValue(entity.typeRawValue) ?: return@mapNotNull null
                AchievementItem(
                    type = type,
                    progress = entity.progress,
                    target = entity.target,
                    isUnlocked = entity.unlockedAt != null,
                    progressFraction = if (entity.target > 0) {
                        entity.progress.toFloat() / entity.target.toFloat()
                    } else 0f
                )
            }.sortedWith(compareByDescending<AchievementItem> { it.isUnlocked }.thenByDescending { it.progressFraction })

            _uiState.value = AchievementsUiState(
                achievements = items,
                unlockedCount = items.count { it.isUnlocked },
                totalCount = items.size,
                isLoading = false
            )
        }
    }
}
