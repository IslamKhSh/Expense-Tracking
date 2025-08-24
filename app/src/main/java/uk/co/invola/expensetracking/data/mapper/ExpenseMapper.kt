package uk.co.invola.expensetracking.data.mapper

import uk.co.invola.expensetracking.data.local.entity.ExpenseEntity
import uk.co.invola.expensetracking.data.local.entity.ExpenseWithCategory
import uk.co.invola.expensetracking.domain.model.Expense

fun ExpenseWithCategory.toDomain(): Expense =
    Expense(
        id = expense.id,
        title = expense.title,
        originalAmount = expense.originalAmount,
        usdAmount = expense.usdAmount,
        category = category?.toDomain(),
        date = expense.date,
    )

/**
 * Convert ExpenseEntity to Expense domain model (without category)
 */
fun ExpenseEntity.toDomain(): Expense =
    Expense(
        id = id,
        title = title,
        originalAmount = originalAmount,
        usdAmount = usdAmount,
        // Category will be loaded separately
        category = null,
        date = date,
    )

/**
 * Convert Expense domain model to ExpenseEntity
 */
fun Expense.toEntity(): ExpenseEntity =
    ExpenseEntity(
        id = id,
        title = title,
        originalAmount = originalAmount,
        usdAmount = usdAmount,
        categoryId = category?.id,
        date = date,
    )
