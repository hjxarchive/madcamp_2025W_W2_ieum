# 이음(IEUM) - 커플 데이트 앱 개발 프레임워크

> 커플을 위한 올인원 데이트 플랫폼

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [프론트엔드 구조 (Android/Kotlin)](#3-프론트엔드-구조)
4. [백엔드 구조 (Spring Boot)](#4-백엔드-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 명세](#6-api-명세)
7. [외부 서비스 연동](#7-외부-서비스-연동)
8. [개발 로드맵](#8-개발-로드맵)

---

## 1. 프로젝트 개요

### 1.1 기술 스택

| 영역 | 기술 |
|------|------|
| **Frontend** | Kotlin, Jetpack Compose, MVVM, Hilt |
| **Backend** | Spring Boot 3.x, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL 15+, Redis (캐싱) |
| **Infrastructure** | Docker, AWS/GCP, Nginx |
| **Real-time** | WebSocket (STOMP), Firebase Cloud Messaging |

### 1.2 핵심 기능 모듈

```
┌─────────────────────────────────────────────────────────────┐
│                        이음 (IEUM)                          │
├─────────────┬─────────────┬─────────────┬─────────────────┤
│  추억 아카이브 │  일정/디데이  │  커플 프로필  │   데이트 추천   │
├─────────────┼─────────────┼─────────────┼─────────────────┤
│    소통      │   재정 관리   │  버킷리스트   │    위젯/알림    │
└─────────────┴─────────────┴─────────────┴─────────────────┘
```

---

## 2. 시스템 아키텍처

### 2.1 전체 아키텍처

```
┌──────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  Android App    │  │  Home Widget    │  │  Notification   │  │
│  │  (Kotlin/Compose)│  │  (Glance API)   │  │  (FCM)          │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
└───────────┼─────────────────────┼─────────────────────┼──────────┘
            │                     │                     │
            ▼                     ▼                     ▼
┌──────────────────────────────────────────────────────────────────┐
│                         API GATEWAY                               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Nginx / Spring Cloud Gateway           │   │
│  │              (Rate Limiting, Load Balancing)              │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────┬───────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                       BACKEND SERVICES                            │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐    │
│  │   Auth     │ │   Couple   │ │   Memory   │ │  Schedule  │    │
│  │  Service   │ │  Service   │ │  Service   │ │  Service   │    │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘    │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐    │
│  │   Chat     │ │  Finance   │ │   Place    │ │  Bucket    │    │
│  │  Service   │ │  Service   │ │  Service   │ │  Service   │    │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘    │
└──────────────────────────────┬───────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   PostgreSQL    │  │     Redis       │  │    AWS S3       │  │
│  │   (Primary DB)  │  │    (Cache)      │  │   (Media)       │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 모듈별 서비스 매핑

| 기능 모듈 | Backend Service | 주요 기능 |
|-----------|-----------------|-----------|
| 추억 아카이브 | Memory Service | 사진 업로드, 위치 기반 갤러리, 데이트 코스 기록 |
| 일정/디데이 | Schedule Service | 캘린더 연동, 기념일 관리, 가능 시간 매칭 |
| 커플 프로필 | Couple Service | 프로필 관리, MBTI/취향 분석, 위시리스트 |
| 데이트 추천 | Place Service | 장소 추천, 코스 생성, 비용 계산 |
| 소통 | Chat Service | 실시간 채팅, 공유 기능 |
| 재정 관리 | Finance Service | 소비 기록, 예산 관리 |
| 버킷리스트 | Bucket Service | 목표 관리, 진행률 추적 |

---

## 3. 프론트엔드 구조

### 3.1 프로젝트 디렉토리 구조

```
app/
├── src/main/java/com/ieum/
│   ├── IeumApplication.kt
│   │
│   ├── core/                          # 핵심 모듈
│   │   ├── common/
│   │   │   ├── base/
│   │   │   │   ├── BaseViewModel.kt
│   │   │   │   └── BaseUiState.kt
│   │   │   ├── extension/
│   │   │   │   ├── ContextExt.kt
│   │   │   │   ├── DateExt.kt
│   │   │   │   └── FlowExt.kt
│   │   │   └── util/
│   │   │       ├── DateUtils.kt
│   │   │       ├── LocationUtils.kt
│   │   │       └── ImageCompressor.kt
│   │   │
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── dao/
│   │   │   │   │   ├── MemoryDao.kt
│   │   │   │   │   ├── ScheduleDao.kt
│   │   │   │   │   └── ChatDao.kt
│   │   │   │   ├── entity/
│   │   │   │   │   ├── MemoryEntity.kt
│   │   │   │   │   ├── ScheduleEntity.kt
│   │   │   │   │   └── MessageEntity.kt
│   │   │   │   └── IeumDatabase.kt
│   │   │   │
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   ├── AuthApi.kt
│   │   │   │   │   ├── CoupleApi.kt
│   │   │   │   │   ├── MemoryApi.kt
│   │   │   │   │   ├── ScheduleApi.kt
│   │   │   │   │   ├── ChatApi.kt
│   │   │   │   │   ├── PlaceApi.kt
│   │   │   │   │   ├── FinanceApi.kt
│   │   │   │   │   └── BucketApi.kt
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   └── response/
│   │   │   │   └── interceptor/
│   │   │   │       ├── AuthInterceptor.kt
│   │   │   │       └── ErrorInterceptor.kt
│   │   │   │
│   │   │   └── datastore/
│   │   │       ├── UserPreferences.kt
│   │   │       └── AppSettings.kt
│   │   │
│   │   ├── di/
│   │   │   ├── AppModule.kt
│   │   │   ├── NetworkModule.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   └── RepositoryModule.kt
│   │   │
│   │   └── network/
│   │       ├── NetworkMonitor.kt
│   │       └── WebSocketManager.kt
│   │
│   ├── domain/                        # 도메인 레이어
│   │   ├── model/
│   │   │   ├── User.kt
│   │   │   ├── Couple.kt
│   │   │   ├── Memory.kt
│   │   │   ├── Schedule.kt
│   │   │   ├── DDay.kt
│   │   │   ├── Message.kt
│   │   │   ├── Place.kt
│   │   │   ├── DateCourse.kt
│   │   │   ├── Finance.kt
│   │   │   └── BucketItem.kt
│   │   │
│   │   ├── repository/
│   │   │   ├── AuthRepository.kt
│   │   │   ├── CoupleRepository.kt
│   │   │   ├── MemoryRepository.kt
│   │   │   ├── ScheduleRepository.kt
│   │   │   ├── ChatRepository.kt
│   │   │   ├── PlaceRepository.kt
│   │   │   ├── FinanceRepository.kt
│   │   │   └── BucketRepository.kt
│   │   │
│   │   └── usecase/
│   │       ├── auth/
│   │       │   ├── LoginUseCase.kt
│   │       │   ├── RegisterUseCase.kt
│   │       │   └── ConnectCoupleUseCase.kt
│   │       ├── memory/
│   │       │   ├── UploadMemoryUseCase.kt
│   │       │   ├── GetMemoriesByLocationUseCase.kt
│   │       │   └── SaveDateCourseUseCase.kt
│   │       ├── schedule/
│   │       │   ├── SyncCalendarUseCase.kt
│   │       │   ├── CalculateDDayUseCase.kt
│   │       │   └── FindAvailableTimeUseCase.kt
│   │       ├── couple/
│   │       │   ├── UpdateProfileUseCase.kt
│   │       │   ├── AnalyzeCompatibilityUseCase.kt
│   │       │   └── ManageWishlistUseCase.kt
│   │       ├── place/
│   │       │   ├── GetRecommendationsUseCase.kt
│   │       │   ├── CreateDateCourseUseCase.kt
│   │       │   └── ShareCourseUseCase.kt
│   │       ├── chat/
│   │       │   ├── SendMessageUseCase.kt
│   │       │   └── GetChatHistoryUseCase.kt
│   │       ├── finance/
│   │       │   ├── RecordExpenseUseCase.kt
│   │       │   └── GetMonthlyReportUseCase.kt
│   │       └── bucket/
│   │           ├── AddBucketItemUseCase.kt
│   │           └── UpdateProgressUseCase.kt
│   │
│   ├── presentation/                   # 프레젠테이션 레이어
│   │   ├── navigation/
│   │   │   ├── IeumNavHost.kt
│   │   │   ├── IeumNavigation.kt
│   │   │   └── Screen.kt
│   │   │
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   ├── Shape.kt
│   │   │   └── IeumTheme.kt
│   │   │
│   │   ├── components/
│   │   │   ├── IeumTopBar.kt
│   │   │   ├── IeumBottomBar.kt
│   │   │   ├── IeumButton.kt
│   │   │   ├── IeumCard.kt
│   │   │   ├── IeumTextField.kt
│   │   │   ├── LoadingIndicator.kt
│   │   │   ├── ErrorView.kt
│   │   │   └── EmptyView.kt
│   │   │
│   │   └── feature/
│   │       ├── auth/
│   │       │   ├── LoginScreen.kt
│   │       │   ├── LoginViewModel.kt
│   │       │   ├── RegisterScreen.kt
│   │       │   ├── RegisterViewModel.kt
│   │       │   ├── CoupleConnectScreen.kt
│   │       │   └── CoupleConnectViewModel.kt
│   │       │
│   │       ├── home/
│   │       │   ├── HomeScreen.kt
│   │       │   ├── HomeViewModel.kt
│   │       │   └── components/
│   │       │       ├── DDayCard.kt
│   │       │       ├── QuickActionCard.kt
│   │       │       └── RecentMemoryCard.kt
│   │       │
│   │       ├── memory/
│   │       │   ├── MemoryScreen.kt
│   │       │   ├── MemoryViewModel.kt
│   │       │   ├── MemoryDetailScreen.kt
│   │       │   ├── MemoryMapScreen.kt
│   │       │   ├── MemoryUploadScreen.kt
│   │       │   ├── DateCourseScreen.kt
│   │       │   └── components/
│   │       │       ├── MemoryGrid.kt
│   │       │       ├── MapGallery.kt
│   │       │       └── CourseTimeline.kt
│   │       │
│   │       ├── schedule/
│   │       │   ├── ScheduleScreen.kt
│   │       │   ├── ScheduleViewModel.kt
│   │       │   ├── DDayScreen.kt
│   │       │   ├── TimeMatchScreen.kt
│   │       │   └── components/
│   │       │       ├── CalendarView.kt
│   │       │       ├── DDayList.kt
│   │       │       └── TimeSlotPicker.kt
│   │       │
│   │       ├── profile/
│   │       │   ├── ProfileScreen.kt
│   │       │   ├── ProfileViewModel.kt
│   │       │   ├── EditProfileScreen.kt
│   │       │   ├── MBTIAnalysisScreen.kt
│   │       │   ├── WishlistScreen.kt
│   │       │   └── components/
│   │       │       ├── ProfileCard.kt
│   │       │       ├── CompatibilityChart.kt
│   │       │       └── WishlistItem.kt
│   │       │
│   │       ├── recommend/
│   │       │   ├── RecommendScreen.kt
│   │       │   ├── RecommendViewModel.kt
│   │       │   ├── PlaceDetailScreen.kt
│   │       │   ├── CourseBuilderScreen.kt
│   │       │   └── components/
│   │       │       ├── CategoryFilter.kt
│   │       │       ├── PlaceCard.kt
│   │       │       └── CoursePreview.kt
│   │       │
│   │       ├── chat/
│   │       │   ├── ChatScreen.kt
│   │       │   ├── ChatViewModel.kt
│   │       │   └── components/
│   │       │       ├── MessageBubble.kt
│   │       │       ├── ChatInput.kt
│   │       │       └── SharedContentCard.kt
│   │       │
│   │       ├── finance/
│   │       │   ├── FinanceScreen.kt
│   │       │   ├── FinanceViewModel.kt
│   │       │   ├── AddExpenseScreen.kt
│   │       │   ├── BudgetSettingScreen.kt
│   │       │   └── components/
│   │       │       ├── ExpenseList.kt
│   │       │       ├── MonthlyChart.kt
│   │       │       └── BudgetProgress.kt
│   │       │
│   │       ├── bucket/
│   │       │   ├── BucketScreen.kt
│   │       │   ├── BucketViewModel.kt
│   │       │   ├── AddBucketScreen.kt
│   │       │   └── components/
│   │       │       ├── BucketCard.kt
│   │       │       └── ProgressBar.kt
│   │       │
│   │       └── settings/
│   │           ├── SettingsScreen.kt
│   │           ├── SettingsViewModel.kt
│   │           └── NotificationSettingsScreen.kt
│   │
│   ├── widget/                         # 홈 화면 위젯
│   │   ├── MemoryWidget.kt
│   │   ├── MemoryWidgetReceiver.kt
│   │   ├── DDayWidget.kt
│   │   └── DDayWidgetReceiver.kt
│   │
│   └── service/                        # 백그라운드 서비스
│       ├── NotificationService.kt
│       ├── SyncService.kt
│       └── WidgetUpdateService.kt
│
├── src/main/res/
│   ├── drawable/
│   ├── layout/
│   ├── values/
│   ├── xml/
│   │   ├── memory_widget_info.xml
│   │   └── dday_widget_info.xml
│   └── raw/
│
└── build.gradle.kts
```

### 3.2 핵심 클래스 구현

#### 3.2.1 BaseViewModel.kt
```kotlin
package com.ieum.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : BaseUiState, Event, Effect> : ViewModel() {
    
    private val initialState: State by lazy { createInitialState() }
    
    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()
    
    private val _effect: Channel<Effect> = Channel()
    val effect: Flow<Effect> = _effect.receiveAsFlow()
    
    protected val currentState: State get() = _uiState.value
    
    abstract fun createInitialState(): State
    abstract fun handleEvent(event: Event)
    
    fun sendEvent(event: Event) {
        handleEvent(event)
    }
    
    protected fun setState(reduce: State.() -> State) {
        _uiState.update { it.reduce() }
    }
    
    protected fun setEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

interface BaseUiState
```

#### 3.2.2 NetworkModule.kt (Hilt DI)
```kotlin
package com.ieum.core.di

import com.ieum.core.data.remote.interceptor.AuthInterceptor
import com.ieum.core.data.remote.interceptor.ErrorInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    
    private const val BASE_URL = "https://api.ieum.app/"
    private const val TIMEOUT = 30L
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
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
}
```

#### 3.2.3 WebSocketManager.kt
```kotlin
package com.ieum.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    
    private val _messages = MutableSharedFlow<ChatMessage>()
    val messages: SharedFlow<ChatMessage> = _messages
    
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState: SharedFlow<ConnectionState> = _connectionState
    
    fun connect(coupleId: String, token: String) {
        val request = Request.Builder()
            .url("wss://api.ieum.app/ws/chat/$coupleId")
            .addHeader("Authorization", "Bearer $token")
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.tryEmit(ConnectionState.Connected)
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = parseMessage(text)
                _messages.tryEmit(message)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.tryEmit(ConnectionState.Disconnected)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.tryEmit(ConnectionState.Error(t.message ?: "Unknown error"))
            }
        })
    }
    
    fun sendMessage(message: String) {
        webSocket?.send(message)
    }
    
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
    
    private fun parseMessage(text: String): ChatMessage {
        // JSON 파싱 로직
        return ChatMessage(/* ... */)
    }
    
    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}
```

#### 3.2.4 홈 위젯 구현 (Glance API)
```kotlin
package com.ieum.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class MemoryWidget : GlanceAppWidget() {
    
    override val sizeMode = SizeMode.Exact
    
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = currentState<MemoryWidgetState>()
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White))
                .cornerRadius(16.dp)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 추억 사진
            prefs.imageUri?.let { uri ->
                Image(
                    provider = ImageProvider(uri),
                    contentDescription = "추억 사진",
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .cornerRadius(12.dp)
                )
            }
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // 한 줄 코멘트
            Text(
                text = prefs.comment ?: "함께한 추억",
                style = TextStyle(
                    color = ColorProvider(Color.DarkGray),
                    fontSize = 14.sp
                )
            )
            
            // 날짜
            Text(
                text = prefs.date ?: "",
                style = TextStyle(
                    color = ColorProvider(Color.Gray),
                    fontSize = 12.sp
                )
            )
        }
    }
}

class MemoryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MemoryWidget()
}
```

### 3.3 의존성 (build.gradle.kts)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.ieum"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.ieum"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Glance (Widget)
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material3:1.0.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Calendar
    implementation("com.kizitonwose.calendar:compose:2.5.0")
    
    // Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
```

---

## 4. 백엔드 구조

### 4.1 프로젝트 디렉토리 구조

```
ieum-backend/
├── src/main/java/com/ieum/
│   ├── IeumApplication.java
│   │
│   ├── global/                        # 전역 설정
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   ├── S3Config.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── JpaConfig.java
│   │   │
│   │   ├── security/
│   │   │   ├── jwt/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtProperties.java
│   │   │   ├── oauth/
│   │   │   │   ├── OAuth2SuccessHandler.java
│   │   │   │   ├── OAuth2UserService.java
│   │   │   │   └── OAuth2UserInfo.java
│   │   │   └── UserPrincipal.java
│   │   │
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BusinessException.java
│   │   │   ├── ErrorCode.java
│   │   │   └── ErrorResponse.java
│   │   │
│   │   └── common/
│   │       ├── BaseEntity.java
│   │       ├── BaseTimeEntity.java
│   │       └── ApiResponse.java
│   │
│   ├── domain/                        # 도메인 모듈
│   │   │
│   │   ├── auth/                      # 인증
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   └── AuthServiceImpl.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── TokenResponse.java
│   │   │   │   └── RefreshTokenRequest.java
│   │   │   └── repository/
│   │   │       └── RefreshTokenRepository.java
│   │   │
│   │   ├── user/                      # 사용자
│   │   │   ├── entity/
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   └── UserServiceImpl.java
│   │   │   └── dto/
│   │   │       ├── UserResponse.java
│   │   │       └── UserUpdateRequest.java
│   │   │
│   │   ├── couple/                    # 커플
│   │   │   ├── controller/
│   │   │   │   └── CoupleController.java
│   │   │   ├── entity/
│   │   │   │   ├── Couple.java
│   │   │   │   ├── CoupleProfile.java
│   │   │   │   ├── Preference.java
│   │   │   │   └── Wishlist.java
│   │   │   ├── repository/
│   │   │   │   ├── CoupleRepository.java
│   │   │   │   ├── CoupleProfileRepository.java
│   │   │   │   └── WishlistRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CoupleService.java
│   │   │   │   └── CoupleServiceImpl.java
│   │   │   └── dto/
│   │   │       ├── CoupleConnectRequest.java
│   │   │       ├── CoupleResponse.java
│   │   │       ├── ProfileUpdateRequest.java
│   │   │       ├── PreferenceRequest.java
│   │   │       └── CompatibilityResponse.java
│   │   │
│   │   ├── memory/                    # 추억 아카이브
│   │   │   ├── controller/
│   │   │   │   ├── MemoryController.java
│   │   │   │   └── DateCourseController.java
│   │   │   ├── entity/
│   │   │   │   ├── Memory.java
│   │   │   │   ├── MemoryPhoto.java
│   │   │   │   ├── DateCourse.java
│   │   │   │   └── CoursePlace.java
│   │   │   ├── repository/
│   │   │   │   ├── MemoryRepository.java
│   │   │   │   ├── MemoryPhotoRepository.java
│   │   │   │   └── DateCourseRepository.java
│   │   │   ├── service/
│   │   │   │   ├── MemoryService.java
│   │   │   │   ├── MemoryServiceImpl.java
│   │   │   │   └── ImageUploadService.java
│   │   │   └── dto/
│   │   │       ├── MemoryCreateRequest.java
│   │   │       ├── MemoryResponse.java
│   │   │       ├── MemoryMapResponse.java
│   │   │       ├── DateCourseRequest.java
│   │   │       └── DateCourseResponse.java
│   │   │
│   │   ├── schedule/                  # 일정 & 디데이
│   │   │   ├── controller/
│   │   │   │   ├── ScheduleController.java
│   │   │   │   └── DDayController.java
│   │   │   ├── entity/
│   │   │   │   ├── Schedule.java
│   │   │   │   ├── DDay.java
│   │   │   │   └── AvailableTime.java
│   │   │   ├── repository/
│   │   │   │   ├── ScheduleRepository.java
│   │   │   │   ├── DDayRepository.java
│   │   │   │   └── AvailableTimeRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ScheduleService.java
│   │   │   │   ├── ScheduleServiceImpl.java
│   │   │   │   ├── DDayService.java
│   │   │   │   └── TimeMatchingService.java
│   │   │   └── dto/
│   │   │       ├── ScheduleRequest.java
│   │   │       ├── ScheduleResponse.java
│   │   │       ├── DDayRequest.java
│   │   │       ├── DDayResponse.java
│   │   │       └── AvailableTimeResponse.java
│   │   │
│   │   ├── place/                     # 장소 & 추천
│   │   │   ├── controller/
│   │   │   │   └── PlaceController.java
│   │   │   ├── entity/
│   │   │   │   ├── Place.java
│   │   │   │   ├── PlaceCategory.java
│   │   │   │   └── PlaceReview.java
│   │   │   ├── repository/
│   │   │   │   ├── PlaceRepository.java
│   │   │   │   └── PlaceReviewRepository.java
│   │   │   ├── service/
│   │   │   │   ├── PlaceService.java
│   │   │   │   ├── PlaceServiceImpl.java
│   │   │   │   └── RecommendationEngine.java
│   │   │   └── dto/
│   │   │       ├── PlaceResponse.java
│   │   │       ├── PlaceSearchRequest.java
│   │   │       ├── RecommendationRequest.java
│   │   │       └── CourseRecommendResponse.java
│   │   │
│   │   ├── chat/                      # 채팅
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java
│   │   │   │   └── ChatWebSocketHandler.java
│   │   │   ├── entity/
│   │   │   │   ├── ChatRoom.java
│   │   │   │   ├── Message.java
│   │   │   │   └── SharedContent.java
│   │   │   ├── repository/
│   │   │   │   ├── ChatRoomRepository.java
│   │   │   │   └── MessageRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java
│   │   │   │   └── ChatServiceImpl.java
│   │   │   └── dto/
│   │   │       ├── MessageRequest.java
│   │   │       ├── MessageResponse.java
│   │   │       └── ChatHistoryResponse.java
│   │   │
│   │   ├── finance/                   # 재정 관리
│   │   │   ├── controller/
│   │   │   │   └── FinanceController.java
│   │   │   ├── entity/
│   │   │   │   ├── Expense.java
│   │   │   │   ├── Budget.java
│   │   │   │   └── ExpenseCategory.java
│   │   │   ├── repository/
│   │   │   │   ├── ExpenseRepository.java
│   │   │   │   └── BudgetRepository.java
│   │   │   ├── service/
│   │   │   │   ├── FinanceService.java
│   │   │   │   └── FinanceServiceImpl.java
│   │   │   └── dto/
│   │   │       ├── ExpenseRequest.java
│   │   │       ├── ExpenseResponse.java
│   │   │       ├── BudgetRequest.java
│   │   │       └── MonthlyReportResponse.java
│   │   │
│   │   ├── bucket/                    # 버킷리스트
│   │   │   ├── controller/
│   │   │   │   └── BucketController.java
│   │   │   ├── entity/
│   │   │   │   └── BucketItem.java
│   │   │   ├── repository/
│   │   │   │   └── BucketRepository.java
│   │   │   ├── service/
│   │   │   │   ├── BucketService.java
│   │   │   │   └── BucketServiceImpl.java
│   │   │   └── dto/
│   │   │       ├── BucketItemRequest.java
│   │   │       └── BucketItemResponse.java
│   │   │
│   │   └── notification/              # 알림
│   │       ├── service/
│   │       │   ├── NotificationService.java
│   │       │   └── FCMService.java
│   │       └── dto/
│   │           └── NotificationRequest.java
│   │
│   └── infra/                         # 인프라
│       ├── s3/
│       │   └── S3Uploader.java
│       ├── redis/
│       │   └── RedisService.java
│       └── external/
│           ├── kakao/
│           │   ├── KakaoMapClient.java
│           │   └── KakaoPlaceResponse.java
│           └── google/
│               ├── GoogleCalendarClient.java
│               └── CalendarEventResponse.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── messages.properties
│
├── src/test/
│   └── java/com/ieum/
│       ├── domain/
│       │   ├── auth/
│       │   ├── couple/
│       │   └── ...
│       └── integration/
│
├── build.gradle
├── settings.gradle
├── Dockerfile
└── docker-compose.yml
```

### 4.2 핵심 클래스 구현

#### 4.2.1 SecurityConfig.java
```java
package com.ieum.global.config;

import com.ieum.global.security.jwt.JwtAuthenticationFilter;
import com.ieum.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### 4.2.2 WebSocketConfig.java
```java
package com.ieum.global.config;

import com.ieum.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = 
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        Authentication auth = jwtTokenProvider.getAuthentication(token);
                        accessor.setUser(auth);
                    }
                }
                return message;
            }
        });
    }
}
```

#### 4.2.3 BaseEntity.java
```java
package com.ieum.global.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    public void softDelete() {
        this.deleted = true;
    }
}
```

#### 4.2.4 Couple Entity
```java
package com.ieum.domain.couple.entity;

import com.ieum.domain.user.entity.User;
import com.ieum.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "couples")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Couple extends BaseEntity {
    
    @Column(unique = true, nullable = false)
    private String coupleCode;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    private User user1;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Enumerated(EnumType.STRING)
    private CoupleStatus status;
    
    @OneToMany(mappedBy = "couple", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CoupleProfile> profiles = new ArrayList<>();
    
    public void connect(User user) {
        if (this.user2 != null) {
            throw new IllegalStateException("이미 연결된 커플입니다.");
        }
        this.user2 = user;
        this.status = CoupleStatus.CONNECTED;
    }
    
    public boolean isMember(Long userId) {
        return (user1 != null && user1.getId().equals(userId)) ||
               (user2 != null && user2.getId().equals(userId));
    }
    
    public User getPartner(Long userId) {
        if (user1.getId().equals(userId)) return user2;
        if (user2.getId().equals(userId)) return user1;
        throw new IllegalArgumentException("커플 멤버가 아닙니다.");
    }
    
    public enum CoupleStatus {
        PENDING, CONNECTED, DISCONNECTED
    }
}
```

#### 4.2.5 Memory Entity
```java
package com.ieum.domain.memory.entity;

import com.ieum.domain.couple.entity.Couple;
import com.ieum.domain.user.entity.User;
import com.ieum.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "memories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Memory extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(length = 200)
    private String comment;
    
    private LocalDateTime memoryDate;
    
    // 위치 정보
    private Double latitude;
    private Double longitude;
    private String placeName;
    private String address;
    
    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemoryPhoto> photos = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_course_id")
    private DateCourse dateCourse;
    
    public void addPhoto(MemoryPhoto photo) {
        photos.add(photo);
        photo.setMemory(this);
    }
    
    public void updateComment(String comment) {
        this.comment = comment;
    }
}
```

#### 4.2.6 ChatController & WebSocket Handler
```java
package com.ieum.domain.chat.controller;

import com.ieum.domain.chat.dto.MessageRequest;
import com.ieum.domain.chat.dto.MessageResponse;
import com.ieum.domain.chat.service.ChatService;
import com.ieum.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // WebSocket 메시지 처리
    @MessageMapping("/chat/{coupleId}")
    public void sendMessage(
            @DestinationVariable Long coupleId,
            @Payload MessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MessageResponse response = chatService.saveAndSendMessage(
            coupleId, principal.getId(), request
        );
        
        // 커플 채팅방으로 메시지 전송
        messagingTemplate.convertAndSend(
            "/topic/chat/" + coupleId,
            response
        );
    }
    
    // REST API - 채팅 히스토리 조회
    @GetMapping("/api/v1/chat/{coupleId}/history")
    @ResponseBody
    public List<MessageResponse> getChatHistory(
            @PathVariable Long coupleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return chatService.getChatHistory(coupleId, principal.getId(), page, size);
    }
}
```

#### 4.2.7 RecommendationEngine (장소 추천)
```java
package com.ieum.domain.place.service;

import com.ieum.domain.couple.entity.Couple;
import com.ieum.domain.couple.entity.Preference;
import com.ieum.domain.place.dto.CourseRecommendResponse;
import com.ieum.domain.place.dto.RecommendationRequest;
import com.ieum.domain.place.entity.Place;
import com.ieum.domain.place.entity.PlaceCategory;
import com.ieum.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationEngine {
    
    private final PlaceRepository placeRepository;
    
    public CourseRecommendResponse recommendCourse(
            Couple couple, 
            RecommendationRequest request
    ) {
        // 1. 커플 취향 분석
        Set<PlaceCategory> preferredCategories = analyzePreferences(couple);
        
        // 2. 위치 기반 장소 검색
        List<Place> nearbyPlaces = placeRepository.findNearbyPlaces(
            request.getLatitude(),
            request.getLongitude(),
            request.getRadiusKm()
        );
        
        // 3. 필터링 (카테고리, 예산, 분위기)
        List<Place> filteredPlaces = filterPlaces(
            nearbyPlaces, 
            request, 
            preferredCategories
        );
        
        // 4. 코스 생성 (식사 → 카페 → 활동 순서)
        List<Place> courseOrder = buildCourseOrder(filteredPlaces, request);
        
        // 5. 예상 비용 계산
        int estimatedCost = calculateEstimatedCost(courseOrder);
        
        return CourseRecommendResponse.builder()
            .places(courseOrder.stream()
                .map(this::toPlaceResponse)
                .collect(Collectors.toList()))
            .estimatedCost(estimatedCost)
            .estimatedDuration(calculateDuration(courseOrder))
            .category(request.getCategory())
            .build();
    }
    
    private Set<PlaceCategory> analyzePreferences(Couple couple) {
        Set<PlaceCategory> categories = new HashSet<>();
        
        for (Preference pref : couple.getProfiles().stream()
                .flatMap(p -> p.getPreferences().stream())
                .toList()) {
            switch (pref.getType()) {
                case FOOD -> categories.add(PlaceCategory.RESTAURANT);
                case ALCOHOL -> categories.add(PlaceCategory.BAR);
                case ACTIVITY -> categories.add(PlaceCategory.ACTIVITY);
                case CULTURE -> categories.add(PlaceCategory.CULTURE);
                case CAFE -> categories.add(PlaceCategory.CAFE);
            }
        }
        
        return categories;
    }
    
    private List<Place> buildCourseOrder(
            List<Place> places, 
            RecommendationRequest request
    ) {
        List<Place> course = new ArrayList<>();
        
        // 시간대별 추천 로직
        if (request.isIncludeMeal()) {
            places.stream()
                .filter(p -> p.getCategory() == PlaceCategory.RESTAURANT)
                .max(Comparator.comparing(Place::getRating))
                .ifPresent(course::add);
        }
        
        // 카페 추가
        places.stream()
            .filter(p -> p.getCategory() == PlaceCategory.CAFE)
            .filter(p -> p.getMood().contains(request.getMood()))
            .findFirst()
            .ifPresent(course::add);
        
        // 활동 추가
        places.stream()
            .filter(p -> p.getCategory() == request.getMainCategory())
            .limit(2)
            .forEach(course::add);
        
        return course;
    }
    
    private int calculateEstimatedCost(List<Place> places) {
        return places.stream()
            .mapToInt(Place::getAverageCost)
            .sum();
    }
}
```

### 4.3 의존성 (build.gradle)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.2'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.ieum'
version = '1.0.0'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    
    // Database
    runtimeOnly 'org.postgresql:postgresql'
    implementation 'com.h2database:h2' // 테스트용
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // OAuth2
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    
    // AWS S3
    implementation 'software.amazon.awssdk:s3:2.23.0'
    
    // Firebase (FCM)
    implementation 'com.google.firebase:firebase-admin:9.2.0'
    
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
    
    // Swagger
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // MapStruct
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

test {
    useJUnitPlatform()
}
```

---

## 5. 데이터베이스 설계

### 5.1 ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              IEUM DATABASE SCHEMA                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────────┐
│   users     │       │   couples   │       │ couple_profiles │
├─────────────┤       ├─────────────┤       ├─────────────────┤
│ id (PK)     │◄──┐   │ id (PK)     │◄──────│ id (PK)         │
│ email       │   │   │ couple_code │       │ couple_id (FK)  │
│ password    │   ├──►│ user1_id(FK)│       │ user_id (FK)    │
│ nickname    │   │   │ user2_id(FK)│       │ nickname_by_ptn │
│ phone       │   │   │ start_date  │       │ mbti            │
│ birth_date  │   │   │ status      │       │ bio             │
│ profile_img │   │   │ created_at  │       │ preferences     │
│ fcm_token   │   │   └─────────────┘       └─────────────────┘
│ created_at  │   │          │
└─────────────┘   │          │
                  │          ▼
                  │   ┌─────────────┐       ┌─────────────────┐
                  │   │  memories   │       │  memory_photos  │
                  │   ├─────────────┤       ├─────────────────┤
                  │   │ id (PK)     │◄──────│ id (PK)         │
                  │   │ couple_id   │       │ memory_id (FK)  │
                  │   │ created_by  │       │ image_url       │
                  │   │ comment     │       │ order_index     │
                  │   │ memory_date │       │ created_at      │
                  │   │ latitude    │       └─────────────────┘
                  │   │ longitude   │
                  │   │ place_name  │
                  │   │ created_at  │
                  │   └─────────────┘
                  │
                  │   ┌─────────────┐       ┌─────────────────┐
                  │   │ date_courses│       │  course_places  │
                  │   ├─────────────┤       ├─────────────────┤
                  │   │ id (PK)     │◄──────│ id (PK)         │
                  │   │ couple_id   │       │ course_id (FK)  │
                  │   │ title       │       │ place_id (FK)   │
                  │   │ course_date │       │ visit_order     │
                  │   │ total_cost  │       │ actual_cost     │
                  │   │ rating      │       │ memo            │
                  │   │ created_at  │       └─────────────────┘
                  │   └─────────────┘
                  │
                  │   ┌─────────────┐       ┌─────────────────┐
                  │   │  schedules  │       │     d_days      │
                  │   ├─────────────┤       ├─────────────────┤
                  │   │ id (PK)     │       │ id (PK)         │
                  │   │ couple_id   │       │ couple_id (FK)  │
                  │   │ user_id     │       │ title           │
                  │   │ title       │       │ target_date     │
                  │   │ start_time  │       │ emoji           │
                  │   │ end_time    │       │ theme           │
                  │   │ is_shared   │       │ repeat_yearly   │
                  │   │ external_id │       │ created_at      │
                  │   │ created_at  │       └─────────────────┘
                  │   └─────────────┘
                  │
                  │   ┌─────────────┐       ┌─────────────────┐
                  │   │   places    │       │ place_reviews   │
                  │   ├─────────────┤       ├─────────────────┤
                  │   │ id (PK)     │◄──────│ id (PK)         │
                  │   │ name        │       │ place_id (FK)   │
                  │   │ category    │       │ couple_id (FK)  │
                  │   │ address     │       │ rating          │
                  │   │ latitude    │       │ content         │
                  │   │ longitude   │       │ created_at      │
                  │   │ avg_cost    │       └─────────────────┘
                  │   │ rating      │
                  │   │ mood_tags   │
                  │   │ kakao_id    │
                  │   └─────────────┘
                  │
                  │   ┌─────────────┐       ┌─────────────────┐
                  │   │  messages   │       │ shared_contents │
                  │   ├─────────────┤       ├─────────────────┤
                  │   │ id (PK)     │◄──────│ id (PK)         │
                  │   │ couple_id   │       │ message_id (FK) │
                  │   │ sender_id   │       │ content_type    │
                  │   │ content     │       │ content_id      │
                  │   │ type        │       │ preview_data    │
                  │   │ read_at     │       └─────────────────┘
                  │   │ created_at  │
                  │   └─────────────┘
                  │
                  │   ┌─────────────┐       ┌─────────────────┐
                  │   │  expenses   │       │    budgets      │
                  │   ├─────────────┤       ├─────────────────┤
                  │   │ id (PK)     │       │ id (PK)         │
                  │   │ couple_id   │       │ couple_id (FK)  │
                  │   │ paid_by     │       │ year_month      │
                  │   │ amount      │       │ limit_amount    │
                  │   │ category    │       │ created_at      │
                  │   │ description │       └─────────────────┘
                  │   │ expense_date│
                  │   │ created_at  │
                  │   └─────────────┘
                  │
                  │   ┌─────────────┐
                  └───│bucket_items │
                      ├─────────────┤
                      │ id (PK)     │
                      │ couple_id   │
                      │ title       │
                      │ description │
                      │ target_date │
                      │ status      │
                      │ progress    │
                      │ created_at  │
                      └─────────────┘
```

### 5.2 주요 테이블 DDL

```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255),
    nickname VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    birth_date DATE,
    profile_image_url VARCHAR(500),
    fcm_token VARCHAR(255),
    provider VARCHAR(20) DEFAULT 'LOCAL',
    provider_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 커플 테이블
CREATE TABLE couples (
    id BIGSERIAL PRIMARY KEY,
    couple_code VARCHAR(10) UNIQUE NOT NULL,
    user1_id BIGINT REFERENCES users(id),
    user2_id BIGINT REFERENCES users(id),
    start_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 추억 테이블
CREATE TABLE memories (
    id BIGSERIAL PRIMARY KEY,
    couple_id BIGINT NOT NULL REFERENCES couples(id),
    created_by BIGINT REFERENCES users(id),
    comment VARCHAR(200),
    memory_date TIMESTAMP,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    place_name VARCHAR(100),
    address VARCHAR(255),
    date_course_id BIGINT REFERENCES date_courses(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 위치 기반 검색을 위한 인덱스
CREATE INDEX idx_memories_location ON memories 
    USING GIST (ll_to_earth(latitude, longitude));

CREATE INDEX idx_memories_couple_date ON memories (couple_id, memory_date DESC);

-- 장소 테이블
CREATE TABLE places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    average_cost INTEGER DEFAULT 0,
    rating DECIMAL(2, 1) DEFAULT 0.0,
    mood_tags VARCHAR(255)[],
    kakao_place_id VARCHAR(50),
    google_place_id VARCHAR(100),
    phone VARCHAR(20),
    operating_hours JSONB,
    image_urls VARCHAR(500)[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_places_location ON places 
    USING GIST (ll_to_earth(latitude, longitude));

CREATE INDEX idx_places_category ON places (category);

-- 메시지 테이블
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    couple_id BIGINT NOT NULL REFERENCES couples(id),
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_couple_created ON messages (couple_id, created_at DESC);
```

---

## 6. API 명세

### 6.1 API 엔드포인트 목록

| 모듈 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| **Auth** | POST | `/api/v1/auth/register` | 회원가입 |
| | POST | `/api/v1/auth/login` | 로그인 |
| | POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| | POST | `/api/v1/auth/oauth/{provider}` | 소셜 로그인 |
| **Couple** | POST | `/api/v1/couples` | 커플 코드 생성 |
| | POST | `/api/v1/couples/connect` | 커플 연결 |
| | GET | `/api/v1/couples/me` | 내 커플 정보 |
| | PUT | `/api/v1/couples/profile` | 프로필 수정 (상대방이 수정) |
| | GET | `/api/v1/couples/compatibility` | 궁합 분석 |
| | POST | `/api/v1/couples/wishlist` | 위시리스트 추가 |
| **Memory** | POST | `/api/v1/memories` | 추억 생성 |
| | GET | `/api/v1/memories` | 추억 목록 (페이징) |
| | GET | `/api/v1/memories/map` | 지도용 추억 목록 |
| | GET | `/api/v1/memories/{id}` | 추억 상세 |
| | DELETE | `/api/v1/memories/{id}` | 추억 삭제 |
| | POST | `/api/v1/memories/courses` | 데이트 코스 저장 |
| | GET | `/api/v1/memories/courses` | 데이트 코스 목록 |
| **Schedule** | GET | `/api/v1/schedules` | 일정 목록 |
| | POST | `/api/v1/schedules` | 일정 생성 |
| | POST | `/api/v1/schedules/sync` | 캘린더 동기화 |
| | GET | `/api/v1/schedules/available` | 가능 시간 매칭 |
| | GET | `/api/v1/d-days` | 디데이 목록 |
| | POST | `/api/v1/d-days` | 디데이 생성 |
| **Place** | GET | `/api/v1/places/search` | 장소 검색 |
| | GET | `/api/v1/places/recommend` | 장소 추천 |
| | GET | `/api/v1/places/course/recommend` | 코스 추천 |
| | POST | `/api/v1/places/course/share` | 코스 공유 |
| **Chat** | GET | `/api/v1/chat/{coupleId}/history` | 채팅 기록 |
| | WS | `/ws/chat/{coupleId}` | 실시간 채팅 |
| **Finance** | GET | `/api/v1/finance/expenses` | 지출 목록 |
| | POST | `/api/v1/finance/expenses` | 지출 기록 |
| | GET | `/api/v1/finance/report` | 월간 리포트 |
| | PUT | `/api/v1/finance/budget` | 예산 설정 |
| **Bucket** | GET | `/api/v1/bucket-list` | 버킷리스트 |
| | POST | `/api/v1/bucket-list` | 버킷 아이템 추가 |
| | PUT | `/api/v1/bucket-list/{id}` | 진행률 업데이트 |

### 6.2 API 응답 형식

```json
// 성공 응답
{
  "success": true,
  "data": { ... },
  "message": null
}

// 에러 응답
{
  "success": false,
  "data": null,
  "error": {
    "code": "COUPLE_NOT_FOUND",
    "message": "커플 정보를 찾을 수 없습니다."
  }
}

// 페이징 응답
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true
  }
}
```

---

## 7. 외부 서비스 연동

### 7.1 연동 서비스 목록

| 서비스 | 용도 | 연동 방식 |
|--------|------|-----------|
| **카카오 지도 API** | 장소 검색, 지도 표시 | REST API |
| **카카오 로그인** | 소셜 로그인 | OAuth 2.0 |
| **Google Calendar API** | 캘린더 동기화 | OAuth 2.0 + REST |
| **Google Maps API** | 장소 데이터 보완 | REST API |
| **Firebase Cloud Messaging** | 푸시 알림 | SDK |
| **AWS S3** | 이미지 저장 | SDK |
| **카카오톡 공유** | 코스 공유 | SDK |

### 7.2 연동 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      Android App                             │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Kakao SDK   │  │ Google SDK  │  │ Firebase SDK│         │
│  │ - Login     │  │ - Calendar  │  │ - FCM       │         │
│  │ - Share     │  │ - Sign In   │  │ - Analytics │         │
│  │ - Map       │  │             │  │             │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Backend Server                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ External Client │  │ External Client │                  │
│  │ - KakaoMap      │  │ - GoogleCalendar│                  │
│  │ - KakaoAuth     │  │ - GooglePlaces  │                  │
│  └────────┬────────┘  └────────┬────────┘                  │
│           │                    │                            │
│           ▼                    ▼                            │
│  ┌─────────────────────────────────────────┐               │
│  │           Infra Layer                    │               │
│  │  - S3Uploader (AWS SDK)                  │               │
│  │  - FCMService (Firebase Admin SDK)       │               │
│  │  - RedisService (캐싱)                    │               │
│  └─────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

---

## 8. 개발 로드맵

### 8.1 Phase 1: 핵심 기능 (8주)

```
Week 1-2: 프로젝트 설정 & 인증
├── 프로젝트 구조 설정
├── 회원가입/로그인 (이메일, 소셜)
├── JWT 인증 시스템
└── 커플 연결 기능

Week 3-4: 추억 아카이브
├── 사진 업로드 (S3 연동)
├── 위치 기반 갤러리
├── 한 줄 코멘트
└── 데이트 코스 기록

Week 5-6: 일정 & 디데이
├── 커플 캘린더
├── 디데이 관리
├── 스마트폰 캘린더 연동
└── 가능 시간 매칭 (기본)

Week 7-8: 채팅 & 알림
├── 실시간 채팅 (WebSocket)
├── 공유 기능 (일정, 장소 등)
├── FCM 푸시 알림
└── Phase 1 테스트 & 버그 수정
```

### 8.2 Phase 2: 확장 기능 (6주)

```
Week 9-10: 커플 프로필 & 취향
├── 상대방 프로필 꾸미기
├── MBTI 입력 및 궁합 분석
├── 취향 설문 시스템
└── 위시리스트

Week 11-12: 데이트 추천
├── 카카오 지도 API 연동
├── 장소 검색 및 필터링
├── 코스 추천 알고리즘
└── 코스 공유 (카카오톡)

Week 13-14: 재정 & 버킷리스트
├── 지출 기록 시스템
├── 월간 리포트
├── 예산 설정 및 알림
└── 버킷리스트 관리
```

### 8.3 Phase 3: 고도화 (4주)

```
Week 15-16: 위젯 & UX 개선
├── 홈 화면 위젯 (추억, 디데이)
├── UI/UX 개선
├── 성능 최적화
└── 다크 모드

Week 17-18: 출시 준비
├── 통합 테스트
├── 보안 점검
├── Play Store 준비
└── 런칭
```

### 8.4 기술 부채 관리

- [ ] 테스트 커버리지 70% 이상 유지
- [ ] API 문서화 (Swagger)
- [ ] 코드 리뷰 프로세스
- [ ] CI/CD 파이프라인 구축
- [ ] 모니터링 시스템 구축 (Prometheus + Grafana)

---

## 부록: 참고 자료

### A. 개발 환경 설정

```bash
# Backend 실행
cd ieum-backend
./gradlew bootRun

# Database (Docker)
docker-compose up -d

# Android 빌드
./gradlew assembleDebug
```

### B. 환경 변수 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ieum
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  redis:
    host: localhost
    port: 6379

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000
  refresh-expiration: 604800000

aws:
  s3:
    bucket: ieum-media
    region: ap-northeast-2
  
kakao:
  client-id: ${KAKAO_CLIENT_ID}
  client-secret: ${KAKAO_CLIENT_SECRET}
  
google:
  client-id: ${GOOGLE_CLIENT_ID}
  client-secret: ${GOOGLE_CLIENT_SECRET}
```

---

**Document Version:** 1.0.0  
**Last Updated:** 2025-01-16  
**Author:** Claude AI Assistant
