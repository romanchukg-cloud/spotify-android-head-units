#!/bin/zsh
set -eu
cd "$(dirname "$0")/../.."
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
mkdir -p tests/profiles/out/classes tests/profiles/out/dex
javac -source 8 -target 8 -cp "${JAR}:classes" -d tests/profiles/out/classes tests/profiles/stubs/ya/e.java tests/profiles/src/local/spotify/unified/ProfileTests.java
"$BT/d8" --min-api 28 --lib "$JAR" --output tests/profiles/out/dex tests/profiles/out/classes/local/spotify/unified/*.class classes/local/spotify/unified/*.class classes/local/spotify/close/*.class
"$BT/aapt2" link -I "$JAR" --manifest tests/profiles/AndroidManifest.xml -o tests/profiles/out/base.apk
python3 - <<'PY'
from pathlib import Path
import zipfile
p=Path('tests/profiles/out')
with zipfile.ZipFile(p/'base.apk') as original,zipfile.ZipFile(p/'unsigned.apk','w') as out,zipfile.ZipFile('backend-rebuilt.apk') as backend:
 for info in original.infolist():out.writestr(info,original.read(info))
 out.writestr('classes.dex',(p/'dex/classes.dex').read_bytes())
 out.writestr('classes2.dex',backend.read('classes.dex'))
PY
"$BT/zipalign" -f 4 tests/profiles/out/unsigned.apk tests/profiles/out/aligned.apk
"$BT/apksigner" sign --ks "${HU_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass "${HU_KEYSTORE_PASS:-pass:android}" --out tests/profiles/out/ProfileTests.apk tests/profiles/out/aligned.apk
