This is a separate test build of test.21 for cars that already have Spotify as a system app, such as European BYDs, where the regular build cannot be installed.

- **Installs next to the built-in Spotify:** it has its own package (`io.github.romanchukgcloud.spotifyhu`) and shows up as "Spotify HU". You sign in to it separately, and the built-in app isn't touched.
- **No built-in Spotify in your car?** Use the [regular test.21](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.21) instead.

**Not in this build:** the Waze panel, because Waze recognises only the regular package, and in-app updates, so new versions have to be installed by hand. The car's media card may not show it.

Validation: the release audit passes 58/58. On an Android 13 emulator next to the original Spotify from the Play Store, it installs, signing in through spotify.com/pair works with one account, covers load and sound plays, the Accounts page opens, and the original Spotify still opens its own sign-in screen.

**Experimental test build.** Not verified: a real car, Spotify installed as a system app, the steering wheel buttons, the car's media card, the lighting, updating this build over itself.

Українською: окрема тестова збірка test.21 для машин, де Spotify вже вбудований, наприклад європейських BYD. Вона встановлюється поруч із ним і показується як «Spotify HU». Панелі Waze й оновлень у застосунку тут немає, а в справжній машині її ще не перевіряли.
