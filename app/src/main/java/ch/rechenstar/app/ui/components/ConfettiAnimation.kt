package ch.rechenstar.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppGrassGreen
import ch.rechenstar.app.ui.theme.AppOrange
import ch.rechenstar.app.ui.theme.AppPurple
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.AppSunYellow
import kotlin.random.Random

private val confettiColors = listOf(
    AppSunYellow, AppSkyBlue, AppCoral, AppGrassGreen, AppOrange, AppPurple
)

private data class Particle(
    val x: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color,
    val shape: Int // 0 = rect, 1 = circle
)

@Composable
fun ConfettiAnimation(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    durationMs: Int = 3000
) {
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    var progress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = durationMs, easing = LinearEasing),
        label = "confetti"
    )

    LaunchedEffect(trigger) {
        if (trigger) {
            val random = Random(System.currentTimeMillis())
            particles = List(particleCount) {
                Particle(
                    x = random.nextFloat(),
                    velocityX = random.nextFloat() * 200f - 100f,
                    velocityY = random.nextFloat() * -600f - 200f,
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = random.nextFloat() * 720f - 360f,
                    size = random.nextFloat() * 8f + 4f,
                    color = confettiColors[random.nextInt(confettiColors.size)],
                    shape = random.nextInt(2)
                )
            }
            progress = 0f
            progress = 1f
        }
    }

    if (particles.isNotEmpty() && animatedProgress > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = animatedProgress
            val gravity = 800f
            val canvasWidth = size.width
            val canvasHeight = size.height
            val alpha = (1f - t).coerceIn(0f, 1f)

            particles.forEach { particle ->
                val timeSeconds = t * (durationMs / 1000f)
                val px = canvasWidth * particle.x + particle.velocityX * timeSeconds
                val py = canvasHeight * 0.5f + particle.velocityY * timeSeconds + 0.5f * gravity * timeSeconds * timeSeconds
                val rotation = particle.rotation + particle.rotationSpeed * timeSeconds

                if (py in -50f..canvasHeight + 50f && px in -50f..canvasWidth + 50f) {
                    rotate(rotation, Offset(px, py)) {
                        val color = particle.color.copy(alpha = alpha * 0.9f)
                        if (particle.shape == 0) {
                            drawRect(
                                color = color,
                                topLeft = Offset(px - particle.size / 2, py - particle.size / 2),
                                size = Size(particle.size, particle.size * 0.6f)
                            )
                        } else {
                            drawCircle(
                                color = color,
                                radius = particle.size / 2,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
        }
    }
}
