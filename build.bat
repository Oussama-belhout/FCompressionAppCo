@echo off
REM Build script for Bit Packing Compression Application (Windows)

echo Building Bit Packing Compression Application...

REM Create output directory
if not exist out mkdir out

REM Find all Java files and compile
dir /s /b src\main\java\*.java > sources.txt
javac -d out -sourcepath src\main\java @sources.txt

if %errorlevel% equ 0 (
    echo Build successful!
    echo Run with: java -cp out com.project.bitpacking.Main
    del sources.txt
) else (
    echo Build failed!
    del sources.txt
    exit /b 1
)


