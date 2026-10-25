# HeadUnit test.17 — Smooth motion: loading placeholders, transitions and covers fading in

Signed test.17 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.17).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.17.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95494** (published test.16: 95493).
- SHA-256: `ad06da3de32647acdaba22b7411dc8a58468a09a0ee44814781cab2da646f8eb`
- Size: **48,387,186 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.16 (apksigner; `zipalign -c -P 16 4`). test.17 installs as an update over test.16.
- Source: commit [`c76a6b2`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/c76a6b2/adapter). See [release.json](headunit-test.17/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (section "After test.16").

- **Motion** (`HuMotion`): Material 3 easing and durations; skeletons in place of "Loading…" (Home, grids, Library's pills and grid, a page's track rows and count line, search's first results) with one shimmer shared across the screen; the skeleton fading out in its parent's overlay while the items on screen rise in; shared-axis page transitions and fade-through tabs; background crossfades.
- **Covers:** loaded covers fade in over their placeholder; a new track's cover and blur fade in over the old ones (mini-player and player).
- **Navigation, player and queue:** the rail's pill slides to the chosen tab and every item ripples; the player and the queue use emphasized easing and turn back from where they are when reopened while closing.

## Verification

- **152** updater, **39** sign-in, **14** panel-access, **13** Connect-message plain-JVM checks and **19** playback-start fixture checks pass.
- The release audit passed **52/52**; see [apk-audit.txt](headunit-test.17/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators (network off): the published test.16 APK (hash checked) installed and started, test.17 installed over it as an update (versionCode 95494), the player started, the update job ran and reported its status; no crash. `adapter/tests/accounts_ui.py` passed on Android 13.
- The motion on an Android 12 emulator (network off), light and dark: skeletons and shimmer, Library's pills and grid rising in, a playlist's skeleton rows handing over to its tracks, its cover and panel fading in, page and tab transitions, the rail's pill, the player rising and leaving; no drawing while idle.
- BYD Tang (DiLink 5, Android 12), a build of the same code (versionCode 95493): installed, playback resumed, no crash; the owner checked the motion.

## Not yet verified

A right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

## Update feed

After the release, the operator publishes [spotify-update-test.17.json](headunit-test.17/spotify-update-test.17.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
