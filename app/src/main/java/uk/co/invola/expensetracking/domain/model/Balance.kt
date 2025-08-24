package uk.co.invola.expensetracking.domain.model

import uk.co.invola.expensetracking.utils.CurrencyUtils

data class Balance(
    val totalIncomeUsd: Double,
    val totalExpensesUsd: Double,
    val remainingBalanceUsd: Double = totalIncomeUsd - totalExpensesUsd,
    val spentPercentage: Double = if (totalIncomeUsd > 0) (totalExpensesUsd / totalIncomeUsd) * 100 else 0.0,
) {
    fun getFormattedBalance(): String = CurrencyUtils.formatWithCurrency(remainingBalanceUsd)
}
