#!/bin/zsh
set -eu
cd "$(dirname "$0")/../.."
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
mkdir -p tests/rhythm/out/classes tests/rhythm/out/dex
javac -source 8 -target 8 -cp "${JAR}:classes" -d tests/rhythm/out/classes tests/rhythm/src/local/spotify/unified/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --output tests/rhythm/out/dex tests/rhythm/out/classes/local/spotify/unified/*.class classes/local/spotify/unified/*.class classes/local/spotify/close/*.class
"$BT/aapt2" link -I "$JAR" --manifest tests/rhythm/AndroidManifest.xml -o tests/rhythm/out/base.apk
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('tests/rhythm/out/base.apk','a') as z:z.write('tests/rhythm/out/dex/classes.dex','classes.dex')
PY
"$BT/zipalign" -f 4 tests/rhythm/out/base.apk tests/rhythm/out/aligned.apk
"$BT/apksigner" sign --ks "${HU_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass "${HU_KEYSTORE_PASS:-pass:android}" --out tests/rhythm/out/RhythmTests.apk tests/rhythm/out/aligned.apk
