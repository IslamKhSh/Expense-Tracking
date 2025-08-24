package uk.co.invola.expensetracking.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.domain.model.Category

interface CategoryRepository {
    /**
     * Get all categories
     */
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Insert multiple categories
     */
    suspend fun insertCategories(categories: List<Category>): List<Long>

    /**
     * Initialize default categories if database is empty
     */
    suspend fun initializeDefaultCategories(): List<Long>

    /**
     * Check if categories are initialized
     */
    suspend fun areCategoriesInitialized(): Boolean
}
