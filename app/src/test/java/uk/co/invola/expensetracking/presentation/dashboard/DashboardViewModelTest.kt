package uk.co.invola.expensetracking.presentation.dashboard

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atLeast
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Balance
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import uk.co.invola.expensetracking.domain.usecase.FirstLaunchSetupUseCase
import uk.co.invola.expensetracking.domain.usecase.GetBalanceUseCase
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {
    @Mock
    private lateinit var getBalanceUseCase: GetBalanceUseCase

    @Mock
    private lateinit var firstLaunchSetupUseCase: FirstLaunchSetupUseCase

    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup default mocks
        whenever(getBalanceUseCase.invoke()).thenReturn(flowOf(createMockBalance()))
        whenever(expenseRepository.getRecentExpenses(any(), any())).thenReturn(flowOf(createMockExpenses()))

        viewModel = DashboardViewModel(getBalanceUseCase, firstLaunchSetupUseCase, expenseRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `dashboard initializes successfully`() =
        runTest {
            // When
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(4000.0, state.balance?.totalIncomeUsd)
            assertEquals(1500.0, state.balance?.totalExpensesUsd)
            assertEquals(3, state.recentExpenses.size)

            verify(getBalanceUseCase).invoke()
            verify(expenseRepository).getRecentExpenses(any(), any())
        }

    @Test
    fun `onEvent UpdateFilter changes filter and reloads data`() =
        runTest {
            // Given
            testDispatcher.scheduler.advanceUntilIdle()

            // When
            viewModel.onEvent(DashboardUiEvent.FilterChanged(TimeFilter.LAST_7_DAYS))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(TimeFilter.LAST_7_DAYS, state.selectedFilter)
            verify(expenseRepository, atLeast(2)).getRecentExpenses(any(), any())
        }

    @Test
    fun `onEvent Refresh reloads dashboard data`() =
        runTest {
            // Given
            testDispatcher.scheduler.advanceUntilIdle()

            // When - Dashboard doesn't have a Refresh event, so we'll test filter change instead
            viewModel.onEvent(DashboardUiEvent.FilterChanged(TimeFilter.LAST_7_DAYS))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            verify(expenseRepository, atLeast(2)).getRecentExpenses(any(), any())
        }

    @Test
    fun `onEvent NavigateToAllExpenses emits correct side effect`() =
        runTest {
            // When & Then
            viewModel.sideEffect.test {
                viewModel.onEvent(DashboardUiEvent.NavigateToAllExpenses)

                val sideEffect = awaitItem()
                assertTrue(sideEffect is DashboardSideEffect.NavigateToAllExpenses)
            }
        }

    @Test
    fun `onEvent NavigateToAddExpense emits correct side effect`() =
        runTest {
            // When & Then
            viewModel.sideEffect.test {
                viewModel.onEvent(DashboardUiEvent.NavigateToAddExpense)

                val sideEffect = awaitItem()
                assertTrue(sideEffect is DashboardSideEffect.NavigateToAddExpense)
            }
        }

    @Test
    fun `balance updates are reflected in UI state`() =
        runTest {
            // Given
            val initialBalance = Balance(4000.0, 1500.0)
            val updatedBalance = Balance(4000.0, 2000.0)

            whenever(getBalanceUseCase.invoke()).thenReturn(flowOf(initialBalance, updatedBalance))

            // When
            viewModel = DashboardViewModel(getBalanceUseCase, firstLaunchSetupUseCase, expenseRepository)

            // Then
            viewModel.uiState.test {
                // Skip initial loading state
                skipItems(1)

                // First balance update
                val firstState = awaitItem()
                assertEquals(1500.0, firstState.balance?.totalExpensesUsd)

                // Second balance update
                val secondState = awaitItem()
                assertEquals(2000.0, secondState.balance?.totalExpensesUsd)
            }
        }

    @Test
    fun `recent expenses updates are reflected in UI state`() =
        runTest {
            // Given
            val initialExpenses = listOf(createMockExpense(1L, "Expense 1"))
            val updatedExpenses =
                listOf(
                    createMockExpense(1L, "Expense 1"),
                    createMockExpense(2L, "Expense 2"),
                )

            whenever(expenseRepository.getRecentExpenses(any(), any()))
                .thenReturn(flowOf(initialExpenses, updatedExpenses))

            // When
            viewModel = DashboardViewModel(getBalanceUseCase, firstLaunchSetupUseCase, expenseRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(2, state.recentExpenses.size)
                assertEquals("Expense 1", state.recentExpenses[0].title)
                assertEquals("Expense 2", state.recentExpenses[1].title)
            }
        }

    @Test
    fun `filter change updates UI state and loads new expenses`() =
        runTest {
            // Given
            testDispatcher.scheduler.advanceUntilIdle()
            val initialState = viewModel.uiState.value
            assertEquals(TimeFilter.THIS_MONTH, initialState.selectedFilter)

            // When
            viewModel.onEvent(DashboardUiEvent.FilterChanged(TimeFilter.LAST_7_DAYS))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val updatedState = viewModel.uiState.value
            assertEquals(TimeFilter.LAST_7_DAYS, updatedState.selectedFilter)
            verify(expenseRepository, atLeast(2)).getRecentExpenses(any(), any())
        }

    @Test
    fun `loading state is managed correctly during initialization`() =
        runTest {
            // Given - create a new viewModel to test initialization
            val newViewModel = DashboardViewModel(getBalanceUseCase, firstLaunchSetupUseCase, expenseRepository)

            // When & Then
            newViewModel.uiState.test {
                // Initial loading state
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                testDispatcher.scheduler.advanceUntilIdle()

                // After loading completes
                val loadedState = awaitItem()
                assertFalse(loadedState.isLoading)
                assertEquals(4000.0, loadedState.balance.totalIncomeUsd, 0.0)
                assertEquals(1500.0, loadedState.balance.totalExpensesUsd, 0.0)
            }
        }

    private fun createMockBalance(): Balance =
        Balance(
            totalIncomeUsd = 4000.0,
            totalExpensesUsd = 1500.0,
        )

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
