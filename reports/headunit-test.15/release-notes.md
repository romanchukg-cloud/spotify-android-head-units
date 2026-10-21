Test.15 shows the time in the mini-player, keeps a thin frame around the screen and lets the navigation close the full-screen player. It includes everything from test.14: home-screen widgets, Spotify links and what's playing for other apps.

- **The time in the mini-player:** elapsed and length ("2:49 / 5:24") at the end of the mini-player when it is wide enough (a full screen or half of one).
- **The navigation closes the player:** with the full-screen player (or the queue) open, a tab in the navigation closes it first and shows the tab.
- **A thin frame:** the interface keeps 3 dp from every edge of the screen.
- **From test.14:** home-screen widgets for launchers that let you add widgets (BYD's own launcher shows only BYD's widgets), Spotify links from other apps (`spotify:`, `open.spotify.com`, `spotify.link`), Spotify's "what's playing" broadcasts for other apps, and an option to start Spotify in the background when the head unit starts (not on BYD). See the [test.14 notes](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.14).

**Updating:** test.9–test.14 offer test.15 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.14. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (sections "After test.13" and "After test.14").

Validation: the release audit passes 51/51; 152 updater, 39 sign-in, 14 panel-access and 19 playback-start checks pass. On Android 9 and 13 emulators test.15 installs as an update over the published test.14, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. The frame and the navigation closing the player were checked on an Android 12 emulator. A build with the same code runs in a BYD Tang (DiLink 5, Android 12).

**Experimental prerelease.** Not verified: the mini-player's time in the car, the widgets with a real song, a song link playing, a real app reading what is playing, a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

Українською: test.15 — час у міні-плеєрі, тонка рамка навколо екрана, навігація згортає плеєр на весь екран; плюс усе з test.14 (віджети робочого столу, посилання Spotify, «що грає» для інших застосунків). З test.9–test.14 оновлення прийде в застосунку.
