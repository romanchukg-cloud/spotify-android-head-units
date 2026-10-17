# Spotify для Android-магнітол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Експериментальний плеєр Spotify Automotive для Android-магнітол. Інтерфейс на основі BYD та музичний сервіс об’єднані в одному APK з однією іконкою і штатним входом Spotify.

[Завантажити APK test.11](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.11/Spotify-5.5.0-HeadUnit-test.11.apk) · [🌐 Spotify для Android-магнітол](https://romanchukg-cloud.github.io/spotify-android-head-units/uk/)

![Головна в Новому стилі в авто BYD, денна тема](docs/screens/car-home-day.jpg)

## Що вже перевірено

Headunit-test.11 додає офлайн-завантаження (експериментально, Spotify Premium), голосовий пошук, якщо в магнітолі є розпізнавання мовлення, редактор черги з радіо пісні й виконавця та Jam із QR-кодом для пасажирів. Також виправлено падіння процесу музики під час видалення треку з черги чи додавання в неї. Оскільки змінився інтерфейс, збірку зроблено повним еталонним збиранням. Release-аудит: 45 PASS / 0 FAIL, пройдено 152 перевірки оновлювача й 39 перевірок входу, сертифікат підпису збігається з test.10. На емуляторах Android 9 і 13 test.11 встановлюється оновленням поверх test.10 і перевіряє оновлення без збоїв. На емуляторі Android 12 з акаунтом Premium завантажене грало без мережі (зокрема після холодного старту), голосовий пошук працював з тестовим розпізнавачем, редагування черги, радіо й Jam працювали.

**Експериментальна headunit-test.11.** Test.11 ще не перевіряли в авто, і невідомо, чи є в BYD DiLink розпізнавання мовлення для кнопки мікрофона. Spotify тримає завантаження вимкненими для свого автомобільного клієнта; ця збірка вмикає наявну в клієнті підтримку, тож Spotify може змінити їхню поведінку. Новий стиль арабською (справа наліво), розкладки для правого керма, DiLink 5.1 / Android 13 та інші магнітоли ще не перевірені.

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

Android 9+ · 55,6 MiB · неофіційна адаптація

## Перевірка файлу

`Spotify-5.5.0-HeadUnit-test.11.apk` · 58 251 193 bytes

SHA-256 поточної тестової збірки:

```text
6e1cf2566026f08859861fc63ab1a6a53eba8c84d1329c29985708e4e7030fe7
```

Неофіційний експериментальний проєкт. Spotify та BYD — торговельні марки відповідних власників.

## Вихідний код і перевірки

Наш Java-код інтеграції, патчі, скрипти складання, аудит і тести емуляторів доступні в репозиторії. Закритий SDK Spotify та інтерфейс BYD є локальними вхідними файлами складання.

[Java / build](adapter/) · [Що вже перевірено](reports/headunit-test.11.md)
