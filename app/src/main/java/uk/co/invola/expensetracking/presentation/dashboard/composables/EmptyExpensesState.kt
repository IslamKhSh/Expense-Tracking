package uk.co.invola.expensetracking.presentation.dashboard.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.invola.expensetracking.presentation.theme.ExpenseTrackingTheme

@Composable
fun EmptyExpensesState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Empty state icon
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "No expenses",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Empty state title
        Text(
            text = "No Expenses Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Empty state description
        Text(
            text = "Start tracking your expenses by adding your first expense using the + button below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyExpensesStatePreview() {
    ExpenseTrackingTheme {
        EmptyExpensesState()
    }
}
