# headunit-test.4 validation — 2026-10-10

Experimental prerelease **v5.5.0-headunit-test.4** merges the account manager into the head-unit adapter. The Spotify Automotive backend remains 5.5.0.

APK: `Spotify-5.5.0-HeadUnit-test.4.apk` · **57,868,288 bytes**.

SHA-256: `534a3d0000ab34c87523509e183e326218d83b01c496da2cb4285c942a09edc4`.

Package `com.spotify.music`, min API 28, target API 33, versionCode 95482. Signer certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, unchanged from public test.2/test.3. An in-place update is supported; this release has not been installed on the car or tested with the user's existing authenticated data.

## Changes

- **Head unit settings → Accounts → Add account · QR**. Accounts is a large button above playback switches, within the native system-bar insets. It is also accessible from native Spotify settings and beside the help button on the QR screen. No floating Home button.
- Up to eight accounts in a private Android Keystore AES-GCM vault. Names and IDs cross the same-UID provider; credentials stay in the backend. Rename saved accounts, switch accounts, remove inactive accounts. Active removal is rejected. English, Ukrainian, Russian, Arabic and Simplified Chinese labels are included.
- A lightweight `:profiles` coordinator survives the SDK/UI restart. The settings entry opens a separate task, avoiding destruction when the player task is cleared. `:settings` finishes when Accounts opens; both lightweight processes use plain Android Application.
- Account changes pause playback, clear sleep/boot resume snapshots and lock transport commands until the old SDK processes stop. Normal explicit PLAY after ordinary task removal remains supported. A failed auth-store write attempts rollback; failed rollback keeps the gate locked and retains the saved vault.
- Restart recovery checks process PID and start uptime. The coordinator waits for old process death and a real UI acknowledgement, with bounded retry and visible failure. Android 9 does not call the Android 11-only decor-fitting API.
- The account feature is **experimental**. The SDK auth store is reused; proprietary SDK/UI code is not duplicated into the published source tree.

## Validation of the final APK

[Static audit](headunit-test.4/apk-audit.txt): **26 PASS, 0 FAIL**. This adds private account/coordinator components, auth storage/settings entry and transition transport guards to the existing head-unit audit. Three DEX files remain version 039. Signature and 16 KiB ZIP/native alignment pass. [Structural checks](headunit-test.4/structural-checks.json) retain 2,438 backend payloads, 22,982 backend resource strings and 5,114 relocated UI classes.

| Check | Android 9 / API 28 AOSP | Android 15 / API 35 Google Play |
| --- | --- | --- |
| Head-unit policy instrumentation | 16 PASS | 16 PASS |
| Logged-out main + settings, five viewports | PASS | PASS |
| Independent MediaBrowserCompat root / external OFF rejection | PASS | PASS |
| Accounts entry, separate task, visible Add at 800×480 | PASS | PASS |
| Add → SDK/UI restart → native QR activity → Accounts return | PASS | PASS |
| Two synthetic account rows and switch/rename/remove buttons at 800×480 | PASS | PASS |
| App crash buffer during account flow | Empty | Empty |
| Isolated account-store and restart regressions | PASS | PASS |
| Real Spotify account A → B → A / audio | **Pending** | **Pending** |

The five viewports are 800×480@160, 1024×600@160, 1280×720@240, 1920×720@240 and 768×1024@160 portrait, on ordinary tablets rather than Automotive images. [API 28 policy](headunit-test.4/api28-policy.txt), [API 35 policy](headunit-test.4/api35-policy.txt), [matrix](headunit-test.4/matrix.json), [account UI results](headunit-test.4/accounts-ui.json).

The account UI test first verifies the private settings entry through instrumentation. It then ends instrumentation and tests process death through normal UI interactions: killing an instrumented backend causes Android to force-stop the target package, which would invalidate that restart test. The native login screen is reached through LOG IN and the account screen through its QR help-row entry. No account is authorized. The test verifies activity launch, backend restart and return navigation, **not** validity of a Spotify QR session or server login. Live QR screenshots and session data are not published. A second pass seeds two synthetic saved-profile rows in the disposable emulator, checks both switch buttons are visible, and repeats the normal QR/restart/return flow; see [seeded UI results](headunit-test.4/accounts-ui-seeded.json). No synthetic credentials are submitted for authentication.

[API 28 account regressions](headunit-test.4/api28-profiles.txt) and [API 35 account regressions](headunit-test.4/api35-profiles.txt) run in an isolated test UID with real Android Keystore encryption and a mocked SDK auth store. They cover tamper rejection without overwrite, deduplication, rename, active removal guard, unknown restore, unreadable active-user rejection, stale-PID rejection, write failure/rollback, failed rollback, playback lock against UI/transport/wake resume, recovery of an interrupted restart, cancellation, restoring two synthetic accounts and inactive removal. They cannot prove real reusable credentials will be accepted by Spotify.

Existing test.3 results for Android 11/13, boot request dispatch and task removal are [historical](headunit-test.3.md); they are not new test.4 hardware passes. The cold-session fixture intentionally kills its process and is not an audio test. Arbitrary background media broadcasts can still encounter Android foreground-service restrictions.

## Screenshots

Only logged-out settings and account screens are published. Android 9 can retain a larger framebuffer PNG than its overridden logical viewport.

- Android 9: [Settings](headunit-test.4/screenshots/api28-settings.png) · [Accounts](headunit-test.4/screenshots/api28-accounts.png) · [Return from QR](headunit-test.4/screenshots/api28-returned-accounts.png).
- Android 15: [Settings](headunit-test.4/screenshots/api35-settings.png) · [Accounts](headunit-test.4/screenshots/api35-accounts.png) · [Return from QR](headunit-test.4/screenshots/api35-returned-accounts.png).

The [Android 9](headunit-test.4/screenshots/api28-synthetic-profiles.png) and [Android 15](headunit-test.4/screenshots/api35-synthetic-profiles.png) two-row screenshots use explicitly synthetic names; neither is a real authenticated account.

## Still required

A real account A → B → A switch with Home/Library/playlist isolation, track selection and playback; existing login/vault preservation on the car; server credential expiry and QR reauthentication; real steering-wheel controls; audio continuity during navigation; Recents ON/OFF behavior; actual sleep/reboot audio restoration; offline downloads; BYD DiLink5, AAOS and other head-unit regression. Account-store mocks and unauthenticated emulator screens are not substitutes for those checks.

No car update was performed. See [source/build and account test instructions](../adapter/README.md).
