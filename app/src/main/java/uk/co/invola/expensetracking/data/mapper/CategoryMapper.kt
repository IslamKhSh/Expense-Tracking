package uk.co.invola.expensetracking.data.mapper

import uk.co.invola.expensetracking.data.local.entity.CategoryEntity
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon

fun CategoryEntity.toDomain(): Category =
    Category(
        id = id,
        name = name,
        icon = CategoryIcon.fromIconId(iconId),
    )

/**
 * Extension function to convert Category domain model to CategoryEntity
 */
fun Category.toEntity(): CategoryEntity =
    CategoryEntity(
        id = id,
        name = name,
        iconId = icon.iconId,
    )
