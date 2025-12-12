@echo off
echo Finding process using port 3000...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3000 ^| findstr LISTENING') do (
    set PID=%%a
    echo Found process with PID: %%a
    taskkill /PID %%a /F
    echo Process killed!
)
if not defined PID (
    echo No process found using port 3000
)
pause

