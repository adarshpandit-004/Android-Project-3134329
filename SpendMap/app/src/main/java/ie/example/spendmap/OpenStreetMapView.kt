
package ie.example.spendmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun OpenStreetMapView(
    expenses: List<Expense>,
    modifier: Modifier = Modifier,
    latitude: Double = 53.3498,
    longitude: Double = -6.2603,
    zoom: Double = 12.0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Remember one map view while this screen remains on display.
    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitude, longitude))

            // Display the required OpenStreetMap copyright information.
            overlays.add(CopyrightOverlay(context))
        }
    }

    // Pass Android lifecycle events to the map so it can manage resources.
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->

            // Remove the previous expense markers before drawing the latest list.
            map.overlays.removeAll { it is Marker }

            // Only expenses with valid coordinates can be placed on the map.
            expenses.forEach { expense ->
                val expenseLatitude = expense.latitude
                val expenseLongitude = expense.longitude

                if (
                    expenseLatitude != null &&
                    expenseLongitude != null &&
                    expenseLatitude in -90.0..90.0 &&
                    expenseLongitude in -180.0..180.0
                ) {
                    val marker = Marker(map).apply {
                        position = GeoPoint(
                            expenseLatitude,
                            expenseLongitude
                        )

                        // Show the category and amount when the marker is tapped.
                        title = expense.category
                        snippet = "€%.2f".format(expense.amount)
                    }

                    map.overlays.add(marker)
                }
            }

            map.invalidate()
        }
    )
}
