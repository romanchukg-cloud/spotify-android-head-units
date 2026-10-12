#!/bin/zsh
set -eu
cd "$(dirname "$0")/../.."
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
mkdir -p tests/resume/out/classes tests/resume/out/dex
javac -source 8 -target 8 -cp "${JAR}:classes" -d tests/resume/out/classes tests/resume/src/hu/resume/*.java tests/resume/src/com/spotify/automotive/mediabrowserservice/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --output tests/resume/out/dex tests/resume/out/classes/hu/resume/*.class tests/resume/out/classes/com/spotify/automotive/mediabrowserservice/*.class classes/local/spotify/unified/*.class classes/local/spotify/close/*.class
"$BT/aapt2" link -I "$JAR" --manifest tests/resume/AndroidManifest.xml -o tests/resume/out/base.apk
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('tests/resume/out/base.apk','a') as z:z.write('tests/resume/out/dex/classes.dex','classes.dex')
PY
"$BT/zipalign" -f 4 tests/resume/out/base.apk tests/resume/out/aligned.apk
"$BT/apksigner" sign --ks "${HU_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass "${HU_KEYSTORE_PASS:-pass:android}" --out tests/resume/out/ResumeTests.apk tests/resume/out/aligned.apk
