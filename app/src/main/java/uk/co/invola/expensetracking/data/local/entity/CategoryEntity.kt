package uk.co.invola.expensetracking.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["iconId"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Category name (e.g., "Groceries", "Entertainment")
    val name: String,
    // Unique icon identifier (matches CategoryIcon enum iconId)
    val iconId: String,
)
