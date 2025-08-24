package uk.co.invola.expensetracking.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.domain.model.Income

interface IncomeRepository {
    /**
     * Get income for a specific month-year
     */
    suspend fun getIncomeForMonth(monthYear: String): Income?

    /**
     * Get current month's income
     */
    suspend fun getCurrentMonthIncome(): Income?

    /**
     * Get total USD income for all months
     */
    fun getTotalUsdIncome(): Flow<Double?>

    /**
     * Initialize default income (static $4000 for current month)
     */
    suspend fun initializeDefaultIncome(): Long
}
