package uk.co.invola.expensetracking.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import uk.co.invola.expensetracking.domain.usecase.FirstLaunchSetupUseCase
import uk.co.invola.expensetracking.domain.usecase.GetBalanceUseCase
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val getBalanceUseCase: GetBalanceUseCase,
        private val firstLaunchSetupUseCase: FirstLaunchSetupUseCase,
        private val expenseRepository: ExpenseRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
        val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

        private val _sideEffect = MutableSharedFlow<DashboardSideEffect>()
        val sideEffect: SharedFlow<DashboardSideEffect> = _sideEffect.asSharedFlow()

        init {
            initializeDashboard()
        }

        fun onEvent(event: DashboardUiEvent) {
            when (event) {
                is DashboardUiEvent.FilterChanged -> updateFilter(event.filter)
                is DashboardUiEvent.NavigateToAllExpenses -> navigateToAllExpenses()
                is DashboardUiEvent.NavigateToAddExpense -> navigateToAddExpense()
            }
        }

        private fun initializeDashboard() {
            viewModelScope.launch {
                // Initialize app data on first launch and waiting until it's done
                async { firstLaunchSetupUseCase() }.await()

                // Load initial data
                loadDashboardData()
            }
        }

        private fun loadDashboardData() {
            viewModelScope.launch {
                // Combine balance and expenses flows
                combine(
                    getBalanceUseCase(),
                    getFilteredExpenses(_uiState.value.selectedFilter),
                ) { balance, expenses ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            balance = balance,
                            recentExpenses = expenses,
                        )
                }.collect()
            }
        }

        private fun getFilteredExpenses(filter: TimeFilter): Flow<List<Expense>> =
            when (filter) {
                TimeFilter.THIS_MONTH -> {
                    val calendar = Calendar.getInstance()
                    // Start of current month
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfMonth = calendar.time

                    expenseRepository.getRecentExpenses(
                        startDate = startOfMonth,
                        limit = 10,
                    )
                }

                TimeFilter.LAST_7_DAYS -> {
                    val sevenDaysAgo =
                        Calendar
                            .getInstance()
                            .apply {
                                add(Calendar.DAY_OF_YEAR, -7)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time

                    expenseRepository.getRecentExpenses(
                        startDate = sevenDaysAgo,
                        limit = 10,
                    )
                }
            }

        private fun updateFilter(filter: TimeFilter) {
            _uiState.value =
                _uiState.value.copy(
                    selectedFilter = filter,
                )

            viewModelScope.launch {
                getFilteredExpenses(filter).collect { expenses ->
                    _uiState.value =
                        _uiState.value.copy(
                            recentExpenses = expenses,
                        )
                }
            }
        }

        private fun navigateToAllExpenses() {
            viewModelScope.launch {
                _sideEffect.emit(DashboardSideEffect.NavigateToAllExpenses)
            }
        }

        private fun navigateToAddExpense() {
            viewModelScope.launch {
                _sideEffect.emit(DashboardSideEffect.NavigateToAddExpense)
            }
        }
    }
