@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Batch Script
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
set "MAVEN_CMD=C:\Users\USUARIO\.maven\apache-maven-3.9.9\bin\mvn.cmd"

if exist "%MAVEN_CMD%" (
    "%MAVEN_CMD%" %*
) else (
    echo Error: Maven executable not found at %MAVEN_CMD%
    exit /b 1
)
