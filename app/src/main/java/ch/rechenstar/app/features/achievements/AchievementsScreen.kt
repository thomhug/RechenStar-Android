package ch.rechenstar.app.features.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.rechenstar.app.ui.components.AchievementCard
import ch.rechenstar.app.ui.components.AchievementData
import ch.rechenstar.app.ui.theme.AppSunYellow
import ch.rechenstar.app.ui.theme.LightTextSecondary

@Composable
fun AchievementsScreen(
    userId: String?,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadAchievements(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        if (state.totalCount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = AppSunYellow,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${state.unlockedCount} von ${state.totalCount} freigeschaltet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary
                )
            }
        }

        state.achievements.forEach { item ->
            AchievementCard(
                achievement = AchievementData(
                    title = item.type.title,
                    description = item.type.description,
                    icon = item.type.icon,
                    progress = item.progressFraction,
                    progressText = if (item.isUnlocked) "${item.target}/${item.target}" else "${item.progress}/${item.target}"
                ),
                isUnlocked = item.isUnlocked,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (state.achievements.isEmpty() && !state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = LightTextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Spiele eine Runde, um Erfolge freizuschalten!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
