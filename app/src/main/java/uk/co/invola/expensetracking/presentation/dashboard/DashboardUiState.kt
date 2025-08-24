package uk.co.invola.expensetracking.presentation.dashboard

import uk.co.invola.expensetracking.domain.model.Balance
import uk.co.invola.expensetracking.domain.model.Expense

data class DashboardUiState(
    val isLoading: Boolean = false,
    val balance: Balance = Balance(totalIncomeUsd = 0.0, totalExpensesUsd = 0.0),
    val recentExpenses: List<Expense> = emptyList(),
    val selectedFilter: TimeFilter = TimeFilter.THIS_MONTH,
)

enum class TimeFilter(
    val displayName: String,
    val days: Int?,
) {
    THIS_MONTH("This month", null),
    LAST_7_DAYS("Last 7 days", 7),
}

sealed class DashboardUiEvent {
    data class FilterChanged(
        val filter: TimeFilter,
    ) : DashboardUiEvent()

    object NavigateToAllExpenses : DashboardUiEvent()

    object NavigateToAddExpense : DashboardUiEvent()
}

sealed class DashboardSideEffect {
    object NavigateToAllExpenses : DashboardSideEffect()

    object NavigateToAddExpense : DashboardSideEffect()
}
