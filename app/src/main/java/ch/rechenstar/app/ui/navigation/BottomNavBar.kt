package ch.rechenstar.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.ui.theme.AppSkyBlue

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val contentDescription: String
)

val tabs = listOf(
    TabItem("Spielen", Icons.Filled.PlayCircle, "tab-spielen"),
    TabItem("Fortschritt", Icons.AutoMirrored.Filled.ShowChart, "tab-fortschritt"),
    TabItem("Erfolge", Icons.Filled.EmojiEvents, "tab-erfolge"),
    TabItem("Einstellungen", Icons.Filled.Settings, "tab-einstellungen")
)

@Composable
fun BottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTab
            val color by animateColorAsState(
                targetValue = if (isSelected) AppSkyBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "tabColor"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(vertical = 4.dp)
                    .semantics { contentDescription = tab.contentDescription },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}
