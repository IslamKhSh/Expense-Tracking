package uk.co.invola.expensetracking.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.invola.expensetracking.data.local.dao.CategoryDao
import uk.co.invola.expensetracking.data.local.entity.CategoryEntity

@RunWith(MockitoJUnitRunner::class)
class CategoryRepositoryImplTest {
    @Mock
    private lateinit var categoryDao: CategoryDao

    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setup() {
        repository = CategoryRepositoryImpl(categoryDao)
    }

    @Test
    fun `getAllCategories returns mapped categories from dao`() =
        runTest {
            // Given
            val categoryEntities =
                listOf(
                    CategoryEntity(1L, "Food", "ICON_GROCERIES"),
                    CategoryEntity(2L, "Transport", "ICON_TRANSPORT"),
                )
            whenever(categoryDao.getAllCategories()).thenReturn(flowOf(categoryEntities))

            // When
            val result = repository.getAllCategories().first()

            // Then
            assertEquals(2, result.size)
            assertEquals("Food", result[0].name)
            assertEquals("Transport", result[1].name)
            verify(categoryDao).getAllCategories()
        }

    @Test
    fun `areCategoriesInitialized returns true when categories exist`() =
        runTest {
            // Given
            whenever(categoryDao.getCategoryCount()).thenReturn(5)

            // When
            val result = repository.areCategoriesInitialized()

            // Then
            assertTrue(result)
            verify(categoryDao).getCategoryCount()
        }

    @Test
    fun `areCategoriesInitialized returns false when no categories exist`() =
        runTest {
            // Given
            whenever(categoryDao.getCategoryCount()).thenReturn(0)

            // When
            val result = repository.areCategoriesInitialized()

            // Then
            assertFalse(result)
            verify(categoryDao).getCategoryCount()
        }
}
