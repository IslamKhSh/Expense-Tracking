package uk.co.invola.expensetracking.data.repository

import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.data.local.dao.IncomeDao
import uk.co.invola.expensetracking.data.mapper.toDomain
import uk.co.invola.expensetracking.data.mapper.toEntity
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Income
import uk.co.invola.expensetracking.domain.repository.IncomeRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl
    @Inject
    constructor(
        private val incomeDao: IncomeDao,
    ) : IncomeRepository {
        override suspend fun getIncomeForMonth(monthYear: String): Income? =
            incomeDao.getIncomeForMonth(monthYear)?.toDomain()

        override suspend fun getCurrentMonthIncome(): Income? = incomeDao.getCurrentMonthIncome()?.toDomain()

        override fun getTotalUsdIncome(): Flow<Double?> = incomeDao.getTotalUsdIncome()

        override suspend fun initializeDefaultIncome(): Long {
            val currentMonthYear = getCurrentMonthYear()

            // Check if income already exists for current month
            val existingIncome = getIncomeForMonth(currentMonthYear)
            if (existingIncome != null) {
                // Ensure we have a valid ID (greater than 0)
                if (existingIncome.id > 0) {
                    return existingIncome.id
                }
            }

            // Create default $4000 USD income for current month
            val defaultIncome =
                Income(
                    monthlyAmount = Amount.usd(4000.0),
                    usdAmount = Amount.usd(4000.0),
                    monthYear = currentMonthYear,
                    date = Date(),
                )

            val insertedId = incomeDao.insertIncome(defaultIncome.toEntity())
            return insertedId
        }

        private fun getCurrentMonthYear(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
