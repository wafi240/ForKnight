@echo off
setlocal

if exist "%USERPROFILE%\.forknight\tools\jdk-21.0.3+9" (
    set "JAVA_HOME=%USERPROFILE%\.forknight\tools\jdk-21.0.3+9"
    set "PATH=%USERPROFILE%\.forknight\tools\jdk-21.0.3+9\bin;%PATH%"
)
if exist "%USERPROFILE%\.forknight\tools\apache-maven-3.9.6" (
    set "MAVEN_HOME=%USERPROFILE%\.forknight\tools\apache-maven-3.9.6"
    set "PATH=%USERPROFILE%\.forknight\tools\apache-maven-3.9.6\bin;%PATH%"
)

echo ========================================================
echo   Building ForKnight Multi-Module Project
echo ========================================================
echo Java Home:  %JAVA_HOME%
call java -version
echo.

call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo [!] Build failed. Please check error output above.
    pause
    exit /b %errorlevel%
)
echo.
echo [✓] Build completed successfully!
pause
