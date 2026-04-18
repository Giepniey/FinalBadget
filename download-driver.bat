@echo off
REM Download MySQL JDBC Driver
echo Downloading MySQL Connector/J 8.0.33...
echo Please wait, this may take a minute...

cd /d "%~dp0\lib"

REM Try downloading from multiple sources
echo Trying download source 1...
powershell -Command "(New-Object System.Net.ServicePointManager).SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; $ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri 'https://downloads.mysql.com/archives/get/p/3/file/mysql-connector-java-8.0.33.jar' -OutFile 'mysql-connector-java-8.0.33.jar'" 2>nul

if exist "mysql-connector-java-8.0.33.jar" (
    echo.
    echo SUCCESS! MySQL driver downloaded.
    echo File: mysql-connector-java-8.0.33.jar
    echo Location: %cd%
    echo.
    echo You can now run: run.bat
    pause
    exit /b 0
)

echo.
echo ============================================
echo MANUAL DOWNLOAD REQUIRED
echo ============================================
echo.
echo Automatic download failed. Please download manually:
echo.
echo 1. Go to: https://dev.mysql.com/downloads/connector/j/
echo 2. Select version 8.0.33 (Platform Independent)
echo 3. Download mysql-connector-java-8.0.33.jar
echo 4. Save it to: %cd%
echo.
echo Alternative download link:
echo https://downloads.mysql.com/archives/get/p/3/file/mysql-connector-java-8.0.33.jar
echo.
pause
