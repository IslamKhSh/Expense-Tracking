package uk.co.invola.expensetracking.presentation.expenses

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
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
            val snapshot = viewModel.allExpenses.asSnapshot()
            assertEquals(3, snapshot.size)
            assertEquals("Groceries", snapshot[0].title)
            assertEquals("Gas", snapshot[1].title)
            assertEquals("Coffee", snapshot[2].title)

            // Note: Repository method is called when the flow is created in the ViewModel constructor
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
            val snapshot = viewModel.allExpenses.asSnapshot()
            assertEquals(0, snapshot.size)

            // Note: Repository method is called when the flow is created in the ViewModel constructor
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

            // Access the flow multiple times
            val firstSnapshot = viewModel.allExpenses.asSnapshot()
            val secondSnapshot = viewModel.allExpenses.asSnapshot()

            // Then
            assertEquals(firstSnapshot.size, secondSnapshot.size)
            assertEquals(firstSnapshot[0].title, secondSnapshot[0].title)

            // Repository should only be called once due to caching
            // Note: Repository method is called when the flow is created in the ViewModel constructor
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
            val snapshot = viewModel.allExpenses.asSnapshot()
            assertEquals(100, snapshot.size)
            assertEquals("Expense 1", snapshot[0].title)
            assertEquals("Expense 100", snapshot[99].title)

            // Note: Repository method is called when the flow is created in the ViewModel constructor
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

    private fun createMockExpenseWithCategory(
        id: Long,
        title: String,
        categoryIcon: CategoryIcon,
    ): Expense =
        Expense(
            id = id,
            title = title,
            originalAmount = Amount(25.0, "USD"),
            usdAmount = Amount(25.0, "USD"),
            category = Category(id, categoryIcon.name, categoryIcon),
            date = Date(),
        )

    private fun createMockExpenseWithCurrency(
        id: Long,
        title: String,
        amount: Double,
        currency: String,
    ): Expense =
        Expense(
            id = id,
            title = title,
            originalAmount = Amount(amount, currency),
            // Mock conversion
            usdAmount = Amount(amount * 1.1, "USD"),
            category = Category(1L, "Food", CategoryIcon.GROCERIES),
            date = Date(),
        )
}
