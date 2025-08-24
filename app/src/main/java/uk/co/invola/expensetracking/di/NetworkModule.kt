package uk.co.invola.expensetracking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import uk.co.invola.expensetracking.BuildConfig
import uk.co.invola.expensetracking.data.remote.ExpenseApi
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /**
     * Provides a configured HttpLoggingInterceptor for network request/response logging.
     *
     * @return HttpLoggingInterceptor configured with BODY level logging
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }

    /**
     * Provides a configured OkHttpClient with logging interceptor and timeout settings
     *
     * @param loggingInterceptor The logging interceptor for network request/response logging
     * @return Configured OkHttpClient instance
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    /**
     * Provides a configured Json instance for Kotlinx Serialization
     *
     * @return Configured Json instance
     */
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    /**
     * Provides a configured Retrofit instance for making API calls.
     *
     * @param okHttpClient The configured OkHttpClient instance
     * @param json The configured Json instance
     * @return Configured Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    /**
     * Provides the ExpenseApi service interface implementation.
     *
     * @param retrofit The configured Retrofit instance
     * @return ExpenseApi service implementation
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ExpenseApi = retrofit.create(ExpenseApi::class.java)
}
