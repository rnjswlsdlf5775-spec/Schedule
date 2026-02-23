# Windows 빠른 시작 가이드

## 5분 안에 시작하기

### 1단계: 필수 프로그램 설치 (10분)

#### ✅ Java 17 설치
```
1. https://www.oracle.com/java/technologies/downloads/#java17 접속
2. "Windows x64 Installer" 다운로드 및 설치
3. 명령 프롬프트에서 확인:
   java -version
```

#### ✅ Android Studio 설치
```
1. https://developer.android.com/studio 접속
2. 설치 프로그램 다운로드 및 실행
3. 마법사 따라 설치 (기본값 선택)
4. 첫 실행 시 SDK 자동 설치됨
```

#### ✅ Git 설치
```
1. https://git-scm.com/download/win 접속
2. 인스톨러 다운로드 및 실행
3. 기본 설정으로 설치
```

### 2단계: 환경 변수 설정 (5분)

#### Windows 시스템 환경 변수 설정

**방법 A: 자동 설정 (권장)**
```powershell
# PowerShell(관리자)에서 실행
$javaPath = "C:\Program Files\Java\jdk-17.0.x"
$androidPath = "$env:LOCALAPPDATA\Android\Sdk"

[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, "User")
[Environment]::SetEnvironmentVariable("ANDROID_HOME", $androidPath, "User")

Write-Host "환경 변수 설정 완료!"
```

**방법 B: 수동 설정**
1. `Windows 키 + Pause` 누르기
2. "고급 시스템 설정" 클릭
3. "환경 변수" 버튼 클릭
4. "사용자 변수" → "새로 만들기"
   - 변수 이름: `JAVA_HOME`
   - 변수 값: `C:\Program Files\Java\jdk-17.0.x`
5. 같은 방식으로 `ANDROID_HOME` 추가
   - 변수 값: `C:\Users\[사용자명]\AppData\Local\Android\Sdk`
6. PC 재부팅

### 3단계: 프로젝트 다운로드 및 빌드 (2분)

```cmd
REM 1. 다운로드 폴더로 이동
cd %USERPROFILE%\Downloads

REM 2. 스크립트 실행 (모든 과정 자동)
setup-and-build.bat
```

**스크립트가 하는 일:**
- ✅ Git으로 프로젝트 다운로드
- ✅ 필수 설정 확인
- ✅ Gradle로 APK 빌드
- ✅ 완료 메시지 표시

### 4단계: 앱 설치 및 실행 (1분)

**에뮬레이터가 있을 경우:**
```cmd
adb install Schedule\app\build\outputs\apk\debug\app-debug.apk
```

**실제 기기가 있을 경우:**
```cmd
REM 1. 기기 연결 (USB 디버깅 모드 활성화)
REM 2. 연결 확인
adb devices

REM 3. 설치
adb install Schedule\app\build\outputs\apk\debug\app-debug.apk
```

---

## 자주 묻는 질문

### Q1: "ANDROID_HOME이 설정되지 않았다"는 오류가 나요
```cmd
REM PowerShell(관리자)에서 실행:
$androidPath = "$env:LOCALAPPDATA\Android\Sdk"
[Environment]::SetEnvironmentVariable("ANDROID_HOME", $androidPath, "User")

REM 또는 Android Studio 설치 확인
REM C:\Users\[사용자명]\AppData\Local\Android\Sdk 폴더 확인
```

### Q2: "adb 명령을 찾을 수 없다"는 오류
```cmd
REM adb 직접 경로로 실행:
"C:\Users\[사용자명]\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
```

### Q3: 에뮬레이터는 어떻게 실행하나요?
```cmd
REM Android Studio에서:
1. 상단 메뉴 → Device Manager
2. "Create Virtual Device" 클릭
3. 기본값으로 생성 및 실행
```

### Q4: 빌드가 실패했어요
```cmd
REM 캐시 삭제 후 다시 시도:
cd Schedule
gradlew.bat clean
gradlew.bat assembleDebug
```

### Q5: 기존 코드를 수정했는데 다시 빌드하고 싶어요
```cmd
cd Schedule
gradlew.bat clean assembleDebug
```

---

## 파일 위치

| 항목 | 경로 |
|------|------|
| **Java** | `C:\Program Files\Java\jdk-17.x.x` |
| **Android SDK** | `%LOCALAPPDATA%\Android\Sdk` |
| **프로젝트** | `%USERPROFILE%\Downloads\Schedule` |
| **APK** | `Schedule\app\build\outputs\apk\debug\app-debug.apk` |

---

## 빠른 명령어 레퍼런스

```cmd
REM 프로젝트 업데이트
cd Schedule
git pull origin claude/android-schedule-task-app-9UCYF

REM 빌드만 수행
gradlew.bat assembleDebug

REM 기기에 설치
adb install app\build\outputs\apk\debug\app-debug.apk

REM 앱 실행
adb shell am start -n com.schedule.app/.ui.MainActivity

REM 로그 보기
adb logcat

REM 기기 연결 확인
adb devices
```

---

## 다음 단계

빌드 완료 후:
1. 에뮬레이터/기기에서 "스케줄 관리" 앱 실행
2. 오늘 탭에서 할 일 추가
3. 달력 탭에서 일정 추가 및 알람 설정
4. 설정 탭에서 테마 변경

**문제가 있으면 README.md의 "문제 해결" 섹션을 참고하세요.**
