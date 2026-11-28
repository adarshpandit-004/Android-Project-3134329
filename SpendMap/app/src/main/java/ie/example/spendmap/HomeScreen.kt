
package ie.example.spendmap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate

// Prepare a monthly spending summary and open Android's sharing menu.
fun shareMonthlySummary(
    context: Context,
    totalSpent: Double,
    monthlyBudget: Double,
    expenseCount: Int
) {
    val remainingBudget = monthlyBudget - totalSpent

    // Build a readable message that the user can send to someone.
    val summary = """
        SpendMap - Monthly Spending Summary

        Total expenses: $expenseCount
        Total spent: €${"%.2f".format(totalSpent)}
        Monthly budget: €${"%.2f".format(monthlyBudget)}
        Remaining budget: €${"%.2f".format(remainingBudget)}

        Tracked using SpendMap.
    """.trimIndent()

    // ACTION_SEND asks Android to find an app that can share text.
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "My SpendMap Monthly Summary")
        putExtra(Intent.EXTRA_TEXT, summary)
    }

    // Let the user choose which installed app should receive the summary.
    context.startActivity(
        Intent.createChooser(shareIntent, "Share monthly summary")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    shakeEnabled: Boolean,
    onShakeEnabledChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // Keep expenses in Compose state so the screen refreshes after changes.
    val expenses = remember {
        mutableStateListOf<Expense>().apply {
            addAll(sampleExpenses)
        }
    }

    // This is the starting monthly budget. The user can change it below.
    var monthlyBudget by remember { mutableStateOf(1000.0) }

    // Controls whether the budget editing window is visible.
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("1000.00") }

    // Controls whether the Settings window is visible.
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Controls whether Home or the Spending Map is currently displayed.
    var showSpendingMap by remember { mutableStateOf(false) }

    // Open the Add Expense screen and wait for the new expense details.
    val addExpenseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            if (data != null) {
                val amount = data.getDoubleExtra(
                    AddExpenseActivity.EXTRA_AMOUNT, 0.0
                )
                val category = data.getStringExtra(
                    AddExpenseActivity.EXTRA_CATEGORY
                ) ?: ""
                val dateText = data.getStringExtra(
                    AddExpenseActivity.EXTRA_DATE
                ) ?: ""
                val note = data.getStringExtra(
                    AddExpenseActivity.EXTRA_NOTE
                ) ?: ""

                // Read the coordinates only if the Add Expense screen returned them.
                // This keeps the location optional when permission was denied
                // or the phone could not retrieve its current location.
                val latitude = if (
                    data.hasExtra(AddExpenseActivity.EXTRA_LATITUDE)
                ) {
                    data.getDoubleExtra(
                        AddExpenseActivity.EXTRA_LATITUDE,
                        0.0
                    )
                } else {
                    null
                }

                val longitude = if (
                    data.hasExtra(AddExpenseActivity.EXTRA_LONGITUDE)
                ) {
                    data.getDoubleExtra(
                        AddExpenseActivity.EXTRA_LONGITUDE,
                        0.0
                    )
                } else {
                    null
                }

                // Convert the returned date into a LocalDate before saving it.
                val date = try {
                    LocalDate.parse(dateText)
                } catch (exception: Exception) {
                    null
                }

                if (amount > 0.0 && category.isNotBlank() && date != null) {
                    val newId = (expenses.maxOfOrNull { it.id } ?: 0) + 1

                    // Create the new expense with its optional location coordinates.
                    expenses.add(
                        Expense(
                            id = newId,
                            amount = amount,
                            category = category,
                            date = date,
                            note = note,
                            latitude = latitude,
                            longitude = longitude
                        )
                    )
                }
            }
        }
    }

    // Open the Details screen and wait for an edit or delete instruction.
    val detailsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            if (data != null) {
                val expenseId = data.getIntExtra(
                    ExpenseDetailsActivity.EXTRA_EXPENSE_ID, -1
                )
                val action = data.getStringExtra(
                    ExpenseDetailsActivity.EXTRA_ACTION
                )

                // Find the selected expense so we can update or remove it.
                val expenseIndex = expenses.indexOfFirst { it.id == expenseId }

                if (expenseIndex != -1) {
                    when (action) {
                        ExpenseDetailsActivity.ACTION_EDIT -> {
                            val amount = data.getDoubleExtra(
                                ExpenseDetailsActivity.EXTRA_AMOUNT, 0.0
                            )
                            val category = data.getStringExtra(
                                ExpenseDetailsActivity.EXTRA_CATEGORY
                            ) ?: ""
                            val dateText = data.getStringExtra(
                                ExpenseDetailsActivity.EXTRA_DATE
                            ) ?: ""
                            val note = data.getStringExtra(
                                ExpenseDetailsActivity.EXTRA_NOTE
                            ) ?: ""

                            val date = try {
                                LocalDate.parse(dateText)
                            } catch (exception: Exception) {
                                null
                            }

                            if (amount > 0.0 && category.isNotBlank() && date != null) {

                                // Keep the original coordinates when the user edits
                                // the expense's amount, category, date or note.
                                val existingExpense = expenses[expenseIndex]

                                // Replace the old item with the edited version.
                                expenses[expenseIndex] = Expense(
                                    id = expenseId,
                                    amount = amount,
                                    category = category,
                                    date = date,
                                    note = note,
                                    latitude = existingExpense.latitude,
                                    longitude = existingExpense.longitude
                                )
                            }
                        }

                        ExpenseDetailsActivity.ACTION_DELETE -> {
                            // Removing the item from the list updates the Home screen.
                            expenses.removeAt(expenseIndex)
                        }
                    }
                }
            }
        }
    }

    // Only count expenses that belong to the current month and year.
    val currentMonthExpenses = expenses.filter {
        it.date.month == LocalDate.now().month &&
                it.date.year == LocalDate.now().year
    }

    val totalSpent = currentMonthExpenses.sumOf { it.amount }
    val remainingBudget = monthlyBudget - totalSpent
    // Calculate how much of the monthly budget has been used.
    // Keep the value between 0 and 1 because the progress bar uses that range.
    val budgetProgress = (totalSpent / monthlyBudget)
        .toFloat()
        .coerceIn(0f, 1f)

    // Display the map while keeping the existing expense list in this screen's state.
    if (showSpendingMap) {
        SpendingMapScreen(
            expenses = expenses,
            onBack = {
                showSpendingMap = false
            },
            onExpenseClick = { expense ->

                // Open the existing Details screen for the selected map marker.
                val intent = Intent(
                    context,
                    ExpenseDetailsActivity::class.java
                ).apply {

                    // Pass the expense ID so the existing result handler
                    // can identify which expense should be edited or deleted.
                    putExtra(
                        ExpenseDetailsActivity.EXTRA_EXPENSE_ID,
                        expense.id
                    )

                    // Pass the remaining details needed by the Details screen.
                    putExtra(
                        ExpenseDetailsActivity.EXTRA_AMOUNT,
                        expense.amount
                    )
                    putExtra(
                        ExpenseDetailsActivity.EXTRA_CATEGORY,
                        expense.category
                    )
                    putExtra(
                        ExpenseDetailsActivity.EXTRA_DATE,
                        expense.date.toString()
                    )
                    putExtra(
                        ExpenseDetailsActivity.EXTRA_NOTE,
                        expense.note
                    )
                }

                // Reuse the existing launcher so edits and deletions are handled
                // by the same result callback as expenses opened from Home.
                detailsLauncher.launch(intent)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SpendMap") },
                    actions = {

                        // Open the Spending Map and pass it the current expenses.
                        TextButton(
                            onClick = {
                                showSpendingMap = true
                            }
                        ) {
                            Text("Spending Map")
                        }

                        // Open Settings without leaving the existing Home screen.
                        TextButton(
                            onClick = {
                                showSettingsDialog = true
                            }
                        ) {
                            Text("Settings")
                        }

                        // Keep the existing monthly budget editing option.
                        TextButton(
                            onClick = {
                                budgetInput = monthlyBudget.toString()
                                showBudgetDialog = true
                            }
                        ) {
                            Text("Edit budget")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // Explicit Intent opens our own Add Expense Activity.
                        val intent = Intent(context, AddExpenseActivity::class.java)
                        addExpenseLauncher.launch(intent)
                    }
                ) {
                    Text("+")
                }
            }
        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Monthly spending",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "€%.2f".format(totalSpent),
                                style = MaterialTheme.typography.headlineLarge
                            )

                            Text(text = "Budget: €%.2f".format(monthlyBudget))
                            LinearProgressIndicator(
                                progress = { budgetProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (remainingBudget < 0) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Text(
                                text = if (remainingBudget >= 0) {
                                    "Remaining: €%.2f".format(remainingBudget)
                                } else {
                                    "Over budget by: €%.2f".format(-remainingBudget)
                                },
                                color = if (remainingBudget < 0) {
                                    // Use the theme's warning/error colour when spending exceeds the budget.
                                    MaterialTheme.colorScheme.error
                                } else {
                                    // Keep the normal card text colour while the budget is still available.
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )

                            // Share the current month's totals and expense count.
                            Button(
                                onClick = {
                                    shareMonthlySummary(
                                        context = context,
                                        totalSpent = totalSpent,
                                        monthlyBudget = monthlyBudget,
                                        expenseCount = currentMonthExpenses.size
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Share Monthly Summary")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Recent expenses",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (expenses.isEmpty()) {
                    item {
                        Text("No expenses yet. Tap + to add your first expense.")
                    }
                } else {
                    items(
                        items = expenses.sortedByDescending { it.date },
                        key = { it.id }
                    ) { expense ->

                        // Tapping an expense opens its Details screen.
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(
                                        context,
                                        ExpenseDetailsActivity::class.java
                                    ).apply {
                                        putExtra(
                                            ExpenseDetailsActivity.EXTRA_EXPENSE_ID,
                                            expense.id
                                        )
                                        putExtra(
                                            ExpenseDetailsActivity.EXTRA_AMOUNT,
                                            expense.amount
                                        )
                                        putExtra(
                                            ExpenseDetailsActivity.EXTRA_CATEGORY,
                                            expense.category
                                        )
                                        putExtra(
                                            ExpenseDetailsActivity.EXTRA_DATE,
                                            expense.date.toString()
                                        )
                                        putExtra(
                                            ExpenseDetailsActivity.EXTRA_NOTE,
                                            expense.note
                                        )
                                    }

                                    detailsLauncher.launch(intent)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = expense.category,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = expense.note.ifBlank { "No note" },
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        text = expense.date.toString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    text = "€%.2f".format(expense.amount),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settings dialog contains the shake detection on/off switch.
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSettingsDialog = false
                },
                title = {
                    Text("Settings")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Shake to add expense",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Shake your phone to open the Add Expense screen.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Switch(
                                checked = shakeEnabled,
                                onCheckedChange = { enabled ->
                                    // Tell MainActivity to enable or disable the sensor.
                                    onShakeEnabledChange(enabled)
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSettingsDialog = false
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        }

        // A small dialog lets the user change their monthly spending limit.
        if (showBudgetDialog) {
            AlertDialog(
                onDismissRequest = { showBudgetDialog = false },
                title = { Text("Monthly budget") },
                text = {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Budget amount (€)") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newBudget = budgetInput.toDoubleOrNull()

                            if (newBudget != null && newBudget > 0.0) {
                                monthlyBudget = newBudget
                                showBudgetDialog = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Enter a valid budget greater than zero.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBudgetDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
