
package ie.example.spendmap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ie.example.spendmap.ui.theme.SpendMapTheme
import java.time.LocalDate

class AddExpenseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpendMapTheme {
                AddExpenseScreen(
                    onSave = { amount, category, date, note ->

                        // Create a result Intent to carry the new expense
                        // back to the Home Activity.
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_AMOUNT, amount)
                            putExtra(EXTRA_CATEGORY, category)
                            putExtra(EXTRA_DATE, date.toString())
                            putExtra(EXTRA_NOTE, note)
                        }

                        // Tell Android that the expense was saved successfully.
                        setResult(Activity.RESULT_OK, resultIntent)

                        // Close this screen and return to Home.
                        finish()
                    },
                    onCancel = {
                        // Close the screen without returning an expense.
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        // These keys are used by both Activities to exchange expense details.
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

    // These variables remember what the user types or selects.
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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

            // The amount field accepts decimal numbers.
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

            // Let the user choose from our predefined categories.
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

            // The date is entered using the ISO format YYYY-MM-DD.
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

            // A note is optional, so the user can leave this empty.
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

            // Show a helpful message if the required details are invalid.
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

                    // Convert the entered text into the correct data types.
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
                            // All required information is valid.
                            // Send it to the Activity to return to Home.
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
