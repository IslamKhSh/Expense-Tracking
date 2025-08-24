package uk.co.invola.expensetracking.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import uk.co.invola.expensetracking.domain.repository.IncomeRepository

@RunWith(MockitoJUnitRunner::class)
class GetBalanceUseCaseTest {
    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    @Mock
    private lateinit var incomeRepository: IncomeRepository

    private lateinit var useCase: GetBalanceUseCase

    @Before
    fun setup() {
        useCase = GetBalanceUseCase(expenseRepository, incomeRepository)
    }

    @Test
    fun `invoke returns balance with correct calculations`() =
        runTest {
            // Given
            val totalIncome = 4000.0
            val totalExpenses = 1500.0

            whenever(incomeRepository.getTotalUsdIncome()).thenReturn(flowOf(totalIncome))
            whenever(expenseRepository.getTotalConvertedExpensesAmount()).thenReturn(flowOf(totalExpenses))

            // When & Then
            useCase.invoke().test {
                val balance = awaitItem()

                assertEquals(totalIncome, balance.totalIncomeUsd, 0.0)
                assertEquals(totalExpenses, balance.totalExpensesUsd, 0.0)
                assertEquals(2500.0, balance.remainingBalanceUsd, 0.0) // 4000 - 1500
                assertEquals(37.5, balance.spentPercentage, 0.0) // (1500 / 4000) * 100

                awaitComplete()
            }
        }

    @Test
    fun `invoke handles null income correctly`() =
        runTest {
            // Given
            val totalExpenses = 1500.0

            whenever(incomeRepository.getTotalUsdIncome()).thenReturn(flowOf(null))
            whenever(expenseRepository.getTotalConvertedExpensesAmount()).thenReturn(flowOf(totalExpenses))

            // When & Then
            useCase.invoke().test {
                val balance = awaitItem()

                assertEquals(0.0, balance.totalIncomeUsd, 0.0)
                assertEquals(totalExpenses, balance.totalExpensesUsd, 0.0)
                assertEquals(-1500.0, balance.remainingBalanceUsd, 0.0) // 0 - 1500
                assertEquals(0.0, balance.spentPercentage, 0.0) // Division by zero handled

                awaitComplete()
            }
        }

    @Test
    fun `invoke handles null expenses correctly`() =
        runTest {
            // Given
            val totalIncome = 4000.0

            whenever(incomeRepository.getTotalUsdIncome()).thenReturn(flowOf(totalIncome))
            whenever(expenseRepository.getTotalConvertedExpensesAmount()).thenReturn(flowOf(null))

            // When & Then
            useCase.invoke().test {
                val balance = awaitItem()

                assertEquals(totalIncome, balance.totalIncomeUsd, 0.0)
                assertEquals(0.0, balance.totalExpensesUsd, 0.0)
                assertEquals(4000.0, balance.remainingBalanceUsd, 0.0) // 4000 - 0
                assertEquals(0.0, balance.spentPercentage, 0.0) // (0 / 4000) * 100

                awaitComplete()
            }
        }

    @Test
    fun `invoke handles both null values correctly`() =
        runTest {
            // Given
            whenever(incomeRepository.getTotalUsdIncome()).thenReturn(flowOf(null))
            whenever(expenseRepository.getTotalConvertedExpensesAmount()).thenReturn(flowOf(null))

            // When & Then
            useCase.invoke().test {
                val balance = awaitItem()

                assertEquals(0.0, balance.totalIncomeUsd, 0.0)
                assertEquals(0.0, balance.totalExpensesUsd, 0.0)
                assertEquals(0.0, balance.remainingBalanceUsd, 0.0)
                assertEquals(0.0, balance.spentPercentage, 0.0)

                awaitComplete()
            }
        }

    @Test
    fun `invoke handles over budget scenario`() =
        runTest {
            // Given
            val totalIncome = 1000.0
            val totalExpenses = 1500.0

            whenever(incomeRepository.getTotalUsdIncome()).thenReturn(flowOf(totalIncome))
            whenever(expenseRepository.getTotalConvertedExpensesAmount()).thenReturn(flowOf(totalExpenses))

            // When & Then
            useCase.invoke().test {
                val balance = awaitItem()

                assertEquals(totalIncome, balance.totalIncomeUsd, 0.0)
                assertEquals(totalExpenses, balance.totalExpensesUsd, 0.0)
                assertEquals(-500.0, balance.remainingBalanceUsd, 0.0) // 1000 - 1500
                assertEquals(150.0, balance.spentPercentage, 0.0) // (1500 / 1000) * 100

                awaitComplete()
            }
        }
}
