# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.3) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

## What has been tested

Headunit-test.3: static APK audit 23 PASS / 0 FAIL; 16 policy checks on ordinary Android 9, 11, 13 and 15 emulators. Logged-out login and settings screens checked at 800×480@160, 1024×600@160, 1280×720@240, 1920×720@240 and 768×1024 portrait. An independent MediaBrowserCompat client connects to the root with external control enabled and is rejected when disabled. Android 11 reboot clears the close gate and sends the resume request. The earlier test.2 was tested on BYD DiLink5; those results do not certify this new build.

**Experimental headunit-test.3.** Signed-in Home, Library, playlists, audio, closed/dead-process media controls, Now Playing Back navigation, actual resume after sleep/reboot, offline downloads and BYD DiLink5 regression still need verification. The multi-account experiment is not included.

Android 9+ (API 28) is required; Android 8 is a later phase. BYD, AAOS and GENERIC profiles are implemented; only GENERIC was tested in this build. Real head units from other manufacturers and AAOS remain unverified.

## How to install

1. Download the APK from GitHub Releases and transfer it to the infotainment system.
2. Update our previous test build with the same signature without uninstalling it: your sign-in may be preserved. Builds signed with a different key cannot be updated this way.
3. Open Spotify and sign in using the QR code if needed.
4. Check music playback and controls. Remove the old separate BYD interface only after the new APK passes these checks.

Android 9+ · 55.2 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.3.apk` · 57 835 520 bytes

SHA-256 of the current test build:

```text
9dc4a3bbd99eabee77ab6b47576f0b431966386c5866e055801d61d78dac059b
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.3.md)
