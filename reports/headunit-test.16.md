# HeadUnit test.16 — Devices (Spotify Connect), shuffle, repeat and the devices in the mini-player

Signed test.16 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.16).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.16.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95493** (published test.15: 95492).
- SHA-256: `517be876fa0cc2fb8a4a5c10b8dc7557da8b15502b3f8bf34929fb5ce92b484a`
- Size: **48,370,802 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.15 (apksigner; `zipalign -c -P 16 4`). test.16 installs as an update over test.15.
- Source: commit [`29b2663`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/29b2663/adapter). See [release.json](headunit-test.16/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (section "After test.15").

- **Devices** (`HeadUnitConnect`, `ConnectWire`, `HuConnect`): the backend's ConnectService client, captured from its media session callback, gives the core's Esperanto router; `State` (subscription), `Pull` and `Transfer` requests, decoded by hand. The devices menu in the player and the mini-player; "Playing on …" in green while another device plays.
- **Mini-player:** shuffle, repeat and the devices button from 900 dp of width.

## Verification

- **152** updater, **39** sign-in, **14** panel-access, **13** Connect-message plain-JVM checks and **19** playback-start fixture checks pass.
- The release audit passed **52/52**; see [apk-audit.txt](headunit-test.16/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators: the published test.15 APK (hash checked) installed and started, test.16 installed over it as an update (versionCode 95493), the player started, the update job ran and reported its status; no crash. `adapter/tests/accounts_ui.py` passed on Android 13.
- BYD Tang (DiLink 5, Android 12), a build of the same code (versionCode 95492): the devices menu listed this car (playing) and the owner's laptop; `Connect Transfer to computer: ok` and `Connect Pull: ok` — the music moved to the laptop and back. The mini-player's buttons and time, the frame and the navigation closing the player were checked by the owner.

## Not yet verified

Moving the music to a phone, speakers or a group, a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

## Update feed

After the release, the operator publishes [spotify-update-test.16.json](headunit-test.16/spotify-update-test.16.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
