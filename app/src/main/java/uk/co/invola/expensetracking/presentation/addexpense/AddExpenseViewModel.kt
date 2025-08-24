package uk.co.invola.expensetracking.presentation.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import uk.co.invola.expensetracking.domain.usecase.AddExpenseUseCase
import uk.co.invola.expensetracking.domain.usecase.GetCategoriesUseCase
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel
    @Inject
    constructor(
        private val getCategoriesUseCase: GetCategoriesUseCase,
        private val addExpenseUseCase: AddExpenseUseCase,
        private val expenseRepository: ExpenseRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddExpenseUiState())
        val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

        private val _sideEffect = MutableSharedFlow<AddExpenseSideEffect>()
        val sideEffect: SharedFlow<AddExpenseSideEffect> = _sideEffect.asSharedFlow()

        private var exchangeRatesMap: Map<String, Double> = emptyMap()

        init {
            loadData()
        }

        /**
         * Handle UI events from the screen
         */
        fun onEvent(event: AddExpenseUiEvent) {
            when (event) {
                is AddExpenseUiEvent.TitleChanged -> updateTitle(event.title)
                is AddExpenseUiEvent.AmountChanged -> updateAmount(event.amount)
                is AddExpenseUiEvent.DateSelected -> updateDate(event.date)
                is AddExpenseUiEvent.CurrencySelected -> updateCurrency(event.currency)
                is AddExpenseUiEvent.CategorySelected -> updateCategory(event.category)
                is AddExpenseUiEvent.ShowDatePicker -> showDatePicker()
                is AddExpenseUiEvent.HideDatePicker -> hideDatePicker()
                is AddExpenseUiEvent.ShowCurrencyDropdown -> showCurrencyDropdown()
                is AddExpenseUiEvent.HideCurrencyDropdown -> hideCurrencyDropdown()
                is AddExpenseUiEvent.SaveExpense -> saveExpense()
                is AddExpenseUiEvent.DismissError -> dismissError()
            }
        }

        private fun loadData() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Load categories and exchange rates in parallel using async
                val categoriesDeferred = async { getCategoriesUseCase().first() }
                val exchangeRatesDeferred = async { expenseRepository.getExchangeRates() }

                // Wait for both operations to complete
                val categories = categoriesDeferred.await()
                val exchangeRatesResult = exchangeRatesDeferred.await()

                if (exchangeRatesResult.isSuccess) {
                    val exchangeRates = exchangeRatesResult.getOrNull()!!
                    val ratesMap =
                        exchangeRates.rates.toMutableMap().apply {
                            put("USD", 1.0) // Add USD as base currency
                        }

                    // Store exchange rates map in ViewModel
                    exchangeRatesMap = ratesMap

                    _uiState.value =
                        _uiState.value.copy(
                            categories = categories,
                            availableCurrencies =
                                ratesMap.keys
                                    .toList()
                                    .sorted(),
                            selectedCurrency = "USD" to 1.0, // Set default to USD with rate 1.0
                            isLoading = false,
                            errorMessage = null,
                        )
                } else {
                    val errorMessage =
                        exchangeRatesResult.exceptionOrNull()?.message
                            ?: "Failed to load currencies"

                    _uiState.value =
                        _uiState.value.copy(
                            categories = categories,
                            isLoading = false,
                            errorMessage = errorMessage,
                            // Fallback to common currencies
                            availableCurrencies = emptyList(),
                        )
                }
            }
        }

        private fun updateTitle(title: String) {
            _uiState.value = _uiState.value.copy(title = title)
        }

        private fun updateAmount(amount: String) {
            _uiState.value = _uiState.value.copy(amount = amount)
        }

        private fun updateDate(date: Date) {
            _uiState.value =
                _uiState.value.copy(
                    selectedDate = date,
                    showDatePicker = false,
                )
        }

        private fun updateCurrency(currency: Pair<String, Double>) {
            // Use the stored exchange rates map to get the most up-to-date rate
            val rate = exchangeRatesMap[currency.first] ?: currency.second
            val updatedCurrency = currency.first to rate

            _uiState.value =
                _uiState.value.copy(
                    selectedCurrency = updatedCurrency,
                    showCurrencyDropdown = false,
                )
        }

        private fun updateCategory(category: Category) {
            _uiState.value = _uiState.value.copy(selectedCategory = category)
        }

        private fun showDatePicker() {
            _uiState.value = _uiState.value.copy(showDatePicker = true)
        }

        private fun hideDatePicker() {
            _uiState.value = _uiState.value.copy(showDatePicker = false)
        }

        private fun showCurrencyDropdown() {
            _uiState.value = _uiState.value.copy(showCurrencyDropdown = true)
        }

        private fun hideCurrencyDropdown() {
            _uiState.value = _uiState.value.copy(showCurrencyDropdown = false)
        }

        private fun saveExpense() {
            val currentState = _uiState.value

            if (!currentState.canSave) {
                return
            }

            _uiState.value = currentState.copy(isSaving = true, errorMessage = null)

            viewModelScope.launch {
                val amount = currentState.amount.toDoubleOrNull() ?: return@launch
                val category = currentState.selectedCategory ?: return@launch

                val result =
                    addExpenseUseCase(
                        title = currentState.title,
                        originalAmount = Amount(amount, currentState.selectedCurrency.first),
                        category = category,
                        exchangeRatesMap = exchangeRatesMap,
                        date = currentState.selectedDate,
                    )

                if (result.isSuccess) {
                    _sideEffect.emit(AddExpenseSideEffect.NavigateBack)
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Failed to save expense"
                    _uiState.value =
                        currentState.copy(
                            isSaving = false,
                            errorMessage = errorMessage,
                        )
                    _sideEffect.emit(AddExpenseSideEffect.ShowError(errorMessage))
                }
            }
        }

        private fun dismissError() {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }
