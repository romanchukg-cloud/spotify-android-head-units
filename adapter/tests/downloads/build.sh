#!/bin/zsh
# Builds the downloads probe: an independent media host client that runs against the Spotify
# backend's own MediaBrowserCompat (classes.dex of the given head-unit APK).
#   bash tests/downloads/build.sh Spotify-5.5.0-HeadUnit-test.10.apk
set -eu
APK=${1:?head-unit APK}
APK="$(cd "$(dirname "$APK")" && pwd)/$(basename "$APK")"
cd "$(dirname "$0")"
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
rm -rf out && mkdir -p out/stubs out/classes out/dex
javac -source 8 -target 8 -cp "$JAR" -d out/stubs ../../stubs/android/support/v4/media/*.java
javac -source 8 -target 8 -cp "$JAR:out/stubs" -d out/classes hu/downloads/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --classpath out/stubs --output out/dex out/classes/hu/downloads/*.class
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml -o out/unsigned.apk
python3 - "$APK" <<'PY'
import sys,zipfile
with zipfile.ZipFile('out/unsigned.apk','a') as z:
 z.write('out/dex/classes.dex','classes.dex')
 with zipfile.ZipFile(sys.argv[1]) as hu:z.writestr('classes2.dex',hu.read('classes.dex'))
PY
"$BT/zipalign" -f 4 out/unsigned.apk out/aligned.apk
"$BT/apksigner" sign --ks "${HU_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass "${HU_KEYSTORE_PASS:-pass:android}" --out out/probe.apk out/aligned.apk
echo out/probe.apk
