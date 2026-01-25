#!/bin/bash
echo "Building the app (Debug variant)..."
./gradlew assembleDebug
if [ $? -eq 0 ]; then
    echo "Build successful! APK is located at app/build/outputs/apk/debug/"
else
    echo "Build failed."
    exit 1
fi
