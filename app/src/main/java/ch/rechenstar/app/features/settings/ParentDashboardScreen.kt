package ch.rechenstar.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.rechenstar.app.ui.components.AppCard
import ch.rechenstar.app.ui.components.AppProgressBar
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppOrange
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppSunYellow
import ch.rechenstar.app.ui.theme.LightTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    userId: String?,
    onDismiss: () -> Unit,
    viewModel: ParentDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadDashboard(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elternbereich") },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Schliessen")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary cards
            Text("Diese Woche", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Genauigkeit", "${(state.weeklyAccuracy * 100).toInt()}%", AppGrassGreen, Modifier.weight(1f))
                StatCard("Spielzeit", state.weeklyPlayTimeFormatted, AppSkyBlue, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Sessions", "${state.weeklySessions}", AppSunYellow, Modifier.weight(1f))
                StatCard("Aufgaben", "${state.weeklyExercises}", AppCoral, Modifier.weight(1f))
            }

            // Strengths & Weaknesses
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Stärken & Schwächen", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)

                    if (state.categoryStats.isEmpty()) {
                        Text("Noch keine Daten diese Woche", style = MaterialTheme.typography.bodyMedium, color = LightTextSecondary)
                    } else {
                        state.categoryStats.forEach { stat ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stat.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("${stat.correct}/${stat.total}", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                                    Text(
                                        " ${(stat.accuracy * 100).toInt()}%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = accuracyColor(stat.accuracy)
                                    )
                                }
                                AppProgressBar(
                                    progress = stat.accuracy.toFloat(),
                                    color = accuracyColor(stat.accuracy)
                                )
                            }
                        }
                    }
                }
            }

            // Overall stats
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Gesamt", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    OverallStatRow("Aufgaben gelöst", "${state.totalExercises}")
                    OverallStatRow("Sterne gesammelt", "${state.totalStars}")
                    OverallStatRow("Längster Streak", "${state.longestStreak} Tage")
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, padding = 16.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        }
    }
}

@Composable
private fun OverallStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LightTextSecondary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun accuracyColor(accuracy: Double): androidx.compose.ui.graphics.Color {
    return when {
        accuracy >= 0.8 -> AppGrassGreen
        accuracy >= 0.5 -> AppSunYellow
        else -> AppCoral
    }
}
