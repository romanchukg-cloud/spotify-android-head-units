# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test.9 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.9/Spotify-5.5.0-HeadUnit-test.9.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

## What has been tested

Headunit-test.9 adds in-app updates: once a day Spotify asks the project's update server for a newer test build, downloads it in the background (on Wi-Fi only by default), checks its size, SHA-256, package, version and signing certificate, and installs it only after you confirm Android's Update window. It is built from the published test.8: only the adapter DEX and the manifest (versionCode, the install permission and one private receiver) changed. The release audit reports 45 PASS / 0 FAIL, 152 updater and 39 sign-in checks pass, and on Android 9 and 13 emulators test.9 installs as an update over test.8, starts and runs its update check against the live server without a crash.

**Experimental headunit-test.9.** Downloading and installing a newer build through the app can only be tried once test.10 is published. Test.9 has not been installed in a car, and the sign-in changes from test.8 are not yet verified in a car either. Real vehicle sleep/reboot, account A → B → A, offline downloads, steering-wheel controls and other head units remain unverified.

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

`Spotify-5.5.0-HeadUnit-test.9.apk` · 58 011 648 bytes

SHA-256 of the current test build:

```text
f78026ba05fa52c67082851e710379358a05027dfcc47c404b8146147c880cd3
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.9.md)
