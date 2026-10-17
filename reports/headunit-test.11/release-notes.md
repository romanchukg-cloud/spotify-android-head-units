Test.11 adds offline downloads, voice search, a queue editor with radio and Jam, and fixes a crash in the queue.

- **Offline downloads (experimental):** a download button on playlist and album pages, next to the add/✓ button: download; downloading (tap to stop); downloaded (tap to remove, with a confirmation). Downloaded music plays without a connection, also right after the car starts, and Library → **Downloads** lists it. Needs Spotify Premium; Spotify's own download limit applies, and downloading adds the item to Your Library, as in the phone app. Spotify keeps downloads switched off for its Automotive client; this build turns the client's existing support on, so Spotify may change how this works.
- **Voice search:** a microphone in the search field when the head unit has a speech recogniser (Google's or another). Say an artist, song or album: the best match plays and the results stay on screen. Without a recogniser the button does not appear.
- **Queue editor and radio:** a pencil on the queue page opens a large queue screen: tap a track to play it, the minus button removes it, **Clear queue** removes the tracks you added. **Song radio** and **Artist radio** start a Spotify station from the current track.
- **Jam:** a Jam button on the queue page (and in the queue editor) opens Spotify's own Jam with a QR code: up to 5 friends listen along and add songs.
- **Fix:** removing a track from the queue or adding one crashed Spotify's music process since test.3. Both now work.

**Updating:** test.9 and test.10 offer test.11 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.10. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`): the new screens change the interface. See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md).

Validation: the release audit passes 45/45, 152 updater and 39 sign-in checks pass. On Android 9 and 13 emulators test.11 installs as an update over the published test.10, starts and runs its update check without a crash. On an Android 12 emulator with a signed-in Premium account: downloads (and offline playback after a cold start), voice search with a test recogniser, removing/adding/clearing the queue, song and artist radio, and Jam were checked.

**Experimental prerelease.** Not yet checked in a car. Whether BYD DiLink has a speech recogniser for the microphone button is unknown. Not verified: the New style in Arabic (right-to-left), right-hand-drive layouts, DiLink 5.1 / Android 13 and other head units.

Українською: test.11 — завантаження для офлайн-прослуховування (кнопка на сторінці плейліста чи альбому, розділ «Downloads» у Бібліотеці), голосовий пошук (якщо в магнітолі є розпізнавання мовлення), редактор черги з радіо пісні й виконавця, Jam з QR-кодом для пасажирів і виправлення падіння при видаленні треку з черги. З test.9 і test.10 оновлення прийде в застосунку.
