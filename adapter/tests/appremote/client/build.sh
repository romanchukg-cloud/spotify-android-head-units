#!/bin/zsh
# The App Remote test client: Spotify's own Android SDK (App Remote 0.8.0, Apache 2.0, from
# github.com/spotify/android-sdk) talking to our debug build's HuAppRemoteService. Neither the SDK
# nor Gson is kept in this repository: point SPOTIFY_APP_REMOTE_AAR at
# spotify-app-remote-release-0.8.0.aar and GSON_JAR at a Gson 2.x jar. Then install
# out/AppRemoteTests.apk on a disposable emulator with our debug build and run
# adb shell am instrument -w hu.appremote/hu.appremote.AppRemoteTests
# Never install this fixture on the user's car.
set -eu
cd "$(dirname "$0")"
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
: ${SPOTIFY_APP_REMOTE_AAR:?Set SPOTIFY_APP_REMOTE_AAR to spotify-app-remote-release-0.8.0.aar}
: ${GSON_JAR:?Set GSON_JAR to a Gson 2.x jar}
rm -rf out && mkdir -p out/classes out/dex out/sdk
unzip -q -o "$SPOTIFY_APP_REMOTE_AAR" classes.jar -d out/sdk
javac -nowarn -source 8 -target 8 -cp "$JAR:out/sdk/classes.jar:$GSON_JAR" -d out/classes src/hu/appremote/*.java src/com/spotify/android/appremote/internal/*.java
"$BT/d8" --min-api 28 --lib "$JAR" --output out/dex $(find out/classes -name '*.class') out/sdk/classes.jar "$GSON_JAR" 2>&1 | grep -v "^Warning" || true
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml -o out/base.apk
(cd out/dex && zip -q ../base.apk classes*.dex)
"$BT/zipalign" -f 4 out/base.apk out/aligned.apk
"$BT/apksigner" sign --ks "${HU_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass "${HU_KEYSTORE_PASS:-pass:android}" --out out/AppRemoteTests.apk out/aligned.apk
echo "Built out/AppRemoteTests.apk"
