#!/bin/bash
# Emulator smoke test of a release candidate: in-place update over the published
# base APK, player start, a forced update-job run against the live feed, no crash.
# Usage: emulator.sh BASE_APK NEW_APK EXPECTED_VERSION_CODE
set -euo pipefail
BASE=$1 NEW=$2 CODE=$3 PKG=com.spotify.music UI=com.spotify.music/local.bydui.com.vivid.spotify.MainActivity
fail(){ echo "::error::$1"; exit 1; }
adb wait-for-device
adb install --no-incremental "$BASE"
adb shell am start -W -n "$UI" >/dev/null; sleep 10
adb logcat -c; adb logcat -b crash -c
adb install --no-incremental -r "$NEW"   # same package and certificate: an update, app data kept
package=$(adb shell dumpsys package "$PKG")
grep -q "versionCode=$CODE " <<<"$package" || fail "versionCode $CODE not installed"
grep -q "android.permission.REQUEST_INSTALL_PACKAGES" <<<"$package" || fail "Installer permission not declared"
adb shell am start -W -n "$UI" >/dev/null; sleep 20
jobs=$(adb shell dumpsys jobscheduler)
grep -qE "/9158: [0-9a-f]+ $PKG/" <<<"$jobs" || { grep -i "$PKG" <<<"$jobs" || true; fail "Update job not scheduled"; }
adb shell cmd jobscheduler run -f "$PKG" 9158
log=""
for _ in $(seq 1 30); do log=$(adb logcat -d -s SpotifyHU:I | grep "Update status=" || true); grep -qv "status=checking" <<<"$log" && [ -n "$log" ] && grep -q "status=\(up_to_date\|check_failed\|available\|ready\|wifi_wait\)" <<<"$log" && break; sleep 2; done
echo "$log"
[ -n "$log" ] || fail "The update job did not report a status"
if grep -q "status=unavailable" <<<"$log"; then fail "Updater failed internally"; fi
if grep -q "status=check_failed" <<<"$log"; then echo "::warning::The emulator could not reach the update feed"; fi
crash=$(adb logcat -d -b crash | grep -A20 "Process: $PKG" || true)
[ -z "$crash" ] || { echo "$crash"; fail "Spotify crashed"; }
adb shell pidof "$PKG" >/dev/null || fail "The backend process is not running"
echo "Emulator smoke passed: update over the base APK, player start, update job, no crash"
