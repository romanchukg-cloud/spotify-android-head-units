Test.13 brings swipes between tracks, right-hand drive, an interface that reaches the screen's edges and a Spotify widget for other apps — and no code from the BYD player app any more.

- **Swipe to switch tracks:** swipe left on the mini-player or the player for the next track, right for the previous one. The cover follows your finger.
- **Right-hand drive:** Head unit settings → Steering wheel: Auto, Left or Right. On BYD, Auto asks the car which seat is the driver's (checked in a BYD Tang: it reports left-hand drive). With right-hand drive the navigation, the player's controls, the mini-player's controls and the search microphone move to the right.
- **Edge to edge:** the interface reaches the status bar, the dock and the split-screen divider, as other apps do, with the navigation on the window's background. In a portrait split screen the full-screen player keeps a large cover.
- **A Spotify widget for other apps:** apps that support it can show a compact Spotify player (cover, title, time, previous / play / next) in their own panels, drawn by Spotify itself. Google Maps for BYD will be the first; until then the widget is not visible anywhere.
- **No BYD player code:** the APK contains only the Spotify Automotive backend and this project's own code; it is 10 MB smaller (46.0 MiB).
- **Changed copies say so:** a copy re-signed with another key shows "This copy has been changed" and gets no updates. The terms for this project's code are in [NOTICE.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/NOTICE.md).
- **Also:** on BYD DiLink, search by voice with the steering-wheel voice button or the keyboard's microphone.

**Updating:** test.9–test.12 offer test.13 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.12. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (sections "Test.13").

Validation: the release audit passes 48/48 (new checks: no BYD player code or vehicle API, the widget's caller check, no test host in a release), 152 updater, 39 sign-in and 14 widget-access checks pass, the compatibility audit reports no failures. On Android 9 and 13 emulators test.13 installs as an update over the published test.12, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. Builds of test.13 ran in a BYD Tang (DiLink 5, Android 12): swipes, steering-wheel buttons, the BYD voice button, the ambient rhythm, the car reporting left-hand drive, split screen with Google Maps; this exact file is installed there.

**Experimental prerelease.** Not verified: a right-hand-drive car (the value comes from BYD's own code), DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

Українською: test.13 — перемикання треків свайпом, праве кермо (на BYD визначається автоматично), інтерфейс до країв екрана, велика обкладинка у вертикальному розділеному екрані й віджет Spotify для панелей інших застосунків (першим буде Google Maps for BYD). У APK більше немає коду плеєра BYD — він на 10 МБ менший. З test.9–test.12 оновлення прийде в застосунку.
