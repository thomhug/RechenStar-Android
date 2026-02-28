package ch.rechenstar.app.features.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.domain.model.Difficulty
import ch.rechenstar.app.domain.model.ExerciseCategory
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.service.ExerciseMetrics
import ch.rechenstar.app.ui.components.AppButton
import ch.rechenstar.app.ui.components.AppButtonVariant
import ch.rechenstar.app.ui.components.AppProgressBar
import ch.rechenstar.app.ui.components.ExerciseCard
import ch.rechenstar.app.ui.components.NumberPad
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppOrange
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppSunYellow
import ch.rechenstar.app.ui.theme.LightTextSecondary
import ch.rechenstar.app.ui.theme.NumberFonts
import ch.rechenstar.app.domain.service.SoundService
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ExerciseScreen(
    sessionLength: Int = 10,
    difficulty: Difficulty = Difficulty.EASY,
    categories: List<ExerciseCategory> = listOf(ExerciseCategory.ADDITION_10, ExerciseCategory.SUBTRACTION_10),
    metrics: ExerciseMetrics? = null,
    adaptiveDifficulty: Boolean = true,
    gapFillEnabled: Boolean = true,
    hideSkipButton: Boolean = false,
    autoShowAnswerSeconds: Int = 0,
    onSessionComplete: (List<ExerciseResult>) -> Unit,
    onCancel: (List<ExerciseResult>) -> Unit
) {
    val viewModel = remember {
        ExerciseViewModel(
            sessionLength = sessionLength,
            difficulty = difficulty,
            categories = categories,
            metrics = metrics,
            adaptiveDifficulty = adaptiveDifficulty,
            gapFillEnabled = gapFillEnabled
        )
    }

    var shakeOffset by remember { mutableStateOf(0f) }
    val animatedShake by animateFloatAsState(
        targetValue = shakeOffset,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "shake"
    )

    // Start session
    LaunchedEffect(Unit) {
        viewModel.startSession()
    }

    // Session completion
    LaunchedEffect(viewModel.sessionState) {
        if (viewModel.sessionState == ExerciseViewModel.SessionState.COMPLETED) {
            onSessionComplete(viewModel.sessionResults)
        }
    }

    // Auto-reveal timer
    LaunchedEffect(viewModel.exerciseIndex, viewModel.feedbackState) {
        if (autoShowAnswerSeconds > 0 && viewModel.feedbackState is ExerciseViewModel.FeedbackState.None) {
            delay(autoShowAnswerSeconds * 1000L)
            if (viewModel.feedbackState is ExerciseViewModel.FeedbackState.None) {
                viewModel.autoRevealAnswer()
                triggerShake { shakeOffset = it }
                delay(2500)
                viewModel.clearShowAnswer()
            }
        }
    }

    // Auto-advance after feedback + sound effects
    LaunchedEffect(viewModel.feedbackState) {
        when (val fb = viewModel.feedbackState) {
            is ExerciseViewModel.FeedbackState.Correct -> {
                SoundService.playCorrect()
                delay(1000)
                viewModel.nextExercise()
            }
            is ExerciseViewModel.FeedbackState.Revenge -> {
                SoundService.playRevenge()
                delay(1500)
                viewModel.nextExercise()
            }
            is ExerciseViewModel.FeedbackState.Incorrect -> {
                SoundService.playIncorrect()
                triggerShake { shakeOffset = it }
                delay(1000)
                viewModel.clearIncorrectFeedback()
            }
            is ExerciseViewModel.FeedbackState.WrongOperation -> {
                SoundService.playOperationHint()
                triggerShake { shakeOffset = it }
                delay(2000)
                viewModel.clearIncorrectFeedback()
            }
            is ExerciseViewModel.FeedbackState.ShowAnswer -> {
                delay(2500)
                viewModel.clearShowAnswer()
            }
            else -> {}
        }
    }

    // Encouragement auto-dismiss
    LaunchedEffect(viewModel.showEncouragement) {
        if (viewModel.showEncouragement) {
            delay(2000)
            viewModel.dismissEncouragement()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp)
        ) {
            // Progress section
            ProgressSection(
                progressText = viewModel.progressText,
                difficultyLabel = if (viewModel.adaptiveDifficulty)
                    "Auto: ${viewModel.currentDifficulty.label}"
                else
                    viewModel.currentDifficulty.label,
                totalStars = viewModel.totalStars,
                progressFraction = viewModel.progressFraction.toFloat(),
                onCancel = { onCancel(viewModel.sessionResults) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exercise card
            viewModel.currentExercise?.let { exercise ->
                val d = exercise.displayNumbers
                val isShowingAnswer = viewModel.feedbackState is ExerciseViewModel.FeedbackState.ShowAnswer
                val revealedAnswer = (viewModel.feedbackState as? ExerciseViewModel.FeedbackState.ShowAnswer)?.answer

                ExerciseCard(
                    leftText = d.left,
                    rightText = d.right,
                    resultText = d.result,
                    operation = exercise.type.symbol,
                    showResult = isShowingAnswer,
                    revealedAnswer = revealedAnswer
                )
            }

            // Answer display
            Text(
                text = viewModel.displayAnswer,
                style = NumberFonts.large,
                color = if (viewModel.userAnswer.isEmpty())
                    LightTextSecondary.copy(alpha = 0.4f)
                else
                    AppSkyBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .offset { IntOffset(animatedShake.roundToInt(), 0) }
                    .semantics { contentDescription = "answer-display" }
            )

            // Feedback section
            FeedbackSection(viewModel.feedbackState)

            Spacer(modifier = Modifier.weight(1f))

            // Number pad
            NumberPad(
                onDigit = { viewModel.appendDigit(it) },
                onDelete = { viewModel.deleteLastDigit() },
                onSubmit = { viewModel.submitAnswer() },
                canSubmit = viewModel.canSubmit,
                showNegativeToggle = viewModel.showNegativeToggle,
                onToggleNegative = { viewModel.toggleNegative() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            ActionButtons(
                feedbackState = viewModel.feedbackState,
                hideSkipButton = hideSkipButton,
                onSkip = {
                    viewModel.skipExercise()
                }
            )
        }

        // Encouragement overlay
        AnimatedVisibility(
            visible = viewModel.showEncouragement,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Text(
                text = "Kein Problem! Wir machen mit leichteren Aufgaben weiter.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppSkyBlue)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ProgressSection(
    progressText: String,
    difficultyLabel: String,
    totalStars: Int,
    progressFraction: Float,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = difficultyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightTextSecondary.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AppSunYellow,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = " $totalStars",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .semantics { contentDescription = "cancel-button" }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Abbrechen",
                    tint = LightTextSecondary.copy(alpha = 0.6f)
                )
            }
        }
        AppProgressBar(
            progress = progressFraction,
            color = AppSkyBlue,
            height = 10.dp
        )
    }
}

@Composable
private fun FeedbackSection(feedbackState: ExerciseViewModel.FeedbackState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = feedbackState !is ExerciseViewModel.FeedbackState.None,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            when (feedbackState) {
                is ExerciseViewModel.FeedbackState.Correct -> {
                    StarRow(count = feedbackState.stars.coerceAtMost(3))
                }
                is ExerciseViewModel.FeedbackState.Revenge -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StarRow(count = feedbackState.stars.coerceAtMost(3))
                        Text(
                            text = "Stark! Du hast es geschafft!",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppSunYellow
                        )
                    }
                }
                is ExerciseViewModel.FeedbackState.Incorrect -> {
                    Text(
                        text = "Versuch es nochmal!",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppCoral
                    )
                }
                is ExerciseViewModel.FeedbackState.WrongOperation -> {
                    Text(
                        text = "Achtung, ${feedbackState.correct} nicht ${feedbackState.wrong}!",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppOrange
                    )
                }
                is ExerciseViewModel.FeedbackState.ShowAnswer -> {
                    Text(
                        text = "Die Antwort ist ${feedbackState.answer}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppGrassGreen
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun StarRow(count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AppSunYellow,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    feedbackState: ExerciseViewModel.FeedbackState,
    hideSkipButton: Boolean,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        if (feedbackState is ExerciseViewModel.FeedbackState.None && !hideSkipButton) {
            AppButton(
                title = "Ueberspringen",
                variant = AppButtonVariant.GHOST,
                onClick = onSkip
            )
        }
    }
}

private suspend fun triggerShake(setOffset: (Float) -> Unit) {
    val offsets = listOf(-10f, 10f, -8f, 8f, -4f, 0f)
    for (offset in offsets) {
        setOffset(offset)
        delay(50)
    }
}
