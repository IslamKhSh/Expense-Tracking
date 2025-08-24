package uk.co.invola.expensetracking.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.co.invola.expensetracking.domain.model.Category

@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                isSelected = selectedCategory?.id == category.id,
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

/**
 * Individual category item
 */
@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clickable { onClick() }
                .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Category icon with background
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            category.color.copy(alpha = 0.3f)
                        } else {
                            category.color.copy(alpha = 0.1f)
                        },
                    ).border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) category.color else Color.Transparent,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = category.icon.icon,
                contentDescription = category.name,
                tint = if (isSelected) category.color else category.color.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category name
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/**
 * Non-scrollable category grid for use in scrollable containers
 */
@Composable
fun CategoryGridNonScrollable(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chunkedCategories = categories.chunked(4) // 4 items per row

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        chunkedCategories.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowCategories.forEach { category ->
                    CategoryItem(
                        category = category,
                        isSelected = selectedCategory?.id == category.id,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill remaining slots with empty space if needed
                repeat(4 - rowCategories.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Horizontal category selector (for forms) - Non-scrollable version
 */
@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        CategoryGridNonScrollable(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
        )
    }
}
