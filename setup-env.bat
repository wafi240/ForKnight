@echo off
echo ========================================================
echo   ForKnight - Environment Setup Check
echo ========================================================

echo [1/3] Checking Java version...
java -version 2>temp_jv.txt
type temp_jv.txt
del temp_jv.txt

echo.
echo [2/3] Checking Maven...
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    echo Maven is installed.
    mvn -version
) else (
    echo [!] Maven is not found in PATH.
    echo To install Maven via Chocolatey, run as Administrator:
    echo   choco install maven -y
)

echo.
echo [3/3] Recommended Setup:
echo If you need JDK 21 and Maven installed automatically:
echo 1. Install JDK 21: winget install Microsoft.OpenJDK.21
echo 2. Install Maven:  choco install maven -y
echo 3. Re-open terminal and run: build-all.bat
echo ========================================================
pause
