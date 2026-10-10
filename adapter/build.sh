#!/bin/zsh
set -eu
cd "$(dirname "$0")"
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
ANDROID_JAR="$SDK_ROOT/platforms/android-35/android.jar"
APKTOOL_JAR=${APKTOOL_JAR:?Set APKTOOL_JAR to apktool 2.11.1 jar}
python3 patch_resources.py
python3 prepare.py
javac -cp "$APKTOOL_JAR" Assemble.java
java -cp ".:$APKTOOL_JAR" Assemble ui-smali ui.dex
rm -rf classes helper-dex
mkdir -p classes helper-dex
javac -source 8 -target 8 -cp "$ANDROID_JAR" -d classes src/local/spotify/{close,unified}/*.java
"$BT/d8" --min-api 28 --lib "$ANDROID_JAR" --output helper-dex classes/local/spotify/{close,unified}/*.class
apktool b ui -o ui-resources.apk
apktool b backend -o backend-rebuilt.apk
python3 package.py
python3 verify.py
python3 ../tools/apk_audit.py Spotify-5.5.0-HeadUnit-test.4.apk --min-api 28
