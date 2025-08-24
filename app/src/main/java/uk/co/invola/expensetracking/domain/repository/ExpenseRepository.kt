package uk.co.invola.expensetracking.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.domain.model.ExchangeRate
import uk.co.invola.expensetracking.domain.model.Expense
import java.util.Date

/**
 * Repository interface for expense operations
 */
interface ExpenseRepository {
    /**
     * Get all expenses with pagination
     */
    fun getAllExpensesPaged(): Flow<PagingData<Expense>>

    /**
     * Get recent expenses with date filtering (limited to first 10)
     * @param startDate Start date for filtering (null for no start filter)
     * @param limit Maximum number of expenses to return (default 10)
     */
    fun getRecentExpenses(
        startDate: Date? = null,
        limit: Int = 10,
    ): Flow<List<Expense>>

    /**
     * Insert a new expense
     */
    suspend fun insertExpense(expense: Expense): Long

    /**
     * Get total expenses amount in base currency (USD)
     */
    fun getTotalConvertedExpensesAmount(): Flow<Double?>

    /**
     * Get exchange rates
     */
    suspend fun getExchangeRates(): Result<ExchangeRate>
}
