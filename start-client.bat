@echo off
echo Starting ForKnight Client (Frontend)...
set MAVEN_HOME=C:\Users\HP\OneDrive\Desktop\ForKnight\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%

cd forknight-client
mvn javafx:run
