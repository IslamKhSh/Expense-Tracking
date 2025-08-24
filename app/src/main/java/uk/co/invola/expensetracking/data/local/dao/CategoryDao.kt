package uk.co.invola.expensetracking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    /**
     * Get all categories ordered by name
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * Insert multiple categories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    /**
     * Check if categories table is empty
     */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
