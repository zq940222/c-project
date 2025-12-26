@echo off
echo Starting C-File-Processor Frontend...
cd /d %~dp0frontend
call npm install
call npm run dev
pause
