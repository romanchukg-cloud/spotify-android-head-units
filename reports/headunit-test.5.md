# headunit-test.5 validation — 2026-10-12

Experimental prerelease **v5.5.0-headunit-test.5** repairs automatic playback restoration and adds a separate opt-in startup switch. The Spotify Automotive backend remains 5.5.0.

APK: `Spotify-5.5.0-HeadUnit-test.5.apk` · **57,876,480 bytes**.

SHA-256: `98b19fb1336fc41c0cf776ffe763b7e1b2850c010421266ff07fa834420112a2`.

Package `com.spotify.music`, min API 28, target API 33, versionCode 95482. The certificate is unchanged from test.2–test.4: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`. An in-place update supports keeping app data; uninstalling removes it.

## Resulting behavior

- **Resume playback after head unit starts or wakes** retains its previous saved value. Boot and long-screen-sleep restoration request playback only for a previously playing session. Screen wake retains the existing two-minute minimum sleep duration.
- **Automatically play when opening Spotify** is a separate switch, default **OFF**. It applies to a fresh player task or a cold UI process. Returning from settings, activity recreation and account/QR restart handoffs do not request playback. All five interface languages include the new switch.
- Service startup is no longer treated as proof of playing. The media connection waits for a paused/stopped session advertising PLAY or PLAY_PAUSE, retries an unacknowledged Play at least five seconds apart, and waits through buffering, connecting, NONE and ERROR states. The attempt ends after two minutes.
- A boot request is acknowledged only at PLAYING. It survives foreground-service startup, a failed connection, timeout and another reboot. A visible fresh player launch can retry it. Transient startup states no longer replace the last drive's playback intent; CONNECTING alone is not recorded as playing.
- Explicit pause, stop-on-close and account changes cancel pending attempts. Disabling the corresponding switch cancels its retry; disabling boot resume clears stale boot/wake intent. Existing account/login storage is unchanged.

## Final APK checks

[Static audit](headunit-test.5/apk-audit.txt): **28 PASS / 0 FAIL**. New checks cover the packaged readiness/retry policy and guarded fresh-player hook. [Structural checks](headunit-test.5/structural-checks.json) preserve 2,438 backend payloads, 22,982 backend resource strings and 5,114 relocated UI classes. DEX version 039, signature and ZIP/native alignment pass.

| Test | Android 9 / API 28 AOSP | Android 15 / API 35 Google Play |
| --- | --- | --- |
| Actual adapter service with synthetic MediaBrowser/MediaSession | 19 PASS | 19 PASS |
| Head-unit policy instrumentation, including opt-in player-open IPC | 19 PASS | 19 PASS |
| Logged-out main and settings in five viewports | PASS | PASS |
| New autoplay switch visible after scrolling | PASS | PASS |
| Independent controller allowed / external-OFF rejected | PASS | PASS |
| Isolated encrypted-account/restart regressions | PASS | PASS |
| Real Spotify audio after car sleep/reboot | **Pending** | **Pending** |

The service fixture uses the production `HeadUnitPlaybackService`, `HeadUnitRuntime`, `HeadUnitPreferences` and playback gate in a separate test UID. A synthetic framework browser deliberately drops the first Play request and delays session readiness. Tests cover no premature Play, retry after readiness, no retry while buffering, acknowledgement only at PLAYING, cancellation after explicit pause, bounded timeout retaining boot intent, settings cancellation, account transition protection and monotonic-clock reset. This is a transport/state test: it does not authenticate Spotify, restore a real queue or generate audio.

[API 28 resume](headunit-test.5/api28-resume.txt), [API 35 resume](headunit-test.5/api35-resume.txt), [API 28 policy](headunit-test.5/api28-policy.txt), [API 35 policy](headunit-test.5/api35-policy.txt), [viewport matrix](headunit-test.5/matrix.json), [API 28 account regression](headunit-test.5/api28-profiles.txt), [API 35 account regression](headunit-test.5/api35-profiles.txt).

Five logical viewports: 800×480@160, 1024×600@160, 1280×720@240, 1920×720@240 and 768×1024@160 portrait. Screenshots show logged-out settings only: [Android 9](headunit-test.5/screenshots/api28-autoplay-settings.png), [Android 15](headunit-test.5/screenshots/api35-autoplay-settings.png). The Android 15 image logged Google Play Services crashes (`NetworkCapability 37 out of range`); none of the captured crash records identify Spotify or the resume fixture. The cold-media smoke test intentionally kills its instrumented process and prints “Process crashed”; it is not counted as an audio or restart pass. See [crash summary](headunit-test.5/crash-summary.json). Account tests use mocked SDK storage and synthetic accounts; native two-account server authentication remains unverified.

## Hardware observation and limits

Read-only inspection of the connected DiLink5 Android 12 unit found `com.spotify.music` stopped, with ActivityManager exit descriptions identifying SystemUI as the stop requester. The boot counter was 69 and uptime exceeded one day, so turning the vehicle on had not caused an Android reboot during that observation. No adapter log records were available. These facts do not prove which vehicle action stopped Spotify or whether every vehicle wake behaves this way.

The screen receiver runs in the backend process. [Android's stopped-package behavior](https://developer.android.com/about/versions/15/behavior-changes-all#enhanced-stop-states) prevents an ordinary app from undoing a system force-stop by itself. The new-launch switch provides a playback path when Spotify is opened again; this release does not modify vehicle firmware or its task killer. Our target API is 33; the Android 15 boot restriction for mediaPlayback services targeting API 35 is not the direct explanation on this Android 12 car.

Still required: authenticated queue/audio restoration after actual vehicle sleep and reboot, in-place login preservation on the car, signed-in navigation/audio continuity, steering-wheel controls, real account A → B → A, offline downloads and BYD/AAOS/other manufacturer hardware regression. Historical [test.4](headunit-test.4.md) account UI results are not new hardware passes for test.5.

This APK has not been installed on the car during this change. No Spotify credentials, QR sessions or private car logs are included in this report.
