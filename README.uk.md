# Spotify для Android-магнітол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Експериментальний плеєр Spotify Automotive для Android-магнітол. Інтерфейс на основі BYD та музичний сервіс об’єднані в одному APK з однією іконкою і штатним входом Spotify.

[Завантажити APK test.8](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.8/Spotify-5.5.0-HeadUnit-test.8.apk) · [🌐 Spotify для Android-магнітол](https://romanchukg-cloud.github.io/spotify-android-head-units/uk/)

## Що вже перевірено

Headunit-test.8 зберігає вхід у Spotify: стабільний ID пристрою, без виходу після видалення старого неактивного акаунта в налаштуваннях Android і одноразове автоматичне відновлення входу, втраченого на магнітолі. Зібрано з опублікованого test.7: змінилися лише DEX Spotify, DEX адаптера та versionCode; ресурси, інтерфейс BYD і нативні бібліотеки байт-у-байт ті самі. Усі 8 нових патчів збіглися по одному разу, 39 перевірок логіки входу й аудит сумісності пройдено, release-аудит: 42 PASS / 0 FAIL, сертифікат підпису збігається з test.7.

**Експериментальна headunit-test.8.** Зміни входу ще не перевірені з реальним акаунтом на емуляторі чи в авто; справжню зміну ANDROID_ID і відновлення після знеструмлення не перевіряли. Також не перевірені реальний сон/перезавантаження авто, акаунти A → B → A, офлайн-завантаження, кнопки на кермі та інші магнітоли. Емуляторні тести test.7 для test.8 повторно не запускались.

Android 9+ (API 28). Ритм BYD перевірено на DiLink 5 / Android 12; DiLink 5.1 / Android 13 та інші прошивки ще не перевірені. Профілі AAOS і GENERIC реалізовано; реальні магнітоли інших виробників та AAOS ще не перевірені.

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## Як встановити

1. Натисніть кнопку завантаження вище, щоб отримати APK, і перенесіть його в мультимедійну систему.
2. Нашу попередню тестову збірку з тим самим підписом оновлюйте без видалення: авторизація може зберегтися. Збірки з іншим підписом так не оновляться.
3. Відкрийте Spotify й за потреби увійдіть через QR-код.
4. Для підсвітки BYD увімкніть «Музичний ритм» у налаштуваннях салонної підсвітки самого авто. Цей перемикач у налаштуваннях машини, а не Spotify.
5. Щоб додати іншого водія: Налаштування магнітоли → Акаунти → Додати акаунт · QR. Перемикання зупиняє музику й перезапускає плеєр.
6. Перевірте музику та керування. Старий окремий інтерфейс BYD видаляйте після успішної перевірки нового APK.

Android 9+ · 55,3 MiB · неофіційна адаптація

## Перевірка файлу

`Spotify-5.5.0-HeadUnit-test.8.apk` · 57 962 496 bytes

SHA-256 поточної тестової збірки:

```text
caa026818b27aa08de38c4c76dab35a02a6ec975095ed5cba9ed0d57c7dc8a93
```

Неофіційний експериментальний проєкт. Spotify та BYD — торговельні марки відповідних власників.

## Вихідний код і перевірки

Наш Java-код інтеграції, патчі, скрипти складання, аудит і тести емуляторів доступні в репозиторії. Закритий SDK Spotify та інтерфейс BYD є локальними вхідними файлами складання.

[Java / build](adapter/) · [Що вже перевірено](reports/headunit-test.8.md)
