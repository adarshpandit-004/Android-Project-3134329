
package ie.example.spendmap

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    val context = LocalContext.current

    // Keep the expenses in a state-aware list.
    // Compose will recompose the screen whenever this list changes.
    val expenses = remember {
        mutableStateListOf<Expense>().apply {
            addAll(sampleExpenses)
        }
    }

    // Temporary monthly budget until we build the budget settings.
    val monthlyBudget = 1000.0

    // Open AddExpenseActivity and wait for the user's result.
    val addExpenseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        // Only process the result if the user saved an expense.
        if (result.resultCode == android.app.Activity.RESULT_OK) {

            val data = result.data

            if (data != null) {

                // Read the expense information sent by AddExpenseActivity.
                val amount = data.getDoubleExtra(
                    AddExpenseActivity.EXTRA_AMOUNT,
                    0.0
                )

                val category = data.getStringExtra(
                    AddExpenseActivity.EXTRA_CATEGORY
                )

                val dateText = data.getStringExtra(
                    AddExpenseActivity.EXTRA_DATE
                )

                val note = data.getStringExtra(
                    AddExpenseActivity.EXTRA_NOTE
                ) ?: ""

                // Convert the returned date text into a LocalDate.
                val expenseDate = runCatching {
                    LocalDate.parse(dateText)
                }.getOrNull()

                // Make sure all the required information is available.
                if (
                    amount > 0 &&
                    category != null &&
                    expenseDate != null
                ) {

                    // Generate an ID for the new expense.
                    val newId = (expenses.maxOfOrNull { it.id } ?: 0) + 1

                    // Create the new Expense object.
                    val newExpense = Expense(
                        id = newId,
                        amount = amount,
                        category = category,
                        date = expenseDate,
                        note = note
                    )

                    // Add it to the list.
                    expenses.add(newExpense)
                }
            }
        }
    }

    // Only calculate spending for the current month.
    val monthlyExpenses = expenses.filter {
        YearMonth.from(it.date) == YearMonth.now()
    }

    val totalSpent = monthlyExpenses.sumOf { it.amount }
    val remainingBudget = monthlyBudget - totalSpent

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SpendMap",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {

                    // An explicit Intent tells Android exactly
                    // which Activity we want to open.
                    val intent = Intent(
                        context,
                        AddExpenseActivity::class.java
                    )

                    // Launch Add Expense and wait for its result.
                    addExpenseLauncher.launch(intent)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Display the monthly spending summary.
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "Monthly Spending",
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "€%.2f".format(totalSpent),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BudgetInfo(
                                title = "Budget",
                                amount = monthlyBudget
                            )

                            BudgetInfo(
                                title = "Remaining",
                                amount = remainingBudget
                            )
                        }
                    }
                }
            }

            // Heading for the expense list.
            item {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Display the newest expenses first.
            items(
                items = expenses.sortedByDescending { it.date },
                key = { it.id }
            ) { expense ->

                ExpenseItem(expense = expense)
            }
        }
    }
}

@Composable
fun BudgetInfo(
    title: String,
    amount: Double
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "€%.2f".format(amount),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ExpenseItem(expense: Expense) {

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = expense.note.ifBlank { "No note added" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = expense.date.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "- €%.2f".format(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
