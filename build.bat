@echo off
echo ==========================================
echo       Building VirtualKit 2.0.0
echo ==========================================
echo.

call "C:\Users\joeal\.m2\apache-maven-3.9.6\bin\mvn.cmd" clean package

echo.
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Build completed successfully!
    echo [SUCCESS] The compiled JAR is at: target\VirtualKit-2.0.0.jar
) else (
    echo [ERROR] Build failed! Please check the compiler errors above.
)
echo.
pause
