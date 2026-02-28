package ch.rechenstar.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 20.dp,
    shadowElevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        shadowElevation = shadowElevation
    ) {
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
