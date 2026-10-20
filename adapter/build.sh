#!/bin/zsh
set -eu
cd "$(dirname "$0")"
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
ANDROID_JAR="$SDK_ROOT/platforms/android-35/android.jar"
python3 prepare.py
python3 build_config.py
rm -rf classes helper-dex stub-classes
mkdir -p classes helper-dex stub-classes
# The backend's own support-library media classes: compile-time only, never in the helper DEX.
javac -source 8 -target 8 -cp "$ANDROID_JAR" -d stub-classes stubs/android/support/v4/media/*.java stubs/android/support/v4/media/session/*.java
# HU_DEBUG=1 adds the debug-only sources (the panel test host).
DEBUG_SOURCES=();[ "${HU_DEBUG:-}" = 1 ] && DEBUG_SOURCES=(src-debug/local/spotify/unified/*.java)
javac -source 8 -target 8 -cp "${ANDROID_JAR}:stub-classes" -d classes src/local/spotify/{close,unified}/*.java src/com/byd/spi/ipc/cursor/*.java generated/local/spotify/unified/*.java "${DEBUG_SOURCES[@]}"
"$BT/d8" --min-api 28 --lib "$ANDROID_JAR" --classpath stub-classes --output helper-dex classes/local/spotify/{close,unified}/*.class classes/com/byd/spi/ipc/cursor/*.class
apktool b backend -o backend-rebuilt.apk
python3 package.py
python3 verify.py
APK_NAME=$(python3 -c 'from build_config import BUILD;print(BUILD["apk_name"])')
AUDIT_FLAGS=();[ "${HU_DEBUG:-}" = 1 ] && AUDIT_FLAGS=(--debug-build)
python3 ../tools/apk_audit.py "$APK_NAME" --min-api 28 "${AUDIT_FLAGS[@]}"
