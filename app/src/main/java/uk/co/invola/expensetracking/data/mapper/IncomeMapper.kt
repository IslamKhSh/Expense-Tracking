package uk.co.invola.expensetracking.data.mapper

import uk.co.invola.expensetracking.data.local.entity.IncomeEntity
import uk.co.invola.expensetracking.domain.model.Income

/**
 * Extension function to convert IncomeEntity to Income domain model
 */
fun IncomeEntity.toDomain(): Income =
    Income(
        id = id,
        monthlyAmount = monthlyAmount,
        usdAmount = usdAmount,
        monthYear = monthYear,
        date = date,
    )

/**
 * Extension function to convert Income domain model to IncomeEntity
 */
fun Income.toEntity(): IncomeEntity =
    IncomeEntity(
        // Ensure 0 for new entities to let Room auto-generate
        id = if (id <= 0) 0 else id,
        monthlyAmount = monthlyAmount,
        usdAmount = usdAmount,
        monthYear = monthYear,
        date = date,
    )
