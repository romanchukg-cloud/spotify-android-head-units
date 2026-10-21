Test.16 shows where the music plays and moves it between this car and your other devices, and puts shuffle, repeat and the devices button in the mini-player.

- **Devices (Spotify Connect):** the player and the mini-player have a devices button. It lists this car first, then your phone, computer or speakers that are online with the same account; the one that plays is green, and a tap moves the music there. While another device plays, the player shows "Playing on …" in green and the mini-player shows it in place of the artist; tap "This car" to bring the music back.
- **Mini-player:** shuffle and repeat around the controls and the devices button at the end, as in the full-screen player, where the mini-player is wide enough (a full screen); narrower, the title keeps the room.

**Updating:** test.9–test.15 offer test.16 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.15. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (section "After test.15").

Validation: the release audit passes 52/52 (new check: the Connect session hook and its provider route); 152 updater, 39 sign-in, 14 panel-access, 13 Connect-message and 19 playback-start checks pass. On Android 9 and 13 emulators test.16 installs as an update over the published test.15, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. In a BYD Tang (DiLink 5, Android 12) a build with the same code listed this car and a laptop, moved the music to the laptop and back.

**Experimental prerelease.** Not verified: moving the music to a phone, speakers or a group, a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

Українською: test.16 — пристрої (Spotify Connect): видно, де грає музика, і її можна перенести між цією машиною, телефоном, комп'ютером чи колонками; у міні-плеєрі тепер перемішування, повтор і кнопка пристроїв. З test.9–test.15 оновлення прийде в застосунку.
