
package ie.example.spendmap

import java.time.LocalDate

val sampleExpenses = listOf(

    Expense(
        id = 1,
        amount = 6.50,
        category = "Food",
        date = LocalDate.now(),
        note = "Lunch at campus"
    ),

    Expense(
        id = 2,
        amount = 12.00,
        category = "Transport",
        date = LocalDate.now().minusDays(1),
        note = "Bus journey"
    ),

    Expense(
        id = 3,
        amount = 3.20,
        category = "Drinks",
        date = LocalDate.now().minusDays(2),
        note = "Coffee"
    ),

    Expense(
        id = 4,
        amount = 25.00,
        category = "Shopping",
        date = LocalDate.now().minusDays(3),
        note = "Groceries"
    ),

    Expense(
        id = 5,
        amount = 8.50,
        category = "Food",
        date = LocalDate.now().minusDays(4),
        note = "Dinner"
    )
)
