@echo off
echo Starting ForKnight Project (Server and Client)...
set MAVEN_HOME=C:\Users\HP\OneDrive\Desktop\ForKnight\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%

echo 1. Starting Backend Server in a new window...
start "ForKnight Server" cmd /c "cd forknight-server && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo Waiting a few seconds for the server to boot up...
timeout /t 10 /nobreak >nul

echo 2. Starting Frontend Client...
cd forknight-client
mvn javafx:run
