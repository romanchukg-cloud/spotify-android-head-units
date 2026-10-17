# HeadUnit test.11 — Offline downloads, voice search, queue editor, radio and Jam

Signed test.11 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.11).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.11.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95488** (published test.10: 95487).
- SHA-256: `6e1cf2566026f08859861fc63ab1a6a53eba8c84d1329c29985708e4e7030fe7`
- Size: **58,251,193 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.10 (apksigner; `zipalign -c -P 16 4`). test.11 installs as an update over test.10.
- Source: commit [`ec036c9`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/ec036c945dd2edc1d3963f9e3e0c2a329cb603b7/adapter). See [release.json](headunit-test.11/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared local inputs, signed with the release key: the release changes the interface (layouts, smali hooks), which `adapter/incremental_release.py` keeps byte-identical.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (sections "Local test.11").

- **Offline downloads (experimental):** Spotify Automotive 5.5.0 implements downloads (browser actions download / downloading / downloaded, Library → Downloads, the Offline and Storage settings), but its remote configuration `android-automotive.is_offline_enabled` keeps them off for this client, and it adds the actions only for a media host. Patch section `# Offline downloads` reads the flag as on and treats every client as a media host. `HeadUnitDownloads` adds a download button next to the favourite button on playlist and album pages; removal asks for confirmation. Premium and Spotify's download limit still apply on the server; downloading adds the item to Your Library.
- **Voice search:** `HeadUnitVoice` adds a microphone to the search field when an activity handles `RECOGNIZE_SPEECH`; the recognised text plays the best match (`playFromSearch`) and is searched in the field.
- **Queue editor and radio:** an edit button on the queue page opens `HeadUnitQueueActivity` (grouped queue with covers, tap to play, remove, Clear queue for added tracks) with **Song radio** / **Artist radio** (`spotify:station:track:…` / `spotify:station:artist:…`).
- **Jam:** a Jam button on the queue page and in the editor opens Spotify's own settings page scrolled to Jam (Start a Jam, QR code for up to 5 friends, End Jam).
- **Fix:** the interface's queue commands (remove from queue, add to queue) sent its relocated `MediaDescriptionCompat`; the backend created that class and its `onCommand` cast crashed the music process (since test.3). `ParcelBridge.putBackendParcelable` now recreates the backend's class for these Bundles.

## Verification

- **152** plain-JVM updater checks and **39** sign-in checks pass.
- The release audit passed **45/45**; see [apk-audit.txt](headunit-test.11/apk-audit.txt). The compatibility audit reports no failures.
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators: the published test.10 APK was installed and started, test.11 installed over it as an update (versionCode 95488), the player started, the update job ran against the live feed and reported `up_to_date`; no crash.
- Android 12 emulator with a signed-in Premium account:
  - downloads: the download actions appear on playlists and albums; a one-track album and a playlist downloaded, appeared under Library → Downloads, played with the network off (also after a cold start without network) and were removed again;
  - voice search with a test recogniser (`adapter/tests/downloads`, `FakeRecognizer`): the button appears, the prompt is localised, the spoken text plays and shows its results; without a recogniser the button stays hidden;
  - queue: the interface's remove cross and add to queue no longer crash and change the queue; removing in the editor and Clear queue work; song and artist radio start a station;
  - Jam: Start a Jam shows the QR code fully on screen; End Jam ends it.
- The probe `adapter/tests/downloads` (independent media-host client) lists the browse tree with each item's actions and sends actions, plays search queries and URIs and logs metadata.

## Not yet verified

Test.11 has not been installed in a car. Whether BYD DiLink has a speech recogniser for the microphone button is unknown. Long downloads of large playlists, Spotify's device limit, offline playback after weeks without a connection, a free account (an error message is shown), the New style in Arabic (right-to-left), right-hand-drive layouts, DiLink 5.1 / Android 13 and other head units remain unverified.

## Update feed

The server does not read GitHub. After the release, the operator publishes [spotify-update-test.11.json](headunit-test.11/spotify-update-test.11.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
