package ch.rechenstar.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.rechenstar.app.ui.components.AppButton
import ch.rechenstar.app.ui.components.AppButtonVariant
import ch.rechenstar.app.ui.components.NumberPad
import ch.rechenstar.app.ui.theme.AppCoral
import ch.rechenstar.app.ui.theme.AppSkyBlue
import ch.rechenstar.app.ui.theme.LightTextSecondary
import ch.rechenstar.app.ui.theme.NumberFonts
import kotlin.random.Random

@Composable
fun ParentGateScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var firstNumber by remember { mutableStateOf(0) }
    var secondNumber by remember { mutableStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firstNumber = Random.nextInt(10, 50)
        secondNumber = Random.nextInt(10, 50)
    }

    val correctAnswer = firstNumber + secondNumber

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Elternbereich",
            style = MaterialTheme.typography.titleLarge,
            color = AppSkyBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Bitte loese diese Aufgabe, um fortzufahren:",
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$firstNumber + $secondNumber = ?",
            style = NumberFonts.large,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (userAnswer.isEmpty()) "_" else userAnswer,
            style = NumberFonts.medium,
            color = if (userAnswer.isEmpty()) LightTextSecondary.copy(alpha = 0.4f) else AppSkyBlue
        )

        if (showError) {
            Text(
                text = "Falsch, versuch es nochmal!",
                style = MaterialTheme.typography.bodyMedium,
                color = AppCoral,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        NumberPad(
            onDigit = { digit ->
                if (userAnswer.length < 4) {
                    userAnswer += "$digit"
                    showError = false
                }
            },
            onDelete = {
                if (userAnswer.isNotEmpty()) {
                    userAnswer = userAnswer.dropLast(1)
                    showError = false
                }
            },
            onSubmit = {
                val answer = userAnswer.toIntOrNull()
                if (answer == correctAnswer) {
                    onSuccess()
                } else {
                    showError = true
                    userAnswer = ""
                }
            },
            canSubmit = userAnswer.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppButton(
            title = "Abbrechen",
            variant = AppButtonVariant.GHOST,
            onClick = onCancel
        )
    }
}
