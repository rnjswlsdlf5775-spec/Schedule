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

cls
echo.
echo  =====================================================
echo   Schedule Android App  ^|  에뮬레이터 실행
echo  =====================================================
echo.

rem ─────────────────────────────────────────────
rem  APK 파일 확인
rem ─────────────────────────────────────────────
echo  [확인] APK 파일 확인...
if not exist "%APK_PATH%" (
    echo.
    echo  [오류] APK 파일을 찾을 수 없습니다:
    echo         %APK_PATH%
    echo.
    echo  setup-and-build.bat 를 먼저 실행하여 앱을 빌드하세요.
    echo.
    goto :error
)
echo       APK 확인 완료.
echo.

rem ─────────────────────────────────────────────
rem  JAVA_HOME 탐색 (블록 밖에서 독립 실행)
rem  참고: :: 주석은 if/for 블록 안에서 사용 불가 → rem 사용
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
    echo  [오류] JAVA_HOME 이 설정되지 않았습니다.
    echo      setup-and-build.bat 를 먼저 실행하여 Java 를 설치하세요.
    goto :error
)

set "PATH=%PATH%;%JAVA_HOME%\bin"
set "PATH=%PATH%;%ANDROID_SDK_ROOT%\emulator;%ANDROID_SDK_ROOT%\platform-tools"
set "JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2,TLSv1.3"

if not exist "%ADB%" (
    echo  [오류] ADB 를 찾을 수 없습니다.
    echo      setup-and-build.bat 를 먼저 실행하여 SDK 를 설치하세요.
    goto :error
)
echo       Java : %JAVA_HOME%
echo       ADB  : 확인 완료
echo.

rem ─────────────────────────────────────────────
rem  1단계: emulator 패키지 설치
rem ─────────────────────────────────────────────
echo  [1/4] Android Emulator 확인...
if not exist "%EMULATOR%" (
    echo       emulator 가 없습니다. 설치 중 (약 300MB)...
    call "%SDKMANAGER%" --sdk_root="%ANDROID_SDK_ROOT%" --no_https "emulator"
    if !errorlevel! neq 0 (
        echo  [오류] emulator 설치 실패
        goto :error
    )
    echo       emulator 설치 완료.
) else (
    echo       emulator 이미 설치됨.
)
echo.

rem ─────────────────────────────────────────────
rem  2단계: 시스템 이미지 설치
rem ─────────────────────────────────────────────
echo  [2/4] Android 34 시스템 이미지 확인...
if not exist "%ANDROID_SDK_ROOT%\system-images\android-34\google_apis\x86_64\" (
    echo       시스템 이미지가 없습니다. 설치 중 (약 1GB, 처음만 필요)...
    call "%SDKMANAGER%" --sdk_root="%ANDROID_SDK_ROOT%" --no_https "%SYSTEM_IMAGE%"
    if !errorlevel! neq 0 (
        echo  [오류] 시스템 이미지 설치 실패
        echo       인터넷 연결과 디스크 여유 공간 (2GB 이상) 을 확인하세요.
        goto :error
    )
    echo       시스템 이미지 설치 완료.
) else (
    echo       시스템 이미지 이미 설치됨.
)
echo.

rem ─────────────────────────────────────────────
rem  3단계: AVD 생성 (최초 1회만)
rem  참고: echo no | call BAT 는 CMD 에서 불안정 → call 없이 직접 실행
rem ─────────────────────────────────────────────
echo  [3/4] AVD 가상 기기 확인...
"%EMULATOR%" -list-avds 2>nul | findstr /i "%AVD_NAME%" >nul
if !errorlevel! neq 0 (
    echo       AVD "%AVD_NAME%" 생성 중...
    echo no | "%AVDMANAGER%" create avd --name "%AVD_NAME%" --package "%SYSTEM_IMAGE%" --device "pixel_4" --force
    if !errorlevel! neq 0 (
        echo  [오류] AVD 생성 실패
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

rem -gpu swiftshader_indirect : HAXM/Hyper-V 없이도 동작
rem -no-snapshot              : 스냅샷 없이 깨끗하게 시작
rem -no-audio                 : 오디오 비활성화 (빠른 시작)
start "" "%EMULATOR%" -avd "%AVD_NAME%" -gpu swiftshader_indirect -no-snapshot -no-audio

echo       에뮬레이터 창이 열립니다. 부팅까지 1~3분 소요됩니다...
echo.

rem ─────────────────────────────────────────────
rem  부팅 대기
rem  참고: set /p =<nul 는 변수명 없어 문법 오류 → echo 로 대체
rem ─────────────────────────────────────────────
set BOOT_STATUS=0
set WAIT_COUNT=0

:wait_device
timeout /t 5 /nobreak >nul
"%ADB%" devices 2>nul | findstr /i "emulator" >nul
if !errorlevel! neq 0 (
    set /a WAIT_COUNT+=1
    if !WAIT_COUNT! gtr 24 (
        echo  [오류] 에뮬레이터 연결 타임아웃 (2분 초과)
        echo       에뮬레이터 창이 열렸는지 확인하세요.
        goto :error
    )
    echo       에뮬레이터 연결 대기 중... (!WAIT_COUNT!/24)
    goto :wait_device
)

:wait_boot
timeout /t 5 /nobreak >nul
set BOOT_STATUS=0
"%ADB%" shell getprop sys.boot_completed 2>nul | findstr /c:"1" >nul
if !errorlevel! equ 0 goto :boot_done
set /a WAIT_COUNT+=1
if !WAIT_COUNT! gtr 36 (
    echo  [오류] 부팅 타임아웃 (3분 초과). 에뮬레이터 창을 확인하세요.
    goto :error
)
echo       부팅 대기 중... (!WAIT_COUNT!/36)
goto :wait_boot

:boot_done
echo       에뮬레이터 부팅 완료!
echo.

rem ─────────────────────────────────────────────
rem  APK 설치 및 앱 실행
rem ─────────────────────────────────────────────
:install_apk
echo       APK 설치 중...
"%ADB%" install -r "%APK_PATH%"
if !errorlevel! neq 0 (
    echo  [오류] APK 설치 실패
    goto :error
)
echo       APK 설치 완료.
echo.

echo       앱 실행 중...
"%ADB%" shell am start -n "%PACKAGE_NAME%/.ui.MainActivity" -a android.intent.action.MAIN
echo.
echo  =====================================================
echo   Schedule 앱이 에뮬레이터에서 실행 중입니다!
echo  =====================================================
echo.
echo   APK: %APK_PATH%
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
