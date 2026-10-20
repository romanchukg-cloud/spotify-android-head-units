# HeadUnit test.13 — Swipes, right-hand drive, edge to edge, a widget for other apps, no BYD player code

Signed test.13 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.13).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.13.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95490** (published test.12: 95489).
- SHA-256: `d7469ce9383defdc9455e40b7fcbabb101ca7f1d55cfd37ddb8b4d684de3433c`
- Size: **48,279,511 bytes** (test.12: 58,378,169).
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.12 (apksigner; `zipalign -c -P 16 4`). test.13 installs as an update over test.12.
- Source: commit [`58391d5`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/58391d58698841218cc9d1f08527c6d4dbaafc9f/adapter). See [release.json](headunit-test.13/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key. `adapter/incremental_release.py` cannot make this release: the manifest loses the BYD-derived components and gains the widget service.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (sections "Test.13").

- **No BYD player code:** no relocated interface (`classes3.dex`), no second resource package, no BYD manifest components (its media service for the voice assistant, permission activity, queue and voice activities, lifecycle provider, metadata) and no external-storage permissions. The resource table and every other payload are the Spotify container's own; the Figtree typeface is in `assets/hu_fonts`.
- **Swipes** (`HuSkipSwipe`) on the mini-player and the player switch tracks.
- **Right-hand drive** (`HeadUnitDriver`): Head unit settings → Steering wheel; Auto asks BYD's car service (`ICarInfoService.getDriverSeat` through its provider) or Android Automotive's occupant zone service.
- **Edge to edge:** no outer gap; the navigation lies on the window's background; window corners at the cards' 22 dp. The full-screen player uses compact controls in windows lower than 820 dp (a portrait split screen).
- **Widget for other apps** (`PanelProviderService`, [PANEL_PROVIDER.md](../adapter/PANEL_PROVIDER.md)): a compact player drawn by Spotify and embedded in an allowed host's SurfaceView (`SurfaceControlViewHost`, Android 11+), with every message's caller checked against the allowed host package and certificate.
- **Changed copies say so** (`HuFree.genuine`), terms in [NOTICE.md](../NOTICE.md), a note for AI agents in [AGENTS.md](../AGENTS.md).

## Verification

- **152** updater, **39** sign-in and **14** widget-access plain-JVM checks pass.
- The release audit passed **48/48**; see [apk-audit.txt](headunit-test.13/apk-audit.txt). The compatibility audit reports no failures.
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators: the published test.12 APK (hash checked) installed and started, test.13 installed over it as an update (versionCode 95490), the player started, the update job ran against the live feed and reported `up_to_date`; no crash. `adapter/tests/accounts_ui.py` passed on Android 13 (800×480).
- Android 12 emulator: swipes, right-hand drive from the setting, the widget in its debug test host (sizes of the Google Maps panel in a BYD Tang, day and night, resize, close and host death without leftover surfaces), the edge-to-edge screen and the player in a portrait split-screen size.
- BYD Tang (DiLink 5, Android 12): builds of test.13 — swipes, steering-wheel buttons, the BYD voice button, the ambient rhythm, the car reporting left-hand drive (`Driver seat=1`), split screen with Google Maps; the widget shown by a development build of Google Maps for BYD. This exact APK is installed in the car (hash checked).

## Not yet verified

A right-hand-drive car (the seat value comes from BYD's own code), DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator. The widget needs a host app; no published app shows it yet.

## Update feed

The server does not read GitHub. After the release, the operator publishes [spotify-update-test.13.json](headunit-test.13/spotify-update-test.13.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
