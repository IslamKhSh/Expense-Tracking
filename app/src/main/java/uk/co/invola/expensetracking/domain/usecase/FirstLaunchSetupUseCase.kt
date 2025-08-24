package uk.co.invola.expensetracking.domain.usecase

import uk.co.invola.expensetracking.domain.repository.CategoryRepository
import uk.co.invola.expensetracking.domain.repository.IncomeRepository
import javax.inject.Inject

class FirstLaunchSetupUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
        private val incomeRepository: IncomeRepository,
    ) {
        suspend operator fun invoke(): Result<Boolean> {
            val categoriesExist = categoryRepository.areCategoriesInitialized()
            val currentIncome = incomeRepository.getCurrentMonthIncome()

            if (categoriesExist && currentIncome != null) {
                return Result.success(true)
            }

            if (!categoriesExist) {
                categoryRepository.initializeDefaultCategories()
            }

            if (currentIncome == null) {
                incomeRepository.initializeDefaultIncome()
            }

            return Result.success(true)
        }
    }
