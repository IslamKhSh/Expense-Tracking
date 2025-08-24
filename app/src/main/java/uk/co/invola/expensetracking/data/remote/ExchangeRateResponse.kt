package uk.co.invola.expensetracking.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateResponse(
    @SerialName("base_code")
    val baseCode: String,
    @SerialName("rates")
    val rates: Map<String, Double>,
)
