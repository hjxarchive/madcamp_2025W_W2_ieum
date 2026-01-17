package com.ieum.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ieum.presentation.feature.login.LoginScreen
import com.ieum.presentation.theme.IeumColors
/**
 * Root Routes
 * - 앱 실행 시 LOGIN 먼저
 * - 로그인 성공 시 MAIN(하단탭)으로
 */

/**
 * ✅ 앱의 "진짜 메인 네비게이션"
 * - startDestination = LOGIN (에뮬 실행하면 로그인부터)
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            // 로그인 성공하면 메인으로 이동
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MBTI_TEST) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // 뒤로가기 시 로그인으로 안 돌아가게
                    }
                }
            )
        }

        composable(Routes.MBTI_TEST) {
            // 이전에 만든 TestMainScreen 호출
            // 테스트가 종료되었을 때 Routes.MAIN으로 이동하는 로직을 추가하면 좋습니다.
            com.ieum.presentation.feature.test.TestMainScreen(
                onTestFinished = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MBTI_TEST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen()
        }
    }
}

/**
 * 하단 네비게이션 탭 정의
 * PDF 기준: 마이페이지, 캘린더, 지도갤러리, 채팅, 대시보드
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object MyPage : BottomNavItem(
        route = "mypage",
        title = "마이페이지",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Calendar : BottomNavItem(
        route = "calendar",
        title = "캘린더",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )

    data object MapGallery : BottomNavItem(
        route = "map_gallery",
        title = "지도갤러리",
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place
    )

    data object Chat : BottomNavItem(
        route = "chat",
        title = "채팅",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    )

    data object Dashboard : BottomNavItem(
        route = "dashboard",
        title = "대시보드",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )
}

val bottomNavItems = listOf(
    BottomNavItem.MyPage,
    BottomNavItem.Calendar,
    BottomNavItem.MapGallery,
    BottomNavItem.Chat,
    BottomNavItem.Dashboard
)

/**
 * 메인 화면 - 하단 네비게이션 포함
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableIntStateOf(4) } // 대시보드가 기본

    Scaffold(
        modifier = modifier,
        // ✅ Theme 배경 이미지(전체 통일) 덮지 않게 투명 처리 추천
        containerColor = Color.Transparent,
        bottomBar = {
            IeumBottomNavigation(
                selectedIndex = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 현재 선택된 탭에 따른 화면 표시
            AnimatedContent(
                targetState = selectedItem,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "screen_transition"
            ) { index ->
                when (bottomNavItems[index]) {
                    BottomNavItem.MyPage -> MyPageContent()
                    BottomNavItem.Calendar -> CalendarContent()
                    BottomNavItem.MapGallery -> MapGalleryContent()
                    BottomNavItem.Chat -> ChatContent()
                    BottomNavItem.Dashboard -> DashboardContent()
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
        containerColor = IeumColors.Surface,
        tonalElevation = 8.dp
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
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IeumColors.Primary,
                    selectedTextColor = IeumColors.Primary,
                    unselectedIconColor = IeumColors.TextSecondary,
                    unselectedTextColor = IeumColors.TextSecondary,
                    indicatorColor = IeumColors.Primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

// 실제 화면 컴포저블 import (각 feature 패키지에서)
@Composable
private fun MyPageContent() {
    com.ieum.presentation.feature.profile.MyPageScreen()
}

@Composable
private fun CalendarContent() {
    com.ieum.presentation.feature.calendar.CalendarScreen()
}

@Composable
private fun MapGalleryContent() {
    com.ieum.presentation.feature.memory.MapGalleryScreen()
}

@Composable
private fun ChatContent() {
    com.ieum.presentation.feature.chat.ChatScreen()
}

@Composable
private fun DashboardContent() {
    com.ieum.presentation.feature.dashboard.DashboardScreen()
}
