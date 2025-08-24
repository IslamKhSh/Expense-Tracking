package uk.co.invola.expensetracking.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.invola.expensetracking.data.local.dao.CategoryDao
import uk.co.invola.expensetracking.data.mapper.toDomain
import uk.co.invola.expensetracking.data.mapper.toEntity
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl
    @Inject
    constructor(
        private val categoryDao: CategoryDao,
    ) : CategoryRepository {
        override fun getAllCategories(): Flow<List<Category>> =
            categoryDao.getAllCategories().map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun insertCategories(categories: List<Category>): List<Long> =
            categoryDao.insertCategories(
                categories.map {
                    it.toEntity()
                },
            )

        override suspend fun initializeDefaultCategories(): List<Long> {
            Log.d("CategoryRepository", "Initializing all categories from CategoryIcon enum...")

            val existingCount = categoryDao.getCategoryCount()
            if (existingCount > 0) {
                Log.d("CategoryRepository", "Categories already exist ($existingCount), skipping initialization")
                return emptyList()
            }

            val allCategories = Category.getAllCategoriesFromEnum()
            Log.d("CategoryRepository", "Inserting ${allCategories.size} categories from enum")

            val insertedIds = insertCategories(allCategories)
            Log.d("CategoryRepository", "All categories inserted with IDs: $insertedIds")

            return insertedIds
        }

        override suspend fun areCategoriesInitialized(): Boolean = categoryDao.getCategoryCount() > 0
    }
