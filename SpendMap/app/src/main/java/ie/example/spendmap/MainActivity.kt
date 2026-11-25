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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    // The shake setting starts enabled and is kept in memory for this milestone.
    // Compose observes this value, so the Settings switch updates when it changes.
    private var shakeEnabled by mutableStateOf(true)

    // Remembers whether this Activity is currently in its active state.
    private var isActivityResumed = false

    // Prevents us from registering the same sensor listener more than once.
    private var isSensorListenerRegistered = false

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
                // Pass the current setting to the Home screen.
                // The callback lets the Settings switch update this Activity's value.
                SpendMapApp(
                    shakeEnabled = shakeEnabled,
                    onShakeEnabledChange = { enabled ->
                        updateShakeDetection(enabled)
                    }
                )
            }
        }
    }

    // Start listening for movement when the Activity becomes visible.
    override fun onResume() {
        super.onResume()

        isActivityResumed = true

        // Only listen when the user has enabled shake detection
        // and the phone actually has an accelerometer.
        if (shakeEnabled) {
            startShakeDetection()
        }
    }

    // Stop listening when the Activity is no longer visible.
    override fun onPause() {
        super.onPause()

        isActivityResumed = false
        stopShakeDetection()
    }

    // Register the accelerometer listener when shake detection is enabled.
    private fun startShakeDetection() {

        // If the phone does not have an accelerometer, there is nothing to register.
        val sensor = accelerometer ?: return

        // Avoid registering the same listener more than once.
        if (!isSensorListenerRegistered) {
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )

            isSensorListenerRegistered = true
        }
    }

    // Unregister the listener when shake detection is disabled or the Activity pauses.
    private fun stopShakeDetection() {
        if (isSensorListenerRegistered) {
            sensorManager.unregisterListener(this)
            isSensorListenerRegistered = false
        }
    }

    // Called when the user changes the shake setting in the Settings dialog.
    private fun updateShakeDetection(enabled: Boolean) {

        // Update the setting that is shared with the Home screen.
        shakeEnabled = enabled

        // Apply the change immediately if the Activity is currently visible.
        if (isActivityResumed) {
            if (shakeEnabled) {
                startShakeDetection()
            } else {
                stopShakeDetection()
            }
        }
    }

    // Android calls this method whenever the accelerometer reports new movement.
    override fun onSensorChanged(event: SensorEvent?) {

        // Do nothing if shake detection has been switched off.
        if (!shakeEnabled) return

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
fun SpendMapApp(
    shakeEnabled: Boolean = true,
    onShakeEnabledChange: (Boolean) -> Unit = {}
) {
    HomeScreen(
        shakeEnabled = shakeEnabled,
        onShakeEnabledChange = onShakeEnabledChange
    )
}

// Preview the SpendMap Home screen in Android Studio.
@Preview(showBackground = true)
@Composable
fun SpendMapPreview() {
    SpendMapTheme {
        SpendMapApp()
    }
}