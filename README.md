# 스케줄 관리 안드로이드 앱

달력과 함께 일정을 관리하고, 오늘 할 일을 추적하며, 정확한 시간에 알람을 받을 수 있는 안드로이드 앱입니다.

## 기능

### 📅 달력 탭
- CalendarView를 이용한 직관적인 날짜 선택
- 선택한 날짜의 일정 목록 표시
- 일정 추가/수정/삭제
- 일정 완료 체크

### ✅ 오늘 할 일 탭
- 오늘의 할 일 목록 관리
- 우선순위 설정 (높음/보통/낮음, 색상으로 구분)
- 완료 진행률 표시
- 할 일 추가/수정/삭제

### 🔔 알람 시스템
- AlarmManager로 정확한 시간 알림
- 알람 옵션: 정시, 5분 전, 10분 전, 15분 전, 30분 전, 1시간 전
- 기기 재부팅 후 알람 자동 재등록
- 알림 센터에서 확인/닫기

### 🎨 테마 설정
- 라이트 모드 / 다크 모드 / 시스템 기본값 선택
- DataStore에 설정 저장
- Material Design 3 적용

## 요구사항

### Windows 환경 설정

#### 1. Java 17 (JDK) 설치
- [Oracle JDK 17 다운로드](https://www.oracle.com/java/technologies/downloads/#java17)
- 설치 후 다음 명령어로 확인:
```cmd
java -version
```

#### 2. Android SDK 설치
- [Android Studio 다운로드](https://developer.android.com/studio)
- Android Studio 설치 후 필요한 SDK 구성요소 설치:
  - Android SDK Platform 34
  - Android SDK Build-Tools 34.0.0
  - Android Emulator (옵션)

#### 3. 환경 변수 설정
Windows 환경 변수에 다음을 추가:

**JAVA_HOME** (필수)
```
C:\Program Files\Java\jdk-17.0.x
```

**ANDROID_HOME** (필수)
```
C:\Users\[사용자명]\AppData\Local\Android\Sdk
```

**PATH** (선택)
```
%JAVA_HOME%\bin
%ANDROID_HOME%\tools
%ANDROID_HOME%\platform-tools
```

#### 4. Git 설치
- [Git for Windows 다운로드](https://git-scm.com/download/win)

## 빌드 및 실행 방법

### 옵션 1: Batch 스크립트 (간단함)

1. 명령 프롬프트(cmd)에서 프로젝트 폴더 이동:
```cmd
cd C:\Users\[사용자명]\Downloads
```

2. 스크립트 실행:
```cmd
setup-and-build.bat
```

### 옵션 2: PowerShell 스크립트 (더 상세한 정보)

1. PowerShell 열기 (관리자 권한)

2. 실행 정책 변경 (처음 한 번만):
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

3. 스크립트 실행:
```powershell
.\setup-and-build.ps1
```

또는 빌드 타입 지정:
```powershell
.\setup-and-build.ps1 -Action debug      # Debug 빌드
.\setup-and-build.ps1 -Action release    # Release 빌드
```

### 옵션 3: 수동 빌드

```cmd
# 1. 프로젝트 다운로드
git clone --branch claude/android-schedule-task-app-9UCYF http://127.0.0.1:34429/git/rnjswlsdlf5775-spec/Schedule
cd Schedule

# 2. Debug APK 빌드
gradlew.bat assembleDebug

# 3. Release APK 빌드
gradlew.bat assembleRelease
```

## 빌드 결과

빌드 완료 후 APK 파일 위치:
- **Debug**: `app\build\outputs\apk\debug\app-debug.apk`
- **Release**: `app\build\outputs\apk\release\app-release.apk`

## 에뮬레이터/기기에 설치

### 방법 1: adb 명령어

```cmd
# 에뮬레이터/기기가 연결되어 있는지 확인
adb devices

# APK 설치
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 방법 2: Android Studio에서 직접 실행

```cmd
# Android Studio에서 프로젝트 열고, Run 버튼 클릭
gradlew.bat installDebug
```

## 프로젝트 구조

```
Schedule/
├── app/
│   ├── src/main/
│   │   ├── java/com/schedule/app/
│   │   │   ├── ui/                    # UI 화면
│   │   │   │   ├── MainActivity       # 메인 화면
│   │   │   │   ├── calendar/          # 달력 탭
│   │   │   │   ├── today/             # 오늘 할 일 탭
│   │   │   │   └── settings/          # 설정 탭
│   │   │   ├── data/                  # 데이터 계층
│   │   │   │   ├── db/                # Room Database
│   │   │   │   ├── model/             # 데이터 모델
│   │   │   │   └── repository/        # Repository 패턴
│   │   │   ├── alarm/                 # 알람 시스템
│   │   │   ├── util/                  # 유틸리티
│   │   │   └── ScheduleApplication    # 앱 클래스
│   │   └── res/                       # 리소스
│   │       ├── layout/                # UI 레이아웃
│   │       ├── drawable/              # 벡터 드로어블
│   │       ├── values/                # 문자열, 색상, 스타일
│   │       └── navigation/            # 네비게이션 그래프
│   └── build.gradle                   # 의존성 설정
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

## 기술 스택

- **Language**: Kotlin
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room ORM
- **UI Framework**: Android Jetpack (ViewBinding, Navigation, BottomNavigationView)
- **State Management**: LiveData + Coroutines
- **Settings Storage**: DataStore Preferences
- **Alarm**: AlarmManager + BroadcastReceiver
- **Material Design**: Material Components 3

## 주요 의존성

- AndroidX Core, AppCompat, Constraint Layout
- Navigation Components
- Room Database
- ViewModel & LiveData
- Coroutines
- Material Components
- DataStore Preferences
- WorkManager

## 문제 해결

### 빌드 오류: "ANDROID_HOME is not set"
```cmd
# 환경 변수 확인
echo %ANDROID_HOME%

# 설정하지 않았다면:
setx ANDROID_HOME "C:\Users\[사용자명]\AppData\Local\Android\Sdk"
```

### Gradle 캐시 삭제
```cmd
gradlew.bat clean
gradlew.bat assembleDebug
```

### SDK 업데이트 필요
```cmd
# Android SDK 버전 확인
gradlew.bat dependencies
```

## 라이선스

MIT License

## 연락처

문의사항이 있으시면 이슈를 등록해주세요.
