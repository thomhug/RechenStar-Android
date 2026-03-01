package ch.rechenstar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.LightTextSecondary
import ch.rechenstar.app.ui.theme.NumberFonts

@Composable
fun ExerciseCard(
    leftText: String,
    rightText: String,
    resultText: String,
    operation: String,
    showResult: Boolean = false,
    revealedAnswer: Int? = null,
    modifier: Modifier = Modifier
) {
    val description = buildString {
        append("$leftText $operation $rightText gleich ")
        if (showResult && revealedAnswer != null) append("$revealedAnswer") else append("unbekannt")
    }

    AppCard(
        modifier = modifier
            .testTag("exercise-card")
            .semantics { contentDescription = description },
        padding = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RevealableText(leftText, showResult, revealedAnswer)
            OperatorText(operation)
            RevealableText(rightText, showResult, revealedAnswer)
            OperatorText("=", isEquals = true)
            RevealableText(resultText, showResult, revealedAnswer)
        }
    }
}

@Composable
private fun RevealableText(
    text: String,
    showResult: Boolean,
    revealedAnswer: Int?
) {
    val isGap = text == "?"
    val displayText = if (isGap && showResult && revealedAnswer != null) "$revealedAnswer" else text
    val color by animateColorAsState(
        targetValue = when {
            isGap && showResult && revealedAnswer != null -> AppGrassGreen
            isGap -> AppSkyBlue
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "textColor"
    )

    Text(
        text = displayText,
        style = NumberFonts.large,
        color = color,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun OperatorText(text: String, isEquals: Boolean = false) {
    Text(
        text = " $text ",
        style = NumberFonts.medium,
        color = if (isEquals) LightTextSecondary else AppSkyBlue
    )
}
