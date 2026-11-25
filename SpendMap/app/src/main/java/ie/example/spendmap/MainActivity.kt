
package ie.example.spendmap

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ie.example.spendmap.ui.theme.SpendMapTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    // Android uses this manager to access the phone's built-in sensors.
    private lateinit var sensorManager: SensorManager

    // This stores the phone's accelerometer sensor.
    private var accelerometer: Sensor? = null

    // A shake must be strong enough to pass this acceleration threshold.
    private val shakeThreshold = 12f

    // This records the time of the previous detected shake.
    private var lastShakeTime = 0L

    // Prevents repeated shake detection within one second.
    private val shakeCooldown = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Get the sensor service provided by Android.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Find the phone's accelerometer, if one is available.
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            SpendMapTheme {
                SpendMapApp()
            }
        }
    }

    // Start listening for movement when the activity becomes visible.
    override fun onResume() {
        super.onResume()

        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    // Stop listening when the activity is no longer visible.
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // Android calls this method whenever the accelerometer reports new movement.
    override fun onSensorChanged(event: SensorEvent?) {

        // Ignore events that do not come from the accelerometer.
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate the total acceleration across all three directions.
        val acceleration = kotlin.math.sqrt(x * x + y * y + z * z)

        // Compare the movement against gravity to identify strong movement.
        val movement = kotlin.math.abs(
            acceleration - SensorManager.GRAVITY_EARTH
        )

        val currentTime = System.currentTimeMillis()

        // Only recognise a shake if it passes the threshold and cooldown.
        if (movement > shakeThreshold &&
            currentTime - lastShakeTime > shakeCooldown
        ) {
            lastShakeTime = currentTime

            // Step 1 only detects the shake.
            // Step 2 will connect this detection to Add Expense.
        }
    }

    // This sensor callback is not needed for the accelerometer.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

// Main entry point for the SpendMap application.
@Composable
fun SpendMapApp() {
    HomeScreen()
}

// Preview the SpendMap Home screen in Android Studio.
@Preview(showBackground = true)
@Composable
fun SpendMapPreview() {
    SpendMapTheme {
        SpendMapApp()
    }
}
