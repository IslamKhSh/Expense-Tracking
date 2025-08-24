package uk.co.invola.expensetracking.domain.usecase

import kotlinx.coroutines.flow.Flow
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        /**
         * Get all categories
         */
        operator fun invoke(): Flow<List<Category>> = categoryRepository.getAllCategories()
    }
