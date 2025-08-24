package uk.co.invola.expensetracking.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.domain.model.Category
import uk.co.invola.expensetracking.domain.model.CategoryIcon
import uk.co.invola.expensetracking.domain.repository.CategoryRepository

@RunWith(MockitoJUnitRunner::class)
class GetCategoriesUseCaseTest {
    @Mock
    private lateinit var categoryRepository: CategoryRepository

    private lateinit var useCase: GetCategoriesUseCase

    @Before
    fun setup() {
        useCase = GetCategoriesUseCase(categoryRepository)
    }

    @Test
    fun `invoke returns categories from repository`() =
        runTest {
            // Given
            val categories =
                listOf(
                    Category(1L, "Food", CategoryIcon.GROCERIES),
                    Category(2L, "Transport", CategoryIcon.TRANSPORT),
                    Category(3L, "Entertainment", CategoryIcon.ENTERTAINMENT),
                )
            whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(categories))

            // When & Then
            useCase.invoke().test {
                val result = awaitItem()

                assertEquals(3, result.size)
                assertEquals("Food", result[0].name)
                assertEquals("Transport", result[1].name)
                assertEquals("Entertainment", result[2].name)
                assertEquals(CategoryIcon.GROCERIES, result[0].icon)
                assertEquals(CategoryIcon.TRANSPORT, result[1].icon)
                assertEquals(CategoryIcon.ENTERTAINMENT, result[2].icon)

                awaitComplete()
            }

            verify(categoryRepository).getAllCategories()
        }

    @Test
    fun `invoke returns empty list when no categories exist`() =
        runTest {
            // Given
            whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(emptyList()))

            // When & Then
            useCase.invoke().test {
                val result = awaitItem()

                assertEquals(0, result.size)

                awaitComplete()
            }

            verify(categoryRepository).getAllCategories()
        }

    @Test
    fun `invoke emits new values when categories change`() =
        runTest {
            // Given
            val initialCategories =
                listOf(
                    Category(1L, "Food", CategoryIcon.GROCERIES),
                )
            val updatedCategories =
                listOf(
                    Category(1L, "Food", CategoryIcon.GROCERIES),
                    Category(2L, "Transport", CategoryIcon.TRANSPORT),
                )

            whenever(categoryRepository.getAllCategories()).thenReturn(
                flowOf(initialCategories, updatedCategories),
            )

            // When & Then
            useCase.invoke().test {
                // First emission
                val firstResult = awaitItem()
                assertEquals(1, firstResult.size)
                assertEquals("Food", firstResult[0].name)

                // Second emission
                val secondResult = awaitItem()
                assertEquals(2, secondResult.size)
                assertEquals("Food", secondResult[0].name)
                assertEquals("Transport", secondResult[1].name)

                awaitComplete()
            }

            verify(categoryRepository).getAllCategories()
        }
}
