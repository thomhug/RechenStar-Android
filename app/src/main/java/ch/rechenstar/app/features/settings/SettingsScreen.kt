package ch.rechenstar.app.features.settings

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.ui.components.AppCard
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.LightTextSecondary

@Composable
fun SettingsScreen(
    userId: String?,
    onShowHelp: () -> Unit,
    onShowParentArea: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadSettings(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Gameplay
        SettingsCard {
            Text("Spieleinstellungen", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            SettingsDropdown(
                label = "Aufgaben pro Runde",
                options = listOf(5 to "5", 10 to "10", 15 to "15", 20 to "20"),
                selected = state.sessionLength,
                onSelected = { viewModel.updateSessionLength(it) }
            )

            SettingsDropdown(
                label = "Schwierigkeit",
                options = listOf(
                    0 to "Automatisch",
                    1 to "Sehr leicht",
                    2 to "Leicht",
                    3 to "Mittel",
                    4 to "Schwer"
                ),
                selected = if (state.adaptiveDifficulty) 0 else state.difficultyLevel,
                onSelected = { viewModel.updateDifficulty(it) }
            )

            SettingsDropdown(
                label = "Tägliches Ziel",
                options = listOf(
                    10 to "10 Aufgaben",
                    20 to "20 Aufgaben",
                    30 to "30 Aufgaben",
                    50 to "50 Aufgaben"
                ),
                selected = state.dailyGoal,
                onSelected = { viewModel.updateDailyGoal(it) }
            )

            SettingsToggle(
                title = "Überspringen ausblenden",
                subtitle = "Versteckt den Überspringen-Button während der Übung",
                checked = state.hideSkipButton,
                onCheckedChange = { viewModel.updateHideSkipButton(it) }
            )

            SettingsDropdown(
                label = "Automatisch Lösung zeigen",
                options = listOf(0 to "Aus", 5 to "Nach 5s", 10 to "Nach 10s", 20 to "Nach 20s"),
                selected = state.autoShowAnswerSeconds,
                onSelected = { viewModel.updateAutoShowAnswer(it) }
            )

            SettingsToggle(
                title = "Lückenaufgaben",
                subtitle = "Auch Aufgaben wie ? + 4 = 7 stellen",
                checked = state.gapFillEnabled,
                onCheckedChange = { viewModel.updateGapFillEnabled(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories
        SettingsCard {
            Text("Aufgabentypen", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            listOf("Addition", "Subtraktion", "Multiplikation").forEach { group ->
                Text(group, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary, modifier = Modifier.padding(top = 8.dp))

                ExerciseCategory.entries.filter { it.groupLabel == group }.forEach { category ->
                    val enabled = state.enabledCategories.contains(category.rawValue)
                    val isLastEnabled = enabled && state.enabledCategories.size == 1

                    SettingsToggle(
                        title = category.label,
                        checked = enabled,
                        enabled = !isLastEnabled || !enabled,
                        onCheckedChange = { viewModel.toggleCategory(category.rawValue, it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Audio
        SettingsCard {
            Text("Ton & Haptik", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            SettingsToggle(
                title = "Töne",
                checked = state.soundEnabled,
                onCheckedChange = { viewModel.updateSoundEnabled(it) }
            )
            SettingsToggle(
                title = "Vibration",
                checked = state.hapticEnabled,
                onCheckedChange = { viewModel.updateHapticEnabled(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Display
        SettingsCard {
            Text("Darstellung", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            SettingsDropdown(
                label = "Schriftgrösse",
                options = listOf("normal" to "Normal", "gross" to "Gross"),
                selected = state.fontSize,
                onSelected = { viewModel.updateFontSize(it) }
            )

            SettingsToggle(
                title = "Weniger Animationen",
                subtitle = "Reduziert Konfetti, Wackeln und Übergänge",
                checked = state.reducedMotion,
                onCheckedChange = { viewModel.updateReducedMotion(it) }
            )

            SettingsDropdown(
                label = "Erscheinungsbild",
                options = listOf("auto" to "Automatisch", "light" to "Hell", "dark" to "Dunkel"),
                selected = state.appearance,
                onSelected = { viewModel.updateAppearance(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Help
        SettingsCard {
            TextButton(onClick = onShowHelp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, null, tint = AppSkyBlue, modifier = Modifier.size(24.dp))
                    Text(
                        "So funktioniert's",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
                    Icon(Icons.Filled.ChevronRight, null, tint = LightTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Parent area
        SettingsCard {
            TextButton(onClick = onShowParentArea, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.People, null, tint = AppSkyBlue, modifier = Modifier.size(24.dp))
                    Text(
                        "Elternbereich",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
                    Icon(Icons.Filled.ChevronRight, null, tint = LightTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = LightTextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column { content() }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = AppSkyBlue)
        )
    }
}

@Composable
private fun <T> SettingsDropdown(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selected }?.second ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        TextButton(onClick = { expanded = true }) {
            Text(selectedLabel, color = AppSkyBlue)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
