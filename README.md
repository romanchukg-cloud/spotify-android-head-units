# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test.7 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.7/Spotify-5.5.0-HeadUnit-test.7.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

## What has been tested

Headunit-test.7: APK audit 39 PASS / 0 FAIL. Android 9, 12 and 13 emulators each passed 94 diagnostic logic checks, 7 Support/notice UI checks, 8 process-restart persistence checks, 18 synthetic rhythm checks and 19 playback-resume checks. Diagnostics delivery was tested with a fake transport; no production Spotify report was submitted.

Experimental headunit-test.7. This version has not been installed on the car. The earlier test.6 lighting integration was verified on a BYD Tang EV with DiLink 5 / Android 12. Physical DiLink 5.1 / Android 13 lighting, real vehicle sleep/reboot, real account switching and other head units remain unverified.

Android 9+ (API 28). BYD, AAOS and GENERIC profiles are implemented; compatibility depends on vehicle firmware.

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

`Spotify-5.5.0-HeadUnit-test.7.apk` · 57 946 112 bytes

SHA-256 of the current test build:

```text
9b127d9e9f9b05dfa210207370e9f21dd9ca5eb61ec6e4d6c6bb748d069f4da7
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.7.md)
