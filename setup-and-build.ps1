# Windows PowerShell용 Android 스케줄 앱 셋업 및 빌드 스크립트
# 요구사항: Git, Java 17, Android SDK가 설치되어 있어야 함

param(
    [string]$Action = "full"  # full, debug, release
)

$PROJECT_NAME = "Schedule"
$REPO_URL = "http://127.0.0.1:34429/git/rnjswlsdlf5775-spec/Schedule"
$BRANCH = "claude/android-schedule-task-app-9UCYF"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "$PROJECT_NAME 안드로이드 앱 셋업 및 빌드" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Git 설치 확인
Write-Host "[1/4] Git 설치 확인..." -ForegroundColor Yellow
$gitCheck = git --version 2>$null
if ($null -eq $gitCheck) {
    Write-Host "오류: Git이 설치되지 않았습니다." -ForegroundColor Red
    Write-Host "https://git-scm.com/download/win 에서 설치하세요." -ForegroundColor Red
    exit 1
}
Write-Host "Git 설치 확인됨." -ForegroundColor Green
Write-Host ""

# 2. Java 설치 확인
Write-Host "[2/4] Java 17 설치 확인..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Java 설치 확인됨: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "오류: Java가 설치되지 않았습니다." -ForegroundColor Red
    Write-Host "https://www.oracle.com/java/technologies/downloads/#java17 에서 JDK 17을 설치하세요." -ForegroundColor Red
    exit 1
}
Write-Host ""

# 3. 프로젝트 클론 또는 업데이트
Write-Host "[3/4] 프로젝트 다운로드..." -ForegroundColor Yellow
if (Test-Path $PROJECT_NAME) {
    Write-Host "폴더 '$PROJECT_NAME'이(가) 이미 존재합니다." -ForegroundColor Cyan
    Write-Host "기존 폴더를 사용합니다." -ForegroundColor Cyan
    Set-Location $PROJECT_NAME
    git pull origin $BRANCH
} else {
    git clone --branch $BRANCH $REPO_URL $PROJECT_NAME
    if ($LASTEXITCODE -ne 0) {
        Write-Host "오류: 프로젝트 클론 실패" -ForegroundColor Red
        exit 1
    }
    Set-Location $PROJECT_NAME
}
Write-Host "프로젝트 다운로드 완료." -ForegroundColor Green
Write-Host ""

# 4. Gradle 빌드
Write-Host "[4/4] Gradle 빌드 시작..." -ForegroundColor Yellow
Write-Host ""

# gradlew.bat 파일이 없으면 생성
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "Gradle wrapper 생성 중..." -ForegroundColor Cyan
    & gradle wrapper
}

# 빌드 타입별 실행
switch ($Action.ToLower()) {
    "debug" {
        Write-Host "▶ Debug APK 빌드 중..." -ForegroundColor Cyan
        & .\gradlew.bat assembleDebug
    }
    "release" {
        Write-Host "▶ Release APK 빌드 중..." -ForegroundColor Cyan
        & .\gradlew.bat assembleRelease
    }
    default {
        Write-Host "▶ Debug APK 빌드 중..." -ForegroundColor Cyan
        & .\gradlew.bat assembleDebug
    }
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "오류: 빌드 실패" -ForegroundColor Red
    Write-Host "다음을 확인하세요:" -ForegroundColor Yellow
    Write-Host "  - ANDROID_HOME 환경 변수 설정 여부" -ForegroundColor Yellow
    Write-Host "  - SDK 및 필수 도구 설치 여부" -ForegroundColor Yellow
    Write-Host "  - 인터넷 연결" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "빌드 완료!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "생성된 APK 위치:" -ForegroundColor Cyan
Write-Host "app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
Write-Host ""
Write-Host "다음 명령어로 에뮬레이터/기기에 설치할 수 있습니다:" -ForegroundColor Cyan
Write-Host "adb install app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
Write-Host ""
