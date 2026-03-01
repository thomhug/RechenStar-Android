package ch.rechenstar.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppSkyBlue

@Composable
fun NumberPad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean,
    showNegativeToggle: Boolean = false,
    onToggleNegative: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: 1 2 3
        NumberRow(listOf(1, 2, 3), onDigit)
        // Row 2: 4 5 6
        NumberRow(listOf(4, 5, 6), onDigit)
        // Row 3: 7 8 9
        NumberRow(listOf(7, 8, 9), onDigit)
        // Row 4: delete 0 submit (like iOS layout)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showNegativeToggle) {
                ActionPadButton(
                    icon = Icons.Filled.Remove,
                    description = "Negativ",
                    color = AppSkyBlue,
                    tag = "negative-button",
                    onClick = onToggleNegative
                )
            } else {
                ActionPadButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    description = "Löschen",
                    color = AppCoral,
                    tag = "delete-button",
                    onClick = onDelete
                )
            }
            NumberPadButton(number = 0, onClick = onDigit)
            ActionPadButton(
                icon = Icons.Filled.CheckCircle,
                description = "Fertig",
                color = if (canSubmit) AppGrassGreen else Color.Gray,
                enabled = canSubmit,
                tag = "submit-button",
                onClick = onSubmit
            )
        }
        // Extra row for delete when negative toggle is shown
        if (showNegativeToggle) {
            ActionPadButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                description = "Löschen",
                color = AppCoral,
                tag = "delete-button",
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun NumberRow(numbers: List<Int>, onDigit: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        numbers.forEach { number ->
            NumberPadButton(number = number, onClick = onDigit)
        }
    }
}

@Composable
fun NumberPadButton(
    number: Int,
    size: Dp = 72.dp,
    onClick: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "padScale"
    )

    Surface(
        onClick = { onClick(number) },
        modifier = Modifier
            .size(size)
            .scale(scale)
            .testTag("number-pad-$number")
            .semantics { contentDescription = "$number" },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ActionPadButton(
    icon: ImageVector,
    description: String,
    color: Color,
    size: Dp = 72.dp,
    enabled: Boolean = true,
    tag: String? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "actionScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .scale(scale)
            .testTag(tag ?: description)
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (enabled) color else Color.Gray,
                modifier = Modifier.size(size * 0.4f)
            )
        }
    }
}
