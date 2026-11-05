
package ie.example.spendmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ie.example.spendmap.ui.theme.SpendMapTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SpendMapTheme {
                SpendMapApp()
            }
        }
    }
}

// Main entry point for the SpendMap application
@Composable
fun SpendMapApp() {
    HomeScreen()
}

// Preview the SpendMap Home screen in Android Studio
@Preview(showBackground = true)
@Composable
fun SpendMapPreview() {
    SpendMapTheme {
        SpendMapApp()
    }
}
