# Head-unit adapter source

This directory contains the project's Java integration code, resource/manifest patch generators, packaging code and emulator test sources. `../tools/apk_audit.py` audits the **finished APK**. It does not contain Spotify's proprietary SDK source or the decompiled BYD interface; those are local build inputs. Headunit-test.4 merges the experimental account manager into the main settings screen.

The APK contains the original Spotify Automotive 5.5.0 backend, our helper DEX, and the relocated BYD-derived interface. `ParcelBridge` translates media parcelables across the two class namespaces. `HeadUnitProfile`, preferences/provider, playback receiver/service and settings activity implement the head-unit adaptation. `PlaybackGate` and `TaskGuardService` implement explicit task-removal behavior. The backend resource IDs stay at package `0x7f`; UI IDs use `0x7e`.

## Build prerequisites

- JDK 17+ (verified with JDK 21), Python 3, apktool **2.11.1** and its JAR.
- Android platform 35 and build-tools 35.0.0. Minimum supported Android API remains **28**. All three DEX files use version 039.
- Two **prepared adaptation input APKs**, not arbitrary Spotify releases. They are not fetched or uploaded by these scripts:

| Input | SHA-256 |
| --- | --- |
| `SpotifySDK-5.5.0-BYD-test.apk` | `6527109d69dbbee4c9bef177879dbb9cebedc806dd6b07cb0e89a223125247d2` |
| `Spotify-BYD-for-5.5-test.apk` | `087a78a67501543bceac2700cbe3e2c3b9ea6e84037fd898fe8941b1f7b72ace` |

- A local signing keystore. Keys are never committed. A developer build with a different key cannot update the published APK or preserve its app data through an in-place update. The published test.4 uses the same package and certificate as public test.2.

From the repository root:

```sh
python3 adapter/bootstrap.py --sdk-apk /path/SpotifySDK-5.5.0-BYD-test.apk --ui-apk /path/Spotify-BYD-for-5.5-test.apk
export ANDROID_SDK_ROOT=/path/to/Android/sdk
export APKTOOL_JAR=/path/to/apktool_2.11.1.jar
export HU_KEYSTORE=/path/to/your.keystore
export HU_KEYSTORE_PASS=pass:your_password
./adapter/build.sh
python3 tools/apk_audit.py adapter/Spotify-5.5.0-HeadUnit-test.4.apk --min-api 28
```

The optional variables `ANDROID_HOME` and `BUILD_TOOLS_VERSION` select the SDK and tools; local defaults match Android Studio on macOS. `adapter/tests/build.sh` builds test APKs signed with `HU_KEYSTORE` (the test keystore password defaults to `android`; set `HU_KEYSTORE_PASS` for another key). Only run instrumentation on disposable emulators: its checks change head-unit preferences and playback state.

The fresh input-to-APK build was verified before publication. ZIP timestamps and resource compiler normalization can change whole-APK hashes between builds; verify the downloaded release against the published hash.

## Verification

```sh
./adapter/tests/build.sh
adb install adapter/Spotify-5.5.0-HeadUnit-test.4.apk
adb install adapter/tests/out/client.apk
adb install adapter/tests/out/test.apk
python3 adapter/tests/matrix.py emulator-5554 35
```

The independent `hu.browser` app uses `MediaBrowserCompat` from the locally supplied backend DEX. It has a different Android UID and connects through the public service. The policy instrumentation runs under Spotify's UID with the same test certificate. It exercises the settings provider, defaults, controller checks, explicit close/play, binder death callback, pause and scale bounds. It **does not simulate successful Spotify authentication or certify audio playback**. The cold-session fixture deliberately terminates its process; an instrumentation “Process crashed” result for that fixture is expected and is not a successful audio test.

See [test.4 validation](../reports/headunit-test.4.md) for what was tested and what requires a signed-in account or real hardware. Android package visibility allows a client that binds to the service to become visible automatically ([Android documentation](https://developer.android.com/training/package-visibility/automatic)); the adapter does not request `QUERY_ALL_PACKAGES`.

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
