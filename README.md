# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units. The BYD-derived interface and music service are combined in one APK, with one icon and the standard Spotify sign-in flow.

[Download test.10 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.10/Spotify-5.5.0-HeadUnit-test.10.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

![Home in the New style in a BYD car, day theme](docs/screens/car-home-day.jpg)

## What has been tested

Headunit-test.10 adds the New style, on by default: rounded cards, the Figtree typeface, day and night themes, colour from cover art, a reordered player with a working like button, the cover and an equaliser in the queue, rounded app corners and new Head unit settings. Classic keeps the previous interface. Closing Spotify always stops the music, and switching day/night with Library open no longer crashes the player. Because the interface resources changed, it was made with the full reference build. The release audit reports 45 PASS / 0 FAIL, 152 updater and 39 sign-in checks pass, and the signing certificate equals test.9. In the owner's car (BYD DiLink 5.0 / Android 12) test.10 was installed as an update over test.9: sign-in and music were kept, and the New style was checked in the day theme without a crash. On an Android 12 emulator with a signed-in account it kept the sign-in after updating over test.9 and was checked in day and night, at 1024×600 and in portrait, switching between Classic and New.

**Experimental headunit-test.10.** The first update from test.9 to test.10 through the app has not yet been seen on a head unit. The New style has not been checked in Arabic (right-to-left) or with right-hand-drive layouts, which keep the Classic player arrangement. Real vehicle sleep/reboot, account A → B → A, offline downloads, steering-wheel controls, DiLink 5.1 / Android 13 and other head units remain unverified.

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

Android 9+ · 55.5 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.10.apk` · 58 222 521 bytes

SHA-256 of the current test build:

```text
ccf440e37acd3208f2b076d1023bfec9d5370c626230b65f12980b73f6fcf806
```

Unofficial experimental project. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java integration code, patch/build scripts, audit and emulator test sources are available in the repository. The proprietary Spotify backend and BYD interface are local build inputs.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.10.md)
