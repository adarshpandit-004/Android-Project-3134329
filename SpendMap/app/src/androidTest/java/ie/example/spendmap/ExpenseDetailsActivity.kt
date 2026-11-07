
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.example.spendmap.ui.theme.SpendMapTheme
import java.time.LocalDate

class ExpenseDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read the selected expense details sent from the Home screen.
        val expenseId = intent.getIntExtra(EXTRA_EXPENSE_ID, -1)
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""
        val date = intent.getStringExtra(EXTRA_DATE) ?: ""
        val note = intent.getStringExtra(EXTRA_NOTE) ?: ""

        // Close the screen if the Intent does not contain a valid expense.
        if (expenseId == -1) {
            finish()
            return
        }

        setContent {
            SpendMapTheme {
                ExpenseDetailsScreen(
                    expenseId = expenseId,
                    startingAmount = amount.toString(),
                    startingCategory = category,
                    startingDate = date,
                    startingNote = note,

                    // Return the edited information to the Home screen.
                    onSave = { newAmount, newCategory, newDate, newNote ->
                        val result = Intent().apply {
                            putExtra(EXTRA_EXPENSE_ID, expenseId)
                            putExtra(EXTRA_AMOUNT, newAmount)
                            putExtra(EXTRA_CATEGORY, newCategory)
                            putExtra(EXTRA_DATE, newDate)
                            putExtra(EXTRA_NOTE, newNote)
                            putExtra(EXTRA_ACTION, ACTION_EDIT)
                        }

                        setResult(Activity.RESULT_OK, result)
                        finish()
                    },

                    // Tell the Home screen to remove this expense.
                    onDelete = {
                        val result = Intent().apply {
                            putExtra(EXTRA_EXPENSE_ID, expenseId)
                            putExtra(EXTRA_ACTION, ACTION_DELETE)
                        }

                        setResult(Activity.RESULT_OK, result)
                        finish()
                    },

                    // Return without changing anything.
                    onCancel = {
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
    startingAmount: String,
    startingCategory: String,
    startingDate: String,
    startingNote: String,
    onSave: (Double, String, String, String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    // Keep the existing expense information in editable screen fields.
    var amount by remember(expenseId) { mutableStateOf(startingAmount) }
    var category by remember(expenseId) { mutableStateOf(startingCategory) }
    var date by remember(expenseId) { mutableStateOf(startingDate) }
    var note by remember(expenseId) { mutableStateOf(startingNote) }
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

            // Display a message when the user enters invalid information.
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

                    // LocalDate checks that the date is a real calendar date.
                    val validDate = try {
                        LocalDate.parse(date)
                    } catch (exception: Exception) {
                        null
                    }

                    when {
                        parsedAmount == null || parsedAmount <= 0.0 ->
                            errorMessage = "Enter an amount greater than zero."

                        category.isBlank() ->
                            errorMessage = "Please enter a category."

                        validDate == null ->
                            errorMessage = "Enter a valid date (YYYY-MM-DD)."

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
