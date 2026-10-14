# HeadUnit test.8 — Stable sign-in

Signed test.8 prerelease published on 2026-10-14. It has not been installed on the car; the last verified car installation remains test.6 on DiLink 5 / Android 12. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.8).

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.8.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95485** (published test.7: 95484).
- SHA-256: `caa026818b27aa08de38c4c76dab35a02a6ec975095ed5cba9ed0d57c7dc8a93`
- Size: **57,962,496 bytes**.
- Signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.7, so test.8 installs as an update over test.7 without uninstalling. Signed V1+V2+V3; minimum API 28; target API 33; all three DEX files remain version 039.
- Source: commit [`8c87de7`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/8c87de72e2e0675c0db6a36a3bc53a8e34d0abff/adapter); built, signed and published by the Release workflow ([run](https://github.com/romanchukg-cloud/spotify-android-head-units/actions/runs/38588766001)). See [release.json](headunit-test.8/release.json).

## How it was built

`adapter/incremental_release.py` took the published test.7 APK (SHA-256 checked against [its release.json](headunit-test.7/release.json)), disassembled its backend DEX, applied the `# Sign-in stability` section of `patch_headunit.py`, reassembled it, recompiled the helper DEX from `adapter/src` and set the manifest versionCode. Only `classes.dex`, `classes2.dex` and `AndroidManifest.xml` differ from test.7; resources, the relocated BYD interface (`classes3.dex`) and native libraries are byte-identical. Every call from the backend and UI DEX into the helper resolves. The APK was zipaligned (16 KiB pages) and signed with the release key from a repository secret; the workflow refuses to continue when the certificate differs from the base release.

## Implemented in this APK

Spotify Automotive 5.5.0 signs out, and deletes its saved login, in four situations. test.8 fixes the ones caused by the head unit and records the reason for all of them:

- **Device ID:** the SDK ties the saved login to `ANDROID_ID`. `HeadUnitIdentity` keeps the ID seen on the first test.8 start, so firmware that regenerates `ANDROID_ID` no longer invalidates the login. A server rejection (`INVALID_CREDENTIALS`, `NOT_ALLOWED`, `UNSUPPORTED_LOGIN_PROTOCOL`) is recorded with its code.
- **Removed Android accounts:** an `ACCOUNT_REMOVED` broadcast signs out only when the removed account is the active user. Stale accounts left by an account switch, removals during a switch and Spotify's own removals after a sign-out are ignored.
- **Lost login:** when the SDK starts without its stored user although nobody signed out (for example after a power cut during a write), the last active account is restored once per boot from the encrypted Accounts vault, which is refreshed 20 seconds after every sign-in.
- **Minimum version:** a remote minimum version above 5.5.0 still signs out; it is recorded as `deprecated`. Only a newer Spotify base fixes it.

Head unit settings show the last sign-in event with its time and a note when the system device ID changed. Unexpected sign-outs are recorded as `auth` failures in diagnostics; no usernames, IDs or credentials leave the backend process.

## Verification

- Each of the 8 new smali patches matched exactly once; the patched backend DEX and the helper DEX assembled.
- **39** plain-JVM checks of the sign-in decisions passed: ID pinning, reason classification, the stale-removal filter, one recovery per boot and the diagnostics codes.
- The compatibility audit (`tools/apk_compat_audit.py --min-api 28 --no-api`) reports no failures.
- The release audit passed **42/42** checks, including the three new `Sign-in /` checks. See [apk-audit.txt](headunit-test.8/apk-audit.txt).
- The published asset was downloaded again: its SHA-256, certificate and versionCode match the values above.

## Not yet verified

No emulator or car run was made for test.8, and the test.7 emulator suites were not rerun. Still required: install test.8 as an update on the car and confirm the existing sign-in survives, then observe sleep/reboot over several days. An actual `ANDROID_ID` change, recovery after a power cut, account switching A → B → A, offline downloads, steering-wheel controls, DiLink 5.1 / Android 13 lighting and other head units remain unverified.
