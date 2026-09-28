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
echo   Starting ForKnight Backend Server (Port 8080)
echo ========================================================
echo Selecting profile:
echo - 1: PostgreSQL (requires PostgreSQL running on port 5432)
echo - 2: Standalone Dev Mode (embedded H2, zero setup needed)
echo.

set "JAR_PATH=forknight-server\target\forknight-server-1.0.0-SNAPSHOT.jar"

set /p PROFILE="Choose profile (1: PostgreSQL, 2: Standalone Dev H2) [Default=2]: "
if "%PROFILE%"=="1" (
    echo Starting server with PostgreSQL profile...
    if exist "%JAR_PATH%" (
        java -jar "%JAR_PATH%"
    ) else (
        call mvn -pl forknight-server spring-boot:run
    )
) else (
    echo Starting server with Standalone Dev (H2) profile...
    if exist "%JAR_PATH%" (
        java -jar "%JAR_PATH%" --spring.profiles.active=dev
    ) else (
        call mvn -pl forknight-server spring-boot:run "-Dspring-boot.run.profiles=dev"
    )
)
pause
