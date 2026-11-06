# HeadUnit test.21 alongside — Next to a built-in Spotify

Signed test build for cars with Spotify as a system app. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.21-alongside).

## Build identity

- File: `Spotify-5.5.0-HeadUnit-test.21-alongside.apk`
- Package: `io.github.romanchukgcloud.spotifyhu`; upstream version: `5.5.0.16251`; versionCode: **95498**, the same as the regular test.21.
- SHA-256: `a723b2086628735bed69910e34db2a8597a6c10fddc6f1f44088b8ace1cf3c94`
- Size: **48,424,050 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, the same as the regular builds (apksigner; `zipalign -c -P 16 4`).
- Source: commit [`9250030`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/9250030/adapter). See [release.json](headunit-test.21-alongside/release.json).

## How it was built

`HU_EDITION=alongside adapter/build.sh`, with the prepared Spotify input only, signed with the release key. See [adapter/README.md](../adapter/README.md) (section "The edition next to a built-in Spotify").

## Verification

- The release audit passed **58/58**, with the edition's own check; see [apk-audit.txt](headunit-test.21-alongside/apk-audit.txt).
- This APK on an Android 13 emulator next to the original Spotify from the Play Store (another signature): both installed, and the original opens its own sign-in screen. Both account types are registered, and the sign-in in this edition used its own. Signing in through spotify.com/pair worked with one account, covers load, the owner heard sound, the Accounts page opens, and the Updates tile is gone. Automatic sending was off.
- The regular build from the same source: release audit **57/57**, and **152** updater, **64** sign-in, **14** panel-access, **13** Connect-message and **66** App Remote plain-JVM checks pass. Those checks do not run this edition's settings.

## Not yet verified

A real car; Spotify installed as a system app rather than a normal install; the steering wheel buttons; BYD's media card; the lighting; updating this build over itself; the update and playback-start emulator tests on this edition; other Android versions.

## Not in this build

The Waze panel, in-app updates and any update feed. The update server was not changed.
