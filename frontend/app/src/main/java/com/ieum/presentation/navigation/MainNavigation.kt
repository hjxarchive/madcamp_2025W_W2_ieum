package com.ieum.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ieum.presentation.theme.IeumColors

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableIntStateOf(4) } // 대시보드가 기본
    
    Scaffold(
        modifier = modifier,
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
