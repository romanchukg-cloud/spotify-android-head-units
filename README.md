# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units, with an interface of its own made for the car: one APK, one icon and the standard Spotify sign-in. Free — never pay for it.

[Download test.13 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.13/Spotify-5.5.0-HeadUnit-test.13.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

![Home at night in a BYD car](docs/screens/car-home-night.jpg)

## What has been tested

Headunit-test.13 removes the last parts of the BYD (Vivid) player app from the APK, adds swiping between tracks, right-hand drive (detected on BYD) and an interface that reaches the screen's edges, keeps a large cover in a portrait split screen, and adds a Spotify widget that other apps can show in their panels (Google Maps for BYD will be the first). A copy re-signed with another key now says it has been changed. It was made with the full reference build. The release audit reports 48 PASS / 0 FAIL, 152 updater, 39 sign-in and 14 panel-access checks pass, and the signing certificate equals test.12. On Android 9 and 13 emulators test.13 installs as an update over test.12 and runs its update check without a crash. Builds of test.13 ran in a BYD Tang (DiLink 5, Android 12): swipes, steering-wheel buttons, the BYD voice button, the ambient rhythm, the car reporting left-hand drive and split screen with Google Maps; this exact file is installed there.

**Experimental headunit-test.13.** Right-hand drive has not been seen on a right-hand-drive car (the value comes from BYD's own code); DiLink 5.1 / Android 13 and other head units remain unverified; Arabic was checked on an emulator only. The widget appears only in apps that support it; support in Google Maps for BYD is in progress. Spotify keeps downloads off for its Automotive client; this build turns the client's support on, so Spotify may change how downloads behave.

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

Android 9+ · 46.0 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.13.apk` · 48 279 511 bytes

SHA-256 of the current test build:

```text
d7469ce9383defdc9455e40b7fcbabb101ca7f1d55cfd37ddb8b4d684de3433c
```

Unofficial experimental project. This app is free — if you paid for it, ask for your money back. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java code — the interface, the integration, patch and build scripts, audits and emulator tests — is in the repository, under the terms in NOTICE.md (free of charge, no selling, keep the free-of-charge notice). The proprietary Spotify backend is a local build input. Since test.13 the APK contains no part of the BYD (Vivid) player app.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.13.md) · [NOTICE](NOTICE.md)
