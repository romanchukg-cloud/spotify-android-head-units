Test.17 makes loading and moving around the interface smooth.

- **Loading:** while a page loads, placeholders in the shape of what comes (Home's rows of cards, the grids, Library's filters, a playlist's tracks, search results) shimmer in place of a "Loading…" line. When the content arrives, the placeholders fade out where they are and the cards and rows rise in one after another.
- **Covers:** a cover that had to load fades in over its grey placeholder; when the track changes, the new cover and its blurred background fade in over the old ones in the mini-player and the player.
- **Pages and tabs:** a page you open slides in from the side and back returns the way it came (mirrored in Arabic); tabs fade through. The navigation highlight slides to the tab you tap, and every item reacts to a press.
- **Player:** the full-screen player rises smoothly and leaves faster; opened again while closing, it turns back from where it is. The queue opens and closes the same way.

With Android's animations turned off, everything simply appears.

**Updating:** test.9–test.16 offer test.17 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.16. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (section "After test.16").

Validation: the release audit passes 52/52; 152 updater, 39 sign-in, 14 panel-access, 13 Connect-message and 19 playback-start checks pass. On Android 9 and 13 emulators test.17 installs as an update over the published test.16, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. The motion was checked on an Android 12 emulator in light and dark themes, and in a BYD Tang (DiLink 5, Android 12) with a build of the same code.

**Experimental prerelease.** Not verified: a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

Українською: test.17 — плавні переходи: замість «Завантаження…» заготовки у формі вмісту, картки й рядки плавно з'являються, обкладинки проявляються, сторінки заходять збоку, вкладки змінюються через згасання, плеєр піднімається плавно. З test.9–test.16 оновлення прийде в застосунку.
