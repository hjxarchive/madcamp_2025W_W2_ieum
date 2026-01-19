package com.ieum.presentation.feature.memory

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.rememberCameraPositionState
import com.ieum.presentation.theme.IeumColors
import com.ieum.R

import android.Manifest
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.MapStyleOptions

// ViewMode enum
enum class ViewMode {
    MAP, GALLERY, TIMELINE
}

/**
 * 지도 갤러리 화면 (추억 아카이브)
 * 위치 기반 사진 갤러리 + 한 줄 코멘트
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapGalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    var viewMode by remember { mutableStateOf(ViewMode.MAP) }
    var selectedMemory by remember { mutableStateOf<Memory?>(null) }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
    ) {
        // 상단 헤더
        MapGalleryHeader(
            viewMode = viewMode,
            onViewModeChange = { viewMode = it }
        )

        // 콘텐츠
        if (locationPermissionsState.allPermissionsGranted) {
            when (viewMode) {
                ViewMode.MAP -> MapView(
                    memories = sampleMemories,
                    onMemoryClick = { selectedMemory = it }
                )
                ViewMode.GALLERY -> GalleryView(
                    memories = sampleMemories,
                    onMemoryClick = { selectedMemory = it }
                )
                ViewMode.TIMELINE -> {
                    // Do nothing for now
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("위치 권한이 필요합니다.")
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("권한 요청")
                }
            }
        }
    }

    // 추억 상세 바텀시트
    selectedMemory?.let { memory ->
        MemoryDetailSheet(
            memory = memory,
            onDismiss = { selectedMemory = null }
        )
    }
}

@Composable
private fun MapGalleryHeader(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = IeumColors.Background,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "추억 갤러리",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IeumColors.TextPrimary
                )
                
                IconButton(onClick = { /* 추억 추가 */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추억 추가",
                        tint = IeumColors.Primary
                    )
                }
            }
            
            // 뷰 모드 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewMode.entries.forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { onViewModeChange(mode) },
                        label = { Text(mode.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IeumColors.Primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * 지도 뷰
 */
@Composable
private fun MapView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit
) {
    val context = LocalContext.current
    val seoul = LatLng(37.5445, 126.9780) // 서울 중심
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(seoul, 13f)
    }
    var isMapLoaded by remember { mutableStateOf(false) }

    // 지도 스타일 안전하게 로드
    val mapStyleOptions = remember {
        try {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        } catch (e: Exception) {
            null // 스타일 로드 실패 시 기본 스타일 사용
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { isMapLoaded = true },
            uiSettings = com.google.maps.android.compose.MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false
            ),
            properties = com.google.maps.android.compose.MapProperties(
                isMyLocationEnabled = true,
                mapType = com.google.maps.android.compose.MapType.NORMAL,
                mapStyleOptions = mapStyleOptions
            )
        ) {
            MapEffect(memories) { map ->
                if (memories.isNotEmpty()) {
                    val clusterManager = ClusterManager<PhotoClusterItem>(context, map)
                    clusterManager.renderer = CustomClusterRenderer(context, map, clusterManager)

                    memories.forEach { memory ->
                        val photoItem = PhotoClusterItem(
                            memoryId = memory.id.toLong(),
                            _position = LatLng(memory.latitude, memory.longitude),
                            _title = memory.placeName,
                            _snippet = memory.comment,
                            thumbnailUrl = memory.imageUrl ?: ""
                        )
                        clusterManager.addItem(photoItem)
                    }

                    clusterManager.setOnClusterClickListener { cluster ->
                        // 클러스터 클릭 시 카메라 줌인
                        val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                        cluster.items.forEach { item -> bounds.include(item.getPosition()) }
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                        true
                    }

                    clusterManager.setOnClusterItemClickListener { item ->
                        // 개별 아이템 클릭 시 상세 정보 표시
                        val memory = memories.find { it.id.toLong() == item.memoryId }
                        memory?.let { onMemoryClick(it) }
                        true
                    }

                    map.setOnCameraIdleListener(clusterManager)
                    map.setOnMarkerClickListener(clusterManager)
                    clusterManager.cluster()
                }
            }
        }

        // 로딩 인디케이터
        if (!isMapLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(IeumColors.Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = IeumColors.Primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // FAB 버튼들 (지도 로드 완료 후에만 표시)
        if (isMapLoaded) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // 현재 위치로 이동 버튼
                FloatingActionButton(
                    onClick = {
                        if (memories.isNotEmpty()) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(memories.first().latitude, memories.first().longitude),
                                    13f
                                )
                            )
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color.White,
                    contentColor = IeumColors.Primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "현재 위치",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 추억 추가 버튼
                FloatingActionButton(
                    onClick = { /* TODO: 추억 추가 화면으로 이동 */ },
                    modifier = Modifier.size(56.dp),
                    containerColor = IeumColors.Primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "추억 추가",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// 구글맵 클러스터링을 위한 데이터 클래스
data class PhotoClusterItem(
    val memoryId: Long,
    private val _position: LatLng,
    private val _title: String,
    private val _snippet: String,
    val thumbnailUrl: String
) : com.google.maps.android.clustering.ClusterItem {
    override fun getPosition(): LatLng = _position
    override fun getTitle(): String = _title
    override fun getSnippet(): String = _snippet
    override fun getZIndex(): Float? = 0f
}

// 커스텀 클러스터 렌더러 - 둥근 정사각형 스타일
class CustomClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<PhotoClusterItem>
) : DefaultClusterRenderer<PhotoClusterItem>(context, map, clusterManager) {

    private val markerSize = 120 // 개별 마커 크기
    private val clusterSize = 130 // 클러스터 마커 크기
    private val borderWidth = 5f
    private val cornerRadius = 20f // 둥근 모서리 반경
    private val badgeSize = 40 // 내부 배지 크기
    private val badgePadding = 6 // 배지 패딩

    init {
        minClusterSize = 2 // 2개 이상일 때 클러스터링
    }

    override fun shouldRenderAsCluster(cluster: Cluster<PhotoClusterItem>): Boolean {
        return cluster.size >= 2
    }

    // 개별 사진 마커 렌더링
    override fun onBeforeClusterItemRendered(item: PhotoClusterItem, markerOptions: MarkerOptions) {
        val placeholder = createRoundedPlaceholder(markerSize, false, 0)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(placeholder))

        if (item.thumbnailUrl.isNotEmpty()) {
            Glide.with(context)
                .asBitmap()
                .load(item.thumbnailUrl)
                .override(markerSize, markerSize)
                .centerCrop()
                .into(object : CustomTarget<Bitmap>(markerSize, markerSize) {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val marker = createRoundedMarker(resource, markerSize, false, 0)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(marker))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    override fun onClusterItemUpdated(item: PhotoClusterItem, marker: Marker) {
        if (item.thumbnailUrl.isNotEmpty()) {
            Glide.with(context)
                .asBitmap()
                .load(item.thumbnailUrl)
                .override(markerSize, markerSize)
                .centerCrop()
                .into(object : CustomTarget<Bitmap>(markerSize, markerSize) {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val bitmap = createRoundedMarker(resource, markerSize, false, 0)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    // 클러스터 마커 렌더링 (사진 + 내부 개수 배지)
    override fun onBeforeClusterRendered(cluster: Cluster<PhotoClusterItem>, markerOptions: MarkerOptions) {
        val count = cluster.size
        val firstItem = cluster.items.firstOrNull()

        val placeholder = createRoundedPlaceholder(clusterSize, true, count)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(placeholder))

        firstItem?.let { item ->
            if (item.thumbnailUrl.isNotEmpty()) {
                Glide.with(context)
                    .asBitmap()
                    .load(item.thumbnailUrl)
                    .override(clusterSize, clusterSize)
                    .centerCrop()
                    .into(object : CustomTarget<Bitmap>(clusterSize, clusterSize) {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val bitmap = createRoundedMarker(resource, clusterSize, true, count)
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }

    override fun onClusterUpdated(cluster: Cluster<PhotoClusterItem>, marker: Marker) {
        val count = cluster.size
        val firstItem = cluster.items.firstOrNull()

        firstItem?.let { item ->
            if (item.thumbnailUrl.isNotEmpty()) {
                Glide.with(context)
                    .asBitmap()
                    .load(item.thumbnailUrl)
                    .override(clusterSize, clusterSize)
                    .centerCrop()
                    .into(object : CustomTarget<Bitmap>(clusterSize, clusterSize) {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val bitmap = createRoundedMarker(resource, clusterSize, true, count)
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }

    // 둥근 정사각형 마커 생성
    private fun createRoundedMarker(bitmap: Bitmap, size: Int, showBadge: Boolean, count: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 그림자 효과를 위한 배경
        val shadowPaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
            setShadowLayer(10f, 0f, 4f, AndroidColor.argb(100, 0, 0, 0))
        }
        val shadowRect = RectF(4f, 4f, size - 4f, size - 4f)
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)

        // 흰색 테두리
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        val borderRect = RectF(2f, 2f, size - 2f, size - 2f)
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)

        // 이미지 클리핑 및 그리기
        val imageSize = size - (borderWidth * 2).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)

        val imageRect = RectF(borderWidth, borderWidth, size - borderWidth, size - borderWidth)
        val innerCornerRadius = cornerRadius - borderWidth

        // 클리핑 패스로 둥근 사각형 이미지
        canvas.save()
        val clipPath = android.graphics.Path().apply {
            addRoundRect(imageRect, innerCornerRadius, innerCornerRadius, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        canvas.drawBitmap(scaledBitmap, borderWidth, borderWidth, null)
        canvas.restore()

        // 클러스터일 경우 내부 배지 그리기
        if (showBadge && count > 1) {
            drawInternalBadge(canvas, count, size)
        }

        return output
    }

    // 플레이스홀더 (로딩 전)
    private fun createRoundedPlaceholder(size: Int, showBadge: Boolean, count: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 그림자
        val shadowPaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
            setShadowLayer(10f, 0f, 4f, AndroidColor.argb(100, 0, 0, 0))
        }
        val shadowRect = RectF(4f, 4f, size - 4f, size - 4f)
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)

        // 흰색 테두리
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        val borderRect = RectF(2f, 2f, size - 2f, size - 2f)
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)

        // 회색 내부
        val innerPaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.parseColor("#E8E8E8")
            style = Paint.Style.FILL
        }
        val innerRect = RectF(borderWidth, borderWidth, size - borderWidth, size - borderWidth)
        val innerCornerRadius = cornerRadius - borderWidth
        canvas.drawRoundRect(innerRect, innerCornerRadius, innerCornerRadius, innerPaint)

        // 클러스터일 경우 배지 그리기
        if (showBadge && count > 1) {
            drawInternalBadge(canvas, count, size)
        }

        return output
    }

    // 내부 배지 그리기 (우하단)
    private fun drawInternalBadge(canvas: Canvas, count: Int, markerSize: Int) {
        val badgeX = markerSize - badgeSize - badgePadding
        val badgeY = markerSize - badgeSize - badgePadding

        // 배지 배경 (반투명 검정)
        val badgePaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.parseColor("#CC000000")
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(
            badgeX.toFloat(),
            badgeY.toFloat(),
            (badgeX + badgeSize).toFloat(),
            (badgeY + badgeSize).toFloat()
        )
        canvas.drawRoundRect(badgeRect, 8f, 8f, badgePaint)

        // 개수 텍스트
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.WHITE
            textSize = when {
                count >= 100 -> 18f
                count >= 10 -> 22f
                else -> 26f
            }
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val countText = if (count >= 1000) "999+" else count.toString()
        val textBounds = Rect()
        textPaint.getTextBounds(countText, 0, countText.length, textBounds)

        canvas.drawText(
            countText,
            badgeX + badgeSize / 2f,
            badgeY + badgeSize / 2f + textBounds.height() / 2f,
            textPaint
        )
    }
}


@Composable
private fun GalleryView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(memories) { memory ->
            GalleryItem(
                memory = memory,
                onClick = { onMemoryClick(memory) }
            )
        }
    }
}

@OptIn(com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi::class)
@Composable
private fun GalleryItem(
    memory: Memory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 실제 이미지 로드
            if (!memory.imageUrl.isNullOrEmpty()) {
                com.bumptech.glide.integration.compose.GlideImage(
                    model = memory.imageUrl,
                    contentDescription = memory.placeName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = com.bumptech.glide.integration.compose.placeholder {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(memory.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = memory.color,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    failure = com.bumptech.glide.integration.compose.placeholder {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(memory.color.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BrokenImage,
                                contentDescription = null,
                                tint = memory.color,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )
            } else {
                // 이미지 없을 때 플레이스홀더
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    memory.color.copy(alpha = 0.2f),
                                    memory.color.copy(alpha = 0.4f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = memory.color,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // 하단 그라데이션 오버레이
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // 하단 정보
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                // 장소명
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = memory.placeName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 날짜
                Text(
                    text = memory.date,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp
                    ),
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

// ViewMode label과 icon을 위한 확장 함수
val ViewMode.label: String
    get() = when(this) {
        ViewMode.MAP -> "지도"
        ViewMode.GALLERY -> "갤러리"
        ViewMode.TIMELINE -> "타임라인"
    }

val ViewMode.icon: ImageVector
    get() = when(this) {
        ViewMode.MAP -> Icons.Outlined.Map
        ViewMode.GALLERY -> Icons.Outlined.GridView
        ViewMode.TIMELINE -> Icons.Outlined.Timeline
    }

// 데이터 클래스 (UI용)
data class Memory(
    val id: Int,
    val placeName: String,
    val address: String,
    val comment: String,
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val color: Color,
    val imageUrl: String? = null
)

// 샘플 데이터 100개 (실제 이미지 URL 포함)
private val sampleMemories = listOf(
    // 성수동 클러스터 (10개)
    Memory(1, "성수동 카페", "서울 성동구 성수동", "우리의 첫 데이트 장소 ❤️", "2026-02-14", 37.5445, 127.0567, IeumColors.Primary, "https://picsum.photos/seed/cafe1/400/400"),
    Memory(2, "성수동 베이커리", "서울 성동구 성수동", "맛있는 빵집 발견!", "2026-02-14", 37.5448, 127.0570, IeumColors.Primary, "https://picsum.photos/seed/bakery/400/400"),
    Memory(3, "성수동 갤러리", "서울 성동구 성수동", "예쁜 전시회", "2026-02-14", 37.5442, 127.0565, IeumColors.Primary, "https://picsum.photos/seed/gallery/400/400"),
    Memory(4, "성수동 편집샵", "서울 성동구 성수동", "빈티지 옷 쇼핑", "2026-02-15", 37.5447, 127.0562, IeumColors.Primary, "https://picsum.photos/seed/vintage/400/400"),
    Memory(5, "성수동 브런치", "서울 성동구 성수동", "주말 브런치", "2026-02-15", 37.5444, 127.0569, IeumColors.Primary, "https://picsum.photos/seed/brunch1/400/400"),
    Memory(6, "성수동 루프탑", "서울 성동구 성수동", "루프탑에서 커피", "2026-02-16", 37.5449, 127.0564, IeumColors.Primary, "https://picsum.photos/seed/rooftop/400/400"),
    Memory(7, "성수동 서점", "서울 성동구 성수동", "독립서점 탐방", "2026-02-16", 37.5443, 127.0568, IeumColors.Primary, "https://picsum.photos/seed/bookstore/400/400"),
    Memory(8, "성수동 공방", "서울 성동구 성수동", "도자기 체험", "2026-02-17", 37.5446, 127.0566, IeumColors.Primary, "https://picsum.photos/seed/pottery/400/400"),
    Memory(9, "성수동 맛집", "서울 성동구 성수동", "파스타 맛집", "2026-02-17", 37.5450, 127.0563, IeumColors.Primary, "https://picsum.photos/seed/pasta/400/400"),
    Memory(10, "성수동 야경", "서울 성동구 성수동", "밤 산책", "2026-02-17", 37.5441, 127.0571, IeumColors.Primary, "https://picsum.photos/seed/night1/400/400"),

    // 한강공원 클러스터 (12개)
    Memory(11, "한강공원", "서울 영등포구 여의도동", "피크닉하던 날", "2026-02-10", 37.5284, 126.9343, IeumColors.Secondary, "https://picsum.photos/seed/picnic/400/400"),
    Memory(12, "한강 자전거", "서울 영등포구 여의도동", "자전거 타기 좋은 날", "2026-02-10", 37.5280, 126.9340, IeumColors.Secondary, "https://picsum.photos/seed/bike/400/400"),
    Memory(13, "한강 노을", "서울 영등포구 여의도동", "노을이 예뻤어", "2026-02-10", 37.5286, 126.9346, IeumColors.Secondary, "https://picsum.photos/seed/sunset/400/400"),
    Memory(14, "한강 치맥", "서울 영등포구 여의도동", "치킨 먹은 날", "2026-02-11", 37.5282, 126.9341, IeumColors.Secondary, "https://picsum.photos/seed/chicken/400/400"),
    Memory(15, "한강 야경", "서울 영등포구 여의도동", "밤에 산책", "2026-02-11", 37.5288, 126.9344, IeumColors.Secondary, "https://picsum.photos/seed/river1/400/400"),
    Memory(16, "한강 라면", "서울 영등포구 여의도동", "편의점 라면", "2026-02-12", 37.5279, 126.9347, IeumColors.Secondary, "https://picsum.photos/seed/ramen/400/400"),
    Memory(17, "한강 돗자리", "서울 영등포구 여의도동", "돗자리 깔고 휴식", "2026-02-12", 37.5285, 126.9339, IeumColors.Secondary, "https://picsum.photos/seed/mat/400/400"),
    Memory(18, "한강 불꽃놀이", "서울 영등포구 여의도동", "불꽃축제", "2026-02-13", 37.5281, 126.9348, IeumColors.Secondary, "https://picsum.photos/seed/firework/400/400"),
    Memory(19, "한강 요트", "서울 영등포구 여의도동", "요트 투어", "2026-02-13", 37.5287, 126.9342, IeumColors.Secondary, "https://picsum.photos/seed/yacht/400/400"),
    Memory(20, "한강 조깅", "서울 영등포구 여의도동", "아침 조깅", "2026-02-14", 37.5283, 126.9345, IeumColors.Secondary, "https://picsum.photos/seed/jogging/400/400"),
    Memory(21, "한강 수상택시", "서울 영등포구 여의도동", "수상택시 탑승", "2026-02-14", 37.5278, 126.9349, IeumColors.Secondary, "https://picsum.photos/seed/taxi/400/400"),
    Memory(22, "한강 벚꽃", "서울 영등포구 여의도동", "벚꽃 구경", "2026-02-15", 37.5289, 126.9338, IeumColors.Secondary, "https://picsum.photos/seed/cherry/400/400"),

    // 홍대 클러스터 (15개)
    Memory(23, "홍대 거리", "서울 마포구 서교동", "맛있는 거 많이 먹은 날", "2026-01-15", 37.5563, 126.9237, IeumColors.CategoryFood, "https://picsum.photos/seed/hongdae/400/400"),
    Memory(24, "홍대 라이브카페", "서울 마포구 서교동", "밴드 공연 봤어", "2026-01-15", 37.5566, 126.9240, IeumColors.CategoryFood, "https://picsum.photos/seed/live/400/400"),
    Memory(25, "홍대 맛집", "서울 마포구 서교동", "줄 서서 먹은 맛집", "2026-01-16", 37.5560, 126.9235, IeumColors.CategoryFood, "https://picsum.photos/seed/food/400/400"),
    Memory(26, "홍대 디저트", "서울 마포구 서교동", "디저트 카페 탐방", "2026-01-16", 37.5568, 126.9242, IeumColors.CategoryFood, "https://picsum.photos/seed/dessert/400/400"),
    Memory(27, "홍대 쇼핑", "서울 마포구 서교동", "예쁜 옷 샀어", "2026-01-17", 37.5561, 126.9238, IeumColors.CategoryFood, "https://picsum.photos/seed/shopping/400/400"),
    Memory(28, "홍대 클럽", "서울 마포구 서교동", "신나는 밤", "2026-01-17", 37.5564, 126.9234, IeumColors.CategoryFood, "https://picsum.photos/seed/club/400/400"),
    Memory(29, "홍대 벽화거리", "서울 마포구 서교동", "벽화 사진", "2026-01-18", 37.5567, 126.9241, IeumColors.CategoryFood, "https://picsum.photos/seed/mural/400/400"),
    Memory(30, "홍대 버스킹", "서울 마포구 서교동", "버스킹 구경", "2026-01-18", 37.5559, 126.9236, IeumColors.CategoryFood, "https://picsum.photos/seed/busking/400/400"),
    Memory(31, "홍대 타투샵", "서울 마포구 서교동", "타투 구경", "2026-01-19", 37.5565, 126.9239, IeumColors.CategoryFood, "https://picsum.photos/seed/tattoo/400/400"),
    Memory(32, "홍대 레코드샵", "서울 마포구 서교동", "LP 구경", "2026-01-19", 37.5562, 126.9243, IeumColors.CategoryFood, "https://picsum.photos/seed/record/400/400"),
    Memory(33, "홍대 피어싱", "서울 마포구 서교동", "피어싱 샵", "2026-01-20", 37.5558, 126.9233, IeumColors.CategoryFood, "https://picsum.photos/seed/piercing/400/400"),
    Memory(34, "홍대 네일샵", "서울 마포구 서교동", "네일아트", "2026-01-20", 37.5569, 126.9244, IeumColors.CategoryFood, "https://picsum.photos/seed/nail/400/400"),
    Memory(35, "홍대 포토부스", "서울 마포구 서교동", "인생네컷", "2026-01-21", 37.5557, 126.9232, IeumColors.CategoryFood, "https://picsum.photos/seed/photo/400/400"),
    Memory(36, "홍대 떡볶이", "서울 마포구 서교동", "떡볶이 맛집", "2026-01-21", 37.5570, 126.9245, IeumColors.CategoryFood, "https://picsum.photos/seed/tteok/400/400"),
    Memory(37, "홍대 아이스크림", "서울 마포구 서교동", "젤라또", "2026-01-22", 37.5556, 126.9231, IeumColors.CategoryFood, "https://picsum.photos/seed/icecream/400/400"),

    // 강남 클러스터 (12개)
    Memory(38, "강남 맛집", "서울 강남구 역삼동", "스테이크 먹은 날", "2026-02-01", 37.4979, 127.0276, IeumColors.CategoryFood, "https://picsum.photos/seed/steak/400/400"),
    Memory(39, "강남 영화관", "서울 강남구 역삼동", "영화 봤어", "2026-02-01", 37.4982, 127.0279, IeumColors.CategoryFood, "https://picsum.photos/seed/movie/400/400"),
    Memory(40, "강남 쇼핑몰", "서울 강남구 역삼동", "쇼핑", "2026-02-02", 37.4976, 127.0273, IeumColors.CategoryFood, "https://picsum.photos/seed/mall/400/400"),
    Memory(41, "강남 스시", "서울 강남구 역삼동", "오마카세", "2026-02-02", 37.4985, 127.0282, IeumColors.CategoryFood, "https://picsum.photos/seed/sushi/400/400"),
    Memory(42, "강남 카페", "서울 강남구 역삼동", "커피 한잔", "2026-02-03", 37.4973, 127.0270, IeumColors.CategoryFood, "https://picsum.photos/seed/coffee1/400/400"),
    Memory(43, "강남 바", "서울 강남구 역삼동", "와인바", "2026-02-03", 37.4988, 127.0285, IeumColors.CategoryFood, "https://picsum.photos/seed/wine/400/400"),
    Memory(44, "강남 헬스장", "서울 강남구 역삼동", "운동", "2026-02-04", 37.4970, 127.0267, IeumColors.CategoryFood, "https://picsum.photos/seed/gym/400/400"),
    Memory(45, "강남 피부과", "서울 강남구 역삼동", "관리받음", "2026-02-04", 37.4991, 127.0288, IeumColors.CategoryFood, "https://picsum.photos/seed/clinic/400/400"),
    Memory(46, "강남 네일", "서울 강남구 역삼동", "네일아트", "2026-02-05", 37.4967, 127.0264, IeumColors.CategoryFood, "https://picsum.photos/seed/nail2/400/400"),
    Memory(47, "강남 미용실", "서울 강남구 역삼동", "헤어컷", "2026-02-05", 37.4994, 127.0291, IeumColors.CategoryFood, "https://picsum.photos/seed/hair/400/400"),
    Memory(48, "강남 백화점", "서울 강남구 역삼동", "명품 구경", "2026-02-06", 37.4964, 127.0261, IeumColors.CategoryFood, "https://picsum.photos/seed/luxury/400/400"),
    Memory(49, "강남 라운지", "서울 강남구 역삼동", "루프탑 바", "2026-02-06", 37.4997, 127.0294, IeumColors.CategoryFood, "https://picsum.photos/seed/lounge/400/400"),

    // 이태원 클러스터 (8개)
    Memory(50, "이태원 브런치", "서울 용산구 이태원동", "브런치 맛집", "2026-01-20", 37.5347, 126.9945, IeumColors.Primary, "https://picsum.photos/seed/brunch2/400/400"),
    Memory(51, "이태원 카페", "서울 용산구 이태원동", "분위기 좋은 카페", "2026-01-20", 37.5350, 126.9948, IeumColors.Primary, "https://picsum.photos/seed/cafe2/400/400"),
    Memory(52, "이태원 바", "서울 용산구 이태원동", "칵테일 맛집", "2026-01-21", 37.5345, 126.9942, IeumColors.Primary, "https://picsum.photos/seed/bar/400/400"),
    Memory(53, "이태원 클럽", "서울 용산구 이태원동", "클럽 나이트", "2026-01-21", 37.5352, 126.9951, IeumColors.Primary, "https://picsum.photos/seed/club2/400/400"),
    Memory(54, "이태원 타코", "서울 용산구 이태원동", "멕시칸 타코", "2026-01-22", 37.5343, 126.9939, IeumColors.Primary, "https://picsum.photos/seed/taco/400/400"),
    Memory(55, "이태원 케밥", "서울 용산구 이태원동", "터키 케밥", "2026-01-22", 37.5354, 126.9954, IeumColors.Primary, "https://picsum.photos/seed/kebab/400/400"),
    Memory(56, "이태원 펍", "서울 용산구 이태원동", "아이리쉬 펍", "2026-01-23", 37.5341, 126.9936, IeumColors.Primary, "https://picsum.photos/seed/pub/400/400"),
    Memory(57, "이태원 루프탑", "서울 용산구 이태원동", "루프탑 파티", "2026-01-23", 37.5356, 126.9957, IeumColors.Primary, "https://picsum.photos/seed/rooftop2/400/400"),

    // 남산 클러스터 (8개)
    Memory(58, "남산타워", "서울 용산구 남산공원길", "야경이 예뻤던 곳", "2026-01-25", 37.5512, 126.9882, IeumColors.Accent, "https://picsum.photos/seed/tower/400/400"),
    Memory(59, "남산 케이블카", "서울 용산구 남산공원길", "케이블카 타고 올라감", "2026-01-25", 37.5515, 126.9885, IeumColors.Accent, "https://picsum.photos/seed/cable/400/400"),
    Memory(60, "남산 산책로", "서울 용산구 남산공원길", "산책하기 좋아", "2026-01-26", 37.5509, 126.9879, IeumColors.Accent, "https://picsum.photos/seed/trail/400/400"),
    Memory(61, "남산 자물쇠", "서울 용산구 남산공원길", "자물쇠 달기", "2026-01-26", 37.5518, 126.9888, IeumColors.Accent, "https://picsum.photos/seed/lock/400/400"),
    Memory(62, "남산 전망대", "서울 용산구 남산공원길", "서울 전경", "2026-01-27", 37.5506, 126.9876, IeumColors.Accent, "https://picsum.photos/seed/view/400/400"),
    Memory(63, "남산 계단", "서울 용산구 남산공원길", "계단 운동", "2026-01-27", 37.5521, 126.9891, IeumColors.Accent, "https://picsum.photos/seed/stairs/400/400"),
    Memory(64, "남산 야경", "서울 용산구 남산공원길", "밤의 남산", "2026-01-28", 37.5503, 126.9873, IeumColors.Accent, "https://picsum.photos/seed/night2/400/400"),
    Memory(65, "남산 일출", "서울 용산구 남산공원길", "해돋이", "2026-01-28", 37.5524, 126.9894, IeumColors.Accent, "https://picsum.photos/seed/sunrise/400/400"),

    // 경복궁/광화문 클러스터 (8개)
    Memory(66, "경복궁", "서울 종로구 세종로", "한복 입고 산책", "2025-12-20", 37.5796, 126.9770, IeumColors.CategoryCulture, "https://picsum.photos/seed/palace/400/400"),
    Memory(67, "광화문", "서울 종로구 세종로", "광화문 광장", "2025-12-20", 37.5759, 126.9768, IeumColors.CategoryCulture, "https://picsum.photos/seed/gate/400/400"),
    Memory(68, "청와대", "서울 종로구 세종로", "청와대 관람", "2025-12-21", 37.5866, 126.9748, IeumColors.CategoryCulture, "https://picsum.photos/seed/bluehouse/400/400"),
    Memory(69, "국립민속박물관", "서울 종로구 세종로", "박물관 구경", "2025-12-21", 37.5816, 126.9789, IeumColors.CategoryCulture, "https://picsum.photos/seed/museum/400/400"),
    Memory(70, "삼청동", "서울 종로구 삼청동", "삼청동 거리", "2025-12-22", 37.5826, 126.9819, IeumColors.CategoryCulture, "https://picsum.photos/seed/samcheong/400/400"),
    Memory(71, "북촌한옥마을", "서울 종로구 가회동", "한옥마을", "2025-12-22", 37.5826, 126.9849, IeumColors.CategoryCulture, "https://picsum.photos/seed/hanok/400/400"),
    Memory(72, "인사동", "서울 종로구 인사동", "전통 거리", "2025-12-23", 37.5736, 126.9856, IeumColors.CategoryCulture, "https://picsum.photos/seed/insadong/400/400"),
    Memory(73, "창덕궁", "서울 종로구 와룡동", "창덕궁 후원", "2025-12-23", 37.5796, 126.9910, IeumColors.CategoryCulture, "https://picsum.photos/seed/changdeok/400/400"),

    // 잠실 클러스터 (10개)
    Memory(74, "롯데월드", "서울 송파구 잠실동", "놀이공원", "2026-01-05", 37.5111, 127.0980, IeumColors.Secondary, "https://picsum.photos/seed/lotteworld/400/400"),
    Memory(75, "석촌호수", "서울 송파구 잠실동", "호수 산책", "2026-01-05", 37.5085, 127.1025, IeumColors.Secondary, "https://picsum.photos/seed/lake/400/400"),
    Memory(76, "롯데타워", "서울 송파구 잠실동", "스카이 전망대", "2026-01-06", 37.5125, 127.1025, IeumColors.Secondary, "https://picsum.photos/seed/tower2/400/400"),
    Memory(77, "롯데몰", "서울 송파구 잠실동", "쇼핑", "2026-01-06", 37.5138, 127.1050, IeumColors.Secondary, "https://picsum.photos/seed/mall2/400/400"),
    Memory(78, "잠실야구장", "서울 송파구 잠실동", "야구 관람", "2026-01-07", 37.5120, 127.0720, IeumColors.Secondary, "https://picsum.photos/seed/baseball/400/400"),
    Memory(79, "올림픽공원", "서울 송파구 방이동", "공원 산책", "2026-01-07", 37.5209, 127.1230, IeumColors.Secondary, "https://picsum.photos/seed/olympic/400/400"),
    Memory(80, "잠실 맛집", "서울 송파구 잠실동", "삼겹살", "2026-01-08", 37.5098, 127.0958, IeumColors.Secondary, "https://picsum.photos/seed/pork/400/400"),
    Memory(81, "잠실 카페", "서울 송파구 잠실동", "커피타임", "2026-01-08", 37.5145, 127.0998, IeumColors.Secondary, "https://picsum.photos/seed/coffee2/400/400"),
    Memory(82, "잠실 영화관", "서울 송파구 잠실동", "영화 데이트", "2026-01-09", 37.5072, 127.0935, IeumColors.Secondary, "https://picsum.photos/seed/cinema/400/400"),
    Memory(83, "잠실 볼링장", "서울 송파구 잠실동", "볼링", "2026-01-09", 37.5158, 127.1018, IeumColors.Secondary, "https://picsum.photos/seed/bowling/400/400"),

    // 명동 클러스터 (7개)
    Memory(84, "명동 쇼핑", "서울 중구 명동", "화장품 쇼핑", "2026-01-10", 37.5636, 126.9869, IeumColors.Accent, "https://picsum.photos/seed/cosmetics/400/400"),
    Memory(85, "명동 맛집", "서울 중구 명동", "칼국수", "2026-01-10", 37.5642, 126.9858, IeumColors.Accent, "https://picsum.photos/seed/noodle/400/400"),
    Memory(86, "명동성당", "서울 중구 명동", "성당 구경", "2026-01-11", 37.5633, 126.9875, IeumColors.Accent, "https://picsum.photos/seed/cathedral/400/400"),
    Memory(87, "명동 길거리음식", "서울 중구 명동", "호떡", "2026-01-11", 37.5648, 126.9852, IeumColors.Accent, "https://picsum.photos/seed/hotteok/400/400"),
    Memory(88, "명동 카페", "서울 중구 명동", "밀크티", "2026-01-12", 37.5627, 126.9882, IeumColors.Accent, "https://picsum.photos/seed/milktea/400/400"),
    Memory(89, "명동 백화점", "서울 중구 명동", "롯데백화점", "2026-01-12", 37.5654, 126.9845, IeumColors.Accent, "https://picsum.photos/seed/dept/400/400"),
    Memory(90, "명동 환전", "서울 중구 명동", "환전소", "2026-01-13", 37.5621, 126.9888, IeumColors.Accent, "https://picsum.photos/seed/exchange/400/400"),

    // 여의도 클러스터 (5개)
    Memory(91, "여의도 IFC", "서울 영등포구 여의도동", "IFC몰", "2026-02-18", 37.5252, 126.9259, IeumColors.CategoryFood, "https://picsum.photos/seed/ifc/400/400"),
    Memory(92, "여의도 공원", "서울 영등포구 여의도동", "공원 산책", "2026-02-18", 37.5285, 126.9245, IeumColors.CategoryFood, "https://picsum.photos/seed/park/400/400"),
    Memory(93, "63빌딩", "서울 영등포구 여의도동", "전망대", "2026-02-19", 37.5198, 126.9405, IeumColors.CategoryFood, "https://picsum.photos/seed/63/400/400"),
    Memory(94, "여의도 맛집", "서울 영등포구 여의도동", "회식", "2026-02-19", 37.5268, 126.9232, IeumColors.CategoryFood, "https://picsum.photos/seed/dinner/400/400"),
    Memory(95, "여의도 카페", "서울 영등포구 여의도동", "강변 카페", "2026-02-20", 37.5238, 126.9272, IeumColors.CategoryFood, "https://picsum.photos/seed/rivercafe/400/400"),

    // 신촌/이대 클러스터 (5개)
    Memory(96, "신촌 맛집", "서울 서대문구 신촌동", "떡볶이 거리", "2026-01-24", 37.5558, 126.9369, IeumColors.Primary, "https://picsum.photos/seed/sinchon/400/400"),
    Memory(97, "이대 카페", "서울 서대문구 대현동", "카페거리", "2026-01-24", 37.5567, 126.9459, IeumColors.Primary, "https://picsum.photos/seed/ewha/400/400"),
    Memory(98, "신촌 노래방", "서울 서대문구 신촌동", "노래방", "2026-01-25", 37.5548, 126.9349, IeumColors.Primary, "https://picsum.photos/seed/karaoke/400/400"),
    Memory(99, "이대 쇼핑", "서울 서대문구 대현동", "옷 쇼핑", "2026-01-25", 37.5577, 126.9479, IeumColors.Primary, "https://picsum.photos/seed/clothes/400/400"),
    Memory(100, "연세대학교", "서울 서대문구 신촌동", "캠퍼스 구경", "2026-01-26", 37.5663, 126.9388, IeumColors.Primary, "https://picsum.photos/seed/yonsei/400/400")
)

/**
 * 추억 상세 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryDetailSheet(
    memory: Memory,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IeumColors.Background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IeumColors.TextSecondary.copy(alpha = 0.3f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 이미지 (실제로는 URL에서 로드)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                memory.color.copy(alpha = 0.3f),
                                memory.color.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = memory.color,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 장소명
            Text(
                text = memory.placeName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 주소
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = IeumColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = memory.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IeumColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 날짜
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = IeumColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = memory.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IeumColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 구분선
            HorizontalDivider(
                color = IeumColors.TextSecondary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 코멘트
            Text(
                text = "추억의 한마디",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = memory.comment,
                style = MaterialTheme.typography.bodyLarge,
                color = IeumColors.TextPrimary,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 액션 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* 수정 */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = IeumColors.Primary
                    ),
                    border = BorderStroke(1.dp, IeumColors.Primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("수정")
                }

                Button(
                    onClick = { /* 공유 */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IeumColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("공유")
                }
            }
        }
    }
}