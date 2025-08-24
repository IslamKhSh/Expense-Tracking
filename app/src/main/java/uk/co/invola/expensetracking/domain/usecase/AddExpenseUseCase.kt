package uk.co.invola.expensetracking.domain.usecase

import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import java.util.Date
import javax.inject.Inject

class AddExpenseUseCase
    @Inject
    constructor(
        private val repository: ExpenseRepository,
    ) {
        suspend operator fun invoke(
            title: String,
            originalAmount: Amount,
            category: Category?,
            date: Date = Date(),
        ): Result<Long> {
            return try {
                val usdAmount =
                    if (originalAmount.isUSD()) {
                        originalAmount
                    } else {
                        val exchangeRatesResult = repository.getExchangeRates()

                        if (exchangeRatesResult.isFailure) {
                            return Result.failure(
                                exchangeRatesResult.exceptionOrNull() ?: Exception("Failed to get exchange rates"),
                            )
                        }

                        val exchangeRates = exchangeRatesResult.getOrNull()!!
                        val exchangeRate =
                            exchangeRates.rates[originalAmount.currency]
                                ?: return Result.failure(Exception("Currency ${originalAmount.currency} not supported"))

                        val convertedValue = originalAmount.value / exchangeRate
                        Amount.usd(convertedValue)
                    }

                val expense =
                    Expense(
                        title = title,
                        originalAmount = originalAmount,
                        usdAmount = usdAmount,
                        category = category,
                        date = date,
                    )

                val id = repository.insertExpense(expense)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend operator fun invoke(
            title: String,
            amount: Double,
            currency: String,
            category: Category?,
            date: Date = Date(),
        ): Result<Long> = invoke(title, Amount(amount, currency), category, date)

        /**
         * Add expense with pre-fetched exchange rates to avoid duplicate API calls
         */
        suspend operator fun invoke(
            title: String,
            originalAmount: Amount,
            category: Category?,
            exchangeRatesMap: Map<String, Double>,
            date: Date = Date(),
        ): Result<Long> {
            return try {
                val usdAmount =
                    if (originalAmount.isUSD()) {
                        originalAmount
                    } else {
                        val exchangeRate =
                            exchangeRatesMap[originalAmount.currency]
                                ?: return Result.failure(Exception("Currency ${originalAmount.currency} not supported"))

                        val convertedValue = originalAmount.value / exchangeRate
                        Amount.usd(convertedValue)
                    }

                val expense =
                    Expense(
                        title = title,
                        originalAmount = originalAmount,
                        usdAmount = usdAmount,
                        category = category,
                        date = date,
                    )

                val id = repository.insertExpense(expense)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
