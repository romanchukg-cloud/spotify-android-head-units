Test.22 says when Spotify asks for Premium, and has a second APK for cars with a built-in Spotify.

- **Login screen:** when Spotify's server asks for Premium, the login screen now says so, instead of "Unknown error.". This was the cause in one car, seen with test.21. The decision is Spotify's: another Free account signed in on an emulator, and what it depends on is not known yet.
- **Settings and reports:** the "Last sign-in event" line still shows the status and code. This case no longer sends an automatic report, because the screen explains it.

**Which APK?**

- `Spotify-5.5.0-HeadUnit-test.22.apk` is the regular build. **Updating:** test.9–test.21 offer it in the app (or Head unit settings → Updates → Check). From older builds, install it once by hand as an update without uninstalling. Package and signing certificate match test.21. Android 9+ (API 28).
- `Spotify-5.5.0-HeadUnit-test.22-alongside.apk` is for cars that already have Spotify as a system app, such as European BYDs, where the regular build cannot be installed. It installs next to the built-in app and shows up as "Spotify HU". You sign in to it separately, and the built-in app isn't touched. It has no Waze panel and no in-app updates, so new versions have to be installed by hand. The car's media card may not show it. **It has not been tried in a real car yet: please tell us what works.**

Built with the full reference build (`adapter/build.sh`). See [adapter/README.md](https://github.com/romanchukg-cloud/spotify-android-head-units/blob/main/adapter/README.md) (sections "After test.21" and "The edition next to a built-in Spotify").

Validation: the release audit passes 58/58 (regular) and 59/59 (alongside); 152 updater, 70 sign-in, 14 panel-access, 13 Connect-message, 66 App Remote and 19 playback-start checks pass. On Android 9 and 13 emulators the regular APK installs as an update over the published test.21, starts and runs its update check without a crash; the sign-in and Accounts flow passes on Android 13. On an Android 13 emulator the app's real error mapping gave the Premium text for this server code, and the login screen showed it. The alongside APK updated over test.21 alongside next to the original Spotify on Android 13 and kept its sign-in.

**Experimental prerelease.** Not verified: the Premium text with a real refusal in a car, a fresh sign-in on these exact APKs, any car with the alongside APK, Spotify installed as a system app, the navigation card from test.19 with right-hand drive, DiLink 5.1 / Android 13 and other head units.

Українською: test.22 прямо каже на екрані входу, коли Spotify вимагає Premium, а не пише «Unknown error.». З test.9–test.21 оновлення прийде в застосунку. Для машин із вбудованим Spotify тут є окремий APK, який встановлюється поруч і показується як «Spotify HU»: у справжній машині його ще не перевіряли, тож розкажіть, що працює.
