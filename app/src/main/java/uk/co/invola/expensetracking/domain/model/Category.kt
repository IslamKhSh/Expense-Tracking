package uk.co.invola.expensetracking.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing an expense category
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: CategoryIcon,
) {
    /**
     * Get color from the enum
     */
    val color: Color
        get() = icon.color

    companion object {
        /**
         * Get all categories from CategoryIcon enum
         */
        fun getAllCategoriesFromEnum(): List<Category> =
            CategoryIcon.entries.map { categoryIcon ->
                Category(
                    name =
                        categoryIcon.name
                            .lowercase()
                            .split('_')
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { it.uppercase() }
                            },
                    icon = categoryIcon,
                )
            }
    }
}
