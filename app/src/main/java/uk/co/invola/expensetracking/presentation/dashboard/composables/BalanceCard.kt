package uk.co.invola.expensetracking.presentation.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.invola.expensetracking.domain.model.Balance
import uk.co.invola.expensetracking.presentation.theme.ExpenseTrackingTheme
import uk.co.invola.expensetracking.utils.CurrencyUtils

@Composable
fun BalanceCard(
    balance: Balance,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFF496EF3),
            ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            // Total Balance Header

            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Balance Amount
            Text(
                text = balance.getFormattedBalance(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Income and Expenses Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Income Section
                Column(
                    horizontalAlignment = Alignment.Start,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Income",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = CurrencyUtils.formatWithCurrency(balance.totalIncomeUsd),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                // Expenses Section
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expenses",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = CurrencyUtils.formatWithCurrency(balance.totalExpensesUsd),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceCardPreview() {
    ExpenseTrackingTheme {
        BalanceCard(
            balance =
                Balance(
                    totalIncomeUsd = 4000.0,
                    totalExpensesUsd = 1452.0,
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceCardNegativePreview() {
    ExpenseTrackingTheme {
        BalanceCard(
            balance =
                Balance(
                    totalIncomeUsd = 2000.0,
                    totalExpensesUsd = 2500.0,
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceCardZeroPreview() {
    ExpenseTrackingTheme {
        BalanceCard(
            balance =
                Balance(
                    totalIncomeUsd = 1000.0,
                    totalExpensesUsd = 1000.0,
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
