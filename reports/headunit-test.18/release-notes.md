Test.18 brings Spotify to Waze's audio panel.

- **Spotify in Waze:** open Waze's audio panel (the music button), pick Spotify and accept Waze's own "Connect to Spotify" once. Waze then shows what is playing with its cover and controls, and opens Home, Recents, Browse and Library without leaving the map. When Spotify restarts, Waze connects again by itself.
- **Buttons in Waze:** the heart beside the track (Liked Songs), and shuffle where Spotify allows it — for example in Liked Songs; Spotify does not offer shuffle in every playlist. Waze shows no repeat button for Spotify.
- **Other apps** built on Spotify's Android SDK can connect through Spotify's App Remote, which the Automotive app lacks.

**Updating:** test.9–test.17 offer test.18 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.17. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (section "After test.17").

Validation: the release audit passes 56/56; 152 updater, 39 sign-in, 14 panel-access, 13 Connect-message, 66 App Remote and 19 playback-start checks pass. On Android 9 and 13 emulators test.18 installs as an update over the published test.17, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. In a BYD Tang (DiLink 5, Android 12) with Waze for BYD 5.22, a build of the same code opened in Waze's audio panel, the heart appeared beside the track and shuffle in Liked Songs; the heart works in the player too. On an Android 12 emulator Waze connected, browsed Home, Recents, Browse and Library and asked for its consent.

**Experimental prerelease.** Not verified: other Waze builds than Waze for BYD 5.22, a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

Українською: test.18 — Spotify в аудіопанелі Waze: виберіть Spotify у Waze й один раз прийміть «Connect to Spotify». Waze показує, що грає, і керує відтворенням, відкриває Головну, Нещодавні, Огляд і Бібліотеку, має серце й перемішування там, де Spotify його дозволяє. З test.9–test.17 оновлення прийде в застосунку.
