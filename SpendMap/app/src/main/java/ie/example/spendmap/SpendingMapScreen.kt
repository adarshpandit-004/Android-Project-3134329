
package ie.example.spendmap

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// This screen provides the main layout for viewing spending locations.
// The actual expense markers will be added in a later substep.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingMapScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Spending Map")
                },
                navigationIcon = {
                    // Let the user return to the previous screen.
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        // Display the OpenStreetMap view underneath the top bar.
        OpenStreetMapView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
