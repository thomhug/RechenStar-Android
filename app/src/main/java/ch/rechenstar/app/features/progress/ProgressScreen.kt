package ch.rechenstar.app.features.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
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
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.Level
import ch.rechenstar.app.ui.components.AppCard
import ch.rechenstar.app.ui.components.AppProgressBar
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppOrange
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppSunYellow
import ch.rechenstar.app.ui.theme.LightTextSecondary

data class ProgressUiState(
    val totalExercises: Int = 0,
    val totalStars: Int = 0,
    val currentStreak: Int = 0,
    val currentLevel: Level = Level.ANFAENGER,
    val levelProgress: Double = 0.0,
    val nextLevelExercises: Int? = null,
    val dailyGoal: Int = 20,
    val dailyCompleted: Int = 0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val weeklyData: List<Int> = List(7) { 0 },
    val isLoading: Boolean = true
)

data class CategoryStat(
    val category: ExerciseCategory,
    val accuracy: Double
)

@Composable
fun ProgressScreen(
    userId: String?,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadProgress(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Level badge
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Level info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.currentLevel.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    state.nextLevelExercises?.let { next ->
                        AppProgressBar(
                            progress = state.levelProgress.toFloat(),
                            color = AppSunYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Noch ${next - state.totalExercises} Aufgaben",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextSecondary
                        )
                    } ?: run {
                        Text(
                            text = "Höchstes Level erreicht!",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppSunYellow
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daily goal
        val dailyDone = state.dailyCompleted >= state.dailyGoal
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = if (dailyDone) AppGrassGreen else AppSkyBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("Tagesziel", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${state.dailyCompleted} von ${state.dailyGoal} Aufgaben", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (dailyDone) {
                        Text("Geschafft!", style = MaterialTheme.typography.bodySmall, color = AppGrassGreen)
                    }
                }
                AppProgressBar(
                    progress = (state.dailyCompleted.toFloat() / state.dailyGoal).coerceIn(0f, 1f),
                    color = if (dailyDone) AppGrassGreen else AppSkyBlue,
                    height = 10.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniCard(
                value = "${state.totalExercises}",
                label = "Aufgaben",
                color = AppGrassGreen,
                modifier = Modifier.weight(1f)
            )
            MiniCard(
                value = "${state.currentStreak}",
                label = "Tage-Serie",
                color = AppOrange,
                modifier = Modifier.weight(1f)
            )
            MiniCard(
                value = "${state.totalStars}",
                label = "Sterne",
                color = AppSunYellow,
                modifier = Modifier.weight(1f)
            )
        }

        // Category strengths
        if (state.categoryStats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Deine Stärken", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    state.categoryStats.forEach { stat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stat.category.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "${(stat.accuracy * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = strengthColor(stat.accuracy)
                            )
                        }
                        AppProgressBar(
                            progress = stat.accuracy.toFloat(),
                            color = strengthColor(stat.accuracy)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniCard(value: String, label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, padding = 12.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        }
    }
}

private fun strengthColor(accuracy: Double): androidx.compose.ui.graphics.Color {
    return when {
        accuracy >= 0.8 -> AppGrassGreen
        accuracy >= 0.5 -> AppSunYellow
        else -> AppCoral
    }
}
