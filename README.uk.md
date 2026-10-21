# Spotify для Android-магнітол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Експериментальний плеєр Spotify Automotive для Android-магнітол із власним інтерфейсом, зробленим для авто: один APK, одна іконка й стандартний вхід у Spotify. Безкоштовно — ніколи не платіть за нього.

[Завантажити APK test.15](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.15/Spotify-5.5.0-HeadUnit-test.15.apk) · [🌐 Spotify для Android-магнітол](https://romanchukg-cloud.github.io/spotify-android-head-units/uk/)

![Головна вночі в авто BYD](docs/screens/car-home-night.jpg)

## Що вже перевірено

Headunit-test.15 додає час у міні-плеєр і тонку рамку навколо екрана, а навігація тепер згортає плеєр на весь екран. Із test.14 він також має віджети робочого столу для лаунчерів, у яких можна додавати віджети (п'ять варіантів за розміром, день і ніч), відкриває посилання Spotify з інших застосунків, повідомляє іншим застосункам, що грає, і може запускати Spotify у фоні під час старту магнітоли (не на BYD). Зроблено повною еталонною збіркою. Аудит релізу: 51 PASS / 0 FAIL; проходять 152 перевірки оновлень, 39 входу, 14 доступу до блоку й 19 запуску відтворення; сертифікат підпису той самий, що в test.14. На емуляторах Android 9 і 13 test.15 встановлюється оновленням поверх test.14 і без падінь перевіряє оновлення; вхід і «Акаунти» проходять на Android 13. Віджети перевірено у стандартних лаунчерах емуляторів Android 11 і 12; посилання відкривали там плейлисти, альбоми й виконавців. Збірка з тим самим кодом працює в BYD Tang (DiLink 5, Android 12).

**Експериментальна headunit-test.15.** Віджети поки показували лише вигадані треки, а штатний лаунчер BYD їх не показує. Відтворення пісні за посиланням і справжній застосунок, що читає «що грає», не перевірено. Праве кермо не бачили на машині з правим кермом; DiLink 5.1 / Android 13 та інші магнітоли не перевірено; арабську перевірено лише на емуляторі. Блок для панелей з'являється лише в застосунках із його підтримкою. Spotify тримає завантаження вимкненими для свого Automotive-клієнта; ця збірка вмикає їх підтримку, тож Spotify може змінити їхню роботу.

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

Android 9+ · 46,1 MiB · неофіційна адаптація

## Перевірка файлу

`Spotify-5.5.0-HeadUnit-test.15.apk` · 48 350 322 bytes

SHA-256 поточної тестової збірки:

```text
8582e41a8b137a78b8842e464152f6539c80f87dad06904069392763e1c6983b
```

Неофіційний експериментальний проєкт. Цей застосунок безкоштовний — якщо ви за нього заплатили, вимагайте гроші назад. Spotify та BYD — торговельні марки відповідних власників.

## Вихідний код і перевірки

Наш Java-код — інтерфейс, інтеграція, скрипти патчів і збирання, аудити й емуляторні тести — у репозиторії, на умовах NOTICE.md (безкоштовно, без продажу, позначку «безкоштовно» не прибирати). Пропрієтарний бекенд Spotify — локальний вхід збирання. З test.13 APK не містить жодної частини плеєра BYD (Vivid).

[Java / build](adapter/) · [Що вже перевірено](reports/headunit-test.15.md) · [NOTICE](NOTICE.md)
