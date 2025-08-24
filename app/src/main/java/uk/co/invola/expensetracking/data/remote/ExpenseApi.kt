package uk.co.invola.expensetracking.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface ExpenseApi {
    /**
     * Fetches the latest currency exchange rates
     *
     * @return Response containing exchange rate data
     */
    @GET("USD")
    suspend fun getExchangeRates(): Response<ExchangeRateResponse>
}
