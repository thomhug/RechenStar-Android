package ch.rechenstar.app.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.rechenstar.app.ui.components.AppButton
import ch.rechenstar.app.ui.components.AppButtonVariant
import ch.rechenstar.app.ui.components.AppProgressBar
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppSunYellow
import ch.rechenstar.app.ui.theme.LightTextSecondary

@Composable
fun HomeScreen(
    userId: String?,
    onStartExercise: (HomeUiState) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadUser(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "RechenStar",
            style = MaterialTheme.typography.displayLarge,
            color = AppSkyBlue
        )

        if (state.userName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hallo, ${state.userName}!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = AppSunYellow,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = " ${state.totalStars} Sterne gesammelt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DailyGoalSection(
                completed = state.dailyCompleted,
                goal = state.dailyGoal
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        AppButton(
            title = "Spielen",
            variant = AppButtonVariant.PRIMARY,
            icon = Icons.Filled.PlayArrow,
            modifier = Modifier.semantics { contentDescription = "play-button" },
            onClick = { onStartExercise(state) }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DailyGoalSection(completed: Int, goal: Int) {
    val fraction = (completed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val done = completed >= goal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Tagesziel: $completed/$goal",
            style = MaterialTheme.typography.bodySmall,
            color = LightTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        AppProgressBar(
            progress = fraction,
            color = if (done) AppGrassGreen else AppSkyBlue,
            modifier = Modifier.width(200.dp)
        )
    }
}
