# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test.11 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.11/Spotify-5.5.0-HeadUnit-test.11.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

![Home in the New style in a BYD car, day theme](docs/screens/car-home-day.jpg)

## What has been tested

Headunit-test.11 adds offline downloads (experimental, Spotify Premium), voice search when the head unit has a speech recogniser, a queue editor with song and artist radio, and Jam with a QR code for passengers. It also fixes a crash of the music process when a track was removed from or added to the queue. Because the interface changed, it was made with the full reference build. The release audit reports 45 PASS / 0 FAIL, 152 updater and 39 sign-in checks pass, and the signing certificate equals test.10. On Android 9 and 13 emulators test.11 installs as an update over test.10 and runs its update check without a crash. On an Android 12 emulator with a signed-in Premium account, downloads played offline (also after a cold start), voice search worked with a test recogniser, and queue editing, radio and Jam worked.

**Experimental headunit-test.11.** Test.11 has not been checked in a car yet, and whether BYD DiLink has a speech recogniser for the microphone button is unknown. Spotify keeps downloads off for its Automotive client; this build turns the client's support on, so Spotify may change how downloads behave. The New style in Arabic (right-to-left), right-hand-drive layouts, DiLink 5.1 / Android 13 and other head units remain unverified.

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

Android 9+ · 55.6 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.11.apk` · 58 251 193 bytes

SHA-256 of the current test build:

```text
6e1cf2566026f08859861fc63ab1a6a53eba8c84d1329c29985708e4e7030fe7
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.11.md)
