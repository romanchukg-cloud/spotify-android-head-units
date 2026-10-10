#!/bin/zsh
set -eu
cd "$(dirname "$0")"
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
mkdir -p out/testclasses out/stubs out/clientclasses out/testdex out/clientdex
javac -source 8 -target 8 -cp "$JAR:../classes" -d out/testclasses src/hu/tests/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --classpath ../classes --output out/testdex out/testclasses/hu/tests/*.class
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml -o out/test-unsigned.apk
javac -source 8 -target 8 -cp "$JAR" -d out/stubs stubs/android/support/v4/media/*.java
javac -source 8 -target 8 -cp "$JAR:out/stubs" -d out/clientclasses client/hu/browser/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --classpath out/stubs --output out/clientdex out/clientclasses/hu/browser/*.class
"$BT/aapt2" link -I "$JAR" --manifest ClientManifest.xml -o out/client-unsigned.apk
python3 - <<'PY'
import zipfile
from pathlib import Path
for name in ('test','client'):
 with zipfile.ZipFile(f'out/{name}-unsigned.apk','a') as z:
  z.write(f'out/{name}dex/classes.dex','classes.dex')
  if name=='client':
   with zipfile.ZipFile('../Spotify-5.5.0-HeadUnit-test.4.apk') as sdk:z.writestr('classes2.dex',sdk.read('classes.dex'))
PY
for NAME in test client; do
 "$BT/zipalign" -f 4 "out/$NAME-unsigned.apk" "out/$NAME-aligned.apk"
 "$BT/apksigner" sign --ks "${HU_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass "${HU_KEYSTORE_PASS:-pass:android}" --out "out/$NAME.apk" "out/$NAME-aligned.apk"
done
