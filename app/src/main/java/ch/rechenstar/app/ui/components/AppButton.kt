package ch.rechenstar.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppPurple
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppWhite

enum class AppButtonVariant {
    PRIMARY, SECONDARY, SUCCESS, DANGER, GHOST;

    val backgroundColor: Color
        get() = when (this) {
            PRIMARY -> AppSkyBlue
            SECONDARY -> AppPurple
            SUCCESS -> AppGrassGreen
            DANGER -> AppCoral
            GHOST -> Color.Transparent
        }

    val foregroundColor: Color
        get() = when (this) {
            GHOST -> AppSkyBlue
            else -> AppWhite
        }
}

@Composable
fun AppButton(
    title: String,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    val shape = RoundedCornerShape(30.dp)

    if (variant == AppButtonVariant.GHOST) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .defaultMinSize(minWidth = 200.dp, minHeight = 60.dp),
            enabled = enabled && !isLoading,
            shape = shape,
            interactionSource = interactionSource,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = variant.foregroundColor,
                disabledContentColor = Color.Gray
            )
        ) {
            ButtonContent(title, icon, isLoading, variant)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .shadow(8.dp, shape, ambientColor = Color.Black.copy(alpha = 0.1f))
                .defaultMinSize(minWidth = 200.dp, minHeight = 60.dp),
            enabled = enabled && !isLoading,
            shape = shape,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = variant.backgroundColor,
                contentColor = variant.foregroundColor,
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                disabledContentColor = Color.Gray
            )
        ) {
            ButtonContent(title, icon, isLoading, variant)
        }
    }
}

@Composable
private fun ButtonContent(
    title: String,
    icon: ImageVector?,
    isLoading: Boolean,
    variant: AppButtonVariant
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = variant.foregroundColor,
                strokeWidth = 2.dp
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
