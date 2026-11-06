Test.20 helps find out why a sign-in fails.

- **Settings:** after a failed sign-in, the "Last sign-in event" line at the bottom of Settings says what kind of failure it was, with the status from Spotify's server, for example "(403 some_code)". This stays on the head unit, and a later successful sign-in clears it.
- **Reports:** diagnostics reports now include the kind of failure, but not the status. With automatic sending on, a failed sign-in the login screen cannot explain also sends an automatic report, at most one new report per 24 hours as before.

If you see "Unknown error." on the login screen: update, try once more, then send a photo of that line. Nothing else changes.

**Updating:** test.9–test.19 offer test.20 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.19. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (section "After test.19").

Validation: the release audit passes 57/57; 152 updater, 64 sign-in, 14 panel-access, 13 Connect-message, 66 App Remote and 19 playback-start checks pass. On Android 9 and 13 emulators test.20 installs as an update over the published test.19, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. The Settings line was checked on an Android 13 emulator with a record written by hand.

**Experimental prerelease.** Not verified: a real failed sign-in, a successful sign-in on this build, a car; the navigation card from test.19 with right-hand drive; DiLink 5.1 / Android 13 and other head units.

Українською: test.20 допомагає з'ясувати, чому не вдається вхід: унизу Settings рядок «Остання подія входу» показує вид збою й статус від сервера Spotify, а звіти діагностики містять вид збою. З test.9–test.19 оновлення прийде в застосунку.
