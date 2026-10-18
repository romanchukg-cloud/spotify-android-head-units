# Head-unit adapter source

This directory contains the project's Java integration code, resource/manifest patch generators, packaging code and emulator test sources. `../tools/apk_audit.py` audits the **finished APK**. It does not contain Spotify's proprietary SDK source or the decompiled BYD interface; those are local build inputs. Published test.7 adds Support and structured diagnostics. Published test.8 keeps the Spotify sign-in stable on head units. Published test.9 adds in-app updates. Build identity and output filename are generated from `build-info.json`.

The APK contains the original Spotify Automotive 5.5.0 backend, our helper DEX, and the relocated BYD-derived interface. `ParcelBridge` translates media parcelables across the two class namespaces. `HeadUnitProfile`, preferences/provider, playback receiver/service and settings activity implement the head-unit adaptation. `PlaybackGate` and `TaskGuardService` implement explicit task-removal behavior. The backend resource IDs stay at package `0x7f`; UI IDs use `0x7e`.

## Build prerequisites

- JDK 17+ (verified with JDK 21), Python 3, apktool **2.11.1** and its JAR.
- Android platform 35 and build-tools 35.0.0. Minimum supported Android API remains **28**. All three DEX files use version 039.
- Two **prepared adaptation input APKs**, not arbitrary Spotify releases. They are not fetched or uploaded by these scripts:

| Input | SHA-256 |
| --- | --- |
| `SpotifySDK-5.5.0-BYD-test.apk` | `6527109d69dbbee4c9bef177879dbb9cebedc806dd6b07cb0e89a223125247d2` |
| `Spotify-BYD-for-5.5-test.apk` | `087a78a67501543bceac2700cbe3e2c3b9ea6e84037fd898fe8941b1f7b72ace` |

- A local signing keystore. Keys are never committed. A developer build with a different key cannot update the published APK or preserve its app data through an in-place update. The published test.5 uses the same package and certificate as public test.2.

From the repository root:

```sh
python3 adapter/bootstrap.py --sdk-apk /path/SpotifySDK-5.5.0-BYD-test.apk --ui-apk /path/Spotify-BYD-for-5.5-test.apk
export ANDROID_SDK_ROOT=/path/to/Android/sdk
export APKTOOL_JAR=/path/to/apktool_2.11.1.jar
export HU_KEYSTORE=/path/to/your.keystore
export HU_KEYSTORE_PASS=pass:your_password
./adapter/build.sh
python3 tools/apk_audit.py adapter/Spotify-5.5.0-HeadUnit-test.8.apk --min-api 28
```

The optional variables `ANDROID_HOME` and `BUILD_TOOLS_VERSION` select the SDK and tools; local defaults match Android Studio on macOS. `adapter/tests/build.sh` builds test APKs signed with `HU_KEYSTORE` (the test keystore password defaults to `android`; set `HU_KEYSTORE_PASS` for another key). Only run instrumentation on disposable emulators: its checks change head-unit preferences and playback state.

The fresh input-to-APK build was verified before publication. ZIP timestamps and resource compiler normalization can change whole-APK hashes between builds; verify the downloaded release against the published hash.

## Releasing without the input APKs

`incremental_release.py` builds the next test release from the previous published APK: it re-applies the smali patch section added since that release (`--since`, a `# …` heading in `patch_headunit.py`) to the published backend DEX, recompiles the helper DEX from `src/`, adds the section's `MANIFEST_ADDITIONS` (permissions and receivers) to the binary manifest and sets the `build-info.json` versionCode. A section without smali changes keeps the backend DEX byte-identical. Resources, the UI DEX and native libraries stay byte-identical, and the script refuses a base APK whose SHA-256 differs from its `reports/<release>/release.json`.

The **Release** GitHub Actions workflow (`.github/workflows/release.yml`, run manually) does this, signs with the `HU_KEYSTORE_B64` repository secret (base64 of the release keystore; optional `HU_KEYSTORE_PASS`, `HU_KEY_ALIAS`, `HU_KEY_PASS` default to the debug-keystore values), requires the certificate of the base release, runs the sign-in and updater tests and both APK audits, updates the published base APK in place on Android 9 and 13 emulators (`tests/updates/emulator.sh`), uploads the APK and the update-feed record as an artifact and, with `publish`, creates the prerelease from `reports/<label>/release-notes.md`. `build.sh` with the prepared inputs remains the reference build.

## Verification

```sh
./adapter/tests/build.sh
adb install -r adapter/Spotify-5.5.0-HeadUnit-test.8.apk
adb install adapter/tests/out/client.apk
adb install adapter/tests/out/test.apk
python3 adapter/tests/matrix.py emulator-5554 35
```

The independent `hu.browser` app uses `MediaBrowserCompat` from the locally supplied backend DEX. It has a different Android UID and connects through the public service. The policy instrumentation runs under Spotify's UID with the same test certificate. It exercises the settings provider, defaults, controller checks, explicit close/play, binder death callback, pause and scale bounds. It **does not simulate successful Spotify authentication or certify audio playback**. The cold-session fixture deliberately terminates its process; an instrumentation “Process crashed” result for that fixture is expected and is not a successful audio test.

See [test.5 validation](../reports/headunit-test.5.md) for what was tested and what requires a signed-in account or real hardware. Android package visibility allows a client that binds to the service to become visible automatically ([Android documentation](https://developer.android.com/training/package-visibility/automatic)); the adapter does not request `QUERY_ALL_PACKAGES`.

## Accounts

Open **Head unit settings → Accounts → Add account · QR**. The same entry is also added to Spotify's native settings. It is not a floating button on Home. The QR screen offers a return to Accounts beside the native help row.

`DriverProfiles` uses the existing SDK auth store, captured by the `ca.c` constructor hook. Up to eight reusable account credentials are saved in a private AES-GCM vault protected by Android Keystore; only account IDs, display names and active status cross the private provider. Backup is disabled. Renaming does not change a Spotify username. The active account cannot be removed.

During a switch, playback and queued media commands are blocked, wake/boot resume snapshots are cleared, and the previous SDK user slot is restored if a write fails. A coordinator in `:profiles` waits for both old SDK processes to stop before launching the player and waits for UI acknowledgement before closing. The separate `:settings` activity finishes when Accounts opens. Both lightweight processes use plain Android Application, so they do not initialize another SDK instance.

```sh
zsh adapter/tests/profiles/build.sh
adb -s YOUR_DISPOSABLE_EMULATOR install adapter/tests/profiles/out/ProfileTests.apk
adb -s YOUR_DISPOSABLE_EMULATOR shell am instrument -w local.spotify.profilestests/local.spotify.unified.ProfileTests
```

The isolated test package uses its own UID, Android Keystore and synthetic credentials with a mocked SDK auth store. It checks encryption/tamper rejection, two-account restoration, rollback, transition locks, cancellation and restart retries. **A real two-account Spotify server login and A → B → A playback remain unverified.** Do not treat the mock as proof that a saved server credential stays valid indefinitely; Spotify can require QR sign-in again.

The logged-out UI restart test is `python3 adapter/tests/accounts_ui.py YOUR_DISPOSABLE_EMULATOR API`. Pass `--seed` to populate two visibly synthetic profile rows in that emulator's Spotify vault; these are not usable Spotify credentials. The test finishes instrumentation before the normal UI restart scenario, because Android force-stops the target when an instrumented backend dies. Never run this fixture against a real signed-in installation.


## Playback startup in test.5

Head unit settings has two separate switches. **Resume playback after head unit starts or wakes** restores a previously playing session after BOOT_COMPLETED or a screen-off period of at least two minutes. **Automatically play when opening Spotify** defaults to OFF and starts the last restored queue on a new player task or a cold UI process. Returning from settings or recreating the activity does not start music. Account restart/QR handoffs are excluded.

Automatic restoration waits for a paused/stopped session advertising PLAY or PLAY_PAUSE. It retries an unacknowledged Play no more often than every five seconds for at most two minutes, waits through buffering/connecting/error, and acknowledges success only at PLAYING. A boot intent survives an accepted service start, timeout and another reboot; opening the player can retry it. Explicit pause, stop-on-close and account changes cancel pending retries. Disabling boot resume also clears stale boot intent. Existing login, accounts and playback switches are preserved on an in-place update.

Screen wake is observed by the running backend. If vehicle firmware force-stops the whole package, [Android](https://developer.android.com/about/versions/15/behavior-changes-all#enhanced-stop-states) does not deliver its normal background wake/boot work until the package is allowed to run again. This adapter cannot override a vehicle task killer. The optional new-launch switch provides playback when the user opens Spotify again; real firmware sleep/reboot behavior still needs a signed-in hardware test.

Run `./adapter/tests/resume/build.sh`, install its `out/ResumeTests.apk` on a disposable emulator and run `adb -s SERIAL shell am instrument -w hu.resume/hu.resume.ResumeTests`. This fixture uses the actual adapter playback service, runtime and preferences with a synthetic Android MediaBrowserService/MediaSession. It checks delayed readiness, dropped Play, acknowledgement, cancellation and timeout; it neither uses Spotify accounts nor produces audio. Do not install test fixtures on the user's car.

## Local test.6: BYD ambient music rhythm

`Spotify-5.5.0-HeadUnit-test.6.apk` adds the vehicle's existing ambient music rhythm. The Spotify backend observes its own MediaSession playback states and invokes the BYD audio service's `startAudioOutput` / `stopAudioOutput` off the main thread. Both calls use `com.byd.mediacenter` and one backend-lifetime Binder token. No lamp colour, brightness, or vehicle mode is written. Enable **Music rhythm** in the car's ambient-light menu; that menu owns the visual effect.

The worker is created only on a detected BYD. PLAYING acquires the session. Short PAUSED/BUFFERING/CONNECTING/skipping gaps are allowed up to two seconds; repeated notifications cannot extend the deadline. Explicit pause/stop commands, stop-on-close, account transition, MediaSession release and shutdown request immediate cleanup. Process death releases the Binder token through the vehicle service. Navigation and other output audio may also influence the effect while Spotify owns a rhythm session, because BYD captures the output mix.

This build uses the direct third-party app call documented for Tang EV / DiLink 5 / Android 12. A successful reflected call means the audio service accepted it; visible lamp activity also depends on firmware and the car's own rhythm switch. Other firmware can reject the hidden API or borrowed package name; errors are logged under `SpotifyCarRhythm`, without interrupting music. Automatic ADB/Shizuku fallback is not included: the Spotify adapter currently has no authorized local-shell transport.

`zsh adapter/tests/rhythm/build.sh` builds an isolated synthetic-output fixture. Install it only on a disposable emulator and run `adb -s SERIAL shell am instrument -w hu.rhythm/local.spotify.unified.RhythmTests`. It tests timing, worker execution, cleanup and the production runtime/gate hooks, without using the car audio API or Spotify accounts. See [test.6 validation](../reports/headunit-test.6.md) for the actual build and hardware results.

## Local test.7: Support and diagnostics

Open **Head unit settings > Support**. **Send diagnostics** shows the frozen, sanitized JSON and public-report disclosure before **Send public report**. It works with automatic sending off. **Export diagnostics** saves or shares a ZIP containing only that report and its disclosure; exporting does not upload it. For missing ambient lighting, select **Other (including ambient lighting)** and first check **Music rhythm** in the car's own menu.

On first foreground player use, a notice has **Automatically send diagnostics** selected by default. No automatic work starts before **Continue**. A previous opt-out is preserved. Automatic reports cover measured playback/BYD integration failures only, at most one new report per installation per 24 hours. Successful sessions and missing physical light/cluster output are not automatic triggers. Crash/ANR detection is not included. The automatic preference can be changed in Support; disabling it removes unattempted automatic payloads and requests cancellation for known queued receipts. Already claimed/public reports, or an acceptance whose response was lost, cannot reliably be recalled. Manual reports remain independent.

The fixed HTTPS service is `https://diagnostics.localcharge.app`, project `spotify-android-head-units`, playback profile, `/v1/reports` with schema **2** and explicit `report_source`. Pending legacy manual schema-1 bodies remain compatible. Before every automatic POST, the client verifies schema 2, automatic reporting and the documented grouping capability at GET `/`. Missing capabilities defer automatic upload. The server groups matching automatic project/build/failure/UTC-day reports; manual reports are independent. A limited technical summary may be published in this project's **public GitHub Issues**. Technical metadata can still identify an unusual configuration; this is not an anonymity guarantee.

Only allowlisted lifecycle, playback, bridge connectivity, wheel, audio-focus and safe failure enums are recorded. Audio focus is `unknown` where the app cannot measure it. BYD audio integration errors use the playback failure stage. No raw logcat, exception text/stacks, account data, media titles, screenshots, location or persistent device IDs are read into the report. Unsupported settings are omitted and unregistered components stay empty. Private logs use four fixed process slots, each two 64-KiB files, with a ten-minute capture window. Active processes prune all slots at startup, snapshot and once per minute; stopped applications cannot run a deletion timer. Report limits are 256 KiB and 500 events.

The three-report private queue persists exact bodies, random UUIDv4 keys and random per-report 256-bit tokens before upload. Automatic work uses at most one slot and never evicts manual work. Offline retries, rate limits and process restarts reuse the original body/key/token; retries stop after 24 hours. A native persisted JobScheduler job handles pending work and seven-day content cleanup without a permanent diagnostics service. Android scheduling, force-stop and firmware restrictions can defer work; pending work is scheduled again when the backend opens. Receipt tokens stay private and never appear in exported reports or URLs. No GitHub credentials are embedded.

**Pending** is local, **Queued** means durable server acceptance, and **Report sent** requires a confirmed URL in this exact GitHub repository. Cancellation applies only before delivery is claimed. Removing a local copy frees a queue slot and does not delete a public issue.

Build `zsh adapter/tests/diagnostics/build.sh` after compiling the adapter. Install its `out/reader.apk` and `out/main.apk` on disposable emulators, then run `adb -s SERIAL shell am instrument -w hu.diagnostics/local.spotify.unified.DiagnosticTests`. Run `-e phase ui` for production Support/notice views with a fake transport. Run `-e phase seed`, force-stop `hu.diagnostics`, then `-e phase verify` for real process-restart persistence. The fixture never schedules the real HTTP upload service. No automated suite should create public test issues. See [test.7 validation](../reports/headunit-test.7.md) for actual coverage and remaining vehicle checks.

## Test.8: Sign-in stability

Spotify Automotive 5.5.0 signs out, and deletes its saved login, in four situations. test.8 fixes the ones caused by the head unit and records the reason for all of them.

| Cause in the SDK | test.8 |
| --- | --- |
| login5 rejects the saved login with `INVALID_CREDENTIALS`, `NOT_ALLOWED` or `UNSUPPORTED_LOGIN_PROTOCOL` (`is.e`, mapping in `za.d`) | The saved login is tied to the device ID, which the SDK reads from `ANDROID_ID` (`ib.c`, `v5.q`). `HeadUnitIdentity` keeps the ID seen on the first test.8 start, so firmware that regenerates `ANDROID_ID` no longer invalidates the login. The rejection code is recorded. |
| Any `ACCOUNT_REMOVED` broadcast for Spotify's account type (`AccountRemovedBroadcastReceiver`), whatever the account name | Signs out only when the removed Android account is the active user. A stale account left behind by an account switch, removals during a switch and Spotify's own removals after it has signed out are ignored. |
| Remote minimum version above 5.5.0 (`ac.j.h`, `android-spotify-ubiquity/min_version`) | Unchanged and recorded as `deprecated`. The fix is a newer Spotify base. |
| The SDK starts without its stored user although nobody signed out, for example after a power cut during a write | Once per boot, just before the SDK's startup read (`ac.j` and `vr.e` constructors), the last active account is put back from the encrypted Accounts vault. The vault copy is refreshed 20 seconds after every sign-in. If that fails, `storage_missing` is recorded and QR sign-in appears as before. |

**Head unit settings** shows the last sign-in event with its time, and a note when the system device ID changed. Unexpected sign-outs are recorded as `auth` failures in diagnostics (`permission_denied`, `schema_mismatch` or `internal_error`), so automatic reports include them when automatic sending is on. A sign-out in Spotify settings and the removal of the active Android account stay local. No usernames, IDs or credentials leave the backend process.

`tests/signin/run.sh` runs plain-JVM checks of the decisions: ID pinning, reason classification, the stale-removal filter, one recovery per boot and the diagnostics codes (`ANDROID_JAR` may point at any full framework jar). `../tools/apk_audit.py` adds three `Sign-in /` checks for the SDK hooks, and every new smali patch must match exactly once.

Checked without the build inputs or the signing key: the new patches applied to smali disassembled from the published test.7 APK (each matched once), the patched DEX and the helper DEX assembled, all 35 cross-DEX helper references resolve, the three new audit checks pass on the patched smali and fail on test.7, and the 39 JVM checks pass. The published APK was then built by the Release workflow from the test.7 APK ([run](https://github.com/romanchukg-cloud/spotify-android-head-units/actions/runs/38588766001)): its certificate equals test.7 and the release audit passes 42/42. **Not yet verified:** a build from the prepared inputs, a real sign-in on an emulator or head unit, an actual `ANDROID_ID` change and recovery after a power cut.

## Test.9: App updates

Spotify updates itself from this project's published test builds, like Google Maps for BYD. Nothing is installed silently: Android's own **Update** confirmation always completes the installation.

- **Feed.** Once a day while the backend process runs (a persisted JobScheduler job, ID 9158, run by the existing `DiagnosticUploadJob` service), `AppUpdates` reads `https://diagnostics.localcharge.app/v1/updates/spotify-android-head-units` over HTTPS without redirects. `UpdateFeed` accepts exactly the server's schema-1 record for this project: build `N`, version `test.N`, versionCode `95477 + N`, package `com.spotify.music`, minSdk 28, and the exact release tag and asset URL `v5.5.0-headunit-test.N/Spotify-5.5.0-HeadUnit-test.N.apk`. Only a higher build and versionCode is an update. A 404 means no published record; failures retry after at least an hour or the server's `Retry-After`.
- **Download.** By default automatic and on Wi-Fi only; Head unit settings → **Updates** has switches for automatic checks, automatic downloads and Wi-Fi only, and buttons to check, download/resume, install and pause. `UpdateDownload` stores the APK in private no-backup storage, resumes with a matching `Content-Range`, follows redirects only to GitHub's release hosts and accepts exactly the feed's size and SHA-256.
- **Verification.** Before staging and again before installing, the APK must be this package with the feed's versionCode, a minSdk the device supports, and the single release certificate `a29d95f7…2dc2b1` pinned in `UpdateFeed`. A file that fails is deleted and that build is not downloaded automatically again.
- **Installation.** A `PackageInstaller` session (user action required on Android 12+) reports to the non-exported `UpdateResultReceiver` with a per-session nonce. The foreground UI opens Android's confirmation activity only after checking that it belongs to a system package. Spotify restarts; sign-in, accounts and settings are kept. If Android first asks to allow installs from Spotify, the app opens that setting and offers the update again on return.
- **Offer.** When an update is verified, opening the player shows one offer per build, only while music is not playing and the diagnostics notice is not pending. Updates stays available in Head unit settings.

All updater work runs on one backend worker thread; no updater failure can reach the uncaught-exception handler of the music process. The manifest gains only `REQUEST_INSTALL_PACKAGES` and the private receiver (`MANIFEST_ADDITIONS` in `patch_headunit.py`).

`tests/updates/run.sh` runs 152 plain-JVM checks: the live test.8 record, every feed field and URL rule, trusted redirect hosts, full/resumed/ignored-range/corrupt/oversized/paused downloads and the update texts in five languages. `../tools/apk_audit.py` adds three `Updates /` checks.

**Publishing a release to the feed** is a manual operator step; the server does not read GitHub. After the GitHub release is public, take `spotify-update-test.N.json` from the Release workflow artifact (or run `python3 tools/update_record.py reports/headunit-test.N/release.json reports/headunit-test.N/update-notes.txt`), check it against the release, then:

```sh
scp spotify-update-test.N.json loch:/opt/byd-diagnostics/staging/spotify-update-test.N.json
ssh loch 'python3 /opt/byd-diagnostics/app/publish_update.py /opt/byd-diagnostics/staging/spotify-update-test.N.json'
curl -fsS https://diagnostics.localcharge.app/v1/updates/spotify-android-head-units
```

The server caches a record for five minutes and rejects lower builds or versionCodes. test.8 and earlier have no updater: install test.9 manually once.

## Test.10: Classic and New style

The New style is the default; Head unit settings has a Classic / New switch and a separate "Colour from cover art" switch. Classic keeps the BYD-derived interface unchanged.

- **Resources.** `restyle.py` runs after `patch_resources.py` and writes the New style as `car` UI-mode resources (`values-car`, `layout-w840dp-h480dp-car`, …) derived from the local decoded interface, so no BYD layouts are committed: a palette for day and night, a copy of `Theme_VIVID_MUSIC_Base` with New attributes, rounded drawables (mini-player card, player panel, navigation highlight, filter pills, list rows), larger radii and reordered mini-player and player layouts (controls first, then like, cover and title). Data-binding tags, ids and view types are unchanged. The right-hand-drive player layouts keep their Classic arrangement.
- **Selection.** `HeadUnitStyle` sets the UI-mode type of the player's configuration to CAR for the New style. AppCompat's day/night handling drops that type from the activity configuration, so `MainActivity.getResources()` (patch section `# Restyle`) returns the styled resources. A change of style or scale restarts the interface process (`HeadUnitSettings.restartPlayer`); playback runs in the backend process and continues.
- **Like button.** Spotify Automotive publishes its like actions as `like`/`unlike`, the interface only matched `likeTrack`/`unlikeTrack`, so the button never appeared. In the New style `HeadUnitStyle.actionMatches` accepts both; the click sends the backend's own id.
- **Code-side styling.** `HeadUnitShell` turns the navigation rail and the content area into rounded cards with a 6 dp gap, keeps the mini-player as the content card's footer and rounds the app's own corners (28 dp, concentric with the 22 dp cards) with a black mask that blends with BYD's black status bar and dock; `CoverAccent` tints the mini-player and the player panel with the most vivid colour of the current cover (24×24 sample, contrast-limited); `HuUi` builds the New settings screen and restyles Support, the diagnostics notice and Accounts.
- **Queue.** The playing row shows the current cover with an animated equaliser instead of an empty square.
- **Stop on close.** Closing Spotify always stops the music; the setting was removed from both styles.
- **Fix.** `ParcelBridge.getParcelableArrayList` no longer casts SavedStateHandle keys to `Parcelable`; recreating the player with Library open (for example on a day/night switch) crashed since test.3.

A full `build.sh` is required: `incremental_release.py` keeps resources byte-identical.

## Local test.11: Offline downloads (experimental)

Spotify Automotive 5.5.0 implements downloads: custom browser actions `download` / `downloading` / `downloaded` on playlists and albums, Library → **Downloads** (`spotify:library:downloads`) and the Offline and Storage sections of its own settings. Spotify keeps them off for this client with the remote configuration `android-automotive.is_offline_enabled`, and adds the actions to items only for a media host (a client that sent `KEY_ROOT_HINT_MEDIA_HOST_VERSION`).

- **Backend** (patch section `# Offline downloads`): the flag reads as on (`pq/d`), and every client counts as a media host (`ee/a.c()`), because the stored host version is overwritten by whichever client connected last.
- **Interface**: `HeadUnitDownloads` adds one button next to the favourite button on playlist and album pages (`DetailFragment` view hook). It reads the item's state through its own connection with the backend's support-library `MediaBrowserCompat` (compile-time stubs in `stubs/`, never in the helper DEX) and sends the action: download; downloading (tap stops); downloaded (tap asks before removing). A requested download shows as downloading until Spotify reports it. `patch_resources.py` lets the favourite button grow (`wrap_content`, `minWidth` = `common_btn_size`) in all layout and motion-scene variants.
- **Server rules still apply**: downloads need Premium, Spotify's device limit for downloads applies, and downloading adds the item to Your Library, as in the phone app.
- **Probe**: `tests/downloads` is an independent media-host client that lists the browse tree with each item's actions and sends an action to one item (logcat `HU_PROBE`).

Checked on an Android 12 emulator with a signed-in Premium account: items carry the download actions; a one-track album and a playlist downloaded, appeared under Library → Downloads and played with the network off, also after a cold start; removal works. Not yet checked in a car.

## Local test.11: Voice search

A microphone button in the search field (`HeadUnitVoice`, `SearchFragment` view hook) opens the head unit's own speech recognition through `HeadUnitVoiceActivity` (player process). It appears only when an activity handles `android.speech.action.RECOGNIZE_SPEECH` (declared under `<queries>` for Android 11+ package visibility); the Spotify backend has no microphone access of its own. The prompt and recognition language follow the system language. The recognised text plays the best match through the backend's media session (`playFromSearch`, PLAY_FROM_SEARCH) and is searched in the field, so the results stay on screen.

Checked on an Android 12 emulator with a signed-in account and a test recogniser (`tests/downloads`, `FakeRecognizer`): the button appears, the prompt is localised, "Imagine Dragons Believer" played Believer and showed its results; without a recogniser the button is hidden. `playFromSearch` alone also played "Bohemian Rhapsody" and the Cyrillic "Океан Ельзи". The Android 15 Play emulator has Google's recogniser; a BYD DiLink head unit has not been checked for one.

## Local test.11: Queue editor, radio and a queue crash fix

- **Fix:** the interface's queue commands (the cross that removes a track from the queue, "add to queue") sent its relocated `local.bydui.…MediaDescriptionCompat`. Both classes exist in this APK, so the backend created the relocated one and its `MediaSessionCallbackApi21.onCommand` cast failed: the music process crashed (since test.3). `prepare.py` now routes the Bundle writes of the relocated `MediaControllerCompat$MediaControllerImplApi21` through `ParcelBridge.putBackendParcelable`, which recreates the backend's class (as already done for browser-request ResultReceivers). Removing and adding now work without a crash.
- **Queue editor:** `patch_resources.py` adds a hidden edit button to the 14 queue layouts (opposite the close button, also right-hand drive); `HeadUnitQueue` (`QueueFragment` view hook) shows it and opens `HeadUnitQueueActivity` (player process, New-style rows): the media-session queue grouped by the backend's titles (Now playing, Next in queue, Next from …) with covers from the backend's image provider. A row plays that track; the minus button removes it (`REMOVE_QUEUE_ITEM_AT`); **Clear queue** removes the added tracks from the last one up.
- **Radio:** **Song radio** and **Artist radio** play `spotify:station:track:…` / `spotify:station:artist:…` built from the current track's `MEDIA_URI` and `com.spotify.music.extra.ARTIST_URI`.

Checked on an Android 12 emulator with a signed-in account: the cross and add-to-queue no longer crash and change the queue (the interface's list refreshes late; the editor shows the new queue), removing in the editor, Clear queue and song/artist radio work.

## Local test.11: Jam

Spotify's own settings page (`SettingsPageActivity`) already runs Jam (Start a Jam, QR code for up to 5 friends, End Jam). The queue page has a Jam button next to the edit button (`hu_queue_jam`, 14 layouts) and the queue editor a **Jam** pill; both open that page with an extra, and `HeadUnitJam.opened` (patch section `# Jam`, called from the page's `onCreate`) keeps the Jam section at the top while the page loads and after Start a Jam, until the user scrolls, so the QR code fits on the head unit's screen. The Jam browser actions of the backend stay unused (their remote flag `is_jam_custom_action_enabled` is off).

Checked on an Android 12 emulator with a signed-in Premium account: Start a Jam shows the QR code fully on screen from both buttons; End Jam ends it.

## Local test.12: Sleep timer

The queue editor has a **Sleep timer** pill: 15, 30 or 45 minutes, 1 hour or the end of the current track (from the session's duration and position); while set, the pill shows the minutes left or "End of track" and offers **Off**. `HeadUnitSleep` runs the timer in the music process, which stays alive while music plays, through the private settings provider (`sleep_set`, `sleep_cancel`, `sleep_get`): an inexact alarm may fire minutes late (Android's window is up to 75 % of the delay) and exact alarms need a permission on Android 12+. At the time it pauses as an explicit user pause (`HeadUnitRuntime.explicitPause`), so no automatic resume follows.

Not available in Spotify Automotive 5.5.0 and therefore not added: Smart Shuffle (the client only has the shuffle / disable-shuffle actions) and an audiobooks section (the backend's Library offers Playlists, Podcasts, Albums, Artists and Downloads).

Checked on an Android 12 emulator with a signed-in account: "End of track" paused 2 s into the next track, the 15-minute timer showed its minutes and Off cancelled it.

## Local test.12: one style

The Classic style is gone: the New style is the only interface style. `HeadUnitStyle.apply` always sets the "car" UI-mode type (the generated resources stay as they are), Head unit settings has no style switch, a stored `ui_style` is ignored, and `HuUi.adopt`, `HeadUnitShell`, `CoverAccent` and the like-action alias no longer check a style. Checked on an Android 12 emulator: an older build switched to Classic opens in the New style after the update, without a crash.

## Local test.12: our own player (first screens without BYD code)

The interface is being rewritten over the Spotify backend's media session, screen by screen, without the BYD-derived code. First: `HuPlayerActivity` (player process, opened from Head unit settings → **New interface (test)**) with our mini-player and player.

- `HuSession`: one connection per process through the backend's own support-library `MediaBrowserCompat` / `MediaControllerCompat` (compile-time stubs in `stubs/`, including `session/`): metadata, playback state, repeat mode, the session's custom actions (like / unlike, shuffle / disable-shuffle) and transport controls; pausing counts as an explicit user pause.
- `HuCovers`: covers from the backend's image provider (`content://com.spotify.music.mediaapi/…`), decoded off the main thread, cached, with the vivid colour from `CoverAccent`.
- `HuMiniPlayer`: controls first, then like, cover and title (the New style order), tinted with the cover's colour, a progress line; the cover or title opens the player.
- `HuNowPlaying`: large cover, title, artist, like, seek bar with elapsed and remaining time, shuffle / previous / play-pause / next / repeat (none → all → one), a gradient from the cover's colour; the top bar collapses it and opens Jam and the queue editor. Wide screens put the controls on the driver's side and the cover at the far end (right; left in right-to-left languages, by mirroring); portrait screens put the cover above them. The seek bar, times and playback controls stay left-to-right in right-to-left languages, as Material does for media controls (the mini-player too).
- Icons are Spotify's Encore drawables from the backend; like and shuffle use the session's own action icons.

Checked on an Android 12 emulator with a signed-in account: the mini-player and player show the current track and cover, play/pause, seek, repeat and shuffle change the backend's state, the cover colour tints both by day and night, and the open player survives a day/night switch; no crash.

## Local test.12: our own Home and Library

`HuPlayerActivity` is now the shell of our interface: navigation, a content card with our mini-player as its footer, and the player sliding up over everything.

- `HuRail`: Home, Recents, Browse, Library, Search and settings (Encore icons). A rail at the side on landscape screens; on portrait screens a bar along the bottom, under the mini-player, within reach.
- `HuPages` / `HuBrowse`: pages from the backend's browse tree (root `spotify:navigation`). Home shows the backend's groups (`android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT`: "Good evening", "Made For …", stations, recently played) as rows of cover cards; Recents and Browse are grids; Library has its sections (Playlists, Podcasts, Albums, Artists, Downloads) as filter pills above a grid. Artists have round covers. Horizontal rows start at their start edge in right-to-left layouts too (HorizontalScrollView only does that on its first layout).
- A card plays its item through the backend's `play` browser action (as a media host does); playlist and album pages come next. Search is a placeholder.

Checked on an Android 12 emulator with a signed-in account: landscape and portrait (1440×2560, as the car's rotated screen), forced right-to-left layout (rail and cover on the left, rows and pills from the right, controls left-to-right), day and night; no crash. A card changed the playing context; Spotify plays one stream per account, so with the same account playing in the car the emulator stayed paused.

Fixes after the first look in the car:

- The player covers the content card and the mini-player at full height and leaves the navigation in view; it is a rounded card like the others and collapses with a swipe down too (short swipes spring back; the seek bar keeps its own drags).
- The cover takes only the room the texts and controls leave (a square as large as its box allows), and narrow or short windows get smaller controls (very small ones keep previous, play and next), so a split screen keeps the controls in view.
- Splitting the screen resized the window without recreating the screen (the new size crossed none of the app's resource size buckets), so the layout stayed the full-screen one; the screen is now built again when its measured size moves away from the one it was built for.
- Our screens (the interface, Head unit settings, the queue editor, accounts, support) get the app's rounded outer corners (28 dp black mask), as the main screen has.

Checked on the emulator: full screen, the car's split window size (635×604 dp) with the player open while resizing in both directions, a very small window, Head unit settings; no crash.

## Local test.12: playlist, album, artist and show pages

Cards now open pages (`HuDetail`); playable-only cards still play. Categories (Browse genres) open a grid with a back button. Back (button or system back) returns along the path of pages; the path survives rotation and resizing.

- Header: back with the page type (Playlist, Album, Artist; shows — podcasts and audiobooks alike in the backend — get none), the cover (round for artists; shows without a cover use their first episode's), title, subtitle, the number of songs and their total time (hidden at 200 children, the backend's limit, as for Liked Songs), and centred buttons: shuffle play (turns shuffle on first; green while shuffle is on; artists start at a random top track), play in the middle in Spotify green (pause when this page is what plays) and download; a button a page lacks keeps its place, so play stays centred (the item's download / downloading / downloaded action, as on the BYD-derived pages).
- List: tracks with album track numbers or covers, the explicit mark, artists and length; the playing one in the accent colour; a tap plays the track in this context (`playFromMediaId` with the backend's media id, which carries the context). Episodes show their date and length from the backend's subtitle. Browsable children (an artist's albums) are a row of cards. Wide content keeps the header beside the list, narrow content scrolls it above the list.
- `HuSession.children`: pages asking for the same node share one request; an unanswered request is sent once more after 10 s; open requests are sent again after reconnecting. Covers that fail right after the backend starts are tried twice more.
- The settings item of the navigation has its label like the other items.

Checked on the emulator with the owner's account while the car was off: a playlist (play from a track, pause from the page), an artist (top tracks, albums row, an album from it and back), an album (track numbers), a genre grid with back, a podcast, Liked Songs (200 rows scroll), shuffle play (shuffle and pause were restored afterwards), portrait, rotation with the path kept, day and night; no crash. Download was not pressed (it can change the account's library).

## Local test.12: search in our interface

- `HuSearch`: the microphone first (nearest the driver's hand; on the right in right-to-left languages), then the field with clear. Results of the backend's own media browser search (`MediaBrowserCompat.search`; the root says `SEARCH_SUPPORTED`) arrive while typing (after 450 ms) in its groups: Top Results, Songs, Albums, Artists, Playlists, Podcasts & Shows, Podcast episodes, Audiobooks; the same item twice in a group (top results) shows once. Cards open their pages, tracks and episodes play; back returns to the query and its results, which also survive rotation.
- Voice: the head unit's speech recognition (as on the BYD-derived search screen); the spoken text plays the best match at once (`playFromSearch`) and shows the results for another choice.
- With an empty field: the recent searches (the newest eight; kept when a search is submitted, spoken or used) and a button to clear them.
- `HuList` now holds the list shared by pages and search results.
- The keyboard shrinks the content, which the resize check took for a split screen and rebuilt the screen; it now compares the window's own size (the decor view: the activity's window metrics come from its scaled configuration and keep the size it was created with). The manifest gives this activity `configChanges=orientation|screenSize`, so rotation also relies on this check.
- The mini-player's progress is a thin rounded bar inside the card over a faint track (a line along the very edge showed only as a short dark piece under the rounded corner).

Checked on the emulator: typing (results while typing, keyboard open), submitting, voice with a test recogniser ("Imagine Dragons Believer": recogniser asked in en-US, Believer played, then paused), recent searches, an artist from the results and back, portrait and rotation in both directions with the query kept; no crash.

## Local test.12: navigation

- Each tab keeps its root page, so switching tabs keeps scroll positions and the search query; tapping the current tab at its root builds it afresh (and scrolls to the top). Pages opened from cards stack above it with their views: back is instant and returns to the same scroll position. After the screen is rebuilt (rotation, split screen) the stack keeps its targets and rebuilds their views when they are reached.
- Pages slide in a little from the side they come from (mirrored in right-to-left languages).
- System back: collapses the player, goes back through the pages, then from another tab's root to Home, then leaves.
- A swipe up on the mini-player opens the player (a swipe down on the player closes it).
- The instrument cluster gets nothing from our interface (owner's decision).

Checked on the emulator: back from a playlist to a scrolled Home, Library and back to Home with its scroll, Home tapped again (refresh), back from Library to Home, swipe up and down; no crash.

## Local test.12: our interface is the main screen

- The launcher entry keeps the BYD-derived activity's component name, `local.bydui.com.vivid.spotify.MainActivity`, as an `activity-alias` of `HuPlayerActivity` (patch_headunit.py); the BYD-derived activity is no longer declared (its code stays in the DEX for now). Launcher icons and pinned shortcuts, the playback notification, profile switching ("Back to Spotify", the QR hand-off) and the BYD media service open the app by that name and reach our screen. `HuPlayerActivity` keeps `configChanges=orientation|screenSize` and gets `windowSoftInputMode=adjustResize`.
- What the BYD-derived main screen did on opening now happens for ours: `TaskGuardService.opened` (the playback gate and the task-removal guard) in `onCreate` and `onResume`; once per task `ui_open` (auto resume), the diagnostics notice and the update offer (UnifiedFactory); the profile hand-off acknowledgement and the QR sign-in request (ProfileButton → `HuPlayerActivity.signIn`); a changed interface scale rebuilds the screen.
- Signed out, the backend's playback state is an error (AUTHENTICATION_EXPIRED) whose resolution intent opens Spotify's QR sign-in screen. The content then shows the backend's message and button ("Please log in to use Spotify.", "Log in") over the pages; the button opens that screen as the BYD-derived screen did (`startIntentSender`, background start allowed on Android 14+). Signed in again, the tabs load afresh.
- The "New interface (test)" tile is gone from Head unit settings.
- The BYD driving restriction (it blocked only the login and settings buttons) and the instrument cluster calls were dormant in this build (their start-up path never runs) and are not carried over.

Checked: on the Android 12 emulator with the owner's account, an update in place, the launcher opening our screen (component shown as the alias), the playback gate opening, play and pause, Head unit settings, Accounts and "Back to Spotify"; on an Android 13 emulator (1024×600) signed out, an update from the published test.11 (95488 → 95489), the sign-in panel, "Log in" opening the QR sign-in screen and back, the forced update job (`up_to_date`), the backend process alive, no crash.

Also: a playback error other than signing in (Premium required, not available) shows the backend's message in place of the artist in the mini-player and the player (not triggered in testing: offline, the backend reports no error, only an idle state). The release audit (`tools/apk_audit.py`) checks that the launcher's only entry is the alias of our activity and that the BYD-derived activity is not declared. Android 9 (API 28 emulator, portrait 768×1024, signed out): update from test.11, our screen with the bottom navigation, the sign-in screen and back, the update job (`installed`), no crash.

Arabic (the emulator's system language set to Arabic (Egypt)): the navigation on the right, rows and pills from the right, the backend's texts in Arabic, the cover on the left in the player, the microphone on the right of the search field. Latin names (track and playlist titles) aligned to the left at first, away from their covers, because a text's own direction decided; our labels now align to the interface's start edge (`HuUi.Label`: `TEXT_ALIGNMENT_VIEW_START`, centred and end gravity unchanged), and the search field too.


QR sign-in on short screens (about 600 dp tall: 1024×600 displays, the car's split screen): Spotify's sign-in card kept 48 dp outer margins and cut the QR code at its bottom edge (it could only be scrolled into view). The card now gets 12 dp margins when the code does not fit and, if it still does not, loses the Spotify logo above the tabs (ProfileButton.fitQr; the scroll area only grows). The Accounts button beside "Need help?" takes that button's text size and no vertical padding, so its label is no longer cut. Signed out after a cold start, our sign-in panel did not appear: the error code reaches the interface process only once the session's extra binder is connected, so any error state with a resolution intent now shows the panel. Checked on the Android 13 emulator (1024×600): the whole QR code shown, the panel after a reboot.

## Local test.12: the queue in our interface

- `HuQueue`: the queue is a card over the player (from its queue button), sliding in from the end side; back closes it. It shows the backend's queue through the session (`MediaControllerCompat.getQueue`) in its groups (Now playing, Next in queue, Next from …); a row plays that track, its minus button removes it (`REMOVE_QUEUE_ITEM_AT`), and the actions above start a song or artist radio, set the sleep timer, open Jam and clear the queued tracks — what the queue editor (`HeadUnitQueueActivity`, still in the APK) did.
- The player's top bar gets a moon for the sleep timer (`HuSleep`, the same menu as in the queue; green while the timer runs).

Checked on the emulator without starting playback (the owner's account may be playing in the car): the queue with its groups, the sleep timer from the moon (15 and 30 min, the label in the queue, Off), back. Radio, skipping to a track and removing were not pressed.

Podcast episodes and audiobook chapters show how much is played: the backend gives each its completion status (`android.media.extra.PLAYBACK_STATUS`: 0 not played, 1 partly played, 2 played) and share (`androidx.media.MediaItem.Extras.COMPLETION_PERCENTAGE`), and already writes "15 mins left" into the subtitle. Partly played rows get a thin progress bar, played ones a check mark. Checked with a partly played episode (2 %); no played one was at hand.

Keyboard (owner's report from the car): with `adjustResize` the keyboard shrank the window on landscape screens and the navigation rail's items squeezed into each other. Landscape screens now let the keyboard lie over the interface (`SOFT_INPUT_ADJUST_NOTHING`; the search field is at the top), portrait screens still make room for it.

## Local test.12: download progress (owner's request)

The backend has no download field for a track: it writes its own localized prefix into the subtitle ("Downloaded • %s", string `downloaded_subtitle_prefix`), and a playlist or album has three states (download, downloading, downloaded) but no share. Our rows now show a green arrow instead of that prefix (grey while the page downloads and the track is not yet there); while a page downloads, its header shows "Downloaded 25 of 150" with a thin bar, counted from the tracks' marks and refreshed every 4 s (only "Downloading…" for nodes at the backend's 200-child limit). Probe (`--es mode action … --ez children true`): the backend's marks grow during a download (0, 9, 24 … 100 of 100 in about 40 s). Checked on the emulator with the owner's agreement (playlists already in the library; downloads removed afterwards, Liked Songs unchanged at 397): Dance Party went from 4 to 141 of 150, then downloaded with green marks.

Page header panel (owner's choice among five trial looks: none, a lighter panel, a gradient from the cover's colour, a panel in the cover's colour, the blurred cover — the last, with rounded corners): the header of playlist, album, artist and show pages sits on its cover, blurred (`RenderEffect`, 40 dp) and dimmed by night (60 % black) or lightened by day (60 % white), in a rounded panel 8 dp inside the content card with 14 dp corners, concentric with the card's 22 dp. Before Android 12 the panel takes the cover's colour; without a cover it is a slightly lighter grey. Portrait pages put the same panel above the list. A cover already in the cache arrived before the panel existed and left it grey; the panel now paints it at once.

Cover corners (owner's report from the car): a thin light rim showed around rounded covers by day. The grey placeholder behind each cover had the same rounded shape and showed through the image's anti-aliased edge. Covers are now `HuUi.Cover`: the corners are cut by the view's own outline and the placeholder is removed as soon as the image is set (cards, rows, the page header, the mini-player, the player, the queue).

Mini-player and player on the blurred cover (owner's choice after a side-by-side trial on the emulator): with "Colour from cover art" on, both are drawn on their cover, centre-cropped and blurred (`HuUi.Blur`: a `RenderNode` with a 40 dp blur on Android 12+, a 16× smaller bitmap drawn large before), under a scrim (mini-player 70 %, player 60 %; black by night, white by day), with the cards' 22 dp corners as their outline. With the setting off they stay plain. Trial comparisons: Spotify/redesign/screens/test12/blur-looks/.

## Local test.12: "This app is free" (owner's request)

People have sold this app as an official product. `HuFree` says the app is free, that a buyer was misled and should ask for the money back, that it comes only from the official page (its address and a QR code to open it on a phone) and that it is unofficial, not made by Spotify or BYD — in five languages. It appears once per version as a dialog over our main screen, as a line with a small code under "Log in" on our sign-in panel, as a line at the bottom of Spotify's QR sign-in screen where the sign-in card leaves room for it (not on screens about 600 dp tall), and from "About this app" in Head unit settings (with the version). The QR code is fixed (the official page) and embedded as its modules (version 4, level M), generated and checked with macOS CoreImage; codes on screenshots of the dialog and of the sign-in panel decode to the official page.

## After test.12: changed copies say so

Removing the free-of-charge notice means changing the APK and signing it with another key (only the owner has the release key). `HuFree.genuine` compares the installed package's signing certificate (and its history) with the release certificate (`UpdateFeed.SIGNER`); a copy signed otherwise shows "This copy has been changed" in red every time the main screen opens — not a free app from the official page, ask for your money back, no updates (Android refuses our updates over another key), may not be safe — and the sign-in lines say the same. The terms are logged once and kept in the code (`HuFree.NOTICE`), and written in [NOTICE.md](../NOTICE.md) and [AGENTS.md](../AGENTS.md) (a note for AI assistants asked to remove them). Checked on an Android 13 emulator with a trial build that expected another certificate (the warning every launch, the red lines) and with the real build (no warning).
