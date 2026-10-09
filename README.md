# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-unified-test.2) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

## What has been tested

Tested on BYD DiLink5 with Android 12: updating with sign-in preserved, Home, Recents, Library and playlist tracks. Music continued while opening playlists, and selecting a track started playback. The user confirmed that closing the app stops music. QR sign-in was also tested on an Android 35 emulator.

**Experimental test release .2.** Steering-wheel controls, offline downloads and other head units still need testing.

Android 9 or newer is required. Compatibility with other manufacturers and head units has not yet been verified; this is not a universal build.

## How to install

1. Download the APK from GitHub Releases and transfer it to the infotainment system.
2. Update our previous test build with the same signature without uninstalling it: your sign-in may be preserved. Builds signed with a different key cannot be updated this way.
3. Open Spotify and sign in using the QR code if needed.
4. Check music playback and controls. Remove the old separate BYD interface only after the new APK passes these checks.

Android 9+ · 54.6 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-BYD-unified-test.apk` · 57 247 666 bytes

SHA-256 of the current test build:

```text
0fa0b0d9eb5b9b95c989b59f751c838d0b318d9b481ff788572f2d9e5635a124
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.
