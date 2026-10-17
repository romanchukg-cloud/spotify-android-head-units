#!/bin/zsh
set -eu
cd "$(dirname "$0")"
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
ANDROID_JAR="$SDK_ROOT/platforms/android-35/android.jar"
APKTOOL_JAR=${APKTOOL_JAR:?Set APKTOOL_JAR to apktool 2.11.1 jar}
python3 patch_resources.py
python3 restyle.py
python3 prepare.py
python3 build_config.py
javac -cp "$APKTOOL_JAR" Assemble.java
java -cp ".:$APKTOOL_JAR" Assemble ui-smali ui.dex
rm -rf classes helper-dex stub-classes
mkdir -p classes helper-dex stub-classes
# The backend's own support-library media classes: compile-time only, never in the helper DEX.
javac -source 8 -target 8 -cp "$ANDROID_JAR" -d stub-classes stubs/android/support/v4/media/*.java
javac -source 8 -target 8 -cp "${ANDROID_JAR}:stub-classes" -d classes src/local/spotify/{close,unified}/*.java generated/local/spotify/unified/*.java
"$BT/d8" --min-api 28 --lib "$ANDROID_JAR" --classpath stub-classes --output helper-dex classes/local/spotify/{close,unified}/*.class
apktool b ui -o ui-resources.apk
apktool b backend -o backend-rebuilt.apk
python3 package.py
python3 verify.py
APK_NAME=$(python3 -c 'from build_config import BUILD;print(BUILD["apk_name"])')
python3 ../tools/apk_audit.py "$APK_NAME" --min-api 28
