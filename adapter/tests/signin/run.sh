#!/bin/zsh
# Plain-JVM sign-in logic checks. ANDROID_JAR may be the SDK android.jar or any
# full framework jar; the tested methods only use Java APIs.
set -eu
cd "$(dirname "$0")/../.."
SDK_ROOT=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}
ANDROID_JAR=${ANDROID_JAR:-$SDK_ROOT/platforms/android-35/android.jar}
OUT=tests/signin/out
rm -rf "$OUT" && mkdir -p "$OUT"
python3 -c 'import build_config;build_config.generate()'
javac -nowarn -source 8 -target 8 -cp "$ANDROID_JAR" -d "$OUT" src/local/spotify/{close,unified}/*.java generated/local/spotify/unified/*.java stubs/android/support/v4/media/*.java stubs/android/support/v4/media/session/*.java tests/signin/src/local/spotify/unified/SignInLogicTest.java
java -cp "$OUT:$ANDROID_JAR" local.spotify.unified.SignInLogicTest
