# Release checks

## apk_audit.py

Audits a built APK against what generic Android head units need, so each test release can be checked before it is published. It uses only the Python standard library (3.8+).

```sh
python3 tools/apk_audit.py Spotify-5.5.0-HeadUnit-test.apk               # support from Android 8.0 (API 26)
python3 tools/apk_audit.py Spotify-5.5.0-HeadUnit-test.apk --min-api 28  # Android 9 baseline
python3 tools/apk_audit.py Spotify-5.5.0-HeadUnit-test.apk --no-api      # offline: skip the framework check
```

The report is Markdown: a status table, then details. The exit code is 1 if any check is `FAIL`.

| Check | What it catches |
|---|---|
| minSdkVersion, Process routing | The APK cannot install on the target API, or `appComponentFactory` routing that Android 8 ignores |
| Back key on Android 13+ | `enableOnBackInvokedCallback="true"` while a dex still relies on `onBackPressed()` |
| Framework APIs | Calls to Android APIs added after `--min-api` up to `--baseline-api` (default 28, the level the Spotify Automotive and BYD UI dex code was compiled for). R8 removes `SDK_INT` checks below that level, so these calls have no fallback. The check keeps working after the manifest `minSdkVersion` is lowered. |
| Native libc symbols, 16 KB alignment, 32-bit ARM | `.so` files that need a newer libc, are unaligned for 16 KB-page devices, or lack `armeabi-v7a` |
| Resources | Table encodings old Android cannot read, resources with no default configuration, framework resource references missing on `--min-api` |
| Translations | App strings missing for `--locales` (default `uk,ru,ar,zh-rCN`, matching the site languages) |
| Vendor (BYD) coupling | BYD classes, broadcasts, permissions and system properties still referenced; each must be inert on other head units |
| Packaging | Signature schemes, `resources.arsc` storage, Play Store stamp metadata, Crashlytics, exported components |

The framework check downloads Robolectric's `android-all` jars from Maven Central (about 100 MB per Android version) and caches them in `~/.cache/apk-audit`. Those jars do not contain the `java.*` core library, so only Android's own APIs are checked.

Call sites name classes as they appear in the dex, so obfuscated names such as `gw.c` are expected.
