
package ie.example.spendmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay

@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    latitude: Double = 53.3498,
    longitude: Double = -6.2603,
    zoom: Double = 12.0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create the native OSMDroid map and keep a reference to it.
    val mapView = androidx.compose.runtime.remember {
        MapView(context).apply {
            setMultiTouchControls(true)

            // Start the map at the supplied location and zoom level.
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitude, longitude))

            // Display the required OpenStreetMap contributor credit.
            overlays.add(CopyrightOverlay(context))
        }
    }

    // OSMDroid needs to know when the screen pauses or resumes.
    // This prevents map resources from continuing unnecessarily in the background.
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

            // Release the map when this composable leaves the screen.
            mapView.onDetach()
        }
    }

    // AndroidView allows the traditional Android MapView to appear
    // inside our Jetpack Compose interface.
    AndroidView(
        modifier = modifier,
        factory = { mapView }
    )
}
