# HeadUnit test.22 — When Spotify asks for Premium

Signed test.22 prerelease with two APKs from one source commit. [Release](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.22).

## Build identity and update compatibility

Source: commit [`772438e`](https://github.com/romanchukg-cloud/spotify-android-head-units/tree/772438e/adapter). Upstream version `5.5.0.16251`, versionCode **95499** (published test.21: 95498). Signing certificate SHA-256 `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.21 (apksigner; `zipalign -c -P 16 4`).

| | Regular | Alongside |
|---|---|---|
| File | `Spotify-5.5.0-HeadUnit-test.22.apk` | `Spotify-5.5.0-HeadUnit-test.22-alongside.apk` |
| Package | `com.spotify.music` | `io.github.romanchukgcloud.spotifyhu` |
| SHA-256 | `e478389a92c73bb4d86f8ab3f2edc1344a66b1b5e314a1973779508586e8ee40` | `f23c058716c23a3d687bdbd869a7da3e368160446a44661cf847bf0c301a317f` |
| Size | 48,424,050 bytes | 48,424,050 bytes |
| Records | [release.json](headunit-test.22/release.json) | [release-alongside.json](headunit-test.22/release-alongside.json) |

The regular APK installs as an update over test.21; the alongside APK over test.21 alongside.

## How it was built

`adapter/build.sh` and `HU_EDITION=alongside adapter/build.sh`, with the prepared Spotify input only, signed with the release key.

## Implemented in these APKs

See [adapter/README.md](../adapter/README.md) (section "After test.21").

- **Premium text:** for `login_needs_premium`, `n5.r.d` and `yc.p.f` use the backend's `error_message_premium_account_required`, through `HeadUnitAccounts.loginText`. Every other code keeps the screen's text.
- **Record:** this code counts as explained by the screen: Settings keeps the status and code, and no automatic report is sent.
- **Evidence:** a car with test.21 showed `401 login_needs_premium` in Settings, and its automatic report reached the server. Another Free account signed in on an emulator, so what the server's decision depends on is not known.

## Verification

- **152** updater, **70** sign-in, **14** panel-access, **13** Connect-message, **66** App Remote plain-JVM checks and **19** playback-start fixture checks pass. These run the regular settings.
- The release audit passed **58/58** (regular, [apk-audit.txt](headunit-test.22/apk-audit.txt)) and **59/59** (alongside, [apk-audit-alongside.txt](headunit-test.22/apk-audit-alongside.txt)).
- `adapter/tests/updates/emulator.sh` with the regular APK on **Android 9 (API 28)** and **Android 13 (API 33)**: the published test.21 APK (hash checked) installed and started, test.22 installed over it (versionCode 95499), the player started, and the forced update job ran with no crash. API 33 reached the feed (`up_to_date`); API 28 could not (`check_failed`, a warning only).
- `adapter/tests/accounts_ui.py` on Android 13 passed, with the first-use choice set beforehand with automatic sending off.
- The regular APK on an Android 13 emulator, a one-off instrumentation: the real `yc.p.f` with a made-up token ran through the new call, Spotify's server refused it (`401 auth_pre_authentication_failed`), and it stayed an unrecognised error. The real `n5.r.d` with the real SDK error classes and the code `login_needs_premium` gave `0x7f11009d`, "Premium access required", the record `login_known (401 login_needs_premium)` and no new failure. The login screen showed that text when given that state. No report reached the server.
- The alongside APK on an Android 13 emulator next to the original Spotify: it updated over test.21 alongside and started, and the sign-in stayed.

## Not yet verified

The Premium text with a real refusal in a car; a fresh sign-in on these exact APKs; any car with the alongside APK; Spotify installed as a system app; the navigation card from test.19 with right-hand drive; DiLink 5.1 / Android 13 and other head units.

## Update feed

After the release, the operator publishes [spotify-update-test.22.json](headunit-test.22/spotify-update-test.22.json) with the commands in [adapter/README.md](../adapter/README.md#test9-app-updates). The alongside APK has no feed.
