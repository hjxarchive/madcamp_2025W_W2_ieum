package com.ieum.data.repository

import com.ieum.domain.model.CoursePlace
import com.ieum.domain.model.DateCategory
import com.ieum.domain.model.DateCourse
import com.ieum.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepositoryImpl @Inject constructor() : RecommendationRepository {

    private val courses = listOf(
        DateCourse(
            id = 1L,
            title = "성수동 감성 카페 투어",
            description = "인스타 감성 넘치는 성수동 핫플레이스 코스! 사진 찍기 좋은 곳들로 구성했어요.",
            category = DateCategory.CAFE,
            duration = "약 4시간",
            estimatedCost = 45000,
            places = listOf(
                CoursePlace(1L, "어니언 성수", "베이커리카페", "서울 성동구 아차산로9길 8", "1시간", 15000),
                CoursePlace(2L, "대림창고", "복합문화공간", "서울 성동구 성수이로 78", "1시간 30분", 10000),
                CoursePlace(3L, "카페 할아버지공장", "카페", "서울 성동구 연무장5길 7", "1시간 30분", 20000)
            )
        ),
        DateCourse(
            id = 2L,
            title = "홍대 맛집 탐방",
            description = "홍대 핫한 맛집들을 돌아보는 미식 코스",
            category = DateCategory.FOOD,
            duration = "약 3시간",
            estimatedCost = 55000,
            places = listOf(
                CoursePlace(1L, "연남동 파스타집", "이탈리안", "서울 마포구 연남로 23", "1시간 30분", 35000),
                CoursePlace(2L, "밀크티 전문점", "디저트", "서울 마포구 와우산로 35", "1시간", 12000),
                CoursePlace(3L, "수제 아이스크림", "디저트", "서울 마포구 어울마당로 42", "30분", 8000)
            )
        ),
        DateCourse(
            id = 3L,
            title = "한강 피크닉 데이트",
            description = "여의도 한강공원에서 즐기는 로맨틱 피크닉",
            category = DateCategory.TRAVEL,
            duration = "약 5시간",
            estimatedCost = 30000,
            places = listOf(
                CoursePlace(1L, "편의점 장보기", "마트", "여의도 한강공원 입구", "30분", 20000),
                CoursePlace(2L, "한강공원 피크닉", "야외", "서울 영등포구 여의동로 330", "3시간", 0),
                CoursePlace(3L, "치맥 배달", "치킨", "한강공원 내", "1시간 30분", 10000)
            )
        ),
        DateCourse(
            id = 4L,
            title = "이태원 와인바 투어",
            description = "분위기 좋은 이태원 와인바에서 로맨틱한 밤을",
            category = DateCategory.DRINK,
            duration = "약 4시간",
            estimatedCost = 80000,
            places = listOf(
                CoursePlace(1L, "와인앤모어", "와인바", "서울 용산구 이태원로 200", "2시간", 50000),
                CoursePlace(2L, "루프탑바", "바", "서울 용산구 이태원로 210", "2시간", 30000)
            )
        )
    )

    override fun getTodayRecommendation(): Flow<DateCourse> = flowOf(courses.first())

    override fun getPopularCourses(): Flow<List<DateCourse>> = flowOf(courses.drop(1))

    override fun getCoursesByCategory(category: DateCategory): Flow<List<DateCourse>> =
        flowOf(courses.filter { it.category == category })

    override suspend fun addToSchedule(courseId: Long) {
        // TODO: Implement when backend is ready
    }
}
