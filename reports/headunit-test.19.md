# HeadUnit test.19 — The navigation on a card

Signed test.19 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.19).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.19.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95496** (published test.18: 95495).
- SHA-256: `5f3930efb748097a8792834c09e5b3bb2e293c33775c0275cd115ad1c94c8860`
- Size: **48,419,954 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.18 (apksigner; `zipalign -c -P 16 4`). test.19 installs as an update over test.18.
- Source: commit [`0a270ae`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/0a270ae/adapter). See [release.json](headunit-test.19/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (section "After test.18").

- **Navigation card** (`HuRail`, `HuPlayerActivity`): the rail and the portrait bar take the card colour and 22 dp corners, with the 6 dp gap to the content; the pill follows the card's corners. No other change.

## Verification

- **152** updater, **39** sign-in, **14** panel-access, **13** Connect-message, **66** App Remote plain-JVM checks and **19** playback-start fixture checks pass.
- The release audit passed **56/56**; see [apk-audit.txt](headunit-test.19/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators (network off): the published test.18 APK (hash checked) installed and started, test.19 installed over it as an update (versionCode 95496), the player started, the update job ran and reported its status; no crash. `adapter/tests/accounts_ui.py` passed on Android 13.
- Android 13 emulator with the network on, a build of the same code: the owner signed in without an error and approved the look; the card was seen by day, by night and in portrait, and in Arabic on the release build.

## Not yet verified

The new card in a car and with right-hand drive; DiLink 5.1 / Android 13 and other head units.

## Update feed

After the release, the operator publishes [spotify-update-test.19.json](headunit-test.19/spotify-update-test.19.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
