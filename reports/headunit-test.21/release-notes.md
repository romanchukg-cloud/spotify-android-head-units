Test.21 fixes the record of why a sign-in failed.

- **The problem:** test.20's record worked only on an older login screen. The screen you actually see words its errors itself, so "Unknown error." still left nothing in Settings or in reports.
- **Settings:** after a failed sign-in, the "Last sign-in event" line at the bottom of Settings now says what kind of failure it was, with the status from Spotify's server, for example "(403 some_code)". This stays on the head unit, and a later successful sign-in clears it.
- **Reports:** diagnostics reports include the kind of failure, but not the status. With automatic sending on, a failed sign-in the login screen cannot explain also sends an automatic report, at most one new report per 24 hours as before.

If you see "Unknown error." on the login screen: update, try once more, then send a photo of that line. Nothing else changes.

**Updating:** test.9–test.20 offer test.21 in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.20. Android 9+ (API 28).

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (section "After test.20").

Validation: the release audit passes 57/57; 152 updater, 64 sign-in, 14 panel-access, 13 Connect-message, 66 App Remote and 19 playback-start checks pass. On Android 9 and 13 emulators test.21 installs as an update over the published test.20, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. On an Android 13 emulator the login screen's own code was run with a made-up token: Spotify's server refused it, and the Settings line and the private log's failure code appeared. Signing in through spotify.com/pair worked on an emulator with a Premium and a Free account, on a candidate of the same code.

**Experimental prerelease.** Not verified: a real failed sign-in after a QR code, a car; the navigation card from test.19 with right-hand drive; DiLink 5.1 / Android 13 and other head units.

Українською: test.21 виправляє запис причини невдалого входу. У test.20 він працював лише на старішому екрані входу, тому «Unknown error.» не лишало слідів. Тепер унизу Settings рядок «Остання подія входу» показує вид збою й статус від сервера Spotify, а звіти діагностики містять вид збою. З test.9–test.20 оновлення прийде в застосунку.
