@echo off
echo Starting C-File-Processor Backend...
cd /d %~dp0backend
call mvn spring-boot:run
pause
