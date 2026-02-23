@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

:: ============================================================
:: Schedule 앱 자동 설치 및 빌드 스크립트 (Windows)
:: - Git, Java 17, Android SDK 자동 설치
:: - 환경 변수 자동 설정
:: - 프로젝트 클론 및 APK 빌드
::
:: [중요] REPO_URL 을 실제 접근 가능한 주소로 변경하세요.
::   - 로컬 Gitea 서버: http://서버IP:포트/git/user/repo
::   - GitHub: https://github.com/user/repo
:: ============================================================

set "REPO_URL=http://127.0.0.1:34429/git/rnjswlsdlf5775-spec/Schedule"
set "BRANCH=claude/android-schedule-task-app-9UCYF"
set "PROJECT_NAME=Schedule"
set "WORK_DIR=%USERPROFILE%\AndroidProjects"
set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
set "CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"

:: ─────────────────────────────────────────────
::  관리자 권한 자동 요청
:: ─────────────────────────────────────────────
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo.
    echo  관리자 권한이 필요합니다.
    echo  UAC 팝업에서 [예]를 클릭하세요.
    echo.
    powershell -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

:: ─────────────────────────────────────────────
::  헤더
:: ─────────────────────────────────────────────
cls
echo.
echo  =====================================================
echo   Schedule Android App  ^|  자동 설치 및 빌드
echo  =====================================================
echo.

:: ─────────────────────────────────────────────
::  1단계: winget 확인
:: ─────────────────────────────────────────────
echo  [1/6] 패키지 관리자(winget) 확인...
winget --version >nul 2>&1
if !errorlevel! neq 0 (
    echo  [!] winget 을 찾을 수 없습니다.
    echo      Windows 11 / Windows 10 21H1 이상에서 지원됩니다.
    echo      Microsoft Store 에서 "앱 설치 관리자" 를 업데이트 하세요.
    echo.
    goto :error
)
for /f "tokens=*" %%v in ('winget --version 2^>nul') do echo      winget %%v 확인됨.
echo.

:: ─────────────────────────────────────────────
::  2단계: Git 설치
:: ─────────────────────────────────────────────
echo  [2/6] Git 설치 확인...
git --version >nul 2>&1
if !errorlevel! equ 0 (
    for /f "tokens=*" %%v in ('git --version 2^>nul') do echo      %%v 이미 설치됨.
) else (
    echo      Git 이 없습니다. 자동 설치 중...
    winget install -e --id Git.Git --silent --accept-package-agreements --accept-source-agreements
    if !errorlevel! neq 0 (
        echo  [오류] Git 설치 실패. 수동으로 설치하세요: https://git-scm.com/download/win
        goto :error
    )
    :: PATH 즉시 반영
    set "PATH=%PATH%;C:\Program Files\Git\cmd;C:\Program Files\Git\bin"
    echo      Git 설치 완료.
)
echo.

:: ─────────────────────────────────────────────
::  3단계: Java 17 설치
:: ─────────────────────────────────────────────
echo  [3/6] Java 17 설치 확인...
java -version >nul 2>&1
if !errorlevel! equ 0 (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set "JAVA_VER=%%v"
    )
    echo      Java !JAVA_VER! 이미 설치됨.
) else (
    echo      Java 가 없습니다. JDK 17 자동 설치 중...
    winget install -e --id Microsoft.OpenJDK.17 --silent --accept-package-agreements --accept-source-agreements
    if !errorlevel! neq 0 (
        echo      Microsoft OpenJDK 실패. Eclipse Temurin 시도 중...
        winget install -e --id EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
        if !errorlevel! neq 0 (
            echo  [오류] Java 설치 실패. 수동으로 설치하세요: https://adoptium.net
            goto :error
        )
    )
    echo      Java 17 설치 완료.
)

:: JAVA_HOME 탐색 및 설정
if not defined JAVA_HOME (
    for /d %%d in (
        "C:\Program Files\Java\jdk-17*"
        "C:\Program Files\Microsoft\jdk-17*"
        "C:\Program Files\Eclipse Adoptium\jdk-17*"
        "%ProgramFiles%\Java\jdk-17*"
    ) do (
        if exist "%%~d\bin\java.exe" (
            set "JAVA_HOME=%%~d"
            goto :java_found
        )
    )
    :: 레지스트리에서 Java 경로 찾기
    for /f "tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v JavaHome 2^>nul') do (
        if exist "%%b\bin\java.exe" set "JAVA_HOME=%%b"
    )
)
:java_found
if defined JAVA_HOME (
    setx JAVA_HOME "%JAVA_HOME%" /M >nul 2>&1
    set "PATH=%PATH%;%JAVA_HOME%\bin"
    echo      JAVA_HOME = %JAVA_HOME%
)
echo.

:: ─────────────────────────────────────────────
::  4단계: Android SDK 설치
:: ─────────────────────────────────────────────
echo  [4/6] Android SDK 설치 확인...

if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" (
    echo      Android SDK 이미 존재: %ANDROID_SDK_ROOT%
    goto :sdk_done
)

if exist "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" (
    echo      cmdline-tools 이미 설치됨.
    goto :install_sdk_packages
)

echo      Android cmdline-tools 다운로드 중 (약 100MB)...
if not exist "%ANDROID_SDK_ROOT%\cmdline-tools" mkdir "%ANDROID_SDK_ROOT%\cmdline-tools"

set "TOOLS_ZIP=%TEMP%\cmdline-tools.zip"
set "TOOLS_EXTRACT=%TEMP%\cmdline-tools-extract"

powershell -NoProfile -Command ^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; " ^
    "Write-Host '      다운로드 중...'; " ^
    "(New-Object Net.WebClient).DownloadFile('%CMDLINE_TOOLS_URL%', '%TOOLS_ZIP%')"

if !errorlevel! neq 0 (
    echo  [오류] cmdline-tools 다운로드 실패. 인터넷 연결을 확인하세요.
    goto :error
)

echo      압축 해제 중...
if exist "%TOOLS_EXTRACT%" rd /s /q "%TOOLS_EXTRACT%"
powershell -NoProfile -Command "Expand-Archive -Path '%TOOLS_ZIP%' -DestinationPath '%TOOLS_EXTRACT%' -Force"

:: sdkmanager 는 cmdline-tools\latest 위치에 있어야 함
if exist "%ANDROID_SDK_ROOT%\cmdline-tools\latest" rd /s /q "%ANDROID_SDK_ROOT%\cmdline-tools\latest"
move "%TOOLS_EXTRACT%\cmdline-tools" "%ANDROID_SDK_ROOT%\cmdline-tools\latest" >nul 2>&1
if !errorlevel! neq 0 (
    echo  [오류] cmdline-tools 이동 실패
    goto :error
)

del /q "%TOOLS_ZIP%" 2>nul
rd /s /q "%TOOLS_EXTRACT%" 2>nul

echo      cmdline-tools 설치 완료.

:install_sdk_packages
echo      SDK 패키지 설치 중 (platform-tools, build-tools, android-34)...
set "SDKMANAGER=%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat"

:: 라이선스 자동 동의
echo y | "%SDKMANAGER%" --licenses >nul 2>&1

call "%SDKMANAGER%" --install "platform-tools" "platforms;android-34" "build-tools;34.0.0" >nul 2>&1
if !errorlevel! neq 0 (
    echo  [오류] SDK 패키지 설치 실패
    goto :error
)
echo      SDK 패키지 설치 완료.

:sdk_done
echo.

:: ─────────────────────────────────────────────
::  환경 변수 설정 (영구 저장)
:: ─────────────────────────────────────────────
setx ANDROID_HOME "%ANDROID_SDK_ROOT%" /M >nul 2>&1
setx ANDROID_SDK_ROOT "%ANDROID_SDK_ROOT%" /M >nul 2>&1

:: 현재 세션에도 반영
set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
set "PATH=%PATH%;%ANDROID_SDK_ROOT%\platform-tools;%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin"
echo      환경 변수 설정 완료.
echo      ANDROID_HOME = %ANDROID_SDK_ROOT%
echo.

:: ─────────────────────────────────────────────
::  5단계: 프로젝트 클론
:: ─────────────────────────────────────────────
echo  [5/6] 프로젝트 다운로드...

if not exist "%WORK_DIR%" mkdir "%WORK_DIR%"

if exist "%WORK_DIR%\%PROJECT_NAME%\.git" (
    echo      기존 폴더 발견. 최신 코드로 업데이트 중...
    pushd "%WORK_DIR%\%PROJECT_NAME%"
    git pull origin "%BRANCH%"
    popd
) else (
    echo      %REPO_URL%
    echo      브랜치: %BRANCH%
    echo.
    git clone --branch "%BRANCH%" "%REPO_URL%" "%WORK_DIR%\%PROJECT_NAME%"
    if !errorlevel! neq 0 (
        echo.
        echo  [오류] 프로젝트 클론 실패.
        echo.
        echo  확인 사항:
        echo    1. REPO_URL 이 올바른지 확인 (스크립트 상단에서 수정)
        echo    2. 로컬 Gitea 서버라면 해당 서버가 실행 중인지 확인
        echo    3. 인터넷/네트워크 연결 상태 확인
        echo    4. Git 인증이 필요한 경우 크리덴셜 입력
        goto :error
    )
)
echo      프로젝트 다운로드 완료.
echo.

:: ─────────────────────────────────────────────
::  6단계: Gradle 빌드
:: ─────────────────────────────────────────────
echo  [6/6] APK 빌드 시작...
pushd "%WORK_DIR%\%PROJECT_NAME%"

if not exist "gradlew.bat" (
    echo  [오류] gradlew.bat 파일을 찾을 수 없습니다.
    echo      프로젝트가 올바르게 다운로드 되었는지 확인하세요.
    popd
    goto :error
)

echo      의존성 다운로드 및 빌드 중 (처음 실행 시 5~10분 소요)...
echo.
call gradlew.bat assembleDebug
if !errorlevel! neq 0 (
    echo.
    echo  [오류] Gradle 빌드 실패.
    echo.
    echo  해결 방법:
    echo    - "gradlew.bat clean" 후 재시도
    echo    - JAVA_HOME, ANDROID_HOME 환경 변수 확인
    echo    - 인터넷 연결 확인 (Gradle 의존성 다운로드 필요)
    popd
    goto :error
)

popd

:: ─────────────────────────────────────────────
::  완료
:: ─────────────────────────────────────────────
echo.
echo  =====================================================
echo   빌드 성공!
echo  =====================================================
echo.
echo   APK 위치:
echo   %WORK_DIR%\%PROJECT_NAME%\app\build\outputs\apk\debug\app-debug.apk
echo.
echo   기기/에뮬레이터에 설치하려면:
echo   adb install "%WORK_DIR%\%PROJECT_NAME%\app\build\outputs\apk\debug\app-debug.apk"
echo.
echo  =====================================================
echo.
goto :end

:: ─────────────────────────────────────────────
::  오류 처리 (창 닫힘 방지)
:: ─────────────────────────────────────────────
:error
echo.
echo  =====================================================
echo   스크립트가 오류로 종료되었습니다.
echo   위의 오류 메시지를 확인하세요.
echo  =====================================================
echo.

:end
echo  아무 키나 누르면 창이 닫힙니다...
pause >nul
