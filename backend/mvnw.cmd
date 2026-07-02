@echo off
setlocal

SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6\apache-maven-3.9.6
SET MVN=%MAVEN_HOME%\bin\mvn.cmd

IF NOT EXIST "%MVN%" (
    echo Maven not found. Please run download_maven.ps1 first.
    exit /B 1
)

echo Using Maven at: %MAVEN_HOME%
echo.
"%MVN%" %*

endlocal
