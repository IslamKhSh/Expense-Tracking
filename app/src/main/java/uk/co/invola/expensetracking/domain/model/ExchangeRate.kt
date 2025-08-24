package uk.co.invola.expensetracking.domain.model

/**
 * Domain model representing exchange rate data
 */
data class ExchangeRate(
    val baseCode: String,
    val rates: Map<String, Double>,
)
