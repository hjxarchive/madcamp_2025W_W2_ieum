package com.ieum.data.dto

import com.google.gson.annotations.SerializedName

// ==================== Auth ====================

data class GoogleLoginRequest(
    val idToken: String
)

data class AuthResponse(
    val accessToken: String,
    val user: UserDto
)

data class MessageResponse(
    val message: String
)

// ==================== User ====================

data class UserDto(
    val id: String,
    val email: String,
    val name: String?,
    val nickname: String?,
    val profileImage: String?,
    val birthday: String?,
    val gender: String?,
    val coupleId: String?,
    val mbtiType: String?,
    val isActive: Boolean
)

data class UserUpdateRequest(
    val name: String? = null,
    val nickname: String? = null,
    val profileImage: String? = null,
    val birthday: String? = null,
    val gender: String? = null
)

// ==================== Couple ====================

data class InviteCodeResponse(
    val inviteCode: String,
    val expiresAt: String
)

data class CoupleJoinRequest(
    val inviteCode: String
)

data class CoupleResponse(
    val id: String,
    val anniversary: String?,
    val partner: UserDto?,
    val createdAt: String
)

data class CoupleUpdateRequest(
    val anniversary: String?
)

// ==================== MBTI ====================

data class MbtiQuestionsResponse(
    val questions: List<MbtiQuestionDto>
)

data class MbtiQuestionDto(
    val id: Int,
    val question: String,
    val optionA: String,
    val optionB: String,
    val dimension: String
)

data class MbtiSubmitRequest(
    val answers: Map<String, String>
)

data class MbtiResultResponse(
    val mbtiType: String,
    val details: Map<String, Int>
)

data class MbtiCoupleResultResponse(
    val myMbti: String?,
    val partnerMbti: String?,
    val compatibility: CompatibilityDto?
)

data class CompatibilityDto(
    val score: Int,
    val description: String,
    val strengths: List<String>,
    val challenges: List<String>
)

// ==================== Memory ====================

data class MemoryRequest(
    val title: String,
    val content: String?,
    val date: String,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val images: List<String>? = null
)

data class MemoryDto(
    val id: String,
    val title: String,
    val content: String?,
    val date: String,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val images: List<String>?,
    val createdById: String,
    val createdAt: String
)

data class MemoriesResponse(
    val memories: List<MemoryDto>,
    val totalCount: Int,
    val page: Int,
    val size: Int
)

// ==================== Event ====================

data class EventRequest(
    val title: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String,
    val isAllDay: Boolean = false,
    val location: String? = null,
    val reminderMinutes: Int? = null,
    val repeat: String = "NONE"
)

data class EventDto(
    val id: String,
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val isAllDay: Boolean,
    val location: String?,
    val reminderMinutes: Int?,
    val repeat: String,
    val createdById: String,
    val createdAt: String
)

data class EventsResponse(
    val events: List<EventDto>
)

// ==================== D-Day ====================

data class DDaysResponse(
    val ddays: List<DDayDto>
)

data class DDayDto(
    val title: String,
    val date: String,
    val dday: Int,
    val type: String
)

// ==================== Expense ====================

data class ExpenseRequest(
    val amount: Double,
    val category: String,
    val description: String?,
    val date: String,
    val paidBy: String
)

data class ExpenseDto(
    val id: String,
    val amount: Double,
    val category: String,
    val description: String?,
    val date: String,
    val paidBy: String,
    val createdById: String,
    val createdAt: String
)

data class ExpensesResponse(
    val expenses: List<ExpenseDto>,
    val totalAmount: Double,
    val totalCount: Int,
    val page: Int,
    val size: Int
)

// ==================== Budget ====================

data class BudgetRequest(
    val totalBudget: Double,
    val categoryBudgets: Map<String, Double>
)

data class BudgetResponse(
    val id: String,
    val yearMonth: String,
    val totalBudget: Double,
    val categoryBudgets: Map<String, Double>,
    val totalSpent: Double,
    val categorySpent: Map<String, Double>,
    val remainingBudget: Double
)

// ==================== Bucket ====================

data class BucketRequest(
    val title: String,
    val description: String? = null,
    val category: String? = null
)

data class BucketUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val isCompleted: Boolean? = null,
    val completedImage: String? = null
)

data class BucketDto(
    val id: String,
    val title: String,
    val description: String?,
    val category: String?,
    val isCompleted: Boolean,
    val completedAt: String?,
    val completedImage: String?,
    val createdById: String,
    val createdAt: String
)

data class BucketsResponse(
    val buckets: List<BucketDto>,
    val totalCount: Int,
    val completedCount: Int
)

// ==================== Chat ====================

data class ChatRoomResponse(
    val coupleId: String,
    val partnerId: String,
    val partnerName: String?,
    val partnerProfileImage: String?,
    val lastMessage: ChatMessageDto?,
    val unreadCount: Int
)

data class ChatMessageRequest(
    val content: String?,
    val type: String = "TEXT",
    val imageUrl: String? = null
)

data class ChatMessageDto(
    val id: String,
    val senderId: String,
    val content: String?,
    val type: String,
    val imageUrl: String?,
    val isRead: Boolean,
    val readAt: String?,
    val createdAt: String
)

data class ChatMessagesResponse(
    val messages: List<ChatMessageDto>,
    val totalCount: Int,
    val page: Int,
    val size: Int
)

// ==================== File ====================

data class FilePresignRequest(
    val filename: String,
    val contentType: String
)

data class FilePresignResponse(
    val fileId: String,
    val uploadUrl: String,
    val fileUrl: String,
    val expiresIn: Int
)

data class FileInfoResponse(
    val fileId: String,
    val url: String,
    val filename: String,
    val contentType: String
)

// ==================== Recommendation ====================

data class RecommendationRequest(
    val locationAddress: String?,
    val locationLat: Double?,
    val locationLng: Double?,
    val date: String,
    val preferences: RecommendationPreferences?
)

data class RecommendationPreferences(
    val budget: String?,
    val mood: String?,
    val categories: List<String>?
)

data class RecommendationDto(
    val id: String,
    val status: String,
    val locationAddress: String?,
    val locationLat: Double?,
    val locationLng: Double?,
    val date: String,
    val preferences: RecommendationPreferences?,
    val result: RecommendationResultDto?,
    val feedback: RecommendationFeedbackDto?,
    val savedEventId: String?,
    val createdAt: String,
    val completedAt: String?
)

data class RecommendationResultDto(
    val title: String,
    val description: String,
    val places: List<RecommendationPlaceDto>,
    val estimatedTime: String?,
    val estimatedCost: String?
)

data class RecommendationPlaceDto(
    val name: String,
    val category: String,
    val address: String?
)

data class RecommendationFeedbackDto(
    val rating: Int,
    val comment: String?,
    val submittedAt: String
)

data class RecommendationsResponse(
    val recommendations: List<RecommendationDto>,
    val totalCount: Int,
    val page: Int,
    val size: Int
)

data class FeedbackRequest(
    val rating: Int,
    val comment: String?,
    val saveAsEvent: Boolean = false
)

// ==================== Error ====================

data class ErrorResponse(
    val status: Int,
    val message: String
)
