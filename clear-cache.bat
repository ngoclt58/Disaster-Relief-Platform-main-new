@echo off
echo Clearing frontend cache...
cd frontend
if exist "node_modules\.cache" (
    rmdir /s /q "node_modules\.cache"
    echo Cache cleared successfully!
) else (
    echo Cache folder not found.
)
cd ..
echo Done!
