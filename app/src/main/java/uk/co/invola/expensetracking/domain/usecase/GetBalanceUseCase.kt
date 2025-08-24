package uk.co.invola.expensetracking.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uk.co.invola.expensetracking.domain.model.Balance
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import uk.co.invola.expensetracking.domain.repository.IncomeRepository
import javax.inject.Inject

class GetBalanceUseCase
    @Inject
    constructor(
        private val expenseRepository: ExpenseRepository,
        private val incomeRepository: IncomeRepository,
    ) {
        /**
         * Get current balance as Flow (reactive)
         */
        operator fun invoke(): Flow<Balance> =
            combine(
                incomeRepository.getTotalUsdIncome(),
                expenseRepository.getTotalConvertedExpensesAmount(),
            ) { totalIncome, totalExpenses ->
                Balance(
                    totalIncomeUsd = totalIncome ?: 0.0,
                    totalExpensesUsd = totalExpenses ?: 0.0,
                )
            }
    }
