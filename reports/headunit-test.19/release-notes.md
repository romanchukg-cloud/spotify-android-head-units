Test.19 gives the navigation a card of its own.

- **Navigation:** Home, Recents, Browse, Library, Search and Settings now sit on a card like the content and the mini-player, with the same colour, corners and gap, by day and by night. Before, they lay on the window's background and did not read as a panel. The bar along the bottom on portrait screens changes the same way.

Nothing else changes.

**Updating:** test.9–test.18 offer test.19 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.18. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (section "After test.18").

Validation: the release audit passes 56/56; 152 updater, 39 sign-in, 14 panel-access, 13 Connect-message, 66 App Remote and 19 playback-start checks pass. On Android 9 and 13 emulators test.19 installs as an update over the published test.18, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. The new card was seen on an Android 13 emulator by day, by night, in portrait and in Arabic.

**Experimental prerelease.** Not verified: the new card in a car and with right-hand drive; DiLink 5.1 / Android 13 and other head units.

Українською: test.19 — навігація тепер на власній картці, як контент і міні-плеєр: той самий колір, кути й проміжок, удень і вночі. Більше нічого не змінено. З test.9–test.18 оновлення прийде в застосунку.
