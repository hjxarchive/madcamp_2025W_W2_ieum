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
                    navController.navigate("connection_screen") {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        /*composable(Routes.LOGIN) {

    LoginScreen(

        onLoginSuccess = {

            // 로그인 성공 시 MBTI 테스트로 이동

            navController.navigate(Routes.MBTI_TEST) {

                popUpTo(Routes.LOGIN) { inclusive = true }

            }

        }

    )

}



// 2. MBTI 테스트 화면

composable(Routes.MBTI_TEST) {

    com.ieum.presentation.feature.test.TestMainScreen(

        onTestFinished = {

            // 테스트 종료 시 코드 연결 화면으로 이동

            navController.navigate("connection_screen") {

                popUpTo(Routes.MBTI_TEST) { inclusive = true }

            }

        }

    )

}*/

        composable("connection_screen") {
            com.ieum.presentation.feature.connection.CodeConnectionScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo("connection_screen") { inclusive = true }
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
 * 하단 네비게이션 아이템 정의
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object MyPage : BottomNavItem("mypage", "마이페이지", Icons.Filled.Person, Icons.Outlined.Person)
    data object Calendar : BottomNavItem("calendar", "캘린더", Icons.Filled.DateRange, Icons.Outlined.DateRange)
    data object MapGallery : BottomNavItem("map_gallery", "지도갤러리", Icons.Filled.Place, Icons.Outlined.Place)
    data object Chat : BottomNavItem("chat", "채팅", Icons.Filled.Chat, Icons.Outlined.Chat)
    data object Dashboard : BottomNavItem("dashboard", "대시보드", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
}

val bottomNavItems = listOf(
    BottomNavItem.MyPage,
    BottomNavItem.Calendar,
    BottomNavItem.MapGallery,
    BottomNavItem.Chat,
    BottomNavItem.Dashboard
)

/**
 * ✅ 메인 화면 (대시보드 및 탭 전환)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    // 대시보드가 기본 (index 4)
    var selectedItem by remember { mutableIntStateOf(4) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            // ✅ 캘린더 화면(index 1)이 아닐 때만 바텀 바를 표시합니다.
            if (selectedItem != 1) {
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
                .padding(if (selectedItem != 1) paddingValues else PaddingValues(0.dp))
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
                    BottomNavItem.MyPage -> MyPageContent()
                    // ✅ 뒤로 가기 시 다시 대시보드(index 4)로 돌아오도록 설정
                    BottomNavItem.Calendar -> CalendarContent(onBack = { selectedItem = 4 })
                    BottomNavItem.MapGallery -> MapGalleryContent()
                    BottomNavItem.Chat -> ChatContent()
                    BottomNavItem.Dashboard -> DashboardContent(
                        onNavigateToCalendar = { selectedItem = 1 }
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
                label = { Text(text = item.title, style = MaterialTheme.typography.labelSmall) },
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

// --- 각 화면 연결 함수 (패키지 경로에 주의하세요) ---

@Composable
private fun MyPageContent() {
    com.ieum.presentation.feature.profile.MyPageScreen()
}

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
private fun ChatContent() {
    com.ieum.presentation.feature.chat.ChatScreen()
}

@Composable
private fun DashboardContent(onNavigateToCalendar: () -> Unit) {
    com.ieum.presentation.feature.dashboard.DashboardScreen(
        onNavigateToCalendar = onNavigateToCalendar
    )
}