@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

rem ============================================================
rem  Schedule 앱 에뮬레이터 실행 스크립트 (Windows)
rem  [사전 조건] setup-and-build.bat 를 먼저 실행하여 APK 를 빌드하세요.
rem ============================================================

set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
set "APK_PATH=%USERPROFILE%\AndroidProjects\Schedule\app\build\outputs\apk\debug\app-debug.apk"
set "AVD_NAME=Schedule_AVD"
set "PACKAGE_NAME=com.schedule.app"
set "SYSTEM_IMAGE=system-images;android-34;google_apis;x86_64"
set "SDKMANAGER=%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat"
set "AVDMANAGER=%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\avdmanager.bat"
set "EMULATOR=%ANDROID_SDK_ROOT%\emulator\emulator.exe"
set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"

rem sdkmanager 네트워크 설정 (TLS + 타임아웃)
set "JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2,TLSv1.3 -Dsun.net.client.defaultConnectTimeout=30000 -Dsun.net.client.defaultReadTimeout=120000"

cls
echo.
echo  =====================================================
echo   Schedule Android App  ^|  에뮬레이터 실행
echo  =====================================================
echo.
echo  [안내] 처음 실행 시 에뮬레이터 다운로드에 수십 분이 소요될 수 있습니다.
echo         CMD 창을 닫지 말고 기다려 주세요.
echo.

rem ─────────────────────────────────────────────
rem  APK 파일 확인
rem ─────────────────────────────────────────────
echo  [확인] APK 파일 확인 중...
if not exist "%APK_PATH%" (
    echo.
    echo  [오류] APK 파일을 찾을 수 없습니다.
    echo         경로: %APK_PATH%
    echo.
    echo  해결: setup-and-build.bat 를 먼저 실행하여 앱을 빌드하세요.
    goto :error
)
echo       OK - APK 존재 확인.
echo.

rem ─────────────────────────────────────────────
rem  JAVA_HOME 탐색 (블록 밖에서 경로별 독립 실행)
rem ─────────────────────────────────────────────
if defined JAVA_HOME goto :java_ok

for /d %%d in ("C:\Program Files\Java\jdk-17*") do (
    if exist "%%~d\bin\java.exe" set "JAVA_HOME=%%~d"
)
if defined JAVA_HOME goto :java_ok

for /d %%d in ("C:\Program Files\Microsoft\jdk-17*") do (
    if exist "%%~d\bin\java.exe" set "JAVA_HOME=%%~d"
)
if defined JAVA_HOME goto :java_ok

for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
    if exist "%%~d\bin\java.exe" set "JAVA_HOME=%%~d"
)
if defined JAVA_HOME goto :java_ok

for /f "tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v JavaHome 2^>nul') do (
    if exist "%%b\bin\java.exe" set "JAVA_HOME=%%b"
)

:java_ok
if not defined JAVA_HOME (
    echo  [오류] JAVA_HOME 을 찾을 수 없습니다.
    echo         setup-and-build.bat 를 먼저 실행하여 Java 를 설치하세요.
    goto :error
)
set "PATH=%PATH%;%JAVA_HOME%\bin"
set "PATH=%PATH%;%ANDROID_SDK_ROOT%\emulator;%ANDROID_SDK_ROOT%\platform-tools"

if not exist "%ADB%" (
    echo  [오류] ADB 를 찾을 수 없습니다.
    echo         setup-and-build.bat 를 먼저 실행하여 Android SDK 를 설치하세요.
    goto :error
)
echo  [확인] Java  : %JAVA_HOME%
echo  [확인] ADB   : OK
echo.

rem ─────────────────────────────────────────────
rem  라이선스 파일 사전 생성 (sdkmanager 무한 대기 방지)
rem ─────────────────────────────────────────────
if not exist "%ANDROID_SDK_ROOT%\licenses" mkdir "%ANDROID_SDK_ROOT%\licenses"
(
    echo 24333f8a63b6825ea9c5514f83c2829b004d1fee
    echo 8933bad161af4178b1185d1a37fbf41ea5269c55
    echo d56f5187479451eabf01fb78af6dfcb131a6481e
) > "%ANDROID_SDK_ROOT%\licenses\android-sdk-license"
(echo 84831b9409646a918e30573bab4c9c91346d8abd) > "%ANDROID_SDK_ROOT%\licenses\android-sdk-preview-license"
(echo 33b6a2b64607f11b759f320ef9dff4ae5c47d97a) > "%ANDROID_SDK_ROOT%\licenses\google-gdk-license"
(echo d975f751698a77b662f1254ddbeed3901e976f5a) > "%ANDROID_SDK_ROOT%\licenses\intel-android-extra-license"

rem ─────────────────────────────────────────────
rem  1단계: Android Emulator 설치 (~300MB)
rem ─────────────────────────────────────────────
echo  [1/4] Android Emulator 확인...
if not exist "%EMULATOR%" (
    echo       emulator 가 없습니다. 설치 중 (약 300MB, 수분 소요)...
    echo       [진행 상황이 아래에 표시됩니다. 창을 닫지 마세요]
    echo.
    call "%SDKMANAGER%" --sdk_root="%ANDROID_SDK_ROOT%" --no_https --verbose "emulator"
    if !errorlevel! neq 0 (
        echo.
        echo  [오류] emulator 설치 실패.
        echo         인터넷 연결 및 디스크 여유 공간을 확인하세요.
        goto :error
    )
    echo.
    echo       emulator 설치 완료.
) else (
    echo       emulator 이미 설치됨.
)
echo.

rem ─────────────────────────────────────────────
rem  2단계: Android 34 시스템 이미지 설치 (~1GB)
rem ─────────────────────────────────────────────
echo  [2/4] Android 34 시스템 이미지 확인...
if not exist "%ANDROID_SDK_ROOT%\system-images\android-34\google_apis\x86_64\" (
    echo       시스템 이미지가 없습니다.
    echo       설치 중 (약 1GB, 처음 한 번만 필요 - 수십 분 소요)...
    echo       [진행 상황이 아래에 표시됩니다. 창을 닫지 마세요]
    echo.
    call "%SDKMANAGER%" --sdk_root="%ANDROID_SDK_ROOT%" --no_https --verbose "%SYSTEM_IMAGE%"
    if !errorlevel! neq 0 (
        echo.
        echo  [오류] 시스템 이미지 설치 실패.
        echo         인터넷 연결 및 디스크 여유 공간 (2GB 이상) 을 확인하세요.
        goto :error
    )
    echo.
    echo       시스템 이미지 설치 완료.
) else (
    echo       시스템 이미지 이미 설치됨.
)
echo.

rem ─────────────────────────────────────────────
rem  3단계: AVD 생성 (최초 1회만)
rem ─────────────────────────────────────────────
echo  [3/4] AVD 가상 기기 확인...
"%EMULATOR%" -list-avds 2>nul | findstr /i "%AVD_NAME%" >nul
if !errorlevel! neq 0 (
    echo       AVD "%AVD_NAME%" 생성 중...
    echo no | "%AVDMANAGER%" create avd --name "%AVD_NAME%" --package "%SYSTEM_IMAGE%" --device "pixel_4" --force
    if !errorlevel! neq 0 (
        echo  [오류] AVD 생성 실패.
        goto :error
    )
    echo       AVD "%AVD_NAME%" 생성 완료.
) else (
    echo       AVD "%AVD_NAME%" 이미 존재.
)
echo.

rem ─────────────────────────────────────────────
rem  4단계: 에뮬레이터 실행
rem ─────────────────────────────────────────────
echo  [4/4] 에뮬레이터 시작...
echo.

rem 이미 실행 중인 에뮬레이터 확인
"%ADB%" devices 2>nul | findstr /i "emulator" >nul
if !errorlevel! equ 0 (
    echo       이미 실행 중인 에뮬레이터를 사용합니다.
    goto :install_apk
)

rem 에뮬레이터 백그라운드 실행
rem   -gpu swiftshader_indirect : HAXM/Hyper-V 없이도 작동 (소프트웨어 렌더링)
rem   -no-snapshot              : 스냅샷 없이 깨끗하게 시작
rem   -no-audio                 : 오디오 비활성화 (빠른 시작)
start "" "%EMULATOR%" -avd "%AVD_NAME%" -gpu swiftshader_indirect -no-snapshot -no-audio

rem 에뮬레이터 프로세스가 실제로 시작됐는지 10초 후 확인
echo       에뮬레이터 창이 별도로 열립니다...
timeout /t 10 /nobreak >nul
tasklist /fi "imagename eq emulator.exe" 2>nul | findstr /i "emulator.exe" >nul
if !errorlevel! neq 0 (
    echo.
    echo  [오류] 에뮬레이터 프로세스가 시작되지 않았습니다.
    echo.
    echo  가능한 원인:
    echo    - emulator.exe 경로 문제: %EMULATOR%
    echo    - 가상화(Hyper-V/WHPX) 미지원 - BIOS 에서 VT-x 활성화 필요
    echo    - 이전 단계가 실패하여 AVD 가 올바르게 생성되지 않음
    echo.
    echo  수동 확인: Android Studio 에서 AVD Manager 를 열어 에뮬레이터를 테스트하세요.
    goto :error
)
echo       에뮬레이터 프로세스 시작 확인.
echo.

rem ─────────────────────────────────────────────
rem  에뮬레이터 ADB 연결 대기 (최대 2분)
rem ─────────────────────────────────────────────
echo       ADB 연결 대기 중 (최대 2분)...
set DEV_COUNT=0

:wait_device
timeout /t 5 /nobreak >nul
"%ADB%" devices 2>nul | findstr /i "emulator" >nul
if !errorlevel! equ 0 goto :device_ready
set /a DEV_COUNT+=1
if !DEV_COUNT! gtr 24 (
    echo.
    echo  [오류] ADB 연결 타임아웃 (2분 초과).
    echo         에뮬레이터 창이 열렸는지 확인하세요.
    goto :error
)
echo       ADB 연결 대기 중... (!DEV_COUNT!/24)
goto :wait_device

:device_ready
echo       ADB 연결 완료.
echo.

rem ─────────────────────────────────────────────
rem  Android 부팅 완료 대기 (최대 5분)
rem  DEV_COUNT 와 별도 카운터 사용
rem ─────────────────────────────────────────────
echo       Android 부팅 완료 대기 중 (최대 5분)...
set BOOT_COUNT=0

:wait_boot
timeout /t 5 /nobreak >nul
"%ADB%" shell getprop sys.boot_completed 2>nul | findstr /c:"1" >nul
if !errorlevel! equ 0 goto :boot_done
set /a BOOT_COUNT+=1
if !BOOT_COUNT! gtr 60 (
    echo.
    echo  [오류] Android 부팅 타임아웃 (5분 초과).
    echo         에뮬레이터 창을 직접 확인하세요.
    goto :error
)
echo       부팅 대기 중... (!BOOT_COUNT!/60)
goto :wait_boot

:boot_done
echo       Android 부팅 완료!
echo.

rem ─────────────────────────────────────────────
rem  APK 설치 및 앱 실행
rem ─────────────────────────────────────────────
:install_apk
echo       APK 설치 중...
"%ADB%" install -r "%APK_PATH%"
if !errorlevel! neq 0 (
    echo  [오류] APK 설치 실패.
    echo         에뮬레이터가 완전히 부팅되지 않았을 수 있습니다. 잠시 후 다시 시도하세요.
    goto :error
)
echo       APK 설치 완료.
echo.

echo       Schedule 앱 실행 중...
"%ADB%" shell am start -n "%PACKAGE_NAME%/.ui.MainActivity" -a android.intent.action.MAIN
if !errorlevel! neq 0 (
    echo  [경고] 앱 실행 명령 전송 실패. 에뮬레이터에서 앱을 직접 실행하세요.
)

echo.
echo  =====================================================
echo   Schedule 앱이 에뮬레이터에서 실행 중입니다!
echo  =====================================================
echo.
echo   APK: %APK_PATH%
echo.
echo   [참고] 에뮬레이터 창이 별도 창으로 열려 있습니다.
echo          에뮬레이터를 닫으면 앱도 종료됩니다.
echo          앱을 재실행하려면 이 스크립트를 다시 실행하세요.
echo.
goto :end

rem ─────────────────────────────────────────────
rem  오류 처리
rem ─────────────────────────────────────────────
:error
echo.
echo  =====================================================
echo   오류가 발생했습니다. 위의 메시지를 확인하세요.
echo  =====================================================
echo.

:end
echo  아무 키나 누르면 창이 닫힙니다...
pause >nul
