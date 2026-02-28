package ch.rechenstar.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.ui.components.AppCard
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppOrange
import ch.rechenstar.app.ui.theme.AppPurple
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppSunYellow
import ch.rechenstar.app.ui.theme.LightTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onDismiss: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("So funktioniert's") },
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
            HelpSection(
                icon = Icons.Filled.PlayCircle,
                color = AppSkyBlue,
                title = "Spielen",
                text = "Tippe auf \"Spielen\", um eine Runde zu starten. Du bekommst Rechenaufgaben mit Plus, Minus und Mal. Tippe die Antwort auf dem Zahlenfeld ein und druecke den gruenen Haken."
            )
            HelpSection(
                icon = Icons.Filled.Star,
                color = AppSunYellow,
                title = "Sterne sammeln",
                text = "Fuer jede richtige Antwort bekommst du Sterne. Beim ersten Versuch gibt es 2 Sterne, beim zweiten noch 1 Stern. Nach zwei falschen Antworten wird die Loesung gezeigt."
            )
            HelpSection(
                icon = Icons.Filled.EmojiEvents,
                color = AppOrange,
                title = "Erfolge",
                text = "Schalte Erfolge frei, indem du viele Aufgaben loest, Serien aufbaust oder besonders schnell rechnest."
            )
            HelpSection(
                icon = Icons.AutoMirrored.Filled.ShowChart,
                color = AppGrassGreen,
                title = "Fortschritt",
                text = "Im Fortschritt-Tab siehst du dein Level und deinen Skill. Dein Level steigt mit der Anzahl geloester Aufgaben, dein Skill zeigt deine Genauigkeit der letzten 7 Tage."
            )
            HelpSection(
                icon = Icons.Filled.Settings,
                color = LightTextSecondary,
                title = "Einstellungen",
                text = "Passe die Aufgabentypen, Toene und die Anzahl der Aufgaben pro Runde an. Eltern koennen auch den Ueberspringen-Knopf ausblenden."
            )
            HelpSection(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                color = AppPurple,
                title = "Elternbereich",
                text = "Eltern koennen ueber die Einstellungen den Elternbereich oeffnen. Nach einer Rechenaufgabe fuer Erwachsene seht ihr detaillierte Statistiken."
            )
        }
    }
}

@Composable
private fun HelpSection(icon: ImageVector, color: Color, title: String, text: String) {
    AppCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = LightTextSecondary)
            }
        }
    }
}
