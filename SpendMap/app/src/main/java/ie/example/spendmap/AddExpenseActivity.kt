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

class AddExpenseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpendMapTheme {
                AddExpenseScreen(
                    onSave = { amount, category, date, note ->

                        // Return the new expense details to the Home screen.
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_AMOUNT, amount)
                            putExtra(EXTRA_CATEGORY, category)
                            putExtra(EXTRA_DATE, date.toString())
                            putExtra(EXTRA_NOTE, note)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onSave: (Double, String, LocalDate, String) -> Unit,
    onCancel: () -> Unit
) {
    // Get the Android context here so it is available to button callbacks.
    val context = LocalContext.current

    // Remember the information entered by the user.
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Display the result of the user's location permission choice.
    var locationPermissionMessage by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Android displays the permission prompt when this launcher is called.
    // We only call it after the user taps the Add location button.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        // The user may grant approximate location, precise location, or both.
        val coarseGranted =
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        val fineGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        hasLocationPermission = coarseGranted || fineGranted

        locationPermissionMessage = if (hasLocationPermission) {
            "Location permission granted."
        } else {
            "Location permission was denied. You can still save this expense without a location."
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

            // Ask for location permission only when the user chooses this option.
            OutlinedButton(
                onClick = {

                    // Check whether the user has already granted either permission.
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    val fineGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (coarseGranted || fineGranted) {
                        hasLocationPermission = true
                        locationPermissionMessage =
                            "Location permission is already granted."
                    } else {
                        // Request approximate and precise location access.
                        // Android lets the user decide which level to allow.
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (hasLocationPermission) {
                        "Location permission granted"
                    } else {
                        "Add location"
                    }
                )
            }

            // Explain the permission result to the user.
            if (locationPermissionMessage.isNotEmpty()) {
                Text(
                    text = locationPermissionMessage,
                    color = if (hasLocationPermission) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Show validation errors for the expense details.
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

                    // Convert the amount and date into their proper data types.
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
                            // Location permission is optional.
                            // Saving an expense works even if permission was denied.
                            onSave(
                                expenseAmount,
                                selectedCategory,
                                expenseDate,
                                note.trim()
                            )
                        }
                    }
                },
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