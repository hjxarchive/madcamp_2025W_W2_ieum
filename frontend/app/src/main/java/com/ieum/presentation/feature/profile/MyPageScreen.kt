package com.ieum.presentation.feature.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.domain.model.ChemistryData
import com.ieum.presentation.theme.IeumColors
import com.ieum.R

// Requested global color
val MainBrown = Color(0xFF5A3E2B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToConsumption: () -> Unit = {},
    onNavigateToBudgetPlanning: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logoutEvent by viewModel.logoutEvent.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // 로그아웃 이벤트 처리
    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            viewModel.resetLogoutEvent()
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("마이페이지", fontWeight = FontWeight.Bold, color = MainBrown) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MainBrown)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = IeumColors.Background
                )
            )
        },
        containerColor = IeumColors.Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.background2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Couple Profile Section
            CoupleProfileSection(uiState)

            Spacer(modifier = Modifier.height(32.dp))

            // 2. MBTI Chemistry Section
            MBTIChemistrySection(uiState)

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Finance Section (Redesigned)
            FinanceSectionList(
                onNavigateToConsumption = onNavigateToConsumption,
                onNavigateToBudgetPlanning = onNavigateToBudgetPlanning
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Settings Section (New)
            SettingsSection(onLogoutClick = { viewModel.logout() })

            Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
private fun CoupleProfileSection(uiState: ProfileUiState) {
    val myNickname = uiState.coupleInfo?.user?.nickname ?: "나"
    val partnerNickname = uiState.coupleInfo?.partner?.nickname ?: "파트너"
    
    val myMbtiImage = getMbtiImage(uiState.myMbti)
    val partnerMbtiImage = getMbtiImage(uiState.partnerMbti)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Me
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (myMbtiImage != -1) {
                    Image(
                        painter = painterResource(id = myMbtiImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = myNickname, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MainBrown)
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Heart
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF6F61),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Partner
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (partnerMbtiImage != -1) {
                    Image(
                        painter = painterResource(id = partnerMbtiImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = partnerNickname, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MainBrown)
        }
    }
}

private fun getMbtiImage(mbti: String?): Int {
    return when (mbti) {
        "MDEP" -> R.drawable.mdep_r
        "MDEF" -> R.drawable.mdef_r
        "MDCP" -> R.drawable.mdcp_r
        "MDCF" -> R.drawable.mdcf_r
        "MTEP" -> R.drawable.mtep_r
        "MTEF" -> R.drawable.mtef_r
        "MTCP" -> R.drawable.mtcp_r
        "MTCF" -> R.drawable.mtcf_r
        "IDEP" -> R.drawable.idep_r
        "IDEF" -> R.drawable.idef_r
        "IDCP" -> R.drawable.idcp_r
        "IDCF" -> R.drawable.idcf_r
        "ITEP" -> R.drawable.itep_r
        "ITEF" -> R.drawable.itef_r
        "ITCP" -> R.drawable.itcp_r
        "ITCF" -> R.drawable.itcf_r
        else -> -1
    }
}

@Composable
private fun MBTIChemistrySection(uiState: ProfileUiState) {
    val myMbti = uiState.myMbti
    val partnerMbti = uiState.partnerMbti

    Text(
        text = "우리의 케미",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MainBrown,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (myMbti != null && partnerMbti != null) {
                val chemistry = ChemistryData.getChemistry(myMbti, partnerMbti)
                val chemistryTitle = chemistry?.title ?: "알 수 없는 케미"
                val chemistryDesc = chemistry?.description ?: "MBTI 정보를 확인해주세요."

                Text(
                    text = "$myMbti ♥ $partnerMbti",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = chemistryTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = IeumColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = chemistryDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            } else {
                Text("MBTI 테스트를 진행해주세요!", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun FinanceSectionList(
    onNavigateToConsumption: () -> Unit,
    onNavigateToBudgetPlanning: () -> Unit
) {
    Text(
        text = "우리의 소비",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MainBrown,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // List Style Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            SettingsItem(
                title = "이 달의 소비",
                icon = Icons.Default.ReceiptLong,
                onClick = onNavigateToConsumption,
                showDivider = true
            )
            SettingsItem(
                title = "이 달의 예산",
                icon = Icons.Default.Savings, // Savings icon is better for budget
                onClick = onNavigateToBudgetPlanning,
                showDivider = false
            )
        }
    }
}

@Composable
private fun SettingsSection(onLogoutClick: () -> Unit = {}) {
    Text(
        text = "설정",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MainBrown,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            SettingsItem(title = "알림 설정", icon = Icons.Outlined.Notifications, onClick = {}, showDivider = true)
            SettingsItem(title = "개인정보 설정", icon = Icons.Outlined.Lock, onClick = {}, showDivider = true)
            SettingsItem(title = "고객센터", icon = Icons.Outlined.Help, onClick = {}, showDivider = true)
            SettingsItem(title = "앱 정보", icon = Icons.Outlined.Info, onClick = {}, showDivider = true)
            SettingsItem(
                title = "로그아웃",
                icon = Icons.Outlined.Logout,
                onClick = onLogoutClick,
                showDivider = false,
                tintColor = Color(0xFFE57373)
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showDivider: Boolean,
    tintColor: Color = Color.Gray
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (tintColor == Color.Gray) Color.Black.copy(alpha = 0.8f) else tintColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tintColor
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}
