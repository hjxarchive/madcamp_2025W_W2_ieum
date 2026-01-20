package com.ieum.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ieum.presentation.feature.login.LoginScreen
import com.ieum.presentation.theme.IeumColors

/**
 * ✅ 앱의 루트 네비게이션
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // 신규 로그인 성공 시 MBTI 테스트로 이동
                    navController.navigate(Routes.MBTI_TEST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onAlreadyLoggedIn = {
                    // 이미 로그인된 사용자는 메인 화면으로 바로 이동
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // MBTI 테스트 화면
        composable(Routes.MBTI_TEST) {
            com.ieum.presentation.feature.test.TestMainScreen(
                onTestFinished = {
                    // 테스트 종료 시 온보딩 화면으로 이동
                    navController.navigate(Routes.ONBOARDING_NICKNAME) {
                        popUpTo(Routes.MBTI_TEST) { inclusive = true }
                    }
                }
            )
        }
        
        // 온보딩 기능 (중첩 그래프)
        navigation(
            startDestination = Routes.ONBOARDING_NICKNAME,
            route = Routes.ONBOARDING
        ) {
            // 온보딩 1단계: 닉네임 입력
            composable(Routes.ONBOARDING_NICKNAME) { backStackEntry ->
                // 온보딩 플로우 전체에서 공유할 ViewModel
                val onboardingBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.ONBOARDING)
                }
                val onboardingViewModel: com.ieum.presentation.feature.onboarding.OnboardingViewModel = 
                    androidx.hilt.navigation.compose.hiltViewModel(onboardingBackStackEntry)
                
                com.ieum.presentation.feature.onboarding.NicknameInputScreen(
                    viewModel = onboardingViewModel,
                    onNext = {
                        navController.navigate(Routes.ONBOARDING_BIRTHDAY)
                    }
                )
            }
            
            // 온보딩 2단계: 생일 입력
            composable(Routes.ONBOARDING_BIRTHDAY) { backStackEntry ->
                // 온보딩 플로우 전체에서 공유할 ViewModel
                val onboardingBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.ONBOARDING)
                }
                val onboardingViewModel: com.ieum.presentation.feature.onboarding.OnboardingViewModel = 
                    androidx.hilt.navigation.compose.hiltViewModel(onboardingBackStackEntry)
                
                com.ieum.presentation.feature.onboarding.BirthdayInputScreen(
                    viewModel = onboardingViewModel,
                    onNext = {
                        navController.navigate(Routes.ONBOARDING_ANNIVERSARY)
                    }
                )
            }
            
            // 온보딩 3단계: 기념일 입력
            composable(Routes.ONBOARDING_ANNIVERSARY) { backStackEntry ->
                // 온보딩 플로우 전체에서 공유할 ViewModel
                val onboardingBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.ONBOARDING)
                }
                val onboardingViewModel: com.ieum.presentation.feature.onboarding.OnboardingViewModel = 
                    androidx.hilt.navigation.compose.hiltViewModel(onboardingBackStackEntry)
                
                com.ieum.presentation.feature.onboarding.AnniversaryInputScreen(
                    viewModel = onboardingViewModel,
                    onNext = {
                        navController.navigate(Routes.CODE_CONNECTION) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.CODE_CONNECTION) {
            com.ieum.presentation.feature.connection.CodeConnectionScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.CODE_CONNECTION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToMyPage = { navController.navigate(Routes.MY_PAGE) },
                onNavigateToBudgetPlanning = { navController.navigate(Routes.BUDGET_PLANNING) },
                onNavigateToClicker = { navController.navigate(Routes.CLICKER_GAME) }
            )
        }

        composable(Routes.MY_PAGE) {
            com.ieum.presentation.feature.profile.MyPageScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToConsumption = { navController.navigate(Routes.CONSUMPTION) },
                onNavigateToBudgetPlanning = { navController.navigate(Routes.BUDGET_PLANNING) },
                onLogout = {
                    // 로그아웃 시 로그인 화면으로 이동 (모든 백스택 제거)
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CONSUMPTION) {
            com.ieum.presentation.feature.finance.ConsumptionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.BUDGET_PLANNING) {
            com.ieum.presentation.feature.finance.BudgetPlanningScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.CLICKER_GAME) {
            com.ieum.presentation.feature.dashboard.ClickerGameScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * 하단 네비게이션 아이템 정의
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Calendar : BottomNavItem("calendar", "캘린더", Icons.Filled.DateRange, Icons.Outlined.DateRange)
    data object MapGallery : BottomNavItem("map_gallery", "지도갤러리", Icons.Filled.Place, Icons.Outlined.Place)
    data object Chat : BottomNavItem("chat", "채팅", Icons.Filled.Chat, Icons.Outlined.Chat)
    data object Dashboard : BottomNavItem("dashboard", "대시보드", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.MapGallery,
    BottomNavItem.Chat,
    BottomNavItem.Calendar
)

/**
 * ✅ 메인 화면 (대시보드 및 탭 전환)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigateToMyPage: () -> Unit,
    onNavigateToBudgetPlanning: () -> Unit,
    onNavigateToClicker: () -> Unit
) {
    // 대시보드가 기본 (index 0 - Dashboard=0, Map=1, Chat=2, Calendar=3)
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            // ✅ 캘린더 화면(index 3)과 채팅 화면(index 2)이 아닐 때만 바텀 바를 표시합니다.
            if (selectedItem != 3 && selectedItem != 2) {
                IeumBottomNavigation(
                    selectedIndex = selectedItem,
                    onItemSelected = { selectedItem = it }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 바텀 바가 숨겨질 때는 패딩을 주지 않아 화면을 꽉 채우게 합니다.
                .padding(if (selectedItem != 3 && selectedItem != 2) paddingValues else PaddingValues(0.dp))
        ) {
            AnimatedContent(
                targetState = selectedItem,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "screen_transition"
            ) { index ->
                when (bottomNavItems[index]) {
                    // ✅ 뒤로 가기 시 다시 대시보드(index 0)로 돌아오도록 설정
                    BottomNavItem.Calendar -> CalendarContent(onBack = { selectedItem = 0 })
                    BottomNavItem.MapGallery -> MapGalleryContent()
                    BottomNavItem.Chat -> ChatContent(onBack = { selectedItem = 0 }, onNavigateToBudgetPlanning = onNavigateToBudgetPlanning)
                    BottomNavItem.Dashboard -> DashboardContent(
                        onNavigateToCalendar = { selectedItem = 3 },
                        onNavigateToMyPage = onNavigateToMyPage,
                        onNavigateToBudgetPlanning = onNavigateToBudgetPlanning,
                        onNavigateToClicker = onNavigateToClicker
                    )
                }
            }
        }
    }
}

@Composable
fun IeumBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        //containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF5A3E2B),
                    selectedTextColor = Color(0xFF5A3E2B),
                    unselectedIconColor = Color(0xFF5A3E2B).copy(alpha = 0.6f),
                    unselectedTextColor = Color(0xFF5A3E2B).copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// --- 각 화면 연결 함수 (패키지 경로에 주의하세요) ---

@Composable
private fun CalendarContent(onBack: () -> Unit) {
    com.ieum.presentation.feature.calendar.CalendarScreen(
        onBackClick = onBack
    )
}

@Composable
private fun MapGalleryContent() {
    com.ieum.presentation.feature.memory.MapGalleryScreen()
}

@Composable
private fun ChatContent(onBack: () -> Unit, onNavigateToBudgetPlanning: () -> Unit) {
    com.ieum.presentation.feature.chat.ChatScreen(
        onBackClick = onBack,
        onNavigateToBudgetPlanning = onNavigateToBudgetPlanning
    )
}

@Composable
private fun DashboardContent(
    onNavigateToCalendar: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    onNavigateToBudgetPlanning: () -> Unit,
    onNavigateToClicker: () -> Unit
) {
    com.ieum.presentation.feature.dashboard.DashboardScreen(
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToProfile = onNavigateToMyPage,
        onNavigateToBudgetPlanning = onNavigateToBudgetPlanning,
        onNavigateToClicker = onNavigateToClicker
    )
}