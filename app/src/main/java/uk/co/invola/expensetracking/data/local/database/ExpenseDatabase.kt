package uk.co.invola.expensetracking.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uk.co.invola.expensetracking.data.local.converter.DateConverter
import uk.co.invola.expensetracking.data.local.dao.CategoryDao
import uk.co.invola.expensetracking.data.local.dao.ExpenseDao
import uk.co.invola.expensetracking.data.local.dao.IncomeDao
import uk.co.invola.expensetracking.data.local.entity.CategoryEntity
import uk.co.invola.expensetracking.data.local.entity.ExpenseEntity
import uk.co.invola.expensetracking.data.local.entity.IncomeEntity

@Database(
    entities = [ExpenseEntity::class, IncomeEntity::class, CategoryEntity::class],
    version = 11,
    exportSchema = false,
)
@TypeConverters(DateConverter::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    abstract fun incomeDao(): IncomeDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "expense_database"
    }
}
