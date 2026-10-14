Test.9 adds in-app updates, so later test builds no longer need a manual APK install.

- **Automatic checks:** once a day Spotify asks the project's update server whether a newer test build is published.
- **Verified downloads:** the new APK is downloaded in the background, on Wi-Fi only by default. Before installing, Spotify checks its size, SHA-256, package, version and the project's signing certificate.
- **You confirm every update:** Spotify offers the update when you open the player while music is not playing. Installation always goes through Android's own **Update** window; Spotify restarts, and your sign-in, accounts and settings are kept. If Android first asks you to allow installs from Spotify, allow it once.
- **Head unit settings → Updates:** check now, download or resume, pause, install, and switches for automatic checks, automatic downloads and Wi-Fi only.

**Install test.9 manually once** (update over test.8 without uninstalling). Later builds arrive in the app. Package and signing certificate match test.8. Android 9+ (API 28).

Built incrementally from the published test.8 APK by `adapter/incremental_release.py`: only the adapter DEX and the manifest change (versionCode, the `REQUEST_INSTALL_PACKAGES` permission and one private receiver). The Spotify backend DEX, resources, the BYD interface and native libraries are byte-identical to test.8.

Validation: 152 updater checks and 39 sign-in checks pass; the compatibility and release audits pass; on Android 9 and 13 emulators the APK installs as an update over test.8, the player starts, the update job runs against the live update server and nothing crashes. The signing certificate equals test.8.

**Experimental prerelease:** downloading and installing a newer build through the app can only be tested once test.10 exists; it has not been tested on a car.

Українською: оновлення тепер приходять у самому застосунку — Spotify раз на день перевіряє нову збірку, завантажує й перевіряє її, а встановлення ви підтверджуєте у вікні Android. Test.9 один раз установіть вручну поверх test.8.
