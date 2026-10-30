
package ie.example.spendmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun SpendMapApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Text(
            text = "SpendMap",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpendMapPreview() {
    SpendMapTheme {
        SpendMapApp()
    }
}
