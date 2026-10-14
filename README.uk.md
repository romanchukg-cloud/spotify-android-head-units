# Spotify для Android-магнітол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Експериментальний плеєр Spotify Automotive для Android-магнітол. Інтерфейс на основі BYD та музичний сервіс об’єднані в одному APK з однією іконкою і штатним входом Spotify.

[Завантажити APK test.7](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.7/Spotify-5.5.0-HeadUnit-test.7.apk) · [🌐 Spotify для Android-магнітол](https://romanchukg-cloud.github.io/spotify-android-head-units/uk/)

## Що вже перевірено

Headunit-test.7: APK audit 39 PASS / 0 FAIL. Android 9, 12 and 13 emulators each passed 94 diagnostic logic checks, 7 Support/notice UI checks, 8 process-restart persistence checks, 18 synthetic rhythm checks and 19 playback-resume checks. Diagnostics delivery was tested with a fake transport; no production Spotify report was submitted.

Experimental headunit-test.7. This version has not been installed on the car. The earlier test.6 lighting integration was verified on a BYD Tang EV with DiLink 5 / Android 12. Physical DiLink 5.1 / Android 13 lighting, real vehicle sleep/reboot, real account switching and other head units remain unverified.

Android 9+ (API 28). BYD, AAOS and GENERIC profiles are implemented; compatibility depends on vehicle firmware.

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## Як встановити

1. Натисніть кнопку завантаження вище, щоб отримати APK, і перенесіть його в мультимедійну систему.
2. Нашу попередню тестову збірку з тим самим підписом оновлюйте без видалення: авторизація може зберегтися. Збірки з іншим підписом так не оновляться.
3. Відкрийте Spotify й за потреби увійдіть через QR-код.
4. Для підсвітки BYD увімкніть «Музичний ритм» у налаштуваннях салонної підсвітки самого авто. Цей перемикач у налаштуваннях машини, а не Spotify.
5. Щоб додати іншого водія: Налаштування магнітоли → Акаунти → Додати акаунт · QR. Перемикання зупиняє музику й перезапускає плеєр.
6. Перевірте музику та керування. Старий окремий інтерфейс BYD видаляйте після успішної перевірки нового APK.

Android 9+ · 55,2 MiB · неофіційна адаптація

## Перевірка файлу

`Spotify-5.5.0-HeadUnit-test.7.apk` · 57 946 112 bytes

SHA-256 поточної тестової збірки:

```text
9b127d9e9f9b05dfa210207370e9f21dd9ca5eb61ec6e4d6c6bb748d069f4da7
```

Неофіційний експериментальний проєкт. Spotify та BYD — торговельні марки відповідних власників.

## Вихідний код і перевірки

Наш Java-код інтеграції, патчі, скрипти складання, аудит і тести емуляторів доступні в репозиторії. Закритий SDK Spotify та інтерфейс BYD є локальними вхідними файлами складання.

[Java / build](adapter/) · [Що вже перевірено](reports/headunit-test.7.md)
