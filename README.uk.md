# Spotify для Android-магнітол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Експериментальний плеєр Spotify Automotive для Android-магнітол. Інтерфейс на основі BYD та музичний сервіс об’єднані в одному APK з однією іконкою і штатним входом Spotify.

[Завантажити APK test.9](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.9/Spotify-5.5.0-HeadUnit-test.9.apk) · [🌐 Spotify для Android-магнітол](https://romanchukg-cloud.github.io/spotify-android-head-units/uk/)

## Що вже перевірено

Headunit-test.9 додає оновлення в самому застосунку: раз на день Spotify питає сервер оновлень проєкту про нову тестову збірку, завантажує її у фоні (за замовчуванням лише через Wi-Fi), перевіряє розмір, SHA-256, пакет, версію й сертифікат підпису і встановлює тільки після вашого підтвердження у вікні «Оновити» Android. Зібрано з опублікованого test.8: змінилися лише DEX адаптера й маніфест (versionCode, дозвіл на встановлення та один приватний приймач). Release-аудит: 45 PASS / 0 FAIL, пройдено 152 перевірки оновлювача й 39 перевірок входу, а на емуляторах Android 9 і 13 test.9 встановлюється оновленням поверх test.8, запускається й перевіряє оновлення на справжньому сервері без збоїв. В авто власника (BYD DiLink 5.0 / Android 12) test.9 встановили вручну оновленням поверх попередньої збірки: вхід, музика й музична підсвітка BYD працюють.

**Експериментальна headunit-test.9.** Завантаження й встановлення новішої збірки через застосунок можна буде перевірити лише після виходу test.10. Виправлення входу з test.8 (зміна ID пристрою, вхід, втрачений після знеструмлення) в авто окремо не відтворювали. Також не перевірені реальний сон/перезавантаження авто, акаунти A → B → A, офлайн-завантаження, кнопки на кермі та інші магнітоли.

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

`Spotify-5.5.0-HeadUnit-test.9.apk` · 58 011 648 bytes

SHA-256 поточної тестової збірки:

```text
f78026ba05fa52c67082851e710379358a05027dfcc47c404b8146147c880cd3
```

Неофіційний експериментальний проєкт. Spotify та BYD — торговельні марки відповідних власників.

## Вихідний код і перевірки

Наш Java-код інтеграції, патчі, скрипти складання, аудит і тести емуляторів доступні в репозиторії. Закритий SDK Spotify та інтерфейс BYD є локальними вхідними файлами складання.

[Java / build](adapter/) · [Що вже перевірено](reports/headunit-test.9.md)
