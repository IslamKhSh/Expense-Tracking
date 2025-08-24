package uk.co.invola.expensetracking.domain.model

import java.util.Date

/**
 * Domain model representing monthly income
 */
data class Income(
    val id: Long = 0,
    // Monthly income amount in original currency
    val monthlyAmount: Amount,
    // USD equivalent of monthly income
    val usdAmount: Amount,
    // Month and year this income applies to (YYYY-MM format)
    val monthYear: String,
    // When this income record was created
    val date: Date = Date(),
) {
    /**
     * Convenience property to check if conversion was needed
     */
    val isConverted: Boolean
        get() = !monthlyAmount.isSameCurrency(usdAmount)

    /**
     * Gets the display amount (monthly) formatted with symbol
     */
    fun getDisplayAmount(): String = monthlyAmount.formatWithSymbol()

    /**
     * Gets the converted amount formatted (if different currency)
     */
    fun getConvertedDisplayAmount(): String? = if (isConverted) "≈ ${usdAmount.formatWithSymbol()}" else null

    /**
     * Computed exchange rate (if needed for display or calculations)
     */
    val exchangeRate: Double
        get() = if (monthlyAmount.isUSD()) 1.0 else monthlyAmount.value / usdAmount.value
}
