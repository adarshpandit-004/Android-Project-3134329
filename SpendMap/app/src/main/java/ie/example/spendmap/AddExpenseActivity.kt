package ie.example.spendmap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ie.example.spendmap.ui.theme.SpendMapTheme
import java.time.LocalDate

// These states make it clear what is happening during a location request.
private enum class LocationRequestState {
    NOT_REQUESTED,
    FETCHING,
    SUCCESS,
    PERMISSION_DENIED,
    UNAVAILABLE
}

class AddExpenseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpendMapTheme {
                AddExpenseScreen(
                    onSave = { amount, category, date, note, latitude, longitude ->

                        // Return the expense details and optional coordinates to Home.
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_AMOUNT, amount)
                            putExtra(EXTRA_CATEGORY, category)
                            putExtra(EXTRA_DATE, date.toString())
                            putExtra(EXTRA_NOTE, note)

                            // Only include coordinates when a location was obtained.
                            latitude?.let {
                                putExtra(EXTRA_LATITUDE, it)
                            }

                            longitude?.let {
                                putExtra(EXTRA_LONGITUDE, it)
                            }
                        }

                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_AMOUNT = "expense_amount"
        const val EXTRA_CATEGORY = "expense_category"
        const val EXTRA_DATE = "expense_date"
        const val EXTRA_NOTE = "expense_note"
        const val EXTRA_LATITUDE = "expense_latitude"
        const val EXTRA_LONGITUDE = "expense_longitude"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onSave: (
        Double,
        String,
        LocalDate,
        String,
        Double?,
        Double?
    ) -> Unit,
    onCancel: () -> Unit
) {

    // Get the Android context for checking permissions and retrieving location.
    val context = LocalContext.current

    // Remember the details entered by the user.
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Store the optional coordinates returned by the location helper.
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Track the current location request state for clear feedback.
    var locationState by remember {
        mutableStateOf(LocationRequestState.NOT_REQUESTED)
    }

    // Request the current location after permission is available.
    fun retrieveCurrentLocation() {

        locationState = LocationRequestState.FETCHING

        // Clear any coordinates from an earlier request before retrying.
        latitude = null
        longitude = null

        LocationHelper.getCurrentLocation(context) { location ->

            if (
                location != null &&
                location.latitude.isFinite() &&
                location.longitude.isFinite() &&
                location.latitude in -90.0..90.0 &&
                location.longitude in -180.0..180.0
            ) {
                // Keep the valid coordinates for the expense being created.
                latitude = location.latitude
                longitude = location.longitude

                locationState = LocationRequestState.SUCCESS
            } else {
                // Location services may return null if the device cannot
                // determine a position, for example when location is switched off.
                latitude = null
                longitude = null

                locationState = LocationRequestState.UNAVAILABLE
            }
        }
    }

    // Android displays its permission prompt only when this launcher is called.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val coarseGranted =
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        val fineGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        if (coarseGranted || fineGranted) {
            // Permission is available, so continue with the location request.
            retrieveCurrentLocation()
        } else {
            // Permission is optional; the user can still save the expense.
            latitude = null
            longitude = null
            locationState = LocationRequestState.PERMISSION_DENIED
        }
    }

    val categories = listOf(
        "Food",
        "Transport",
        "Shopping",
        "Drinks",
        "Entertainment",
        "Bills",
        "Other"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Expense")
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Enter your expense details below.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Enter the amount spent.
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    errorMessage = ""
                },
                label = { Text("Amount (€)") },
                placeholder = { Text("e.g. 15.50") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Choose one of the available expense categories.
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    placeholder = { Text("Choose a category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                                errorMessage = ""
                            }
                        )
                    }
                }
            }

            // Enter the date in YYYY-MM-DD format.
            OutlinedTextField(
                value = date,
                onValueChange = {
                    date = it
                    errorMessage = ""
                },
                label = { Text("Date") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // The note is optional.
            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                },
                label = { Text("Note (Optional)") },
                placeholder = { Text("What did you spend on?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Ask for permission or retrieve the location when the user taps here.
            OutlinedButton(
                onClick = {

                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    val fineGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (coarseGranted || fineGranted) {
                        // Permission already exists, so retrieve the location.
                        retrieveCurrentLocation()
                    } else {
                        // Request permission only after the user's action.
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                },
                enabled = locationState != LocationRequestState.FETCHING,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (locationState) {
                        LocationRequestState.FETCHING -> "Getting location..."
                        LocationRequestState.SUCCESS -> "Refresh location"
                        LocationRequestState.PERMISSION_DENIED -> "Try adding location again"
                        LocationRequestState.UNAVAILABLE -> "Retry location"
                        LocationRequestState.NOT_REQUESTED -> "Add location"
                    }
                )
            }

            // Explain the result so the user knows whether coordinates were obtained.
            when (locationState) {
                LocationRequestState.FETCHING -> {
                    Text(
                        text = "Please wait while SpendMap retrieves your location.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LocationRequestState.SUCCESS -> {
                    Text(
                        text = "Location added successfully.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LocationRequestState.PERMISSION_DENIED -> {
                    Text(
                        text = "Location permission was denied. You can still save this expense without a location.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LocationRequestState.UNAVAILABLE -> {
                    Text(
                        text = "Your location could not be retrieved. Check that location services are enabled, then try again. You can still save without a location.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LocationRequestState.NOT_REQUESTED -> {
                    // No message is needed before the user requests a location.
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {

                    val expenseAmount = amount.toDoubleOrNull()

                    val expenseDate = runCatching {
                        LocalDate.parse(date.trim())
                    }.getOrNull()

                    when {
                        expenseAmount == null ||
                                !expenseAmount.isFinite() ||
                                expenseAmount <= 0 -> {
                            errorMessage = "Please enter a valid amount."
                        }

                        selectedCategory.isBlank() -> {
                            errorMessage = "Please select an expense category."
                        }

                        expenseDate == null -> {
                            errorMessage = "Please enter a valid date (YYYY-MM-DD)."
                        }

                        else -> {
                            // Return the expense with coordinates when available.
                            // A denied or unavailable location never blocks saving.
                            onSave(
                                expenseAmount,
                                selectedCategory,
                                expenseDate,
                                note.trim(),
                                latitude,
                                longitude
                            )
                        }
                    }
                },
                // Avoid finishing the screen while a location request is still running.
                enabled = locationState != LocationRequestState.FETCHING,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}