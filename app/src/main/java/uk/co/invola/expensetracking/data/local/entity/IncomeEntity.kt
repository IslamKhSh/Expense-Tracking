package uk.co.invola.expensetracking.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import uk.co.invola.expensetracking.domain.model.Amount
import java.util.Date

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Monthly income amount (embedded as separate columns)
    @Embedded(prefix = "monthly_")
    val monthlyAmount: Amount,
    // USD equivalent of monthly income (embedded as separate columns)
    @Embedded(prefix = "usd_")
    val usdAmount: Amount,
    // Month and year this income applies to (YYYY-MM format)
    val monthYear: String,
    // When this income record was created
    val date: Date = Date(),
)
