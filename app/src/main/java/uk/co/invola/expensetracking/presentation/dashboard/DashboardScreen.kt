package uk.co.invola.expensetracking.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Balance
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.presentation.composables.ExpenseListItem
import uk.co.invola.expensetracking.presentation.dashboard.composables.BalanceCard
import uk.co.invola.expensetracking.presentation.dashboard.composables.EmptyExpensesState
import uk.co.invola.expensetracking.presentation.dashboard.composables.HeaderSection
import uk.co.invola.expensetracking.presentation.theme.ExpenseTrackingTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAllExpenses: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle side effects
    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is DashboardSideEffect.NavigateToAllExpenses -> onNavigateToAllExpenses()
                is DashboardSideEffect.NavigateToAddExpense -> onNavigateToAddExpense()
            }
        }
    }

    DashboardContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(DashboardUiEvent.NavigateToAddExpense) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                )
            }
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }
            else -> {
                DashboardSuccessContent(
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun DashboardSuccessContent(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Header Section with Balance Card
        item {
            Box(contentAlignment = Alignment.BottomCenter) {
                Column {
                    HeaderSection(
                        selectedFilter = uiState.selectedFilter,
                        onFilterChanged = { filter ->
                            onEvent(DashboardUiEvent.FilterChanged(filter))
                        },
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }

                BalanceCard(
                    balance = uiState.balance,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (uiState.recentExpenses.isNotEmpty()) {
            item {
                RecentExpensesHeader(
                    onSeeAllClick = { onEvent(DashboardUiEvent.NavigateToAllExpenses) },
                    modifier = Modifier.padding(16.dp),
                )
            }

            // Recent Expenses List
            items(uiState.recentExpenses) { expense ->
                ExpenseListItem(
                    expense = expense,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        } else {
            item {
                EmptyExpensesState(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun RecentExpensesHeader(
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recent Expenses",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "see all",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// Preview Data
private val mockBalance =
    Balance(
        totalIncomeUsd = 4000.0,
        totalExpensesUsd = 1452.0,
    )

private val mockExpenses =
    listOf(
        Expense(
            id = 1,
            title = "Groceries",
            originalAmount = Amount.usd(100.0),
            usdAmount = Amount.usd(100.0),
            category =
                Category(
                    id = 1,
                    name = "Groceries",
                    icon = CategoryIcon.GROCERIES,
                ),
            date = Date(),
        ),
        Expense(
            id = 2,
            title = "Entertainment",
            originalAmount = Amount.usd(50.0),
            usdAmount = Amount.usd(50.0),
            category =
                Category(
                    id = 2,
                    name = "Entertainment",
                    icon = CategoryIcon.ENTERTAINMENT,
                ),
            date = Date(),
        ),
        Expense(
            id = 3,
            title = "Transportation",
            originalAmount = Amount.usd(75.0),
            usdAmount = Amount.usd(75.0),
            category =
                Category(
                    id = 3,
                    name = "Transportation",
                    icon = CategoryIcon.TRANSPORT,
                ),
            date = Date(),
        ),
    )

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    ExpenseTrackingTheme {
        DashboardContent(
            uiState =
                DashboardUiState(
                    isLoading = false,
                    balance = mockBalance,
                    recentExpenses = mockExpenses,
                    selectedFilter = TimeFilter.THIS_MONTH,
                ),
            onEvent = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenEmptyPreview() {
    ExpenseTrackingTheme {
        DashboardContent(
            uiState =
                DashboardUiState(
                    isLoading = false,
                    balance = Balance(totalIncomeUsd = 4000.0, totalExpensesUsd = 0.0),
                    recentExpenses = emptyList(),
                    selectedFilter = TimeFilter.THIS_MONTH,
                ),
            onEvent = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenLast7DaysPreview() {
    ExpenseTrackingTheme {
        DashboardContent(
            uiState =
                DashboardUiState(
                    isLoading = false,
                    balance = mockBalance,
                    recentExpenses = mockExpenses.take(2),
                    selectedFilter = TimeFilter.LAST_7_DAYS,
                ),
            onEvent = { },
        )
    }
}
