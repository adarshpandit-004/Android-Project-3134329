package ie.example.spendmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// This helper keeps location retrieval separate from the screen.
// Other parts of SpendMap can use it without repeating the GPS code.
object LocationHelper {

    fun getCurrentLocation(
        context: Context,
        onResult: (Location?) -> Unit
    ) {

        // Check whether approximate or precise location permission is available.
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasFinePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Do not attempt to access location without the user's permission.
        if (!hasCoarsePermission && !hasFinePermission) {
            onResult(null)
            return
        }

        // Get Google's fused location service.
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        // This token belongs to this individual location request.
        val cancellationTokenSource = CancellationTokenSource()

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            )
                .addOnSuccessListener { location ->

                    // Android can return null when it cannot determine a location.
                    onResult(location)
                }
                .addOnFailureListener {

                    // Report retrieval errors without crashing the application.
                    onResult(null)
                }
                .addOnCanceledListener {

                    // Treat a cancelled request as an unavailable location.
                    onResult(null)
                }

        } catch (exception: SecurityException) {

            // Permission may have changed while the request was being made.
            onResult(null)

        } catch (exception: Exception) {

            // Handle other unexpected location-service errors safely.
            onResult(null)
        }
    }
}