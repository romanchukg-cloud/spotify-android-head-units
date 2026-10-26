# HeadUnit test.18 — Spotify in Waze's audio panel

Signed test.18 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.18).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.18.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95495** (published test.17: 95494).
- SHA-256: `114962299a487d7459481567a196f969fe3d76a67e720dca483fce344863467d`
- Size: **48,419,954 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.17 (apksigner; `zipalign -c -P 16 4`). test.18 installs as an update over test.17.
- Source: commit [`447b97f`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/447b97f/adapter). See [release.json](headunit-test.18/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (section "After test.17").

- **Waze's Audio SDK** (`HuWazeLink`): Waze sends `com.waze.sdk.audio.ACTION_INIT` with a token when Spotify is picked; the app registers with an allowed Waze (package and certificate) at its `SdkService`, and Waze browses and controls the music through the backend's media browser service. `versionCode` 10 gives Waze's panel two buttons beside the track.
- **Session actions with the phone app's names:** the backend's custom actions are `ADD_TO_COLLECTION`/`REMOVE_FROM_COLLECTION` and `TURN_SHUFFLE_ON`/`TURN_SHUFFLE_OFF`, which Waze's panel looks for; our player reads the same names.
- **App Remote** (`HuAppRemoteService`): Spotify's App Remote service for apps on the Spotify Android SDK (allowed clients only, by uid and certificate), with a foreground notification while a client is connected.

## Verification

- **152** updater, **39** sign-in, **14** panel-access, **13** Connect-message, **66** App Remote plain-JVM checks and **19** playback-start fixture checks pass.
- The release audit passed **56/56**; see [apk-audit.txt](headunit-test.18/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators (network off): the published test.17 APK (hash checked) installed and started, test.18 installed over it as an update (versionCode 95495), the player started, the update job ran and reported its status; no crash. `adapter/tests/accounts_ui.py` passed on Android 13.
- Android 12 emulator with a copy of Waze for BYD 5.22 that only adds logging (Spotify's network blocked): Waze sent its init, the app registered, Waze connected the media browser, asked for its consent and showed the Spotify panel with Home, Recents, Browse and Library. Spotify's own SDK (App Remote 0.8.0 and 0.6.1) clients read the player state, the browse tree and covers.
- BYD Tang (DiLink 5, Android 12) with Waze for BYD 5.22, a build of the same code (versionCode 95494): Spotify opens in Waze's audio panel; after a reinstall Waze connected again by itself; the heart appears beside the track and shuffle in Liked Songs (Spotify offers no shuffle in some playlists); the heart works in the player.

## Not yet verified

Other Waze builds; repeat (Waze shows none for Spotify); whether the backend starts in time when Waze wakes it from nothing; a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

## Update feed

After the release, the operator publishes [spotify-update-test.18.json](headunit-test.18/spotify-update-test.18.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
