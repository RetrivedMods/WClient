#!/bin/bash

echo "WClient Desktop Launcher"
echo "=========================="
echo ""

if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

echo "Starting WClient Desktop..."
echo ""

cd "$(dirname "$0")/build/libs"
if [ -f "W-Desktop-12.jar" ]; then
    java -jar WClient-Desktop-12.jar &
    echo "WClient Desktop started successfully!"
else
    echo "ERROR: WClient-Desktop-12.jar not found"
    echo "Please build the project first using: ./gradlew :desktop:shadowJar"
    exit 1
fi
