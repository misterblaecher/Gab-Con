@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1"
if errorlevel 1 (
  echo ERREUR pendant la compilation.
  pause
  exit /b 1
)
echo Compilation terminee.
pause
