package uk.co.invola.expensetracking.utils

object CurrencyUtils {
    /**
     * Format amount with custom currency symbol
     * @param amount The amount to format
     * @param currency The currency
     * @return Formatted string like "$1,234.56"
     */
    fun formatWithCurrency(
        amount: Double,
        currency: String = "$",
    ): String = "$currency${String.format("%,.2f", amount)}"
}
