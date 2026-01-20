package com.ieum.di

import android.content.Context
import com.ieum.data.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://54.66.195.91/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context
    ): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ==================== API Services ====================

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideCoupleService(retrofit: Retrofit): CoupleService {
        return retrofit.create(CoupleService::class.java)
    }

    @Provides
    @Singleton
    fun provideMbtiService(retrofit: Retrofit): MbtiService {
        return retrofit.create(MbtiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMemoryService(retrofit: Retrofit): MemoryService {
        return retrofit.create(MemoryService::class.java)
    }

    @Provides
    @Singleton
    fun provideEventService(retrofit: Retrofit): EventService {
        return retrofit.create(EventService::class.java)
    }

    @Provides
    @Singleton
    fun provideExpenseService(retrofit: Retrofit): ExpenseService {
        return retrofit.create(ExpenseService::class.java)
    }

    @Provides
    @Singleton
    fun provideBudgetService(retrofit: Retrofit): BudgetService {
        return retrofit.create(BudgetService::class.java)
    }

    @Provides
    @Singleton
    fun provideBucketService(retrofit: Retrofit): BucketService {
        return retrofit.create(BucketService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatService(retrofit: Retrofit): ChatService {
        return retrofit.create(ChatService::class.java)
    }

    @Provides
    @Singleton
    fun provideFileService(retrofit: Retrofit): FileService {
        return retrofit.create(FileService::class.java)
    }

    @Provides
    @Singleton
    fun provideRecommendationService(retrofit: Retrofit): RecommendationService {
        return retrofit.create(RecommendationService::class.java)
    }
}
