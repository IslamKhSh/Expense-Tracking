package uk.co.invola.expensetracking.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.invola.expensetracking.domain.model.Amount
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon
import uk.co.invola.expensetracking.domain.model.Expense
import uk.co.invola.expensetracking.presentation.theme.ExpenseTrackingTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseListItem(
    expense: Expense,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Category Icon
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            expense.category?.color?.copy(alpha = 0.15f)
                                ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector =
                        expense.category?.icon?.icon
                            ?: Icons.Default.Add,
                    contentDescription = expense.category?.name,
                    tint =
                        expense.category?.color
                            ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Expense Details
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = expense.category?.name ?: "No Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Manually",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // Amount and Time
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "- ${expense.getDisplayAmount()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )

                Text(
                    text = formatTime(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/**
 * Format date to show time like "Today 12:00 PM"
 */
private fun formatTime(date: Date): String {
    val calendar = Calendar.getInstance()
    val today = calendar.time
    calendar.time = date

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Check if it's today
    val todayCalendar = Calendar.getInstance()
    todayCalendar.time = today

    return if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
    ) {
        "Today ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)}"
    } else {
        SimpleDateFormat("MMM dd h:mm a", Locale.getDefault()).format(date)
    }
}

// Preview Functions
@Preview(showBackground = true)
@Composable
private fun ExpenseListItemPreview() {
    ExpenseTrackingTheme {
        ExpenseListItem(
            expense =
                createMockExpense(
                    categoryIcon = CategoryIcon.GROCERIES,
                    amount = 25.50,
                    currency = "USD",
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListItemTransportPreview() {
    ExpenseTrackingTheme {
        ExpenseListItem(
            expense =
                createMockExpense(
                    categoryIcon = CategoryIcon.TRANSPORT,
                    amount = 12.75,
                    currency = "EUR",
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListItemHealthPreview() {
    ExpenseTrackingTheme {
        ExpenseListItem(
            expense =
                createMockExpense(
                    categoryIcon = CategoryIcon.HEALTH,
                    amount = 89.99,
                    currency = "GBP",
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListItemNoCategoryPreview() {
    ExpenseTrackingTheme {
        ExpenseListItem(
            expense =
                Expense(
                    id = 4,
                    title = "Unknown Expense",
                    originalAmount = Amount(45.00, "USD"),
                    usdAmount = Amount(45.00, "USD"),
                    category = null,
                    date = Date(),
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Multiple Items")
@Composable
private fun ExpenseListItemsPreview() {
    ExpenseTrackingTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExpenseListItem(
                expense = createMockExpense(CategoryIcon.GROCERIES, 25.50, "USD"),
            )
            ExpenseListItem(
                expense = createMockExpense(CategoryIcon.TRANSPORT, 12.75, "EUR"),
            )
            ExpenseListItem(
                expense = createMockExpense(CategoryIcon.ENTERTAINMENT, 45.00, "GBP"),
            )
        }
    }
}

/**
 * Helper function to create mock expense data for previews
 */
private fun createMockExpense(
    categoryIcon: CategoryIcon,
    amount: Double,
    currency: String,
): Expense {
    val category =
        Category(
            id = categoryIcon.ordinal.toLong() + 1,
            name = categoryIcon.name.lowercase().replaceFirstChar { it.uppercase() },
            icon = categoryIcon,
        )

    return Expense(
        id = categoryIcon.ordinal.toLong() + 1,
        title = "${category.name} Expense",
        originalAmount = Amount(amount, currency),
        // Mock conversion
        usdAmount = Amount(amount * 1.1, "USD"),
        category = category,
        date = Date(),
    )
}
