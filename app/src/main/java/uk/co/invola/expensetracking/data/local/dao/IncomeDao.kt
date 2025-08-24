package uk.co.invola.expensetracking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.data.local.entity.IncomeEntity

@Dao
interface IncomeDao {
    /**
     * Get income for a specific month-year
     */
    @Query("SELECT * FROM income WHERE monthYear = :monthYear LIMIT 1")
    suspend fun getIncomeForMonth(monthYear: String): IncomeEntity?

    /**
     * Get current month's income (latest entry)
     */
    @Query("SELECT * FROM income ORDER BY date DESC LIMIT 1")
    suspend fun getCurrentMonthIncome(): IncomeEntity?

    /**
     * Insert or update income for a month
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    /**
     * Get total USD income for all months
     */
    @Query("SELECT SUM(usd_value) FROM income")
    fun getTotalUsdIncome(): Flow<Double?>
}
