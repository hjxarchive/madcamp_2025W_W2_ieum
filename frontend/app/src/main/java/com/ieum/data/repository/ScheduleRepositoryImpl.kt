package com.ieum.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ieum.data.api.EventService
import com.ieum.data.dto.EventRequest
import com.ieum.data.websocket.ChatWebSocketClient
import com.ieum.data.websocket.ScheduleDto
import com.ieum.data.websocket.ScheduleSyncMessage
import com.ieum.domain.model.Anniversary
import com.ieum.domain.model.Schedule
import com.ieum.domain.repository.ScheduleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val Context.scheduleDataStore by preferencesDataStore(name = "schedule_prefs")

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val eventService: EventService,
    private val chatWebSocketClient: ChatWebSocketClient,
    @ApplicationContext private val context: Context
) : ScheduleRepository {

    private val schedules = MutableStateFlow<List<Schedule>>(emptyList())
    private val anniversaries = MutableStateFlow<List<Anniversary>>(emptyList())
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    private val ANNIVERSARIES_KEY = stringPreferencesKey("anniversaries_json")

    init {
        // 앱 시작 시 저장된 기념일 불러오기
        coroutineScope.launch {
            loadAnniversariesFromDataStore()
        }
    }

    private suspend fun loadAnniversariesFromDataStore() {
        try {
            val prefs = context.scheduleDataStore.data.first()
            val json = prefs[ANNIVERSARIES_KEY]
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<AnniversaryDto>>() {}.type
                val dtos: List<AnniversaryDto> = gson.fromJson(json, type)
                val loadedAnniversaries = dtos.map { dto ->
                    Anniversary(
                        id = dto.id,
                        title = dto.title,
                        emoji = dto.emoji,
                        dDay = dto.dDay,
                        date = LocalDate.parse(dto.date)
                    )
                }
                anniversaries.value = loadedAnniversaries
                Log.d("ScheduleRepository", "Loaded ${loadedAnniversaries.size} anniversaries from DataStore")
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to load anniversaries from DataStore", e)
        }
    }

    private suspend fun saveAnniversariesToDataStore() {
        try {
            val dtos = anniversaries.value.map { anniversary ->
                AnniversaryDto(
                    id = anniversary.id,
                    title = anniversary.title,
                    emoji = anniversary.emoji,
                    dDay = anniversary.dDay,
                    date = anniversary.date.toString()
                )
            }
            val json = gson.toJson(dtos)
            context.scheduleDataStore.edit { prefs ->
                prefs[ANNIVERSARIES_KEY] = json
            }
            Log.d("ScheduleRepository", "Saved ${dtos.size} anniversaries to DataStore")
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to save anniversaries to DataStore", e)
        }
    }

    // DataStore 저장용 DTO (LocalDate를 String으로 변환)
    private data class AnniversaryDto(
        val id: Long,
        val title: String,
        val emoji: String,
        val dDay: String,
        val date: String
    )

    // 로컬 ID (hashCode) -> 서버 ID (UUID) 매핑
    private val scheduleIdMap = mutableMapOf<Int, String>()

    // Note: refresh() is called when user navigates to schedule screen
    // Not in init to avoid calling API before login

    private suspend fun refreshSchedules() {
        try {
            val now = LocalDateTime.now()
            val startDate = now.minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val endDate = now.plusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val response = eventService.getEvents(startDate, endDate)
            val scheduleList = response.events.map { dto ->
                val localId = dto.id.hashCode()
                // ID 매핑 저장
                scheduleIdMap[localId] = dto.id

                Schedule(
                    id = localId,
                    title = dto.title,
                    date = LocalDate.parse(dto.startDate.substring(0, 10)),
                    time = dto.startDate.substring(11, 16),
                    colorHex = "#FF6B9D",
                    isShared = true,
                    description = dto.description
                )
            }
            schedules.value = scheduleList
            Log.d("ScheduleRepository", "Loaded ${scheduleList.size} schedules from API")

            // Load D-days for anniversaries
            try {
                val dDaysResponse = eventService.getDDays()
                val anniversaryList = dDaysResponse.ddays.mapIndexed { index, dto ->
                    Anniversary(
                        id = index.toLong(),
                        title = dto.title,
                        emoji = if (dto.type == "anniversary") "💕" else "📅",
                        dDay = if (dto.dday >= 0) "D-${dto.dday}" else "D+${-dto.dday}",
                        date = LocalDate.parse(dto.date)
                    )
                }
                anniversaries.value = anniversaryList
            } catch (e: Exception) {
                Log.e("ScheduleRepository", "Failed to load D-days", e)
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to load schedules", e)
        }
    }

    override fun getSchedules(): Flow<List<Schedule>> = schedules

    override fun getSchedulesForMonth(yearMonth: YearMonth): Flow<List<Schedule>> {
        // Refresh if needed
        coroutineScope.launch {
            try {
                val startDate = yearMonth.atDay(1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val response = eventService.getEvents(startDate, endDate)
                val newSchedules = response.events.map { dto ->
                    val localId = dto.id.hashCode()
                    // ID 매핑 저장
                    scheduleIdMap[localId] = dto.id

                    Schedule(
                        id = localId,
                        title = dto.title,
                        date = LocalDate.parse(dto.startDate.substring(0, 10)),
                        time = dto.startDate.substring(11, 16),
                        colorHex = "#FF6B9D",
                        isShared = true,
                        description = dto.description
                    )
                }
                // Merge with existing schedules
                val existingIds = newSchedules.map { it.id }.toSet()
                schedules.value = schedules.value.filter { it.id !in existingIds } + newSchedules
            } catch (e: Exception) {
                Log.e("ScheduleRepository", "Failed to load schedules for month", e)
            }
        }

        return schedules.map { list ->
            list.filter { YearMonth.from(it.date) == yearMonth }
        }
    }

    override fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> =
        schedules.map { list ->
            list.filter { it.date == date }
        }

    override fun getAnniversaries(): Flow<List<Anniversary>> {
        // schedules와 anniversaries를 합쳐서 반환
        return kotlinx.coroutines.flow.combine(schedules, anniversaries) { scheduleList, anniversaryList ->
            val today = LocalDate.now()

            // schedules에서 Anniversary로 변환
            val fromSchedules = scheduleList
                .filter { !it.date.isBefore(today) }
                .map { schedule ->
                    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, schedule.date).toInt()
                    Anniversary(
                        id = schedule.id.toLong(),
                        title = schedule.title,
                        emoji = "📅",
                        dDay = if (daysUntil == 0) "D-Day" else "D-$daysUntil",
                        date = schedule.date
                    )
                }

            // anniversaries에서 미래 기념일만 필터링
            val fromAnniversaries = anniversaryList
                .filter { !it.date.isBefore(today) }
                .map { anniversary ->
                    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, anniversary.date).toInt()
                    anniversary.copy(
                        dDay = if (daysUntil == 0) "D-Day" else "D-$daysUntil"
                    )
                }

            // 합쳐서 날짜순 정렬
            (fromSchedules + fromAnniversaries)
                .distinctBy { "${it.title}_${it.date}" } // 중복 제거
                .sortedBy { it.date }
        }
    }

    override suspend fun addSchedule(schedule: Schedule) {
        // 낙관적 업데이트: 즉시 UI에 표시
        val tempSchedule = schedule.copy(id = System.currentTimeMillis().toInt())
        schedules.value = schedules.value + tempSchedule
        Log.d("ScheduleRepository", "Added schedule optimistically: ${tempSchedule.title}")

        try {
            val timeStr = if (schedule.time.isNullOrEmpty()) "00:00" else schedule.time
            val request = EventRequest(
                title = schedule.title,
                description = schedule.description,
                startDate = "${schedule.date}T${timeStr}:00",
                endDate = "${schedule.date}T${timeStr}:00",
                isAllDay = schedule.time.isNullOrEmpty(),
                location = null,
                reminderMinutes = null,
                repeat = "NONE"
            )
            val response = eventService.createEvent(request)
            Log.d("ScheduleRepository", "Created event on server: ${response.id}")

            // 서버 ID로 업데이트 (임시 ID를 실제 ID로 교체)
            val localId = response.id.hashCode()
            // ID 매핑 저장
            scheduleIdMap[localId] = response.id

            schedules.value = schedules.value.map {
                if (it.id == tempSchedule.id) {
                    it.copy(id = localId)
                } else it
            }

            // WebSocket을 통해 파트너에게 추가 이벤트 전송
            val scheduleDto = ScheduleDto(
                id = response.id,
                title = schedule.title,
                date = schedule.date.toString(),
                time = schedule.time,
                colorHex = schedule.colorHex,
                description = schedule.description
            )
            chatWebSocketClient.sendScheduleSyncEvent("ADDED", scheduleDto)
            Log.d("ScheduleRepository", "📤 Sent add sync event for: ${schedule.title}")
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to add schedule to server", e)
            // 에러 발생 시 낙관적 업데이트는 유지 (로컬에만 존재)
        }
    }

    override suspend fun addAnniversary(anniversary: Anniversary) {
        // 중복 체크 (같은 제목과 날짜가 있으면 추가하지 않음)
        val isDuplicate = anniversaries.value.any {
            it.title == anniversary.title && it.date == anniversary.date
        }
        if (!isDuplicate) {
            anniversaries.value = anniversaries.value + anniversary
            saveAnniversariesToDataStore()
            Log.d("ScheduleRepository", "Added anniversary: ${anniversary.title} on ${anniversary.date}")
        } else {
            Log.d("ScheduleRepository", "Anniversary already exists: ${anniversary.title} on ${anniversary.date}")
        }
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        try {
            val timeStr = if (schedule.time.isNullOrEmpty()) "00:00" else schedule.time
            val request = EventRequest(
                title = schedule.title,
                description = schedule.description,
                startDate = "${schedule.date}T${timeStr}:00",
                endDate = "${schedule.date}T${timeStr}:00",
                isAllDay = schedule.time.isNullOrEmpty(),
                location = null,
                reminderMinutes = null,
                repeat = "NONE"
            )
            // Find original ID (we need to store mapping)
            eventService.updateEvent(schedule.id.toString(), request)
            Log.d("ScheduleRepository", "Updated event: ${schedule.id}")

            // WebSocket을 통해 파트너에게 수정 이벤트 전송
            val scheduleDto = ScheduleDto(
                id = schedule.id.toString(),
                title = schedule.title,
                date = schedule.date.toString(),
                time = schedule.time,
                colorHex = schedule.colorHex,
                description = schedule.description
            )
            chatWebSocketClient.sendScheduleSyncEvent("UPDATED", scheduleDto)
            Log.d("ScheduleRepository", "📤 Sent update sync event for: ${schedule.title}")

            refreshSchedules()
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to update schedule", e)
            schedules.value = schedules.value.map {
                if (it.id == schedule.id) schedule else it
            }
        }
    }

    override suspend fun deleteSchedule(scheduleId: Int) {
        // 서버 ID(UUID) 가져오기
        val serverId = scheduleIdMap[scheduleId]
        if (serverId == null) {
            Log.e("ScheduleRepository", "❌ Cannot find server ID for schedule: $scheduleId")
            // 로컬에서만 삭제
            schedules.value = schedules.value.filter { it.id != scheduleId }
            return
        }

        // 삭제할 스케줄 정보를 먼저 저장 (동기화 이벤트 전송용)
        val scheduleToDelete = schedules.value.find { it.id == scheduleId }

        // 낙관적 업데이트: 즉시 UI에서 제거
        schedules.value = schedules.value.filter { it.id != scheduleId }
        Log.d("ScheduleRepository", "✅ Deleted schedule optimistically: $scheduleId (serverId: $serverId)")

        try {
            eventService.deleteEvent(serverId)
            Log.d("ScheduleRepository", "✅ Deleted event on server: $serverId")

            // 매핑 제거
            scheduleIdMap.remove(scheduleId)

            // WebSocket을 통해 파트너에게 삭제 이벤트 전송 (서버가 자동으로 브로드캐스트하므로 필요 없을 수 있음)
            scheduleToDelete?.let { schedule ->
                val scheduleDto = ScheduleDto(
                    id = serverId,
                    title = schedule.title,
                    date = schedule.date.toString(),
                    time = schedule.time,
                    colorHex = schedule.colorHex,
                    description = schedule.description
                )
                chatWebSocketClient.sendScheduleSyncEvent("DELETED", scheduleDto)
                Log.d("ScheduleRepository", "📤 Sent delete sync event for: ${schedule.title}")
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "❌ Failed to delete schedule on server: ${e.message}", e)
            // 에러 발생해도 낙관적 업데이트 유지 (이미 삭제됨)
        }
    }

    override suspend fun refresh() {
        refreshSchedules()
    }

    /**
     * WebSocket을 통한 일정 동기화 이벤트 처리
     * 백엔드에서 id는 UUID(String)로 전송되므로 hashCode()로 Int 변환
     */
    override fun handleScheduleSync(message: ScheduleSyncMessage) {
        Log.d("ScheduleRepository", "📨 Handling schedule sync: ${message.eventType} - ${message.schedule.title}")
        Log.d("ScheduleRepository", "Schedule ID (UUID): ${message.schedule.id}")

        // UUID String을 Int로 변환 (기존 ID 체계와 호환)
        val scheduleId = message.schedule.id.hashCode()

        when (message.eventType) {
            com.ieum.data.websocket.ScheduleEventType.ADDED -> {
                // 일정 추가됨
                // ID 매핑 저장
                scheduleIdMap[scheduleId] = message.schedule.id

                val newSchedule = Schedule(
                    id = scheduleId,
                    title = message.schedule.title,
                    date = LocalDate.parse(message.schedule.date),
                    time = message.schedule.time ?: "00:00",
                    colorHex = message.schedule.colorHex ?: "#FF6B9D",
                    isShared = true,
                    description = message.schedule.description
                )

                // 중복 체크 후 추가
                val existingIds = schedules.value.map { it.id }.toSet()
                if (newSchedule.id !in existingIds) {
                    schedules.value = schedules.value + newSchedule
                    Log.d("ScheduleRepository", "✅ Added schedule via WebSocket: ${newSchedule.title}")
                } else {
                    Log.d("ScheduleRepository", "⚠️ Schedule already exists (duplicate): ${newSchedule.title}")
                }
            }

            com.ieum.data.websocket.ScheduleEventType.UPDATED -> {
                // 일정 수정됨
                // ID 매핑 업데이트
                scheduleIdMap[scheduleId] = message.schedule.id

                val updatedSchedule = Schedule(
                    id = scheduleId,
                    title = message.schedule.title,
                    date = LocalDate.parse(message.schedule.date),
                    time = message.schedule.time ?: "00:00",
                    colorHex = message.schedule.colorHex ?: "#FF6B9D",
                    isShared = true,
                    description = message.schedule.description
                )

                schedules.value = schedules.value.map {
                    if (it.id == updatedSchedule.id) updatedSchedule else it
                }
                Log.d("ScheduleRepository", "✅ Updated schedule via WebSocket: ${updatedSchedule.title}")
            }

            com.ieum.data.websocket.ScheduleEventType.DELETED -> {
                // 일정 삭제됨
                schedules.value = schedules.value.filter { it.id != scheduleId }
                // 매핑 제거
                scheduleIdMap.remove(scheduleId)
                Log.d("ScheduleRepository", "✅ Deleted schedule via WebSocket: ${message.schedule.title}")
            }
        }
    }
}
