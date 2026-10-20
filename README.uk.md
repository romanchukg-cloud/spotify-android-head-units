# Spotify для Android-магнітол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Експериментальний плеєр Spotify Automotive для Android-магнітол із власним інтерфейсом, зробленим для авто: один APK, одна іконка й стандартний вхід у Spotify. Безкоштовно — ніколи не платіть за нього.

[Завантажити APK test.13](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.13/Spotify-5.5.0-HeadUnit-test.13.apk) · [🌐 Spotify для Android-магнітол](https://romanchukg-cloud.github.io/spotify-android-head-units/uk/)

![Головна вночі в авто BYD](docs/screens/car-home-night.jpg)

## Що вже перевірено

Headunit-test.13 прибирає з APK останні частини плеєра BYD (Vivid), додає перемикання треків свайпом, праве кермо (на BYD визначається автоматично) й інтерфейс до країв екрана, зберігає велику обкладинку у вертикальному розділеному екрані і додає віджет Spotify, який інші застосунки можуть показувати у своїх панелях (першим буде Google Maps for BYD). Копія, перепідписана іншим ключем, тепер повідомляє, що її змінено. Зібрано повною еталонною збіркою. Release-аудит: 48 PASS / 0 FAIL; 152 перевірки оновлювача, 39 входу й 14 доступу до панелі проходять; сертифікат підпису збігається з test.12. На емуляторах Android 9 і 13 test.13 встановлюється оновленням поверх test.12 і перевіряє оновлення без падінь. Збірки test.13 працювали в BYD Tang (DiLink 5, Android 12): свайпи, кнопки керма, голосова кнопка BYD, підсвітка в ритм музики, машина повідомляє ліве кермо, розділений екран із Google Maps; у машині стоїть саме цей файл.

**Експериментальна headunit-test.13.** Праве кермо ще не бачили на машині з правим кермом (значення взято з коду BYD); DiLink 5.1 / Android 13 та інші магнітоли не перевірено; арабську — лише на емуляторі. Віджет з'являється лише в застосунках, які його підтримують; підтримку в Google Maps for BYD готують. Spotify вимикає завантаження для свого клієнта Automotive; ця збірка вмикає їхню підтримку, тож Spotify може змінити поведінку завантажень.

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

Android 9+ · 46,0 MiB · неофіційна адаптація

## Перевірка файлу

`Spotify-5.5.0-HeadUnit-test.13.apk` · 48 279 511 bytes

SHA-256 поточної тестової збірки:

```text
d7469ce9383defdc9455e40b7fcbabb101ca7f1d55cfd37ddb8b4d684de3433c
```

Неофіційний експериментальний проєкт. Цей застосунок безкоштовний — якщо ви за нього заплатили, вимагайте гроші назад. Spotify та BYD — торговельні марки відповідних власників.

## Вихідний код і перевірки

Наш Java-код — інтерфейс, інтеграція, скрипти патчів і збирання, аудити й емуляторні тести — у репозиторії, на умовах NOTICE.md (безкоштовно, без продажу, позначку «безкоштовно» не прибирати). Пропрієтарний бекенд Spotify — локальний вхід збирання. З test.13 APK не містить жодної частини плеєра BYD (Vivid).

[Java / build](adapter/) · [Що вже перевірено](reports/headunit-test.13.md) · [NOTICE](NOTICE.md)
