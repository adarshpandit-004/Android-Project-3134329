
package ie.example.spendmap

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
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

                        Toast.makeText(
                            this,
                            "Expense details saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onSave: (Double, String, LocalDate, String) -> Unit,
    onCancel: () -> Unit
) {

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

            // Expense amount
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    errorMessage = ""
                },
                label = {
                    Text("Amount (€)")
                },
                placeholder = {
                    Text("e.g. 15.50")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Expense category
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
                    label = {
                        Text("Category")
                    },
                    placeholder = {
                        Text("Choose a category")
                    },
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
                            text = {
                                Text(category)
                            },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                                errorMessage = ""
                            }
                        )
                    }
                }
            }

            // Expense date
            OutlinedTextField(
                value = date,
                onValueChange = {
                    date = it
                    errorMessage = ""
                },
                label = {
                    Text("Date")
                },
                placeholder = {
                    Text("YYYY-MM-DD")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Optional note
            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                },
                label = {
                    Text("Note (Optional)")
                },
                placeholder = {
                    Text("What did you spend on?")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Display validation message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save expense
            Button(
                onClick = {

                    val expenseAmount = amount.toDoubleOrNull()
                    val expenseDate = runCatching {
                        LocalDate.parse(date.trim())
                    }.getOrNull()

                    when {
                        expenseAmount == null || expenseAmount <= 0 -> {
                            errorMessage = "Please enter a valid amount."
                        }

                        selectedCategory.isBlank() -> {
                            errorMessage = "Please select an expense category."
                        }

                        expenseDate == null -> {
                            errorMessage = "Please enter a valid date (YYYY-MM-DD)."
                        }

                        else -> {
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

            // Cancel button
            androidx.compose.material3.OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
