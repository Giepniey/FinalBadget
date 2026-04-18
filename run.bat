@echo off
cd /d "%~dp0"
echo Compiling...
javac -cp "lib\mysql-connector-j-8.0.33.jar" *.java
echo Running Badget...
java -cp ".;lib\mysql-connector-j-8.0.33.jar" Badget
pause