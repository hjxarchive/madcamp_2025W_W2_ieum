# 이음 (Ieum) - 커플 데이팅 앱

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-2024.12-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Min_SDK-26-green?style=flat-square" />
  <img src="https://img.shields.io/badge/Target_SDK-35-blue?style=flat-square" />
</p>

## 📱 소개

**이음**은 커플들을 위한 종합 데이팅 앱입니다. "서로의 일상이 이어진 이음"이라는 콘셉트로, 연인과 함께 추억을 기록하고, 일정을 공유하고, 데이트를 계획할 수 있습니다.

## ✨ 주요 기능

### 1. 추억 아카이브 📸
- 사진 저장 및 위치 기반 이미지 지도 갤러리
- 한 줄 코멘트 기능
- 데이트 코스 기록 및 저장
- 홈 화면 위젯으로 추억 사진 띄우기

### 2. 일정 & 디데이 📅
- 데이팅 일정 공유 (스마트폰 캘린더 연동)
- 기념일 디데이 관리 + 특별 테마 적용
- 서로의 일정 기반 데이트 가능 시간 매칭

### 3. 커플 프로필 & 취향 💕
- "서로가 꾸며주는" 상대방 프로필
- 커플 MBTI 및 취향 분석
- 선물 취향 기록 + 위시리스트

### 4. 데이트 코스 추천 🗺️
- 실제 장소 데이터 기반 코스 추천 (예상 금액 포함)
- 성향별 카테고리: 술, 맛집, 게임, 문화생활, 여행
- 분위기 좋은 카페 추천
- 친구에게 데이트 코스 공유

### 5. 커플 채팅 💬
- 커플 전용 채팅
- 일정/취향/버킷리스트 공유

### 6. 재정 관리 💰
- 데이트 통장 소비내역 기록
- 월간 데이트 비용 한도 설정
- 카테고리별 소비 분석

### 7. 버킷리스트 ✅
- 함께 이루고 싶은 목표 리스트 관리
- 진행률 추적 및 완료 축하 효과

## 🛠️ 기술 스택

- **언어**: Kotlin 2.0
- **UI**: Jetpack Compose + Material3
- **아키텍처**: MVVM + Clean Architecture
- **의존성 주입**: Hilt (확장 예정)
- **비동기 처리**: Coroutines + Flow
- **네비게이션**: Navigation Compose

## 📂 프로젝트 구조

```
app/src/main/java/com/ieum/
├── MainActivity.kt
├── presentation/
│   ├── theme/
│   │   ├── Theme.kt          # 앱 테마 및 컬러
│   │   ├── Type.kt           # 타이포그래피
│   │   └── Shape.kt          # Shape 정의
│   ├── navigation/
│   │   └── MainNavigation.kt # 하단 네비게이션
│   └── feature/
│       ├── onboarding/       # 온보딩/MBTI 테스트
│       ├── dashboard/        # 대시보드
│       ├── calendar/         # 캘린더/일정
│       ├── memory/           # 지도갤러리/추억
│       ├── chat/             # 채팅
│       ├── profile/          # 마이페이지
│       ├── finance/          # 재정 관리
│       ├── bucket/           # 버킷리스트
│       └── recommend/        # 데이트 추천
```

## 🚀 시작하기

### 요구사항
- Android Studio Ladybug (2024.2) 이상
- JDK 17 이상
- Android SDK 35

### 빌드 방법
1. 프로젝트 클론
```bash
git clone https://github.com/your-repo/ieum.git
```

2. Android Studio에서 프로젝트 열기

3. Gradle Sync 실행

4. 앱 실행

## 📱 화면 구성

| 온보딩 | 대시보드 | 캘린더 |
|:---:|:---:|:---:|
| MBTI/취향 테스트 | 바로가기 메뉴 | 일정 & 디데이 |

| 지도갤러리 | 채팅 | 마이페이지 |
|:---:|:---:|:---:|
| 추억 아카이브 | 커플 채팅 | 프로필 & MBTI |

| 재정관리 | 버킷리스트 | 데이트추천 |
|:---:|:---:|:---:|
| 소비 분석 | 목표 관리 | 코스 추천 |

## 🎨 디자인 시스템

### 컬러 팔레트
- **Primary**: Coral Pink (#FF8A80)
- **Secondary**: Lavender (#B39DDB)
- **Accent**: Mint Green (#80CBC4)

### 타이포그래피
- Pretendard 폰트 (권장)
- Material3 타이포그래피 적용

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

<p align="center">
  Made with 💕 for couples
</p>
