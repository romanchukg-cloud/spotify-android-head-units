# HeadUnit test.20 — Why a sign-in failed

Signed test.20 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.20).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.20.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95497** (published test.19: 95496).
- SHA-256: `29bab616a4f690f63db7d50c5e15bbb826cafd2e30d6bfc31839b6e51b567691`
- Size: **48,424,050 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.19 (apksigner; `zipalign -c -P 16 4`). test.20 installs as an update over test.19.
- Source: commit [`c9c0859`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/c9c0859/adapter). See [release.json](headunit-test.20/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (section "After test.19").

- **Failed sign-ins recorded** (`HeadUnitAccounts`): the login screen's error mapping (`n5.r.d`) records a server error the app does not recognise, a refused response or an account that could not be saved; `vr.e` passes on the response's status.
- **Settings:** the last sign-in event shows the kind, the status and a code-like server message, on the head unit only.
- **Reports:** the kind, through the server's fixed failure codes (`auth / schema_mismatch`, `internal_error`, `unavailable`). The server was not changed.

## Verification

- **152** updater, **64** sign-in, **14** panel-access, **13** Connect-message, **66** App Remote plain-JVM checks and **19** playback-start fixture checks pass.
- The release audit passed **57/57**; see [apk-audit.txt](headunit-test.20/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators (network off): the published test.19 APK (hash checked) installed and started, test.20 installed over it as an update (versionCode 95497), the player started, the update job ran and reported its status; no crash. `adapter/tests/accounts_ui.py` passed on Android 13.
- Android 13 emulator, a debug build of the same code: the Settings line with a record written by hand.

## Not yet verified

A real failed sign-in; a successful sign-in on this build; a car; the navigation card from test.19 with right-hand drive; DiLink 5.1 / Android 13 and other head units.

## Update feed

After the release, the operator publishes [spotify-update-test.20.json](headunit-test.20/spotify-update-test.20.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
