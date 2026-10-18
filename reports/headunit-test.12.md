# HeadUnit test.12 — Our own interface, blurred covers, download progress and a free-app notice

Signed test.12 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.12).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.12.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95489** (published test.11: 95488).
- SHA-256: `00f82a80d9a40d9d3138c3fde1d4c3c88edf270c380c54a70fa01032ab6982a1`
- Size: **58,378,169 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.11 (apksigner; `zipalign -c -P 16 4`). test.12 installs as an update over test.11.
- Source: commit [`0b4fe0d`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/0b4fe0d557bceda099f55470eebfd892849cca9e/adapter). See [release.json](headunit-test.12/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared local inputs, signed with the release key. `adapter/incremental_release.py` cannot make this release: it adds only permissions and receivers to the manifest, while test.12 declares a new main activity with a launcher alias and drops the BYD-derived one.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (sections "Local test.12").

- **Our own interface as the main screen:** `HuPlayerActivity` (helper DEX, no BYD code in its screens) is the launcher's target. The launcher entry keeps the earlier component name `local.bydui.com.vivid.spotify.MainActivity` as an `activity-alias`, so launcher icons, the playback notification, profile switching and the BYD media service still reach it. It opens the playback gate, runs the once-per-task opening work (auto resume, diagnostics notice, update offer) and the profile hand-off like the BYD-derived screen did. Signed out, it shows the backend's sign-in request and opens Spotify's QR sign-in screen.
- **Pages over the backend's browse tree:** Home groups, Recents, Browse, Library with filter pills, playlist / album / artist / show pages (`HuDetail`), search through the media browser (`HuSearch`) with recent searches, a shared list (`HuList`); navigation with kept tab state and a view stack.
- **Player and mini-player** over the backend's media session (`HuSession`, the backend's own support-library classes through compile-time stubs), on the blurred cover (`HuUi.Blur`); the queue as a card (`HuQueue`); the sleep timer (`HuSleep`, run by `HeadUnitSleep` in the music process).
- **Download progress** from the backend's subtitle prefix for downloaded tracks; episode progress from `PLAYBACK_STATUS` / `COMPLETION_PERCENTAGE`.
- **"This app is free"** (`HuFree`): a dialog once per version, lines on the sign-in panel and Spotify's QR sign-in screen, "About this app" in Head unit settings; a QR code to the official page generated and checked with CoreImage.
- **Layout:** the screen rebuilds when a split screen or rotation changes its size without recreating it; the keyboard lies over landscape screens; rounded corners on every own screen; the QR sign-in card fits short screens.
- **Classic style removed;** the New style is the only one.

## Verification

- **152** plain-JVM updater checks and **39** sign-in checks pass.
- The release audit passed **46/46**, including the new check that the launcher's only entry is the alias of our interface; see [apk-audit.txt](headunit-test.12/apk-audit.txt). The compatibility audit reports no failures.
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators: the published test.11 APK (hash checked) was installed and started, test.12 installed over it as an update (versionCode 95489), the player started, the update job ran against the live feed and reported `up_to_date`; no crash.
- Android 12 emulator with a signed-in Premium account: all pages, search, voice search with a test recogniser, navigation, queue, sleep timer, downloads with progress (playlists already in the library, downloads removed afterwards), Arabic (right-to-left), portrait, a split-screen window size, day and night.
- Android 13 emulator, signed out (800×480 and 1024×600): `adapter/tests/accounts_ui.py` passed (Accounts, our Log in to the QR screen, Add account restarting the backend into QR sign-in, back); the QR sign-in screen fits.
- Pre-release builds in a BYD car (DiLink 5, Android 12, 2560×1440): the interface in landscape, portrait and split screen with Google Maps, signing out and in with the QR code, steering-wheel next / previous (standard media keys to the Spotify session), the BYD voice button opening Spotify, Spotify Connect from a laptop, download marks.

## Not yet verified

The exact release APK has not been installed in a car (the car runs pre-release builds with the same versionCode). Right-hand-drive layouts, DiLink 5.1 / Android 13 and other head units remain unverified; Arabic only on an emulator. BYD DiLink has no speech recogniser for apps, so the search microphone does not appear there.

## Update feed

The server does not read GitHub. After the release, the operator publishes [spotify-update-test.12.json](headunit-test.12/spotify-update-test.12.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
