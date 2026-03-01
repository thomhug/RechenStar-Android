#!/bin/bash
# android.sh - Wrapper für Android Build/Test/Emulator Befehle
# Setzt JAVA_HOME und ANDROID_SDK_ROOT korrekt für dieses Projekt.
#
# Verwendung:
#   ./scripts/android.sh build          - assembleDebug
#   ./scripts/android.sh test           - Unit Tests
#   ./scripts/android.sh uitest         - UI Tests auf Emulator
#   ./scripts/android.sh install        - App auf Emulator installieren
#   ./scripts/android.sh run            - App starten
#   ./scripts/android.sh screenshot     - Screenshot vom Emulator
#   ./scripts/android.sh gradle <args>  - Beliebiger Gradle-Befehl

set -e

export JAVA_HOME=/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_SDK_ROOT=/usr/local/share/android-commandlinetools
ADB=~/Library/Android/sdk/platform-tools/adb

case "${1:-build}" in
    build)
        ./gradlew assembleDebug
        ;;
    test)
        ./gradlew test
        ;;
    uitest)
        ./gradlew connectedDebugAndroidTest
        ;;
    install)
        ./gradlew installDebug
        ;;
    run)
        $ADB shell am force-stop ch.rechenstar.app
        $ADB shell am start -n ch.rechenstar.app/.MainActivity
        ;;
    screenshot)
        OUT="${2:-/tmp/screenshot.png}"
        $ADB exec-out screencap -p > "$OUT"
        echo "Screenshot saved to $OUT"
        ;;
    gradle)
        shift
        ./gradlew "$@"
        ;;
    *)
        echo "Unbekannter Befehl: $1"
        echo "Verfügbar: build, test, uitest, install, run, screenshot, gradle"
        exit 1
        ;;
esac
