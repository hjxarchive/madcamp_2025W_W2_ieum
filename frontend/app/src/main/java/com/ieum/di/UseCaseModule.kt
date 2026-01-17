package com.ieum.di

import com.ieum.domain.repository.*
import com.ieum.domain.usecase.bucket.*
import com.ieum.domain.usecase.chat.*
import com.ieum.domain.usecase.finance.*
import com.ieum.domain.usecase.memory.*
import com.ieum.domain.usecase.recommendation.*
import com.ieum.domain.usecase.schedule.*
import com.ieum.domain.usecase.user.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    // User UseCases
    @Provides
    @ViewModelScoped
    fun provideGetCoupleInfoUseCase(
        userRepository: UserRepository
    ): GetCoupleInfoUseCase = GetCoupleInfoUseCase(userRepository)

    // Schedule UseCases
    @Provides
    @ViewModelScoped
    fun provideGetSchedulesForMonthUseCase(
        scheduleRepository: ScheduleRepository
    ): GetSchedulesForMonthUseCase = GetSchedulesForMonthUseCase(scheduleRepository)

    @Provides
    @ViewModelScoped
    fun provideGetAnniversariesUseCase(
        scheduleRepository: ScheduleRepository
    ): GetAnniversariesUseCase = GetAnniversariesUseCase(scheduleRepository)

    // Chat UseCases
    @Provides
    @ViewModelScoped
    fun provideGetChatMessagesUseCase(
        chatRepository: ChatRepository
    ): GetChatMessagesUseCase = GetChatMessagesUseCase(chatRepository)

    @Provides
    @ViewModelScoped
    fun provideSendMessageUseCase(
        chatRepository: ChatRepository
    ): SendMessageUseCase = SendMessageUseCase(chatRepository)

    // Bucket UseCases
    @Provides
    @ViewModelScoped
    fun provideGetBucketItemsUseCase(
        bucketRepository: BucketRepository
    ): GetBucketItemsUseCase = GetBucketItemsUseCase(bucketRepository)

    @Provides
    @ViewModelScoped
    fun provideToggleBucketCompleteUseCase(
        bucketRepository: BucketRepository
    ): ToggleBucketCompleteUseCase = ToggleBucketCompleteUseCase(bucketRepository)

    @Provides
    @ViewModelScoped
    fun provideAddBucketItemUseCase(
        bucketRepository: BucketRepository
    ): AddBucketItemUseCase = AddBucketItemUseCase(bucketRepository)

    // Memory UseCases
    @Provides
    @ViewModelScoped
    fun provideGetMemoriesUseCase(
        memoryRepository: MemoryRepository
    ): GetMemoriesUseCase = GetMemoriesUseCase(memoryRepository)

    // Recommendation UseCases
    @Provides
    @ViewModelScoped
    fun provideGetTodayRecommendationUseCase(
        recommendationRepository: RecommendationRepository
    ): GetTodayRecommendationUseCase = GetTodayRecommendationUseCase(recommendationRepository)

    @Provides
    @ViewModelScoped
    fun provideGetPopularCoursesUseCase(
        recommendationRepository: RecommendationRepository
    ): GetPopularCoursesUseCase = GetPopularCoursesUseCase(recommendationRepository)

    // Finance UseCases
    @Provides
    @ViewModelScoped
    fun provideGetBudgetUseCase(
        financeRepository: FinanceRepository
    ): GetBudgetUseCase = GetBudgetUseCase(financeRepository)

    @Provides
    @ViewModelScoped
    fun provideGetExpensesUseCase(
        financeRepository: FinanceRepository
    ): GetExpensesUseCase = GetExpensesUseCase(financeRepository)

    @Provides
    @ViewModelScoped
    fun provideGetExpensesByCategoryUseCase(
        financeRepository: FinanceRepository
    ): GetExpensesByCategoryUseCase = GetExpensesByCategoryUseCase(financeRepository)

    @Provides
    @ViewModelScoped
    fun provideSetBudgetUseCase(
        financeRepository: FinanceRepository
    ): SetBudgetUseCase = SetBudgetUseCase(financeRepository)
}
