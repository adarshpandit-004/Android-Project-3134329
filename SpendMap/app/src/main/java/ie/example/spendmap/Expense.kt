
package ie.example.spendmap

import java.time.LocalDate

data class Expense(
    val id: Int,
    val amount: Double,
    val category: String,
    val date: LocalDate,
    val note: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null   //null values
)
