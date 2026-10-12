# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.5/Spotify-5.5.0-HeadUnit-test.5.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

## What has been tested

Headunit-test.5: APK audit 28 PASS / 0 FAIL. Android 9 and 15 emulator fixtures cover delayed player readiness, a lost first Play command, playback acknowledgement, pause cancellation and account transition guards. Logged-out settings and media-controller policies also pass. Actual audio after car sleep or reboot still needs verification.

**Experimental headunit-test.5.** Signed-in browsing and audio, a real A → B → A account switch, offline downloads, steering-wheel controls, actual sleep/reboot audio restoration and BYD/AAOS hardware regression still need verification.

Android 9+ (API 28) is required; Android 8 is a later phase. BYD, AAOS and GENERIC profiles are implemented; only GENERIC was tested in this build. Real head units from other manufacturers and AAOS remain unverified.

## How to install

1. Use the download button above to get the APK, then transfer it to the infotainment system.
2. Update our previous test build with the same signature without uninstalling it: your sign-in may be preserved. Builds signed with a different key cannot be updated this way.
3. Open Spotify and sign in using the QR code if needed.
4. To add another driver: Head unit settings → Accounts → Add account · QR. Switching stops playback and restarts the player.
5. Check music playback and controls. Remove the old separate BYD interface only after the new APK passes these checks.

Android 9+ · 55.2 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.5.apk` · 57 876 480 bytes

SHA-256 of the current test build:

```text
98b19fb1336fc41c0cf776ffe763b7e1b2850c010421266ff07fa834420112a2
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.5.md)
