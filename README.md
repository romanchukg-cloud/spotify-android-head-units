# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test.6 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.6/Spotify-5.5.0-HeadUnit-test.6.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

## What has been tested

Headunit-test.6: APK audit 31 PASS / 0 FAIL. Android 9 and 15 emulators each pass 18 synthetic rhythm checks and 19 playback-resume checks. On a 2024 BYD Tang EV with DiLink 5 / Android 12, the in-place update preserved sign-in; music starts the native rhythm, pause stops it and process death releases it. The visible lamp response was confirmed by the user.

**Experimental headunit-test.6.** Real vehicle sleep/reboot restoration, long-term use, real account A → B → A, offline downloads, steering-wheel controls and other BYD/AAOS hardware remain unverified. The broader account and UI suites were not rerun for test.6.

Android 9+ (API 28). BYD rhythm is verified on DiLink 5 / Android 12; DiLink 5.1 / Android 13 and other firmware are not yet verified. AAOS and GENERIC profiles are implemented; real head units from other manufacturers and AAOS remain unverified.

## How to install

1. Use the download button above to get the APK, then transfer it to the infotainment system.
2. Update our previous test build with the same signature without uninstalling it: your sign-in may be preserved. Builds signed with a different key cannot be updated this way.
3. Open Spotify and sign in using the QR code if needed.
4. For BYD ambient lighting, enable Music rhythm in the car’s own ambient-light settings. This switch is in the vehicle settings, not in Spotify.
5. To add another driver: Head unit settings → Accounts → Add account · QR. Switching stops playback and restarts the player.
6. Check music playback and controls. Remove the old separate BYD interface only after the new APK passes these checks.

Android 9+ · 55.2 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.6.apk` · 57 880 576 bytes

SHA-256 of the current test build:

```text
f5eac6dcb254d095bc9bf26a7aabaf111ea6af71079fcccd79b4f3463cf4c086
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.6.md)
