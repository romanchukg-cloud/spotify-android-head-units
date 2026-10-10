# headunit-test.3 validation — 2026-10-10

Experimental prerelease: **v5.5.0-headunit-test.3**, `Spotify-5.5.0-HeadUnit-test.3.apk`, **57,835,520 bytes**.

SHA-256: `9dc4a3bbd99eabee77ab6b47576f0b431966386c5866e055801d61d78dac059b`.

Package `com.spotify.music`, minimum API **28**, target API 33, versionCode 95482. The release keeps the public test.2 signing certificate: SHA-256 `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`. Package and signer equality permit an in-place update; preservation of a real signed-in session in **this** release awaits the car test. Android 8 is outside this change.

## Changes

- Cached BYD / AAOS / GENERIC detection. Ten BYD adapter methods are guarded at entry; vendor-only listeners are constructed behind that guard. The BYD media-manager component and old separate UI trust are disabled outside BYD.
- Stop-on-close defaults to BYD only. Explicit task removal consults the setting; UI binder death does not close playback. A new boot clears the persistent close gate. Explicit PLAY reopens it.
- System controllers are allowed; third-party controllers follow the setting (default enabled for GENERIC/AAOS, disabled for BYD), with package/UID validation and rejected-package logging.
- A manifest media-button receiver routes six key codes to a foreground playback service. State snapshots, boot and long-screen-sleep resume requests use the common backend, without reviving the vendor restore service. Explicit pause clears resume snapshots.
- Legacy Back dispatch enabled on Android 13+ by disabling `enableOnBackInvokedCallback`.
- Landscape aliases for narrow and short screens, compact dimensions below 400dp height, portrait qualifiers preserved. Main UI density can be set to 80–130% through a private settings provider. The backend and native Spotify screens keep device density.
- Complete Ukrainian and Russian app-owned strings; Arabic and Simplified Chinese completeness checked. There are 33 UI strings plus `app_name`, counted as 34 audit entries. Player-start error wording is neutral; helper notifications/settings are localized in five languages.
- Crashlytics collection metadata disabled; AD_ID permission and Android stamp/vending metadata removed.
- The existing settings button opens head-unit settings, with access to the native Spotify account/audio settings. This is based on public unified test.2; the separate multi-account experiment is **not included**.

## Audit

```sh
python3 tools/apk_audit.py Spotify-5.5.0-HeadUnit-test.3.apk --min-api 28
```

**23 PASS, 0 FAIL.** [Full output](headunit-test.3/apk-audit.txt).

The audit checks final APK manifest policy, guarded vendor call sites, controller hooks, binder-death behavior, unblocked transport commands, UI settings entry, landscape configurations, translations, DEX versions, signature and ZIP/native alignment. All three DEX files use version 039, fixing a launch failure found on Android 9 when the old UI assembler produced version 040.

The old public test.2 APK was run through the same checker as a negative control: **4 PASS, 19 FAIL**. These are newly required policy checks, not a retrospective claim that test.2 failed its original scope. The checker does not certify all obfuscated SDK API references, authentication, audio or firmware compatibility.

[Structural checks](headunit-test.3/structural-checks.json) preserved 2,438 backend payloads, 22,982 backend resource strings, original backend resource chunks, service metadata and all 5,114 relocated UI classes. APK signature and 16 KiB ZIP/native alignment checks pass. A fresh build from the published adapter scripts and the two documented prepared APK inputs also passes [the audit](headunit-test.3/build-reproduction-audit.txt); all three DEX payloads and the manifest match the tested release. Recompiled resource-table normalization/ZIP metadata can differ between builds.

## Device × check matrix

All emulator images are ordinary ARM64 tablets, **not Automotive**. Five logical viewports were selected with `wm size` and `wm density` on each image. PASS below is limited to the named observation; no test account was signed in.

| Device | Logged-out UI + settings, 5 viewports | Policy instrumentation | Independent MediaBrowserCompat | Back from logged-out main | Real Recents / UI-process death | Reboot | Signed-in browsing/audio |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Android 9 / API 28, AOSP | PASS | 16 PASS | Root `spotify:navigation`; OFF rejects | Leaves main UI | Callback logic only | Pending | Pending |
| Android 11 / API 30, AOSP | PASS | 16 PASS | Root; OFF rejects | Leaves main UI | Callback logic only | Gate reset + resume request PASS; audio pending | Pending |
| Android 13 / API 33, AOSP | PASS | 16 PASS | Root; OFF rejects | Leaves main UI | Recents ON: `closed=true`; OFF: `closed=false`. Killing `:bydui` logs binder death and keeps backend PID alive | Pending | Pending |
| Android 15 / API 35, Google Play | PASS | 16 PASS | Root; OFF rejects | Leaves main UI | Callback logic only | Pending | Pending |
| BYD DiLink5 / Android 12 | Pending | Pending | Pending | Pending | Pending | Pending | Pending; car was off |
| Real AAOS / other manufacturers | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

The 16 instrumentation checks cover GENERIC defaults, vendor service disabling, explicit-close setting OFF/ON, explicit PLAY, binder-death callback, pause snapshots, scale bounds, system and third-party policy, UID mismatch, and short/paused screen wake. This changes disposable emulator preferences; it is not an audio simulator. Scale 80% and 130% additionally launched successfully on Android 11.

On Android 13, all six codes (126/127/85/87/88/79) reached the exported receiver and were logged. An **arbitrary third-party synthetic broadcast** was denied foreground-service startup by Android (`ForegroundServiceStartNotAllowedException`); the denial is caught and logged. This does not prove the system media-session path works. `input keyevent` and `cmd media_session dispatch` were issued while logged out, but no real playback session existed, so these remain **unverified**, including cold-process controls. The cold-session instrumentation fixture intentionally ends its process and must not be counted as an audio pass.

The Android 11 reboot test persisted a playing snapshot and closed gate, then rebooted while the fixture was alive. The next boot logged `Command key=126 source=boot-resume`, connected the playback browser and cleared the gate. It validates the request mechanism, **not** actual Spotify audio restoration. Native SDK startup can emit handled missing-AAOS/vendor-class verification diagnostics on a generic tablet; the inspected crash buffers were empty.

## Screenshots

These show the unauthenticated login screen and the new settings, without QR/session credentials. They do not show authenticated Home, Library or playlist layouts. API 28 can retain a larger framebuffer PNG while the logical viewport is overridden. On API 35 two captures were repeated after resize/startup settled; the final screenshots and [matrix JSON](headunit-test.3/matrix.json) show the loaded screens.

| Logical viewport | Android 9 | Android 11 | Android 13 | Android 15 |
| --- | --- | --- | --- | --- |
| 800x480-160 | [Login](headunit-test.3/screenshots/api28-800x480-160-login.png) · [Settings](headunit-test.3/screenshots/api28-800x480-160-settings.png) | [Login](headunit-test.3/screenshots/api30-800x480-160-login.png) · [Settings](headunit-test.3/screenshots/api30-800x480-160-settings.png) | [Login](headunit-test.3/screenshots/api33-800x480-160-login.png) · [Settings](headunit-test.3/screenshots/api33-800x480-160-settings.png) | [Login](headunit-test.3/screenshots/api35-800x480-160-login.png) · [Settings](headunit-test.3/screenshots/api35-800x480-160-settings.png) |
| 1024x600-160 | [Login](headunit-test.3/screenshots/api28-1024x600-160-login.png) · [Settings](headunit-test.3/screenshots/api28-1024x600-160-settings.png) | [Login](headunit-test.3/screenshots/api30-1024x600-160-login.png) · [Settings](headunit-test.3/screenshots/api30-1024x600-160-settings.png) | [Login](headunit-test.3/screenshots/api33-1024x600-160-login.png) · [Settings](headunit-test.3/screenshots/api33-1024x600-160-settings.png) | [Login](headunit-test.3/screenshots/api35-1024x600-160-login.png) · [Settings](headunit-test.3/screenshots/api35-1024x600-160-settings.png) |
| 1280x720-240 | [Login](headunit-test.3/screenshots/api28-1280x720-240-login.png) · [Settings](headunit-test.3/screenshots/api28-1280x720-240-settings.png) | [Login](headunit-test.3/screenshots/api30-1280x720-240-login.png) · [Settings](headunit-test.3/screenshots/api30-1280x720-240-settings.png) | [Login](headunit-test.3/screenshots/api33-1280x720-240-login.png) · [Settings](headunit-test.3/screenshots/api33-1280x720-240-settings.png) | [Login](headunit-test.3/screenshots/api35-1280x720-240-login.png) · [Settings](headunit-test.3/screenshots/api35-1280x720-240-settings.png) |
| 1920x720-240 | [Login](headunit-test.3/screenshots/api28-1920x720-240-login.png) · [Settings](headunit-test.3/screenshots/api28-1920x720-240-settings.png) | [Login](headunit-test.3/screenshots/api30-1920x720-240-login.png) · [Settings](headunit-test.3/screenshots/api30-1920x720-240-settings.png) | [Login](headunit-test.3/screenshots/api33-1920x720-240-login.png) · [Settings](headunit-test.3/screenshots/api33-1920x720-240-settings.png) | [Login](headunit-test.3/screenshots/api35-1920x720-240-login.png) · [Settings](headunit-test.3/screenshots/api35-1920x720-240-settings.png) |
| 768x1024-160 | [Login](headunit-test.3/screenshots/api28-768x1024-160-login.png) · [Settings](headunit-test.3/screenshots/api28-768x1024-160-settings.png) | [Login](headunit-test.3/screenshots/api30-768x1024-160-login.png) · [Settings](headunit-test.3/screenshots/api30-768x1024-160-settings.png) | [Login](headunit-test.3/screenshots/api33-768x1024-160-login.png) · [Settings](headunit-test.3/screenshots/api33-768x1024-160-settings.png) | [Login](headunit-test.3/screenshots/api35-768x1024-160-login.png) · [Settings](headunit-test.3/screenshots/api35-768x1024-160-settings.png) |

## Required follow-up on real playback/hardware

QR account authorization, signed-in Home/Recents/Library, playlist contents and track selection; playback continuity during navigation; system/steering-wheel keys with UI closed and process dead; Back from Now Playing and lists; actual audio behavior after Recents ON/OFF and UI LMK; long-sleep and reboot audio restoration; offline downloads; BYD DiLink5 regression against test.2 and real AAOS/other head units.

The car was switched off throughout this work. No update was installed on it. The previous test.2 BYD results are historical and are not carried over as new test.3 passes.

See [adapter source and build instructions](../adapter/README.md). Proprietary input APKs, signing keys, user data and private authentication logs are not committed.
