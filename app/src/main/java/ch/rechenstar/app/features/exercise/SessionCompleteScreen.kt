package ch.rechenstar.app.features.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.domain.model.AchievementType
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.model.Level
import ch.rechenstar.app.ui.components.AppButton
import ch.rechenstar.app.ui.components.AppButtonVariant
import ch.rechenstar.app.ui.components.AppCard
import ch.rechenstar.app.domain.service.SoundService
import ch.rechenstar.app.ui.components.ConfettiAnimation
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppOrange
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppSunYellow
import ch.rechenstar.app.ui.theme.LightTextSecondary

data class EngagementResult(
    val newlyUnlockedAchievements: List<AchievementType> = emptyList(),
    val currentStreak: Int = 0,
    val isNewStreak: Boolean = false,
    val dailyGoalReached: Boolean = false,
    val newLevel: Level? = null
)

@Composable
fun SessionCompleteScreen(
    results: List<ExerciseResult>,
    sessionLength: Int,
    engagement: EngagementResult = EngagementResult(),
    onDismiss: () -> Unit
) {
    val attemptedResults = results.filter { !it.wasSkipped }
    val totalStars = results.sumOf { it.stars }
    val maxStars = sessionLength * 2
    val correctCount = attemptedResults.count { it.isCorrect }
    val accuracy = if (attemptedResults.isNotEmpty()) correctCount.toDouble() / attemptedResults.size else 0.0
    val totalTime = attemptedResults.sumOf { it.timeSpent }
    val skippedCount = results.count { it.wasSkipped }

    val motivationText = when {
        accuracy >= 0.9 -> "Fantastisch!"
        accuracy >= 0.7 -> "Super gemacht!"
        accuracy >= 0.5 -> "Gut gemacht!"
        else -> "Toll, dass du geübt hast!"
    }

    val formattedTime = run {
        val seconds = totalTime.toInt()
        val minutes = seconds / 60
        val secs = seconds % 60
        if (minutes > 0) "${minutes}:${secs.toString().padStart(2, '0')} Min" else "$secs Sek"
    }

    // Play session complete sound
    LaunchedEffect(Unit) {
        SoundService.playSessionComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .semantics { contentDescription = "session-complete" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Geschafft!",
            style = MaterialTheme.typography.displayLarge,
            color = AppSkyBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stars
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalStars.coerceAtMost(5)) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = AppSunYellow,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            text = "$totalStars von $maxStars Sternen",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Level Up
        engagement.newLevel?.let { newLevel ->
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AppSkyBlue.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Level Up!",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppSkyBlue
                    )
                    Text(
                        text = "Du bist jetzt ${newLevel.title}!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppCard(modifier = Modifier.weight(1f), padding = 12.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, null, tint = AppGrassGreen, modifier = Modifier.size(24.dp))
                    Text("$correctCount/${results.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        if (skippedCount > 0) "$skippedCount übersprungen" else "Richtig",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (skippedCount > 0) AppSunYellow else LightTextSecondary
                    )
                }
            }
            AppCard(modifier = Modifier.weight(1f), padding = 12.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("%", style = MaterialTheme.typography.titleLarge, color = AppSkyBlue)
                    Text("${(accuracy * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Genauigkeit", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                }
            }
            AppCard(modifier = Modifier.weight(1f), padding = 12.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⏱", style = MaterialTheme.typography.titleLarge)
                    Text(formattedTime, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Zeit", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Motivation
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = motivationText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (accuracy < 0.7) {
            Text(
                text = "Du hast $correctCount Aufgaben richtig gelöst — das ist ein guter Anfang!",
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Daily goal
        if (engagement.dailyGoalReached) {
            Spacer(modifier = Modifier.height(12.dp))
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AppSunYellow.copy(alpha = 0.12f)
            ) {
                Column {
                    Text("Tagesziel geschafft!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Du hast dein Ziel für heute erreicht.", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                }
            }
        }

        // Category breakdown
        val categoryGroups = attemptedResults
            .groupBy { it.exercise.category }
            .map { (cat, catResults) ->
                Triple(cat, catResults.count { it.isCorrect }, catResults.size)
            }
            .sortedBy { it.first.rawValue }

        if (categoryGroups.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nach Kategorie", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    categoryGroups.forEach { (cat, correct, total) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("$correct/$total richtig", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                        }
                    }
                }
            }
        }

        // Streak
        if (engagement.currentStreak > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AppOrange.copy(alpha = 0.1f)
            ) {
                Column {
                    Text("${engagement.currentStreak} Tage am Stück!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    if (engagement.isNewStreak) {
                        Text("Weiter so!", style = MaterialTheme.typography.bodySmall, color = AppOrange)
                    }
                }
            }
        }

        // New achievements
        if (engagement.newlyUnlockedAchievements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Neue Erfolge!", style = MaterialTheme.typography.headlineMedium, color = AppSunYellow)
            engagement.newlyUnlockedAchievements.forEach { type ->
                Spacer(modifier = Modifier.height(8.dp))
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AppSunYellow.copy(alpha = 0.08f)
                ) {
                    Column {
                        Text(type.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text(type.description, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            title = "Weiter",
            variant = AppButtonVariant.PRIMARY,
            icon = Icons.Filled.Home,
            modifier = Modifier.semantics { contentDescription = "done-button" },
            onClick = onDismiss
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Confetti overlay — nur bei >= 60% Genauigkeit
    if (accuracy >= 0.6) {
        ConfettiAnimation(trigger = true)
    }
    }
}
