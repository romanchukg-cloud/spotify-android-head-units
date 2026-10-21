Test.14 adds Spotify widgets for the home screen, opens Spotify links from other apps, tells other apps what is playing and can start Spotify in the background when the head unit starts.

- **Home-screen widgets:** two Spotify widgets (about 4 × 2 and 2 × 2) for launchers that let you add widgets. Their layout follows their size — a strip, squares or a large player with the cover, elapsed and remaining time and previous / play / next — in the launcher's day and night colours. Without Spotify running they keep the last song, paused; Play starts Spotify. BYD's own launcher shows only BYD's widgets, so on BYD they need another launcher.
- **Spotify links:** links that other apps open — `spotify:`, `open.spotify.com` and `spotify.link` — show the playlist, album, artist or podcast, play the song or search. On Android 12+ web links open here after you allow them (Settings → Apps → Spotify → Open by default).
- **What's playing, for other apps:** Spotify sends Android's standard Spotify broadcasts for the song, play/pause and the queue, so launchers, scrobblers and head-up apps can show it. Head unit settings → Show other apps what is playing.
- **Start with the head unit:** Head unit settings → Start Spotify in the background when the head unit starts (off by default). Spotify starts without opening and without playing. It does not work on BYD: switching the car off stops apps instead of restarting the unit.

**Updating:** test.9–test.13 offer test.14 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.13. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (sections "After test.13").

Validation: the release audit passes 51/51 (new checks: the widgets and their resources, the link filters, the session hooks for the broadcasts); 152 updater, 39 sign-in, 14 panel-access and 19 playback-start checks pass. On Android 9 and 13 emulators test.14 installs as an update over the published test.13, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. The widgets were checked in the stock launchers of Android 11 and 12 emulators, links opened playlists, albums and artists there, and starting with the unit was checked by restarting an Android 12 emulator. This exact file is installed in a BYD Tang (DiLink 5, Android 12).

**Experimental prerelease.** Not verified: the widgets with a real song, a song link playing, a real app reading what is playing, a right-hand-drive car, DiLink 5.1 / Android 13 and other head units; Arabic only on an emulator.

Українською: test.14 — віджети робочого столу для лаунчерів, у яких можна додавати віджети (на BYD — лише з іншим лаунчером), посилання Spotify з інших застосунків відкриваються в Spotify, інші застосунки бачать, що грає, і Spotify може запускатися у фоні під час старту магнітоли (не на BYD). З test.9–test.13 оновлення прийде в застосунку.
