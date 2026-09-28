@echo off
echo Starting ForKnight Server...
set MAVEN_HOME=C:\Users\HP\OneDrive\Desktop\ForKnight\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%

cd forknight-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
