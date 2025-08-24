package uk.co.invola.expensetracking.presentation.addexpense

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon
import uk.co.invola.expensetracking.domain.model.ExchangeRate
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import uk.co.invola.expensetracking.domain.usecase.AddExpenseUseCase
import uk.co.invola.expensetracking.domain.usecase.GetCategoriesUseCase
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AddExpenseViewModelTest {
    @Mock
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase

    @Mock
    private lateinit var addExpenseUseCase: AddExpenseUseCase

    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    private lateinit var viewModel: AddExpenseViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup default mocks
        whenever(getCategoriesUseCase.invoke()).thenReturn(flowOf(createMockCategories()))
        runBlocking {
            whenever(expenseRepository.getExchangeRates()).thenReturn(Result.success(createMockExchangeRate()))
        }

        viewModel = AddExpenseViewModel(getCategoriesUseCase, addExpenseUseCase, expenseRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `categories are loaded on init`() =
        runTest {
            // Given - setup is done in @BeforeEach

            // When
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(3, state.categories.size)
            assertEquals("Food", state.categories[0].name)
            verify(getCategoriesUseCase).invoke()
        }

    @Test
    fun `categories are loaded correctly`() =
        runTest {
            // Given - setup is done in @BeforeEach

            // When
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(3, state.categories.size)
            assertEquals("Food", state.categories[0].name)
            assertEquals("Transport", state.categories[1].name)
            assertEquals("Entertainment", state.categories[2].name)
            verify(getCategoriesUseCase).invoke()
        }

    @Test
    fun `exchange rates and currencies are loaded correctly`() =
        runTest {
            // Given - setup is done in @BeforeEach

            // When
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(4, state.availableCurrencies.size) // EUR, GBP, JPY, USD
            assertTrue(state.availableCurrencies.contains("USD"))
            assertTrue(state.availableCurrencies.contains("EUR"))
            assertTrue(state.availableCurrencies.contains("GBP"))
            assertTrue(state.availableCurrencies.contains("JPY"))
            assertEquals("USD" to 1.0, state.selectedCurrency)
            verify(expenseRepository).getExchangeRates()
        }

    @Test
    fun `exchange rates API failure shows error and uses empty currencies`() =
        runTest {
            // Given
            runBlocking {
                whenever(expenseRepository.getExchangeRates())
                    .thenReturn(Result.failure(Exception("API Error")))
            }

            val viewModelWithError = AddExpenseViewModel(getCategoriesUseCase, addExpenseUseCase, expenseRepository)

            // When
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModelWithError.uiState.value
            assertEquals(0, state.availableCurrencies.size)
            assertEquals("API Error", state.errorMessage)
            assertFalse(state.isLoading)
        }

    @Test
    fun `onEvent TitleChanged updates title`() =
        runTest {
            // When
            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("New Title"))

            // Then
            assertEquals("New Title", viewModel.uiState.value.title)
        }

    @Test
    fun `onEvent AmountChanged updates amount`() =
        runTest {
            // When
            viewModel.onEvent(AddExpenseUiEvent.AmountChanged("100.50"))

            // Then
            assertEquals("100.50", viewModel.uiState.value.amount)
        }

    @Test
    fun `onEvent DateSelected updates selected date`() =
        runTest {
            // Given
            val newDate = Date(System.currentTimeMillis() + 86400000) // Tomorrow

            // When
            viewModel.onEvent(AddExpenseUiEvent.DateSelected(newDate))

            // Then
            assertEquals(newDate, viewModel.uiState.value.selectedDate)
        }

    @Test
    fun `onEvent CurrencySelected updates selected currency`() =
        runTest {
            // Given
            testDispatcher.scheduler.advanceUntilIdle() // Wait for initialization

            // When
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("EUR" to 0.85))

            // Then
            assertEquals("EUR" to 0.85, viewModel.uiState.value.selectedCurrency)
        }

    @Test
    fun `onEvent CurrencySelected uses exchange rate from ViewModel map`() =
        runTest {
            // Given
            testDispatcher.scheduler.advanceUntilIdle() // Wait for initialization

            // When - Pass a different rate, but ViewModel should use its stored rate
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("EUR" to 999.0))

            // Then - Should use the rate from the ViewModel's exchange rates map (0.85)
            assertEquals("EUR" to 0.85, viewModel.uiState.value.selectedCurrency)
        }

    @Test
    fun `onEvent CategorySelected updates selected category`() =
        runTest {
            // Given
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)

            // When
            viewModel.onEvent(AddExpenseUiEvent.CategorySelected(category))

            // Then
            assertEquals(category, viewModel.uiState.value.selectedCategory)
        }

    @Test
    fun `onEvent ShowDatePicker shows date picker`() =
        runTest {
            // When
            viewModel.onEvent(AddExpenseUiEvent.ShowDatePicker)

            // Then
            assertTrue(viewModel.uiState.value.showDatePicker)
        }

    @Test
    fun `onEvent HideDatePicker hides date picker`() =
        runTest {
            // Given
            viewModel.onEvent(AddExpenseUiEvent.ShowDatePicker)

            // When
            viewModel.onEvent(AddExpenseUiEvent.HideDatePicker)

            // Then
            assertFalse(viewModel.uiState.value.showDatePicker)
        }

    @Test
    fun `onEvent ShowCurrencyDropdown shows currency dropdown`() =
        runTest {
            // When
            viewModel.onEvent(AddExpenseUiEvent.ShowCurrencyDropdown)

            // Then
            assertTrue(viewModel.uiState.value.showCurrencyDropdown)
        }

    @Test
    fun `onEvent HideCurrencyDropdown hides currency dropdown`() =
        runTest {
            // Given
            viewModel.onEvent(AddExpenseUiEvent.ShowCurrencyDropdown)

            // When
            viewModel.onEvent(AddExpenseUiEvent.HideCurrencyDropdown)

            // Then
            assertFalse(viewModel.uiState.value.showCurrencyDropdown)
        }

    @Test
    fun `onEvent SaveExpense with valid data succeeds`() =
        runTest {
            // Given
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            whenever(addExpenseUseCase.invoke(any(), any<Amount>(), any(), any<Map<String, Double>>(), any()))
                .thenReturn(Result.success(1L))

            // Wait for initialization to complete
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("Test Expense"))
            viewModel.onEvent(AddExpenseUiEvent.AmountChanged("100.0"))
            viewModel.onEvent(AddExpenseUiEvent.CategorySelected(category))

            // When & Then
            viewModel.sideEffect.test {
                viewModel.onEvent(AddExpenseUiEvent.SaveExpense)
                testDispatcher.scheduler.advanceUntilIdle()

                val sideEffect = awaitItem()
                assertTrue(sideEffect is AddExpenseSideEffect.NavigateBack)
            }

            verify(addExpenseUseCase).invoke(any(), any<Amount>(), any(), any<Map<String, Double>>(), any())
        }

    @Test
    fun `onEvent SaveExpense with invalid amount does nothing`() =
        runTest {
            // Given
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)

            // Wait for initialization to complete
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("Test Expense"))
            viewModel.onEvent(AddExpenseUiEvent.AmountChanged("invalid"))
            viewModel.onEvent(AddExpenseUiEvent.CategorySelected(category))

            // When
            viewModel.onEvent(AddExpenseUiEvent.SaveExpense)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            assertFalse(viewModel.uiState.value.isSaving)
            verify(addExpenseUseCase, never()).invoke(any(), any<Amount>(), any(), any<Map<String, Double>>(), any())
        }

    @Test
    fun `onEvent SaveExpense without category does nothing`() =
        runTest {
            // Given
            // Wait for initialization to complete
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("Test Expense"))
            viewModel.onEvent(AddExpenseUiEvent.AmountChanged("100.0"))

            // When
            viewModel.onEvent(AddExpenseUiEvent.SaveExpense)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            assertFalse(viewModel.uiState.value.isSaving)
            verify(addExpenseUseCase, never()).invoke(any(), any<Amount>(), any(), any<Map<String, Double>>(), any())
        }

    @Test
    fun `onEvent SaveExpense failure shows error`() =
        runTest {
            // Given
            val category = Category(1L, "Food", CategoryIcon.GROCERIES)
            runBlocking {
                whenever(addExpenseUseCase.invoke(any(), any<Amount>(), any(), any<Map<String, Double>>(), any()))
                    .thenReturn(Result.failure(Exception("Save failed")))
            }

            // Wait for initialization to complete
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("Test Expense"))
            viewModel.onEvent(AddExpenseUiEvent.AmountChanged("100.0"))
            viewModel.onEvent(AddExpenseUiEvent.CategorySelected(category))

            // When & Then
            viewModel.sideEffect.test {
                viewModel.onEvent(AddExpenseUiEvent.SaveExpense)
                testDispatcher.scheduler.advanceUntilIdle()

                val sideEffect = awaitItem()
                assertTrue(sideEffect is AddExpenseSideEffect.ShowError)
                assertEquals("Save failed", (sideEffect as AddExpenseSideEffect.ShowError).message)
            }

            assertTrue(
                viewModel.uiState.value.errorMessage
                    ?.contains("Save failed") == true,
            )
        }

    @Test
    fun `onEvent DismissError clears error message`() =
        runTest {
            // Given
            // Wait for initialization to complete
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("Test Expense"))
            viewModel.onEvent(AddExpenseUiEvent.AmountChanged("100.0"))
            viewModel.onEvent(AddExpenseUiEvent.CategorySelected(Category(1L, "Food", CategoryIcon.GROCERIES)))

            whenever(addExpenseUseCase.invoke(any(), any<Amount>(), any(), any<Map<String, Double>>(), any()))
                .thenReturn(Result.failure(Exception("Save failed")))

            viewModel.onEvent(AddExpenseUiEvent.SaveExpense)
            testDispatcher.scheduler.advanceUntilIdle()

            // When
            viewModel.onEvent(AddExpenseUiEvent.DismissError)

            // Then
            assertEquals(null, viewModel.uiState.value.errorMessage)
        }

    private fun createMockCategories(): List<Category> =
        listOf(
            Category(1L, "Food", CategoryIcon.GROCERIES),
            Category(2L, "Transport", CategoryIcon.TRANSPORT),
            Category(3L, "Entertainment", CategoryIcon.ENTERTAINMENT),
        )

    private fun createMockExchangeRate(): ExchangeRate =
        ExchangeRate(
            baseCode = "USD",
            rates =
                mapOf(
                    "EUR" to 0.85,
                    "GBP" to 0.73,
                    "JPY" to 110.0,
                ),
        )
}
