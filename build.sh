#!/bin/bash

# Build script for Bit Packing Compression Application

echo "Building Bit Packing Compression Application..."

# Create output directory
mkdir -p out

# Find all Java files and compile
find src/main/java -name "*.java" > sources.txt
javac -d out -sourcepath src/main/java @sources.txt

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Run with: java -cp out com.project.bitpacking.Main"
    rm sources.txt
else
    echo "Build failed!"
    rm sources.txt
    exit 1
fi


