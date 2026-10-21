# HeadUnit test.14 — Home-screen widgets, Spotify links, what's playing for other apps, start with the head unit

Signed test.14 prerelease. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.14).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.14.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95491** (published test.13: 95490).
- SHA-256: `9a6408d84b2d193762c72efe5a082627e771c696ac74748251456294109cf905`
- Size: **48,350,322 bytes** (test.13: 48,279,511).
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.13 (apksigner; `zipalign -c -P 16 4`). test.14 installs as an update over test.13.
- Source: commit [`d7ac349`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/d7ac349/adapter). See [release.json](headunit-test.14/release.json).

## How it was built

The full reference build `adapter/build.sh` with the prepared Spotify input only, signed with the release key. `adapter/incremental_release.py` cannot make this release: the manifest gains the widget providers, the link filters and a second resource package.

## Implemented in this APK

See [adapter/README.md](../adapter/README.md) (sections "After test.13").

- **Home-screen widgets** (`HuWidgetWide`, `HuWidgetSquare`, `HuWidgets`): five layouts chosen by the widget's size (one `RemoteViews` by size on Android 12+, the size the launcher reports before), day and night colours from resources, preview pictures for older launchers, the last song kept paused without the backend. Their resources are a second package again (`0x7e`, `res-hu/`, `widget_resources.py`, `merge_resources.py`); the backend's chunks stay byte for byte and its string pool keeps its UTF-8 encoding. BYD's DiLink 5 launcher offers only its listed widgets.
- **Spotify links** (`HuLinks`): `spotify:`, `open.spotify.com` and `spotify.link` open a page (title and cover from Spotify's public oEmbed, the backend's browser with `ref=search`), play a song or episode, or search.
- **What's playing for other apps** (`HuBroadcasts`): Spotify's `metadatachanged`, `playbackstatechanged` and `queuechanged` broadcasts (a new `setQueue` hook), with a setting.
- **Start with the head unit** (`start_boot`, off by default): on `BOOT_COMPLETED` the backend starts without a screen or music. Not on BYD, whose quickboot force-stops apps.

## Verification

- **152** updater, **39** sign-in, **14** panel-access plain-JVM checks and **19** playback-start fixture checks (`tests/resume`) pass.
- The release audit passed **51/51**; see [apk-audit.txt](headunit-test.14/apk-audit.txt).
- `adapter/tests/updates/emulator.sh` on **Android 9 (API 28)** and **Android 13 (API 33)** emulators: the published test.13 APK (hash checked) installed and started, test.14 installed over it as an update (versionCode 95491), the player started, the update job ran and reported its status; no crash. `adapter/tests/accounts_ui.py` passed on Android 13.
- Android 12 emulator: each widget layout in the debug host and in the stock launcher, day and night, the buttons, Play with Spotify stopped; `spotify:search:`, album, playlist (`intl-uk`) and artist links with title, cover and tracks; the broadcasts with made-up session objects (exact sequence, nothing with the setting off); start with the head unit after `adb reboot`. Android 11 emulator: the provider picks the layouts from the reported sizes, the launcher's list shows the preview pictures.
- BYD Tang (DiLink 5, Android 12): this exact APK is installed (hash checked, data kept); its system registers both widget providers.

## Not yet verified

The widgets with a real song, a song link playing, a real app reading the broadcasts, a short `spotify.link`, a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

## Update feed

The server does not read GitHub. After the release, the operator publishes [spotify-update-test.14.json](headunit-test.14/spotify-update-test.14.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates).
