
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
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import kotlin.collections.filter
import kotlin.math.max

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

    // Create the map once so normal Compose updates do not recreate it.
    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitude, longitude))

            // Display the required OpenStreetMap copyright information.
            overlays.add(CopyrightOverlay(context))
        }
    }

    // Forward the screen lifecycle events to the native map.
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

            // Remove the previous expense markers before adding the latest ones.
            map.overlays.removeAll { it is Marker }

            // Keep only expenses with valid latitude and longitude values.
            val locatedExpenses = expenses.filter { expense ->
                val expenseLatitude = expense.latitude
                val expenseLongitude = expense.longitude

                expenseLatitude != null &&
                        expenseLongitude != null &&
                        expenseLatitude in -90.0..90.0 &&
                        expenseLongitude in -180.0..180.0
            }

            // Add one map marker for every expense that has a valid location.
            locatedExpenses.forEach { expense ->
                val marker = Marker(map).apply {
                    position = GeoPoint(
                        expense.latitude!!,
                        expense.longitude!!
                    )

                    title = expense.category
                    snippet = "€%.2f".format(expense.amount)
                }

                map.overlays.add(marker)
            }

            // Automatically adjust the camera to show the located expenses.
            when (locatedExpenses.size) {

                0 -> {
                    // No saved locations yet, so use the default Dublin view.
                    map.controller.setZoom(zoom)
                    map.controller.setCenter(GeoPoint(latitude, longitude))
                }

                1 -> {
                    // With one marker, centre the map on that expense.
                    val expense = locatedExpenses.first()

                    map.controller.setZoom(15.0)
                    map.controller.setCenter(
                        GeoPoint(
                            expense.latitude!!,
                            expense.longitude!!
                        )
                    )
                }

                else -> {
                    // Find the northern, southern, eastern and western limits.
                    val north = locatedExpenses.maxOf {
                        it.latitude!!
                    }
                    val south = locatedExpenses.minOf {
                        it.latitude!!
                    }
                    val east = locatedExpenses.maxOf {
                        it.longitude!!
                    }
                    val west = locatedExpenses.minOf {
                        it.longitude!!
                    }

                    // Add a little space around the markers so they are not
                    // placed directly against the edge of the visible map.
                    val latitudePadding = max(
                        (north - south) * 0.15,
                        0.002
                    )
                    val longitudePadding = max(
                        (east - west) * 0.15,
                        0.002
                    )

                    val bounds = BoundingBox(
                        minOf(north + latitudePadding, 90.0),
                        minOf(east + longitudePadding, 180.0),
                        maxOf(south - latitudePadding, -90.0),
                        maxOf(west - longitudePadding, -180.0)
                    )

                    // Zoom out until all located expenses fit inside the map.
                    map.zoomToBoundingBox(bounds, true, 100)
                }
            }

            // Refresh the map so the latest markers and camera position appear.
            map.invalidate()
        }
    )
}
