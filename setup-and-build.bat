@echo off
REM Windows용 Android 스케줄 앱 셋업 및 빌드 스크립트
REM 요구사항: Git, Java 17, Android SDK가 설치되어 있어야 함

setlocal enabledelayedexpansion
set "PROJECT_NAME=Schedule"
set "REPO_URL=http://127.0.0.1:34429/git/rnjswlsdlf5775-spec/Schedule"
set "BRANCH=claude/android-schedule-task-app-9UCYF"

echo.
echo ========================================
echo %PROJECT_NAME% 안드로이드 앱 셋업 및 빌드
echo ========================================
echo.

REM 1. Git 설치 확인
echo [1/4] Git 설치 확인...
git --version >nul 2>&1
if !errorlevel! neq 0 (
    echo 오류: Git이 설치되지 않았습니다.
    echo https://git-scm.com/download/win 에서 설치하세요.
    exit /b 1
)
echo Git 설치 확인됨.
echo.

REM 2. Java 설치 확인
echo [2/4] Java 17 설치 확인...
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo 오류: Java가 설치되지 않았습니다.
    echo https://www.oracle.com/java/technologies/downloads/#java17 에서 JDK 17을 설치하세요.
    exit /b 1
)
for /f "tokens=2" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%i
echo Java %JAVA_VERSION% 설치 확인됨.
echo.

REM 3. 프로젝트 클론
echo [3/4] 프로젝트 다운로드...
if exist "%PROJECT_NAME%" (
    echo 폴더 "%PROJECT_NAME%"이(가) 이미 존재합니다.
    echo 기존 폴더를 사용합니다.
    cd "%PROJECT_NAME%"
    git pull origin "%BRANCH%"
) else (
    git clone --branch "%BRANCH%" "%REPO_URL%" "%PROJECT_NAME%"
    if !errorlevel! neq 0 (
        echo 오류: 프로젝트 클론 실패
        exit /b 1
    )
    cd "%PROJECT_NAME%"
)
echo 프로젝트 다운로드 완료.
echo.

REM 4. Gradle 빌드
echo [4/4] Gradle 빌드 시작...
echo.

REM gradlew.bat 파일이 없으면 생성
if not exist "gradlew.bat" (
    echo Gradle wrapper 생성 중...
    call gradle wrapper
)

REM 디버그 빌드
echo ▶ Debug APK 빌드 중...
call gradlew.bat assembleDebug
if !errorlevel! neq 0 (
    echo.
    echo 오류: 빌드 실패
    echo 다음을 확인하세요:
    echo  - ANDROID_HOME 환경 변수 설정 여부
    echo  - SDK 및 필수 도구 설치 여부
    echo  - 인터넷 연결
    exit /b 1
)

echo.
echo ========================================
echo 빌드 완료!
echo ========================================
echo.
echo 생성된 APK 위치:
echo app\build\outputs\apk\debug\app-debug.apk
echo.
echo 다음 명령어로 에뮬레이터/기기에 설치할 수 있습니다:
echo adb install app\build\outputs\apk\debug\app-debug.apk
echo.

pause
