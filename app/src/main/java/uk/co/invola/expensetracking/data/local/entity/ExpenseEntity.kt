package uk.co.invola.expensetracking.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.invola.expensetracking.domain.model.Amount
import java.util.Date

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index(value = ["categoryId"])],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    // Original amount and currency as entered by user (embedded as separate columns)
    @Embedded(prefix = "original_")
    val originalAmount: Amount,
    // Converted amount in base currency (USD) (embedded as separate columns)
    @Embedded(prefix = "usd_")
    val usdAmount: Amount,
    // Foreign key reference to category
    val categoryId: Long?,
    val date: Date,
)
