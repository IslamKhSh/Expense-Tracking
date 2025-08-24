package uk.co.invola.expensetracking.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import javax.inject.Inject

@HiltViewModel
class AllExpensesViewModel
    @Inject
    constructor(
        private val expenseRepository: ExpenseRepository,
    ) : ViewModel() {
        val allExpenses: Flow<PagingData<Expense>> =
            expenseRepository
                .getAllExpensesPaged()
                .cachedIn(viewModelScope)
    }
