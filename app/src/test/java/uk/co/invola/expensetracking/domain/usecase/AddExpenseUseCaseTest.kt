package uk.co.invola.expensetracking.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon
import uk.co.invola.expensetracking.domain.model.ExchangeRate
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class AddExpenseUseCaseTest {
    @Mock
    private lateinit var repository: ExpenseRepository

    private lateinit var useCase: AddExpenseUseCase

    @Before
    fun setup() {
        useCase = AddExpenseUseCase(repository)
    }

    @Test
    fun `invoke with USD amount returns success without conversion`() =
        runTest {
            // Given
            val title = "Test Expense"
            val originalAmount = Amount(100.0, "USD")
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            val date = Date()

            whenever(repository.insertExpense(any())).thenReturn(1L)

            // When
            val result = useCase.invoke(title, originalAmount, category, date)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(1L, result.getOrNull())
            verify(repository).insertExpense(any())
        }

    @Test
    fun `invoke with non-USD amount converts to USD successfully`() =
        runTest {
            // Given
            val title = "Test Expense"
            val originalAmount = Amount(100.0, "EUR")
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            val date = Date()

            val exchangeRate =
                ExchangeRate(
                    baseCode = "USD",
                    rates = mapOf("EUR" to 0.85),
                )
            whenever(repository.getExchangeRates()).thenReturn(Result.success(exchangeRate))
            whenever(repository.insertExpense(any())).thenReturn(1L)

            // When
            val result = useCase.invoke(title, originalAmount, category, date)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(1L, result.getOrNull())
            verify(repository).getExchangeRates()
            verify(repository).insertExpense(any())
        }

    @Test
    fun `invoke returns failure when exchange rate fetch fails`() =
        runTest {
            // Given
            val title = "Test Expense"
            val originalAmount = Amount(100.0, "EUR")
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            val date = Date()

            whenever(repository.getExchangeRates()).thenReturn(Result.failure(Exception("Network error")))

            // When
            val result = useCase.invoke(title, originalAmount, category, date)

            // Then
            assertTrue(result.isFailure)
            verify(repository).getExchangeRates()
        }

    @Test
    fun `invoke returns failure when currency not supported`() =
        runTest {
            // Given
            val title = "Test Expense"
            val originalAmount = Amount(100.0, "XYZ")
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            val date = Date()

            val exchangeRate =
                ExchangeRate(
                    baseCode = "USD",
                    rates = mapOf("EUR" to 0.85, "GBP" to 0.73),
                )
            whenever(repository.getExchangeRates()).thenReturn(Result.success(exchangeRate))

            // When
            val result = useCase.invoke(title, originalAmount, category, date)

            // Then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Currency XYZ not supported") == true)
            verify(repository).getExchangeRates()
        }

    @Test
    fun `invoke convenience method with double amount works correctly`() =
        runTest {
            // Given
            val title = "Test Expense"
            val amount = 100.0
            val currency = "USD"
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            val date = Date()

            whenever(repository.insertExpense(any())).thenReturn(1L)

            // When
            val result = useCase.invoke(title, amount, currency, category, date)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(1L, result.getOrNull())
            verify(repository).insertExpense(any())
        }

    @Test
    fun `invoke handles exception and returns failure`() =
        runTest {
            // Given
            val title = "Test Expense"
            val originalAmount = Amount(100.0, "USD")
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            val date = Date()

            whenever(repository.insertExpense(any())).thenThrow(RuntimeException("Database error"))

            // When
            val result = useCase.invoke(title, originalAmount, category, date)

            // Then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Database error") == true)
            verify(repository).insertExpense(any())
        }
}
