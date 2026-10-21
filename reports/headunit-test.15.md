# HeadUnit test.15 — The time in the mini-player, a thin frame, the navigation closes the player

Signed test.15 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.15). Test.14's news was held back by the owner and goes out with this release.

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.15.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95492** (published test.14: 95491).
- SHA-256: `8582e41a8b137a78b8842e464152f6539c80f87dad06904069392763e1c6983b`
- Size: **48,350,322 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.14 (apksigner; `zipalign -c -P 16 4`). test.15 installs as an update over test.14.
- Source: commit [`0dd0906`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/0dd0906/adapter). See [release.json](headunit-test.15/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (section "After test.14").

- **Mini-player time** (`HuMiniPlayer`): "elapsed / length", tabular digits, from 640 dp of width.
- **Navigation closes the player** (`HuPlayerActivity.picked`): the queue and the full-screen player close before a tab shows; the tab under them is only uncovered.
- **A 3 dp frame** of the window's background around the main screen.

## Verification

- **152** updater, **39** sign-in, **14** panel-access plain-JVM checks and **19** playback-start fixture checks pass.
- The release audit passed **51/51**; see [apk-audit.txt](headunit-test.15/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators: the published test.14 APK (hash checked) installed and started, test.15 installed over it as an update (versionCode 95492), the player started, the update job ran and reported its status; no crash. `adapter/tests/accounts_ui.py` passed on Android 13.
- Android 12 emulator: the frame; the full-screen player open, Library tapped: the player closed and Library showed.
- BYD Tang (DiLink 5, Android 12): a build of the same code (versionCode 95491) is installed (hash checked); the car can update itself to test.15 through the app.

## Not yet verified

The mini-player's time in the car, the widgets with a real song, a song link playing, a real app reading the broadcasts, a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

## Update feed

After the release, the operator publishes [spotify-update-test.15.json](headunit-test.15/spotify-update-test.15.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
