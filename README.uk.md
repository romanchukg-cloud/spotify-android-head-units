# Spotify для Android-магнітол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Експериментальний плеєр Spotify Automotive для Android-магнітол. Інтерфейс на основі BYD та музичний сервіс об’єднані в одному APK з однією іконкою і штатним входом Spotify.

[Завантажити APK test.10](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.10/Spotify-5.5.0-HeadUnit-test.10.apk) · [🌐 Spotify для Android-магнітол](https://romanchukg-cloud.github.io/spotify-android-head-units/uk/)

![Головна в Новому стилі в авто BYD, денна тема](docs/screens/car-home-day.jpg)

## Що вже перевірено

Headunit-test.10 додає Новий стиль, увімкнений за замовчуванням: заокруглені картки, шрифт Figtree, денну й нічну теми, колір з обкладинки, перекомпонований плеєр із робочою кнопкою «подобається», обкладинку й еквалайзер у черзі, заокруглені кути застосунку та нові Налаштування магнітоли. Класичний стиль зберігає попередній інтерфейс. Закриття Spotify тепер завжди зупиняє музику, а перемикання дня й ночі з відкритою Бібліотекою більше не обвалює плеєр. Оскільки змінилися ресурси інтерфейсу, збірку зроблено повним еталонним збиранням. Release-аудит: 45 PASS / 0 FAIL, пройдено 152 перевірки оновлювача й 39 перевірок входу, сертифікат підпису збігається з test.9. В авто власника (BYD DiLink 5.0 / Android 12) test.10 встановлено оновленням поверх test.9: вхід і музика збереглися, Новий стиль перевірено в денній темі без збоїв. На емуляторі Android 12 з реальним акаунтом вхід зберігся після оновлення з test.9; перевірено день і ніч, 1024×600 і портретну орієнтацію, перемикання Класичний ↔ Новий.

**Експериментальна headunit-test.10.** Перше оновлення з test.9 до test.10 через сам застосунок на магнітолі ще не спостерігали. Новий стиль не перевіряли арабською (справа наліво) і з розкладкою для правого керма, де плеєр лишається класичним. Також не перевірені реальний сон/перезавантаження авто, акаунти A → B → A, офлайн-завантаження, кнопки на кермі, DiLink 5.1 / Android 13 та інші магнітоли.

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

Android 9+ · 55,5 MiB · неофіційна адаптація

## Перевірка файлу

`Spotify-5.5.0-HeadUnit-test.10.apk` · 58 222 521 bytes

SHA-256 поточної тестової збірки:

```text
ccf440e37acd3208f2b076d1023bfec9d5370c626230b65f12980b73f6fcf806
```

Неофіційний експериментальний проєкт. Spotify та BYD — торговельні марки відповідних власників.

## Вихідний код і перевірки

Наш Java-код інтеграції, патчі, скрипти складання, аудит і тести емуляторів доступні в репозиторії. Закритий SDK Spotify та інтерфейс BYD є локальними вхідними файлами складання.

[Java / build](adapter/) · [Що вже перевірено](reports/headunit-test.10.md)
