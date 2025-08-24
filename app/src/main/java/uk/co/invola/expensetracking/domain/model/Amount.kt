package uk.co.invola.expensetracking.domain.model

import kotlinx.serialization.Serializable
import uk.co.invola.expensetracking.utils.CurrencyUtils

@Serializable
data class Amount(
    val value: Double,
    val currency: String,
) {
    fun formatValue(): String = String.format("%.2f", value)

    fun isSameCurrency(other: Amount): Boolean = currency == other.currency

    fun isUSD(): Boolean = currency == "USD"

    companion object {
        /**
         * Creates an Amount instance in USD
         */
        fun usd(value: Double): Amount = Amount(value, "USD")
    }

    /**
     * Formats the amount with currency symbol (e.g., "$100.00")
     */
    fun formatWithSymbol(): String = CurrencyUtils.formatWithCurrency(value, currency)
}
