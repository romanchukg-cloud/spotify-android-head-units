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

## Local test.10: Classic and New style

The New style is the default; Head unit settings has a Classic / New switch and a separate "Colour from cover art" switch. Classic keeps the BYD-derived interface unchanged.

- **Resources.** `restyle.py` runs after `patch_resources.py` and writes the New style as `car` UI-mode resources (`values-car`, `layout-w840dp-h480dp-car`, …) derived from the local decoded interface, so no BYD layouts are committed: a palette for day and night, a copy of `Theme_VIVID_MUSIC_Base` with New attributes, rounded drawables (mini-player card, player panel, navigation highlight, filter pills, list rows), larger radii and reordered mini-player and player layouts (controls first, then like, cover and title). Data-binding tags, ids and view types are unchanged. The right-hand-drive player layouts keep their Classic arrangement.
- **Selection.** `HeadUnitStyle` sets the UI-mode type of the player's configuration to CAR for the New style. AppCompat's day/night handling drops that type from the activity configuration, so `MainActivity.getResources()` (patch section `# Restyle`) returns the styled resources. A change of style or scale restarts the interface process (`HeadUnitSettings.restartPlayer`); playback runs in the backend process and continues.
- **Like button.** Spotify Automotive publishes its like actions as `like`/`unlike`, the interface only matched `likeTrack`/`unlikeTrack`, so the button never appeared. In the New style `HeadUnitStyle.actionMatches` accepts both; the click sends the backend's own id.
- **Code-side styling.** `HeadUnitShell` turns the navigation rail and the content area into rounded cards with a 6 dp gap; `CoverAccent` tints the mini-player and the player panel with the most vivid colour of the current cover (24×24 sample, contrast-limited); `HuUi` builds the New settings screen and restyles Support, the diagnostics notice and Accounts.
- **Fix.** `ParcelBridge.getParcelableArrayList` no longer casts SavedStateHandle keys to `Parcelable`; recreating the player with Library open (for example on a day/night switch) crashed since test.3.

A full `build.sh` is required: `incremental_release.py` keeps resources byte-identical.
