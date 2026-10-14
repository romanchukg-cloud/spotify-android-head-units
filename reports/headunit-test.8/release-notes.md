Test.8 keeps the Spotify sign-in on head units.

- **Stable device ID:** Spotify's saved login is tied to the device ID, which it read from the system `ANDROID_ID`. The ID seen on the first test.8 start is kept, so firmware that changes `ANDROID_ID` no longer signs you out.
- **Old accounts:** removing an inactive Spotify account in Android settings (left after switching accounts) no longer signs out the active account.
- **Lost login restored:** if the saved login disappears on the head unit without a sign-out, for example after a power cut, the last active account is restored once per boot from the encrypted Accounts vault.
- **Sign-in history:** Head unit settings show the last sign-in event and its reason. Unexpected sign-outs are recorded as authentication failures in diagnostics; with automatic sending on, they are reported like other measured failures.

If Spotify itself rejects the login (password change or "sign out everywhere"), sign in again with the QR code.

**Update without uninstalling.** Package and signing certificate match test.7. Android 9+ (API 28).

Built incrementally from the published test.7 APK by `adapter/incremental_release.py`: only the Spotify backend DEX (new sign-in hooks), the adapter DEX and the manifest versionCode change. Resources, the BYD interface and native libraries are byte-identical to test.7.

Validation: each of the 8 new smali patches matched exactly once; the patched backend and adapter DEX assembled; every backend/UI call into the adapter resolves; 39 sign-in logic checks, the compatibility audit and the release policy audit pass; the signing certificate equals test.7.

**Experimental prerelease:** not yet tested with a signed-in account on an emulator or in a car. An actual `ANDROID_ID` change and recovery after a power cut are unverified.

Українською: вхід у Spotify більше не злітає через зміну ID пристрою, видалення старого акаунта чи втрату даних на магнітолі. Оновлюйте без видалення.
