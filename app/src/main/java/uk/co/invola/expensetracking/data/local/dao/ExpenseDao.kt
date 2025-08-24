package uk.co.invola.expensetracking.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.data.local.entity.ExpenseEntity
import uk.co.invola.expensetracking.data.local.entity.ExpenseWithCategory
import java.util.Date

@Dao
interface ExpenseDao {
    /**
     * Get all expenses with pagination support (with categories)
     */
    @Transaction
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesPaged(): PagingSource<Int, ExpenseWithCategory>

    /**
     * Get all expenses as Flow for real-time updates (with categories)
     */
    @Transaction
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseWithCategory>>

    /**
     * Get expenses after a specific date (with categories)
     */
    @Transaction
    @Query("SELECT * FROM expenses WHERE date >= :startDate ORDER BY date DESC")
    fun getExpensesAfterDate(startDate: Date): Flow<List<ExpenseWithCategory>>

    /**
     * Insert a new expense
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    /**
     * Get total expenses amount in base currency (USD)
     * Using embedded column names
     */
    @Query("SELECT SUM(usd_value) FROM expenses")
    fun getTotalConvertedExpensesAmount(): Flow<Double?>
}
