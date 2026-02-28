package ch.rechenstar.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppSunYellow

data class AchievementData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val progress: Float,
    val progressText: String? = null
)

@Composable
fun AchievementCard(
    achievement: AchievementData,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val accessibilityLabel = "${achievement.title}, " +
        "${if (isUnlocked) "freigeschaltet" else "gesperrt"}, " +
        "${(achievement.progress * 100).toInt()} Prozent"

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityLabel },
        backgroundColor = if (isUnlocked)
            MaterialTheme.colorScheme.surface
        else
            Color.Gray.copy(alpha = 0.1f),
        shadowElevation = if (isUnlocked) 4.dp else 1.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = if (isUnlocked)
                    AppSunYellow.copy(alpha = 0.2f)
                else
                    Color.Gray.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = achievement.icon,
                        contentDescription = null,
                        tint = if (isUnlocked) AppSunYellow else Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isUnlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        Color.Gray
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        Color.Gray.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppProgressBar(
                        progress = achievement.progress,
                        color = if (isUnlocked) AppSunYellow else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    achievement.progressText?.let { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUnlocked)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else
                                Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Status icon
            Icon(
                imageVector = if (isUnlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = if (isUnlocked) "Freigeschaltet" else "Gesperrt",
                tint = if (isUnlocked) AppGrassGreen else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
