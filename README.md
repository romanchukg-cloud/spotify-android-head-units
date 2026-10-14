# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test.8 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.8/Spotify-5.5.0-HeadUnit-test.8.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

## What has been tested

Headunit-test.8 keeps the Spotify sign-in: a stable device ID, no sign-out when an old inactive account is removed in Android settings, and one automatic restore of a login lost on the head unit. It is built from the published test.7: only the Spotify backend DEX, the adapter DEX and the versionCode changed; resources, the BYD interface and native libraries are byte-identical. All 8 new patches matched once, 39 sign-in logic checks and the compatibility audit pass, the release audit reports 42 PASS / 0 FAIL, and the signing certificate equals test.7.

**Experimental headunit-test.8.** The sign-in changes have not been tested with a signed-in account on an emulator or in a car; an actual ANDROID_ID change and recovery after a power cut are unverified. Real vehicle sleep/reboot, account A → B → A, offline downloads, steering-wheel controls and other head units also remain unverified. The test.7 emulator suites were not rerun for test.8.

Android 9+ (API 28). BYD rhythm is verified on DiLink 5 / Android 12; DiLink 5.1 / Android 13 and other firmware are not yet verified. AAOS and GENERIC profiles are implemented; real head units from other manufacturers and AAOS remain unverified.

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## How to install

1. Use the download button above to get the APK, then transfer it to the infotainment system.
2. Update our previous test build with the same signature without uninstalling it: your sign-in may be preserved. Builds signed with a different key cannot be updated this way.
3. Open Spotify and sign in using the QR code if needed.
4. For BYD ambient lighting, enable Music rhythm in the car’s own ambient-light settings. This switch is in the vehicle settings, not in Spotify.
5. To add another driver: Head unit settings → Accounts → Add account · QR. Switching stops playback and restarts the player.
6. Check music playback and controls. Remove the old separate BYD interface only after the new APK passes these checks.

Android 9+ · 55.3 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.8.apk` · 57 962 496 bytes

SHA-256 of the current test build:

```text
caa026818b27aa08de38c4c76dab35a02a6ec975095ed5cba9ed0d57c7dc8a93
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.8.md)
