# HeadUnit test.21 — The sign-in record on the login screen in use

Signed test.21 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.21).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.21.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95498** (published test.20: 95497).
- SHA-256: `53fd303ec658625f8f6651de4ca19a679424632515a5fadd1fbb95f80ca0a1a7`
- Size: **48,424,050 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.20 (apksigner; `zipalign -c -P 16 4`). test.21 installs as an update over test.20.
- Source: commit [`89ed004`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/89ed004/adapter). See [release.json](headunit-test.21/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (section "After test.20").

- **The record on the login screen in use:** test.20 hooked `n5.r.d`, which only the older `QrCodeLoginActivity` calls. `QrCodeLoginActivityV2` and `PinLoginActivityV2` handle the result in `yc.p.f`, whose failure branch now also calls `HeadUnitAccounts.loginFailed`.
- **Audit:** `tools/apk_audit.py` also fails if any class that branches on `ur.d` does not call the record.

## Verification

- **152** updater, **64** sign-in, **14** panel-access, **13** Connect-message, **66** App Remote plain-JVM checks and **19** playback-start fixture checks pass.
- The release audit passed **57/57**; see [apk-audit.txt](headunit-test.21/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators: the published test.20 APK (hash checked) installed and started, test.21 installed over it as an update (versionCode 95498), the player started, and the forced update job ran with no crash. API 33 reached the feed (`up_to_date`); API 28 could not (`check_failed`, a warning only).
- `adapter/tests/accounts_ui.py` on Android 13 passed, with the first-use choice set beforehand with automatic sending off.
- This APK on an Android 13 emulator: on the login screen opened the normal way, a one-off instrumentation called the real `yc.p.f` with a made-up token. Spotify's server refused it; Settings showed "Sign-in did not complete… (401 auth_pre_authentication_failed)", and the private log got `auth / schema_mismatch`. Automatic sending was off and no report reached the server.
- A candidate of the same code on an Android 13 emulator: signing in through spotify.com/pair worked with a Premium and a Free account.

## Not yet verified

A real failed sign-in after a QR code; a car; the navigation card from test.19 with right-hand drive; DiLink 5.1 / Android 13 and other head units.

## Update feed

After the release, the operator publishes [spotify-update-test.21.json](headunit-test.21/spotify-update-test.21.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
