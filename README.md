# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units, with an interface of its own made for the car: one APK, one icon and the standard Spotify sign-in. Free — never pay for it.

[Download test.16 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.16/Spotify-5.5.0-HeadUnit-test.16.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

![Home at night in a BYD car](docs/screens/car-home-night.jpg)

## What has been tested

Headunit-test.16 shows where the music plays and moves it between this car and your other devices (Spotify Connect); the mini-player has the time, shuffle, repeat and the devices, the screen a thin frame, and the navigation closes the full-screen player. Since test.14 it also has home-screen widgets for launchers that let you add widgets (five layouts by size, day and night), opens Spotify links from other apps, tells other apps what is playing and can start Spotify in the background when the head unit starts (not on BYD). It was made with the full reference build. The release audit reports 51 PASS / 0 FAIL; 152 updater, 39 sign-in, 14 panel-access and 19 playback-start checks pass, and the signing certificate equals test.15. On Android 9 and 13 emulators test.16 installs as an update over test.15 and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. The widgets were checked in the stock launchers of Android 11 and 12 emulators; links opened playlists, albums and artists there. A build with the same code runs in a BYD Tang (DiLink 5, Android 12), where moving the music to a laptop and back was checked.

**Experimental headunit-test.16.** Moving the music was checked with a laptop only. The widgets have shown only made-up tracks so far, and BYD's own launcher cannot show them. A song link playing and a real app reading what is playing are unverified. Right-hand drive has not been seen on a right-hand-drive car; DiLink 5.1 / Android 13 and other head units remain unverified; Arabic was checked on an emulator only. The panel block appears only in apps that support it. Spotify keeps downloads off for its Automotive client; this build turns the client's support on, so Spotify may change how downloads behave.

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

Android 9+ · 46.1 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.16.apk` · 48 370 802 bytes

SHA-256 of the current test build:

```text
517be876fa0cc2fb8a4a5c10b8dc7557da8b15502b3f8bf34929fb5ce92b484a
```

Unofficial experimental project. This app is free — if you paid for it, ask for your money back. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java code — the interface, the integration, patch and build scripts, audits and emulator tests — is in the repository, under the terms in NOTICE.md (free of charge, no selling, keep the free-of-charge notice). The proprietary Spotify backend is a local build input. Since test.13 the APK contains no part of the BYD (Vivid) player app.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.16.md) · [NOTICE](NOTICE.md)
