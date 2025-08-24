package uk.co.invola.expensetracking.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import uk.co.invola.expensetracking.presentation.addexpense.AddExpenseScreen
import uk.co.invola.expensetracking.presentation.dashboard.DashboardScreen
import uk.co.invola.expensetracking.presentation.expenses.AllExpensesScreen
import uk.co.invola.expensetracking.presentation.theme.ExpenseTrackingTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackingTheme {
                ExpenseTrackingApp()
            }
        }
    }
}

const val DASHBOARD = "dashboard"
const val ALL_EXPENSES = "all_expenses"
const val ADD_EXPENSE = "add_expense"

@Composable
fun ExpenseTrackingApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DASHBOARD,
    ) {
        composable(DASHBOARD) {
            DashboardScreen(
                onNavigateToAllExpenses = {
                    navController.navigate(ALL_EXPENSES)
                },
                onNavigateToAddExpense = {
                    navController.navigate(ADD_EXPENSE)
                },
            )
        }

        composable(ALL_EXPENSES) {
            AllExpensesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(ADD_EXPENSE) {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
