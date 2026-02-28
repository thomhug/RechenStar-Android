package ch.rechenstar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ch.rechenstar.app.ui.navigation.RechenStarNavigation
import ch.rechenstar.app.ui.theme.RechenStarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RechenStarTheme {
                RechenStarNavigation()
            }
        }
    }
}
