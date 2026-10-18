# Spotify for Android Head Units

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

An experimental Spotify Automotive player for Android head units, with an interface of its own made for the car: one APK, one icon and the standard Spotify sign-in. Free — never pay for it.

[Download test.12 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.12/Spotify-5.5.0-HeadUnit-test.12.apk) · [🌐 Spotify for Android Head Units](https://romanchukg-cloud.github.io/spotify-android-head-units/)

![Home at night in a BYD car](docs/screens/car-home-night.jpg)

## What has been tested

Headunit-test.12 replaces the BYD-derived interface with one of its own: Home, Library, Search, pages for playlists, albums, artists and podcasts, a full-screen player and the queue on blurred cover art, download progress, a sleep timer and a notice that the app is free. It was made with the full reference build. The release audit reports 46 PASS / 0 FAIL, 152 updater and 39 sign-in checks pass, and the signing certificate equals test.11. On Android 9 and 13 emulators test.12 installs as an update over test.11 and runs its update check without a crash. Pre-release builds ran in a BYD car (DiLink 5, Android 12): landscape, portrait and split screen with navigation, QR sign-in, steering-wheel buttons and Spotify Connect.

**Experimental headunit-test.12.** The exact release file has not been installed in a car yet. Right-hand-drive layouts, DiLink 5.1 / Android 13 and other head units remain unverified; Arabic was checked on an emulator only. Spotify keeps downloads off for its Automotive client; this build turns the client's support on, so Spotify may change how downloads behave.

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

Android 9+ · 55.7 MiB · unofficial adaptation

## Verify the file

`Spotify-5.5.0-HeadUnit-test.12.apk` · 58 378 169 bytes

SHA-256 of the current test build:

```text
00f82a80d9a40d9d3138c3fde1d4c3c88edf270c380c54a70fa01032ab6982a1
```

Unofficial experimental project. This app is free — if you paid for it, ask for your money back. Spotify and BYD are trademarks of their respective owners.

## Source code and validation

Our Java code — the interface, the integration, patch and build scripts, audits and emulator tests — is in the repository. The proprietary Spotify backend is a local build input. The APK still contains parts of the BYD (Vivid) player app (its media service for the car's voice assistant, resources and a typeface); they are to be removed.

[Java / build](adapter/) · [What has been tested](reports/headunit-test.12.md)
