package uk.co.invola.expensetracking.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.invola.expensetracking.data.local.dao.ExpenseDao
import uk.co.invola.expensetracking.data.mapper.toDomain
import uk.co.invola.expensetracking.data.mapper.toEntity
import uk.co.invola.expensetracking.data.remote.ExpenseApi
import uk.co.invola.expensetracking.domain.model.ExchangeRate
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl
    @Inject
    constructor(
        private val expenseDao: ExpenseDao,
        private val expenseApi: ExpenseApi,
    ) : ExpenseRepository {
        override fun getAllExpensesPaged(): Flow<PagingData<Expense>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = 10,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = { expenseDao.getAllExpensesPaged() },
            ).flow.map { pagingData ->
                pagingData.map { it.toDomain() }
            }

        override fun getRecentExpenses(
            startDate: Date?,
            limit: Int,
        ): Flow<List<Expense>> =
            if (startDate != null) {
                expenseDao.getExpensesAfterDate(startDate)
            } else {
                expenseDao.getAllExpensesFlow()
            }.map { expenseWithCategories ->
                expenseWithCategories.take(limit).map { it.toDomain() }
            }

        override suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense.toEntity())

        override fun getTotalConvertedExpensesAmount(): Flow<Double?> = expenseDao.getTotalConvertedExpensesAmount()

        override suspend fun getExchangeRates(): Result<ExchangeRate> =
            try {
                val response = expenseApi.getExchangeRates()
                if (response.isSuccessful && response.body() != null) {
                    val exchangeRateResponse = response.body()!!
                    Result.success(
                        ExchangeRate(
                            baseCode = exchangeRateResponse.baseCode,
                            rates = exchangeRateResponse.rates,
                        ),
                    )
                } else {
                    Result.failure(Exception("Failed to fetch exchange rates"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
