
package ie.example.spendmap

import android.content.Context
import android.content.Intent
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
import kotlin.jvm.java

class MainActivity : ComponentActivity(), SensorEventListener {

    // Android uses this manager to access the phone's built-in sensors.
    private lateinit var sensorManager: SensorManager

    // This stores the phone's accelerometer, if the device has one.
    private var accelerometer: Sensor? = null

    // A shake must pass this acceleration threshold to be recognised.
    private val shakeThreshold = 12f

    // This records when the previous shake was detected.
    private var lastShakeTime = 0L

    // Prevents the phone from opening Add Expense repeatedly within one second.
    private val shakeCooldown = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Get the Android sensor service before trying to access the accelerometer.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Check whether the phone has an accelerometer.
        // If it does not, this value remains null and shake detection stays inactive.
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

        // Only register the listener if an accelerometer is available.
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

        // Remove the effect of gravity to focus on the phone's movement.
        val movement = kotlin.math.abs(
            acceleration - SensorManager.GRAVITY_EARTH
        )

        val currentTime = System.currentTimeMillis()

        // Only recognise a shake if it passes the threshold and cooldown.
        if (movement > shakeThreshold &&
            currentTime - lastShakeTime > shakeCooldown
        ) {
            lastShakeTime = currentTime

            // Open the existing Add Expense screen when a shake is detected.
            // This reuses the AddExpenseActivity created in Milestone 1.
            val addExpenseIntent = Intent(
                this,
                AddExpenseActivity::class.java
            )

            startActivity(addExpenseIntent)
        }
    }

    // This callback is not needed for our accelerometer functionality.
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
