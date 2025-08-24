package uk.co.invola.expensetracking.presentation.expenses

import androidx.paging.PagingData
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AllExpensesViewModelTest {
    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    private lateinit var viewModel: AllExpensesViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `allExpenses flow returns paged data from repository`() =
        runTest {
            // Given
            val mockExpenses = createMockExpenses()
            val pagingData = PagingData.from(mockExpenses)
            whenever(expenseRepository.getAllExpensesPaged()).thenReturn(flowOf(pagingData))

            // When
            viewModel = AllExpensesViewModel(expenseRepository)

            // Then
            viewModel.allExpenses.test {
                val emittedPagingData = awaitItem()
                // We can't directly inspect PagingData contents in unit tests easily,
                // but we can verify that the flow emits and the repository was called
                verify(expenseRepository).getAllExpensesPaged()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `allExpenses flow handles empty data`() =
        runTest {
            // Given
            val pagingData = PagingData.from(emptyList<Expense>())
            whenever(expenseRepository.getAllExpensesPaged()).thenReturn(flowOf(pagingData))

            // When
            viewModel = AllExpensesViewModel(expenseRepository)

            // Then
            viewModel.allExpenses.test {
                val emittedPagingData = awaitItem()
                // Verify that the flow emits even with empty data
                verify(expenseRepository).getAllExpensesPaged()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `allExpenses flow is cached in viewModelScope`() =
        runTest {
            // Given
            val mockExpenses = createMockExpenses()
            val pagingData = PagingData.from(mockExpenses)
            whenever(expenseRepository.getAllExpensesPaged()).thenReturn(flowOf(pagingData))

            // When
            viewModel = AllExpensesViewModel(expenseRepository)

            // Then
            // Test that multiple collectors get the same cached flow
            viewModel.allExpenses.test {
                val firstEmission = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.allExpenses.test {
                val secondEmission = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            // Repository should only be called once due to caching
            verify(expenseRepository).getAllExpensesPaged()
        }

    @Test
    fun `allExpenses flow handles large dataset`() =
        runTest {
            // Given
            val largeExpenseList =
                (1..100).map { index ->
                    createMockExpense(index.toLong(), "Expense $index")
                }
            val pagingData = PagingData.from(largeExpenseList)
            whenever(expenseRepository.getAllExpensesPaged()).thenReturn(flowOf(pagingData))

            // When
            viewModel = AllExpensesViewModel(expenseRepository)

            // Then
            viewModel.allExpenses.test {
                val emittedPagingData = awaitItem()
                // Verify that the flow emits with large dataset
                verify(expenseRepository).getAllExpensesPaged()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `viewModel calls repository getAllExpensesPaged on initialization`() =
        runTest {
            // Given
            val mockExpenses = createMockExpenses()
            val pagingData = PagingData.from(mockExpenses)
            whenever(expenseRepository.getAllExpensesPaged()).thenReturn(flowOf(pagingData))

            // When
            viewModel = AllExpensesViewModel(expenseRepository)

            // Then
            viewModel.allExpenses.test {
                val emittedPagingData = awaitItem()
                // Verify repository was called during flow creation
                verify(expenseRepository).getAllExpensesPaged()
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createMockExpenses(): List<Expense> =
        listOf(
            createMockExpense(1L, "Groceries"),
            createMockExpense(2L, "Gas"),
            createMockExpense(3L, "Coffee"),
        )

    private fun createMockExpense(
        id: Long,
        title: String,
    ): Expense =
        Expense(
            id = id,
            title = title,
            originalAmount = Amount(25.0, "USD"),
            usdAmount = Amount(25.0, "USD"),
            category = Category(1L, "Food", CategoryIcon.GROCERIES),
            date = Date(),
        )
}
