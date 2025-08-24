package uk.co.invola.expensetracking.presentation.addexpense

import uk.co.invola.expensetracking.domain.model.Category
import java.util.Date

data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val selectedDate: Date = Date(),
    val selectedCurrency: Pair<String, Double> = "USD" to 1.0,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val availableCurrencies: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    // UI states
    val showDatePicker: Boolean = false,
    val showCurrencyDropdown: Boolean = false,
) {
    /**
     * Check if the form is valid for submission
     */
    val canSave: Boolean
        get() =
            title.isNotBlank() &&
                amount.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                amount.toDoubleOrNull()!! > 0 &&
                selectedCategory != null &&
                availableCurrencies.contains(selectedCurrency.first) &&
                !isSaving

    /**
     * Get formatted date string for display
     */
    fun getFormattedDate(): String {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(selectedDate)
    }
}

sealed class AddExpenseUiEvent {
    data class TitleChanged(
        val title: String,
    ) : AddExpenseUiEvent()

    data class AmountChanged(
        val amount: String,
    ) : AddExpenseUiEvent()

    data class DateSelected(
        val date: Date,
    ) : AddExpenseUiEvent()

    data class CurrencySelected(
        val currency: Pair<String, Double>,
    ) : AddExpenseUiEvent()

    data class CategorySelected(
        val category: Category,
    ) : AddExpenseUiEvent()

    object ShowDatePicker : AddExpenseUiEvent()

    object HideDatePicker : AddExpenseUiEvent()

    object ShowCurrencyDropdown : AddExpenseUiEvent()

    object HideCurrencyDropdown : AddExpenseUiEvent()

    object SaveExpense : AddExpenseUiEvent()

    object DismissError : AddExpenseUiEvent()
}

sealed class AddExpenseSideEffect {
    object NavigateBack : AddExpenseSideEffect()

    data class ShowError(
        val message: String,
    ) : AddExpenseSideEffect()
}
