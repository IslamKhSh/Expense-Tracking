package uk.co.invola.expensetracking.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.data.local.dao.IncomeDao
import uk.co.invola.expensetracking.data.local.entity.IncomeEntity
import uk.co.invola.expensetracking.domain.model.Amount
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class IncomeRepositoryImplTest {
    @Mock
    private lateinit var incomeDao: IncomeDao

    private lateinit var repository: IncomeRepositoryImpl

    @Before
    fun setup() {
        repository = IncomeRepositoryImpl(incomeDao)
    }

    @Test
    fun `getCurrentMonthIncome returns mapped income from dao`() =
        runTest {
            // Given
            val incomeEntity = createMockIncomeEntity(1L, 4000.0, "2024-01")
            whenever(incomeDao.getCurrentMonthIncome()).thenReturn(incomeEntity)

            // When
            val result = repository.getCurrentMonthIncome()

            // Then
            assertNotNull(result)
            assertEquals(4000.0, result!!.monthlyAmount.value, 0.0)
            verify(incomeDao).getCurrentMonthIncome()
        }

    @Test
    fun `getCurrentMonthIncome returns null when no income exists`() =
        runTest {
            // Given
            whenever(incomeDao.getCurrentMonthIncome()).thenReturn(null)

            // When
            val result = repository.getCurrentMonthIncome()

            // Then
            assertNull(result)
            verify(incomeDao).getCurrentMonthIncome()
        }

    @Test
    fun `getTotalUsdIncome returns flow from dao`() =
        runTest {
            // Given
            val expectedTotal = 8500.0
            whenever(incomeDao.getTotalUsdIncome()).thenReturn(flowOf(expectedTotal))

            // When
            val result = repository.getTotalUsdIncome().first()

            // Then
            assertEquals(expectedTotal, result)
            verify(incomeDao).getTotalUsdIncome()
        }

    @Test
    fun `initializeDefaultIncome creates new income when none exists`() =
        runTest {
            // Given
            whenever(incomeDao.getIncomeForMonth(any())).thenReturn(null)
            whenever(incomeDao.insertIncome(any())).thenReturn(1L)

            // When
            val result = repository.initializeDefaultIncome()

            // Then
            assertEquals(1L, result)
            verify(incomeDao).getIncomeForMonth(any())
            verify(incomeDao).insertIncome(any())
        }

    @Test
    fun `initializeDefaultIncome returns existing income id when income already exists`() =
        runTest {
            // Given
            val existingIncome = createMockIncomeEntity(5L, 4000.0, "2024-01")
            whenever(incomeDao.getIncomeForMonth(any())).thenReturn(existingIncome)

            // When
            val result = repository.initializeDefaultIncome()

            // Then
            assertEquals(5L, result)
            verify(incomeDao).getIncomeForMonth(any())
            verify(incomeDao, never()).insertIncome(any())
        }

    private fun createMockIncomeEntity(
        id: Long,
        amount: Double,
        monthYear: String,
    ): IncomeEntity =
        IncomeEntity(
            id = id,
            monthlyAmount = Amount(amount, "USD"),
            usdAmount = Amount(amount, "USD"),
            monthYear = monthYear,
            date = Date(),
        )
}
