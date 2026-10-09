# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test APK](https://github.com/romanchukg-cloud/spotify-byd/releases/tag/v5.5.0-byd-unified-test.1) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-byd/)

## What has been tested

Installation, interface startup, connection to the internal service, and opening the sign-in screen have been tested on an Android 35 emulator.

**This single APK has not yet been tested in a car.** Audio, the catalog after sign-in, steering-wheel controls, closing the app during playback, and offline downloads still need testing.

Android 9 or newer is required. Compatibility with other manufacturers and head units has not yet been verified; this is not a universal build.

## How to install

1. Download the APK from GitHub Releases and transfer it to the infotainment system.
2. Update our previous test build with the same signature without uninstalling it: your sign-in may be preserved. Builds signed with a different key cannot be updated this way.
3. Open Spotify and sign in using the QR code if needed.
4. Check music playback and controls. Remove the old separate BYD interface only after the new APK passes these checks.

Android 9+ · 54.6 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-BYD-unified-test.apk` · 57 243 570 bytes

SHA-256 of the current test build:

```text
0e057caf7f3a7962da0ba73207b16a7f699cc8b3220d198d9102bf60654d3d65
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.
