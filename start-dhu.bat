@echo off
title Android Auto DHU - Renault Kwid Media Nav Emulator
echo =======================================================
echo    Android Auto DHU - Renault Kwid Media Nav Emulator
echo    Hardware Target: 800x480 @ 160 DPI (Touchscreen)
echo =======================================================
echo.

echo [1/3] Checking connected ADB devices...
adb devices
echo.

echo [2/3] Forwarding TCP port 5277...
adb forward tcp:5277 tcp:5277
echo.

echo [3/3] Launching Desktop Head Unit with Kwid Profile...
cd /d "%LOCALAPPDATA%\Android\Sdk\extras\google\auto"
desktop-head-unit.exe -c config\kwid.ini

pause
