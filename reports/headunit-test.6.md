# headunit-test.6 validation — 2026-10-13

Test build **headunit-test.6** adds the car's own ambient music rhythm to Spotify on BYD. It was installed and verified on the connected DiLink 5 / Android 12 head unit. The user confirmed that the lamps visibly respond to Spotify.

APK: `Spotify-5.5.0-HeadUnit-test.6.apk` · **57,880,576 bytes**.

SHA-256: `f5eac6dcb254d095bc9bf26a7aabaf111ea6af71079fcccd79b4f3463cf4c086`.

Package `com.spotify.music`, versionCode **95483**, upstream versionName `5.5.0.16251`, min API 28, target API 33. Signing certificate SHA-256 remains `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`. The car's pre-update APK matched the published test.5 SHA-256 exactly. Installation used `adb install -r`, preserving the existing first-install time and app data directory inode. The installed APK hash matches this build; package flags confirm it is non-debuggable. The existing authenticated library/queue and playback remained available without a new QR login.

## Behavior

- Only a detected BYD creates the rhythm worker. The backend's actual MediaSession PLAYING state starts the session; buffering alone does not start it.
- `startAudioOutput` and `stopAudioOutput` run on a dedicated HandlerThread with the allowlisted name `com.byd.mediacenter` and one retained Binder token. The app writes no lamp colours, brightness, or vehicle mode. The car's ambient-light menu owns these settings and its Music rhythm switch must be enabled.
- PAUSED/BUFFERING/CONNECTING/skipping transitions have a bounded two-second grace period. Returning to PLAYING cancels the pending stop. Repeated callbacks cannot indefinitely postpone it.
- Explicit pause/stop commands, stop-on-close, account transition, session release and shutdown request immediate release. Closed/account-transition gates reject stale PLAYING states. Process death releases the token through BYD's binder-death handling.
- The direct BYD API is firmware-specific. Failures are caught and logged under `SpotifyCarRhythm`; normal playback continues. An automatic ADB/Shizuku fallback is not implemented because the Spotify adapter has no existing authorized local-shell transport. It was unnecessary on this car.

## Verification

[Final APK audit](headunit-test.6/apk-audit.txt): **31 PASS / 0 FAIL**, including the three new rhythm guard/transport/lifecycle checks. [Structural checks](headunit-test.6/structural-checks.json) preserve 2,438 backend payloads, 22,982 backend resource strings and 5,114 relocated UI classes. APK signing, DEX 039 compatibility and ZIP/native alignment pass.

| Check | Android 9 / API 28 | Android 15 / API 35 |
| --- | --- | --- |
| Isolated synthetic-output rhythm fixture | 18 PASS | 18 PASS |
| Existing synthetic MediaBrowser/resume fixture | 19 PASS | 19 PASS |

[API 28 rhythm](headunit-test.6/api28-rhythm.txt), [API 35 rhythm](headunit-test.6/api35-rhythm.txt), [API 28 resume](headunit-test.6/api28-resume.txt), [API 35 resume](headunit-test.6/api35-resume.txt). Fixtures ran in disposable emulator UIDs, without Spotify credentials or the real vehicle audio API. They verify duplicate suppression, bounded gaps, worker execution, failure cleanup, runtime hooks, close/account guards, session release, shutdown and automatic playback regressions. The full APK also launched on the API 28 emulator without a captured crash. Account and broad UI regression suites were not rerun for this change.

## Hardware results

The non-debuggable Spotify backend itself called the audio service. ADB was used for installation, observation and UI test actions; the implementation does not use ADB, a shell helper, root, extra permissions, or a system signature.

[Sanitized car rhythm evidence](headunit-test.6/car-rhythm-excerpt.txt) records:

1. Existing opt-in opening autoplay reached PLAYING after the in-place update. `SpotifyCarRhythm: start accepted` coincided with BYD `start Session`, `AtmosphereLampCore: start: mode = 2`, `startSendColor` and `send color ... to ECU`.
2. Pausing from Spotify's visible player produced `stop accepted`, `stop Session`, `stopAll`, `stopSendColor` and playback state PAUSED. Starting again reused the same Binder token and resumed the effect. The user independently confirmed the visible lamp response.
3. Force-stopping Spotify while PLAYING produced `binderDied: Session{...com.byd.mediacenter...}`, followed by `stopAll` and `stopSendColor`.
4. Reopening the app after the force-stop restored authenticated playback and acquired a new rhythm session. Spotify was left open with playback and rhythm active.

The focused log capture also contained an unrelated `com.google.android.gms` / microG LocationManagerService crash. It did not identify a Spotify process. Private UI screenshots, full car logs, playlist metadata and identifiers remain in ignored local diagnostics and are not included in this report.

Not yet verified: rhythm behavior during real vehicle sleep/reboot, long-duration use and track boundaries on hardware, account switching with two real Spotify logins, other BYD firmware, AAOS/other manufacturers. BYD captures the output mix, so navigation or other sounds can influence the rhythm while Spotify owns a session. Next step: collect feedback on everyday play/pause and actual vehicle sleep/wake, particularly on other firmware.

Known cosmetic issue: the Head unit settings footer still displays `headunit-test.5`. Identify this APK by versionCode 95483, its test.6 filename and the SHA-256 above. The footer does not indicate which rhythm implementation is installed.
