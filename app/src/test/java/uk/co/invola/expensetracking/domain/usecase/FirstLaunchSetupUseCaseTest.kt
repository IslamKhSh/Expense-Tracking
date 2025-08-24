package uk.co.invola.expensetracking.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Income
import uk.co.invola.expensetracking.domain.repository.CategoryRepository
import uk.co.invola.expensetracking.domain.repository.IncomeRepository

@RunWith(MockitoJUnitRunner::class)
class FirstLaunchSetupUseCaseTest {
    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var incomeRepository: IncomeRepository

    private lateinit var useCase: FirstLaunchSetupUseCase

    @Before
    fun setup() {
        useCase = FirstLaunchSetupUseCase(categoryRepository, incomeRepository)
    }

    @Test
    fun `invoke returns success true when app already initialized`() =
        runTest {
            // Given
            val mockIncome = createMockIncome()
            whenever(categoryRepository.areCategoriesInitialized()).thenReturn(true)
            whenever(incomeRepository.getCurrentMonthIncome()).thenReturn(mockIncome)

            // When
            val result = useCase.invoke()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(true, result.getOrNull())
            verify(categoryRepository).areCategoriesInitialized()
            verify(incomeRepository).getCurrentMonthIncome()
            verify(categoryRepository, never()).initializeDefaultCategories()
            verify(incomeRepository, never()).initializeDefaultIncome()
        }

    @Test
    fun `invoke initializes categories and income when not initialized`() =
        runTest {
            // Given
            whenever(categoryRepository.areCategoriesInitialized()).thenReturn(false)
            whenever(incomeRepository.getCurrentMonthIncome()).thenReturn(null)
            whenever(categoryRepository.initializeDefaultCategories()).thenReturn(listOf(1L, 2L, 3L))
            whenever(incomeRepository.initializeDefaultIncome()).thenReturn(1L)

            // When
            val result = useCase.invoke()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(true, result.getOrNull())
            verify(categoryRepository).areCategoriesInitialized()
            verify(incomeRepository).getCurrentMonthIncome()
            verify(categoryRepository).initializeDefaultCategories()
            verify(incomeRepository).initializeDefaultIncome()
        }

    @Test
    fun `invoke initializes only categories when income exists but categories don't`() =
        runTest {
            // Given
            val mockIncome = createMockIncome()
            whenever(categoryRepository.areCategoriesInitialized()).thenReturn(false)
            whenever(incomeRepository.getCurrentMonthIncome()).thenReturn(mockIncome)
            whenever(categoryRepository.initializeDefaultCategories()).thenReturn(listOf(1L, 2L, 3L))

            // When
            val result = useCase.invoke()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(true, result.getOrNull())
            verify(categoryRepository).areCategoriesInitialized()
            verify(incomeRepository).getCurrentMonthIncome()
            verify(categoryRepository).initializeDefaultCategories()
            verify(incomeRepository, never()).initializeDefaultIncome()
        }

    @Test
    fun `invoke initializes only income when categories exist but income doesn't`() =
        runTest {
            // Given
            whenever(categoryRepository.areCategoriesInitialized()).thenReturn(true)
            whenever(incomeRepository.getCurrentMonthIncome()).thenReturn(null)
            whenever(incomeRepository.initializeDefaultIncome()).thenReturn(1L)

            // When
            val result = useCase.invoke()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(true, result.getOrNull())
            verify(categoryRepository).areCategoriesInitialized()
            verify(incomeRepository).getCurrentMonthIncome()
            verify(categoryRepository, never()).initializeDefaultCategories()
            verify(incomeRepository).initializeDefaultIncome()
        }

    private fun createMockIncome(): Income =
        Income(
            id = 1L,
            monthlyAmount = Amount(4000.0, "USD"),
            usdAmount = Amount(4000.0, "USD"),
            monthYear = "2024-01",
            date = java.util.Date(),
        )
}
