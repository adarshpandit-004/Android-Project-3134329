
package ie.example.spendmap

import android.app.Activity
import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.example.spendmap.ui.theme.SpendMapTheme
import java.time.LocalDate

class ExpenseDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The Home screen sends the ID of the expense the user selected.
        val expenseId = intent.getIntExtra(EXTRA_EXPENSE_ID, -1)

        // If no valid ID arrives, there is no expense to display.
        if (expenseId == -1) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            SpendMapTheme {
                ExpenseDetailsScreen(
                    expenseId = expenseId,
                    onSave = { amount, category, date, note ->
                        // Send the edited values back to the Home screen.
                        val result = Intent().apply {
                            putExtra(EXTRA_EXPENSE_ID, expenseId)
                            putExtra(EXTRA_AMOUNT, amount)
                            putExtra(EXTRA_CATEGORY, category)
                            putExtra(EXTRA_DATE, date)
                            putExtra(EXTRA_NOTE, note)
                            putExtra(EXTRA_ACTION, ACTION_EDIT)
                        }

                        setResult(Activity.RESULT_OK, result)
                        finish()
                    },
                    onDelete = {
                        // Tell the Home screen which expense should be removed.
                        val result = Intent().apply {
                            putExtra(EXTRA_EXPENSE_ID, expenseId)
                            putExtra(EXTRA_ACTION, ACTION_DELETE)
                        }

                        setResult(Activity.RESULT_OK, result)
                        finish()
                    },
                    onCancel = {
                        // Closing without saving leaves the original expense unchanged.
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_AMOUNT = "expense_amount"
        const val EXTRA_CATEGORY = "expense_category"
        const val EXTRA_DATE = "expense_date"
        const val EXTRA_NOTE = "expense_note"
        const val EXTRA_ACTION = "expense_action"

        const val ACTION_EDIT = "edit"
        const val ACTION_DELETE = "delete"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailsScreen(
    expenseId: Int,
    onSave: (Double, String, String, String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    // Find the selected expense in the sample data for now.
    val expense = sampleExpenses.firstOrNull { it.id == expenseId }

    if (expense == null) {
        LaunchedEffect(Unit) {
            onCancel()
        }
        return
    }

    // These fields start with the expense's existing values.
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var category by remember { mutableStateOf(expense.category) }
    var date by remember { mutableStateOf(expense.date.toString()) }
    var note by remember { mutableStateOf(expense.note) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details") }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Expense #$expenseId",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (€)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Show validation feedback instead of accepting invalid values.
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    val validDate = try {
                        LocalDate.parse(date)
                    } catch (exception: Exception) {
                        null
                    }

                    when {
                        parsedAmount == null || parsedAmount <= 0.0 ->
                            errorMessage = "Enter a valid amount greater than zero."

                        category.isBlank() ->
                            errorMessage = "Please enter a category."

                        validDate == null ->
                            errorMessage = "Enter a valid date in YYYY-MM-DD format."

                        else -> onSave(
                            parsedAmount,
                            category.trim(),
                            date,
                            note.trim()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Expense")
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
