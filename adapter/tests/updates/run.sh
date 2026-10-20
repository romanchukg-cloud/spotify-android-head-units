#!/bin/bash
# Plain-JVM updater checks: feed contract, download resume/limits/hashes and text formats.
# ANDROID_JAR may be the SDK android.jar; org.json comes from a pinned jar (JSON_JAR).
set -euo pipefail
cd "$(dirname "$0")/../.."
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
ANDROID_JAR=${ANDROID_JAR:-$SDK_ROOT/platforms/android-35/android.jar}
OUT=tests/updates/out
rm -rf "$OUT" && mkdir -p "$OUT"
JSON_JAR=${JSON_JAR:-$OUT/json-20250517.jar}
[ -f "$JSON_JAR" ] || curl -fsSL -o "$JSON_JAR" https://repo1.maven.org/maven2/org/json/json/20250517/json-20250517.jar
echo "3ea61b2a06e31edf1c91134fe9106b0ebb16628be169f3db75bc7a2b06b45796  $JSON_JAR" | sha256sum -c - >/dev/null
python3 -c 'import build_config;build_config.generate()'
# Compile against Android's org.json API; run with the real implementation before the SDK stubs.
javac -nowarn -source 8 -target 8 -cp "$ANDROID_JAR" -d "$OUT" src/local/spotify/{close,unified}/*.java src/com/byd/spi/ipc/cursor/*.java generated/local/spotify/unified/*.java stubs/android/support/v4/media/*.java stubs/android/support/v4/media/session/*.java tests/updates/src/local/spotify/unified/UpdateLogicTest.java
java -cp "$OUT:$JSON_JAR:$ANDROID_JAR" local.spotify.unified.UpdateLogicTest
