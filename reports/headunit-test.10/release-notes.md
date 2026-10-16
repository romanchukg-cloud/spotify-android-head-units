Test.10 brings a redesigned interface, the **New style**, closer to today's Spotify. It is on by default; the previous look stays available as **Classic**.

![Home in the New style in a BYD car, day theme](https://romanchukg-cloud.github.io/spotify-android-head-units/screens/car-home-day.jpg)

![Library with the Settings button, day theme](https://romanchukg-cloud.github.io/spotify-android-head-units/screens/car-library-day.jpg)

![Library at night, the mini-player tinted by the cover](https://romanchukg-cloud.github.io/spotify-android-head-units/screens/car-library-night.jpg)

- **New style:** rounded cards with small gaps, the Figtree typeface, day and night themes that follow the car, rounded app corners, and the mini-player as the footer of the content card. Prefer the old look? Head unit settings → Appearance → Classic.
- **Colour from cover art:** the mini-player and the player take the colour of the current cover. Separate switch, on by default.
- **Player:** the controls come first, then like, cover and title. The **like button** now appears and works.
- **Queue:** the playing track shows its cover with an animated equaliser.
- **Library:** a Settings button (gear) replaces "Your Library" and opens Head unit settings.
- **Head unit settings** redesigned: Appearance (style, colour from cover art, interface scale), Spotify playback switches, Accounts, Updates and Support.
- **Closing Spotify always stops the music;** the separate setting was removed.
- **Fix:** switching between day and night with Library open no longer crashes the player (a bug since test.3).

**Updating:** on test.9, Spotify offers test.10 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.9. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`) from the prepared inputs, because the New style changes the interface resources: they are generated as `car` UI-mode resources by `adapter/restyle.py`, and Classic keeps the BYD-derived interface unchanged. See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md#test10-classic-and-new-style).

Validation: the release audit passes 45/45, 152 updater and 39 sign-in checks pass. On Android 9 and 13 emulators test.10 installs as an update over the published test.9, starts and runs its update check against the live server without a crash. On an Android 12 emulator with a signed-in account the update kept the sign-in; day and night, 1024×600 and portrait, and Classic ↔ New switching were checked. In the owner's car (BYD DiLink 5.0 / Android 12) this exact APK was installed over test.9 without uninstalling: sign-in and music were kept, and the New style was checked in the day theme with no crash.

**Experimental prerelease.** Not yet verified: the update from test.9 through the app on a head unit, the New style in Arabic (right-to-left), right-hand-drive layouts (the player keeps the Classic arrangement there), DiLink 5.1 / Android 13 and other head units.

Українською: test.10 — новий вигляд, ближчий до сучасного Spotify (увімкнений за замовчуванням, Класичний лишається в Налаштуваннях магнітоли → Вигляд): заокруглені картки, шрифт Figtree, денна й нічна теми, колір з обкладинки, робоча кнопка «подобається», обкладинка й еквалайзер у черзі, нові налаштування. З test.9 оновлення прийде в застосунку.
