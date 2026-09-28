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
echo   Launching ForKnight JavaFX Desktop Client
echo ========================================================
call mvn -pl forknight-client javafx:run
pause
