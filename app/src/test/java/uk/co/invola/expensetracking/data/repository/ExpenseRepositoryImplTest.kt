package uk.co.invola.expensetracking.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import uk.co.invola.expensetracking.data.local.dao.ExpenseDao
import uk.co.invola.expensetracking.data.local.entity.ExpenseEntity
import uk.co.invola.expensetracking.data.local.entity.ExpenseWithCategory
import uk.co.invola.expensetracking.data.remote.ExchangeRateResponse
import uk.co.invola.expensetracking.data.remote.ExpenseApi
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Expense
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class ExpenseRepositoryImplTest {
    @Mock
    private lateinit var expenseDao: ExpenseDao

    @Mock
    private lateinit var expenseApi: ExpenseApi

    private lateinit var repository: ExpenseRepositoryImpl

    @Before
    fun setup() {
        repository = ExpenseRepositoryImpl(expenseDao, expenseApi)
    }

    @Test
    fun `getAllExpensesPaged returns flow of paging data`() =
        runTest {
            // When
            val result = repository.getAllExpensesPaged()

            // Then
            assertNotNull(result)
            // The result should be a Flow<PagingData<Expense>>
            // This test verifies that the method returns a non-null flow
            // The paging source factory will be called when the flow is collected by consumers
        }

    @Test
    fun `getRecentExpenses with startDate returns filtered expenses`() =
        runTest {
            // Given
            val startDate = Date()
            val expenseWithCategory = createMockExpenseWithCategory()
            whenever(expenseDao.getExpensesAfterDate(startDate))
                .thenReturn(flowOf(listOf(expenseWithCategory)))

            // When
            val result = repository.getRecentExpenses(startDate, 10).first()

            // Then
            assertEquals(1, result.size)
            assertEquals("Test Expense", result[0].title)
            verify(expenseDao).getExpensesAfterDate(startDate)
        }

    @Test
    fun `getRecentExpenses without startDate returns all expenses`() =
        runTest {
            // Given
            val expenseWithCategory = createMockExpenseWithCategory()
            whenever(expenseDao.getAllExpensesFlow())
                .thenReturn(flowOf(listOf(expenseWithCategory)))

            // When
            val result = repository.getRecentExpenses(null, 5).first()

            // Then
            assertEquals(1, result.size)
            verify(expenseDao).getAllExpensesFlow()
        }

    @Test
    fun `insertExpense calls dao insertExpense`() =
        runTest {
            // Given
            val expense = createMockExpense()
            whenever(expenseDao.insertExpense(any())).thenReturn(1L)

            // When
            val result = repository.insertExpense(expense)

            // Then
            assertEquals(1L, result)
            verify(expenseDao).insertExpense(any())
        }

    @Test
    fun `getTotalConvertedExpensesAmount returns flow from dao`() =
        runTest {
            // Given
            val expectedAmount = 1000.0
            whenever(expenseDao.getTotalConvertedExpensesAmount())
                .thenReturn(flowOf(expectedAmount))

            // When
            val result = repository.getTotalConvertedExpensesAmount().first()

            // Then
            assertEquals(expectedAmount, result)
            verify(expenseDao).getTotalConvertedExpensesAmount()
        }

    @Test
    fun `getExchangeRates returns success when api call succeeds`() =
        runTest {
            // Given
            val exchangeRateResponse =
                ExchangeRateResponse(
                    baseCode = "USD",
                    rates = mapOf("EUR" to 0.85, "GBP" to 0.73),
                )
            val response = Response.success(exchangeRateResponse)
            whenever(expenseApi.getExchangeRates()).thenReturn(response)

            // When
            val result = repository.getExchangeRates()

            // Then
            assertTrue(result.isSuccess)
            val exchangeRate = result.getOrNull()
            assertEquals("USD", exchangeRate?.baseCode)
            assertEquals(0.85, exchangeRate?.rates?.get("EUR"))
            verify(expenseApi).getExchangeRates()
        }

    @Test
    fun `getExchangeRates returns failure when api call fails`() =
        runTest {
            // Given
            val response = Response.error<ExchangeRateResponse>(404, "".toResponseBody(null))
            whenever(expenseApi.getExchangeRates()).thenReturn(response)

            // When
            val result = repository.getExchangeRates()

            // Then
            assertTrue(result.isFailure)
            verify(expenseApi).getExchangeRates()
        }

    private fun createMockExpense(): Expense =
        Expense(
            id = 1L,
            title = "Test Expense",
            originalAmount = Amount(100.0, "USD"),
            usdAmount = Amount(100.0, "USD"),
            category = null,
            date = Date(),
        )

    private fun createMockExpenseWithCategory(): ExpenseWithCategory {
        val expenseEntity =
            ExpenseEntity(
                id = 1L,
                title = "Test Expense",
                originalAmount = Amount(100.0, "USD"),
                usdAmount = Amount(100.0, "USD"),
                categoryId = null,
                date = Date(),
            )
        return ExpenseWithCategory(expenseEntity, null)
    }
}
