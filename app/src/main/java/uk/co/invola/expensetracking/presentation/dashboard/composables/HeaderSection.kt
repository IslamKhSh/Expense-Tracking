package uk.co.invola.expensetracking.presentation.dashboard.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.invola.expensetracking.presentation.dashboard.TimeFilter
import uk.co.invola.expensetracking.presentation.theme.ExpenseTrackingTheme

@Composable
fun HeaderSection(
    selectedFilter: TimeFilter,
    onFilterChanged: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = Color.Blue,
                    shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                ).padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "I",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Column {
                        Text(
                            text = "Hello!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                        Text(
                            text = "Islam",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }

                // Time Filter Dropdown
                TimeFilterDropdown(
                    selectedFilter = selectedFilter,
                    onFilterChanged = onFilterChanged,
                )
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFilterDropdown(
    selectedFilter: TimeFilter,
    onFilterChanged: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        Card(
            modifier = Modifier.menuAnchor(),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.25f),
                ),
            shape = RoundedCornerShape(20.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = selectedFilter.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            TimeFilter.values().forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = filter.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        onFilterChanged(filter)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderSectionPreview() {
    ExpenseTrackingTheme {
        HeaderSection(
            selectedFilter = TimeFilter.THIS_MONTH,
            onFilterChanged = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderSectionWithLast7DaysPreview() {
    ExpenseTrackingTheme {
        HeaderSection(
            selectedFilter = TimeFilter.LAST_7_DAYS,
            onFilterChanged = { },
        )
    }
}
