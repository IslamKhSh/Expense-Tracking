package uk.co.invola.expensetracking.domain.model

import java.util.Date

data class Expense(
    val id: Long = 0,
    val title: String,
    // Original amount and currency as entered by user
    val originalAmount: Amount,
    // Converted amount in base currency (USD)
    val usdAmount: Amount,
    val category: Category?,
    val date: Date,
) {
    /**
     * Convenience property to check if conversion was needed
     */
    val isConverted: Boolean
        get() = !originalAmount.isSameCurrency(usdAmount)

    /**
     * Gets the display amount (original) formatted with symbol
     */
    fun getDisplayAmount(): String = originalAmount.formatWithSymbol()

    /**
     * Gets the converted amount formatted (if different currency)
     */
    fun getConvertedDisplayAmount(): String? = if (isConverted) "≈ ${usdAmount.formatWithSymbol()}" else null

    /**
     * Computed exchange rate (if needed for display or calculations)
     * Returns the rate used to convert from original to converted currency
     */
    val exchangeRate: Double
        get() = if (originalAmount.isUSD()) 1.0 else originalAmount.value / usdAmount.value
}
