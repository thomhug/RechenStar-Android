package ch.rechenstar.app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.rechenstar.app.ui.components.AppButton
import ch.rechenstar.app.ui.components.AppButtonVariant
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
                    TextButton(onClick = onDismiss) {
                        Text("Fertig", color = AppSkyBlue)
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
            // Charts section
            ChartSection(state)

            // Summary cards
            SummaryCards(state)

            // Strengths & Weaknesses
            StrengthsSection(state)

            // Focus areas
            if (state.focusAreas.isNotEmpty()) {
                FocusSection(state)
            }

            // Exercise details
            if (state.exerciseDetails.isNotEmpty()) {
                ExerciseDetailsSection(state)
            }

            // Overall stats
            OverallSection(state)

            // Recent sessions
            if (state.recentSessions.isNotEmpty()) {
                RecentSessionsSection(state)
            }

            // Settings
            ParentSettingsSection(state, viewModel)

            // Progress adjustment
            ProgressAdjustmentSection(state, viewModel)

            // Adjustment logs
            if (state.adjustmentLogs.isNotEmpty()) {
                AdjustmentLogsSection(state)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChartSection(state: ParentDashboardUiState) {
    // Aufgaben pro Tag
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Aufgaben pro Tag",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            SimpleBarChart(data = state.weeklyChartData)
        }
    }

    // Genauigkeit
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Genauigkeit",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            AccuracyChart(data = state.weeklyAccuracyData)
        }
    }
}

@Composable
private fun SimpleBarChart(data: List<Int>) {
    val maxValue = data.maxOrNull()?.coerceAtLeast(1) ?: 1
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val chartHeight = 100.dp

    Column {
        // Y labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("$maxValue", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { value ->
                val fraction = value.toFloat() / maxValue.toFloat()
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(chartHeight * fraction.coerceAtLeast(0.02f))
                        .background(
                            color = if (value > 0) AppSkyBlue else LightTextSecondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEach { label ->
                Text(label, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("0", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        }
    }
}

@Composable
private fun AccuracyChart(data: List<Double?>) {
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val chartHeight = 80.dp
    val yLabels = listOf("100%", "75%", "50%", "25%", "0%")

    Row(modifier = Modifier.fillMaxWidth()) {
        // Chart area
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                // Grid lines
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(5) {
                        HorizontalDivider(color = LightTextSecondary.copy(alpha = 0.15f))
                    }
                }

                // Data points
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEach { accuracy ->
                        if (accuracy != null) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = (chartHeight.value * accuracy.toFloat()).dp.coerceAtMost(chartHeight))
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AppGrassGreen)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                    }
                }
            }

            // Day labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayLabels.forEach { label ->
                    Text(label, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                }
            }
        }

        // Y-axis labels
        Column(
            modifier = Modifier
                .height(chartHeight)
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            yLabels.forEach { label ->
                Text(label, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
            }
        }
    }
}

@Composable
private fun SummaryCards(state: ParentDashboardUiState) {
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
}

@Composable
private fun StrengthsSection(state: ParentDashboardUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Stärken & Schwächen",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (state.categoryStats.isEmpty()) {
                Text(
                    "Noch keine Daten diese Woche",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary
                )
            } else {
                state.categoryStats.forEach { stat ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stat.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "${stat.correct}/${stat.total}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
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
}

@Composable
private fun FocusSection(state: ParentDashboardUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Übungsfokus",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            state.focusAreas.forEach { area ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            area.categoryLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (area.example.isNotEmpty()) {
                            Text(
                                area.example,
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        }
                    }
                }
            }
            Text(
                "Diese Aufgaben werden automatisch häufiger gestellt",
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
        }
    }
}

@Composable
private fun ExerciseDetailsSection(state: ParentDashboardUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Aufgaben-Details",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Aufgabe",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    "R",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGrassGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    "F",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppCoral,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    "Zeit",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Best",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = LightTextSecondary.copy(alpha = 0.2f))

            state.exerciseDetails.forEach { detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        detail.exercise,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        "${detail.correct}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppGrassGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        "${detail.wrong}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (detail.wrong > 0) AppCoral else LightTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        detail.avgTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        detail.bestTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverallSection(state: ParentDashboardUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Gesamt",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            OverallStatRow("\u2705 Aufgaben gelöst", "${state.totalExercises}")
            OverallStatRow("\u2B50 Sterne gesammelt", "${state.totalStars}")
            OverallStatRow("\uD83D\uDD25 Längster Streak", "${state.longestStreak} Tage")
            if (state.memberSince.isNotEmpty()) {
                OverallStatRow("\uD83D\uDCC5 Dabei seit", state.memberSince)
            }
        }
    }
}

@Composable
private fun RecentSessionsSection(state: ParentDashboardUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Letzte Sessions",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            state.recentSessions.forEach { session ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            session.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${session.correctCount}/${session.totalCount} richtig",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = AppSunYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "${session.starsEarned}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Accuracy badge
                        val badgeColor = when {
                            session.accuracy >= 0.8 -> AppGrassGreen
                            session.accuracy >= 0.5 -> AppSunYellow
                            else -> AppCoral
                        }
                        Text(
                            "${(session.accuracy * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .background(badgeColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                HorizontalDivider(color = LightTextSecondary.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
private fun ParentSettingsSection(
    state: ParentDashboardUiState,
    viewModel: ParentDashboardViewModel
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Einstellungen",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Break reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pausen-Erinnerung",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.breakReminderEnabled,
                    onCheckedChange = { viewModel.updateBreakReminder(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AppSkyBlue)
                )
            }

            // Break interval
            var expanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pause nach",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { expanded = true }) {
                    Text("${state.breakIntervalMinutes} Min", color = AppSkyBlue)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf(10, 15, 20, 30).forEach { minutes ->
                        DropdownMenuItem(
                            text = { Text("$minutes Min") },
                            onClick = {
                                viewModel.updateBreakInterval(minutes)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressAdjustmentSection(
    state: ParentDashboardUiState,
    viewModel: ParentDashboardViewModel
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Fortschritt anpassen",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            StepperRow("Sterne", state.adjustStars) { delta -> viewModel.adjustStars(delta) }
            StepperRow("Aufgaben (korrekt)", state.adjustExercises) { delta -> viewModel.adjustExercises(delta) }
            StepperRow("Streak (Tage)", state.adjustStreak) { delta -> viewModel.adjustStreak(delta) }

            Text(
                "Level: ${state.currentLevel.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppSkyBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = { viewModel.resetAdjustments() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }
                AppButton(
                    title = "Speichern",
                    variant = AppButtonVariant.DANGER,
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Änderung bestätigen") },
            text = {
                Text("Diese Änderung wird im Protokoll gespeichert und ist sichtbar. Wirklich ändern?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveAdjustments()
                    showConfirmDialog = false
                }) {
                    Text("Ändern", color = AppCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun StepperRow(label: String, value: Int, onAdjust: (Int) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onAdjust(-1) },
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, AppCoral, CircleShape)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = "Verringern",
                    tint = AppCoral,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(80.dp)
                    .border(
                        1.dp,
                        LightTextSecondary.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp)
            )
            IconButton(
                onClick = { onAdjust(1) },
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, AppSkyBlue, CircleShape)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Erhöhen",
                    tint = AppSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AdjustmentLogsSection(state: ParentDashboardUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Änderungsprotokoll",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            state.adjustmentLogs.forEach { log ->
                Column {
                    Text(
                        log.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                    Text(
                        log.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(color = LightTextSecondary.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier, padding = 16.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun accuracyColor(accuracy: Double): androidx.compose.ui.graphics.Color {
    return when {
        accuracy >= 0.8 -> AppGrassGreen
        accuracy >= 0.5 -> AppSunYellow
        else -> AppCoral
    }
}
