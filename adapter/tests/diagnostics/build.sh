#!/bin/zsh
set -eu
cd "$(dirname "$0")/../.."
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
mkdir -p tests/diagnostics/out/{main,reader}/{classes,dex}
javac -source 8 -target 8 -cp "${JAR}:classes" -d tests/diagnostics/out/main/classes tests/diagnostics/src/local/spotify/unified/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --output tests/diagnostics/out/main/dex tests/diagnostics/out/main/classes/local/spotify/unified/*.class classes/local/spotify/unified/*.class classes/local/spotify/close/*.class
javac -source 8 -target 8 -cp "$JAR" -d tests/diagnostics/out/reader/classes tests/diagnostics/reader/src/hu/diagnostics/reader/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --output tests/diagnostics/out/reader/dex tests/diagnostics/out/reader/classes/hu/diagnostics/reader/*.class
"$BT/aapt2" link -I "$JAR" --manifest tests/diagnostics/AndroidManifest.xml -o tests/diagnostics/out/main/base.apk
"$BT/aapt2" link -I "$JAR" --manifest tests/diagnostics/reader/AndroidManifest.xml -o tests/diagnostics/out/reader/base.apk
python3 - <<'PY'
from pathlib import Path
import zipfile
for part in ('main','reader'):
 p=Path('tests/diagnostics/out')/part
 with zipfile.ZipFile(p/'base.apk','a') as z:z.write(p/'dex/classes.dex','classes.dex')
PY
for part in main reader; do
 "$BT/zipalign" -f 4 tests/diagnostics/out/$part/base.apk tests/diagnostics/out/$part/aligned.apk
 "$BT/apksigner" sign --ks "${HU_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass "${HU_KEYSTORE_PASS:-pass:android}" --out tests/diagnostics/out/$part.apk tests/diagnostics/out/$part/aligned.apk
done
