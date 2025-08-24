package uk.co.invola.expensetracking.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uk.co.invola.expensetracking.data.local.dao.CategoryDao
import uk.co.invola.expensetracking.data.local.dao.ExpenseDao
import uk.co.invola.expensetracking.data.local.dao.IncomeDao
import uk.co.invola.expensetracking.data.local.database.ExpenseDatabase
import uk.co.invola.expensetracking.data.repository.CategoryRepositoryImpl
import uk.co.invola.expensetracking.data.repository.ExpenseRepositoryImpl
import uk.co.invola.expensetracking.data.repository.IncomeRepositoryImpl
import uk.co.invola.expensetracking.domain.repository.CategoryRepository
import uk.co.invola.expensetracking.domain.repository.ExpenseRepository
import uk.co.invola.expensetracking.domain.repository.IncomeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindExpenseRepository(expenseRepositoryImpl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    abstract fun bindIncomeRepository(incomeRepositoryImpl: IncomeRepositoryImpl): IncomeRepository

    @Binds
    abstract fun bindCategoryRepository(categoryRepositoryImpl: CategoryRepositoryImpl): CategoryRepository

    companion object {
        /**
         * Provides the Room database instance
         */
        @Provides
        @Singleton
        fun provideExpenseDatabase(
            @ApplicationContext context: Context,
        ): ExpenseDatabase =
            Room
                .databaseBuilder(
                    context,
                    ExpenseDatabase::class.java,
                    ExpenseDatabase.DATABASE_NAME,
                ).fallbackToDestructiveMigration(true) // For development - removes all data on schema change
                .build()

        /**
         * Provides the ExpenseDao from the database
         */
        @Provides
        fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao = database.expenseDao()

        /**
         * Provides the IncomeDao from the database
         */
        @Provides
        fun provideIncomeDao(database: ExpenseDatabase): IncomeDao = database.incomeDao()

        /**
         * Provides the CategoryDao from the database
         */
        @Provides
        fun provideCategoryDao(database: ExpenseDatabase): CategoryDao = database.categoryDao()
    }
}
