# Spotify для Android-магнитол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Экспериментальный плеер Spotify Automotive для Android-магнитол с собственным интерфейсом, сделанным для авто: один APK, одна иконка и стандартный вход в Spotify. Бесплатно — никогда не платите за него.

[Скачать APK test.12](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.12/Spotify-5.5.0-HeadUnit-test.12.apk) · [🌐 Spotify для Android-магнитол](https://romanchukg-cloud.github.io/spotify-android-head-units/ru/)

![Главная ночью в автомобиле BYD](docs/screens/car-home-night.jpg)

## Что уже проверено

Headunit-test.12 заменяет интерфейс на основе BYD собственным: Главная, Библиотека, Поиск, страницы плейлистов, альбомов, исполнителей и подкастов, полноэкранный плеер и очередь на размытой обложке, прогресс загрузок, таймер сна и сообщение, что приложение бесплатное. Собрано полной эталонной сборкой. Аудит релиза: 46 PASS / 0 FAIL, 152 проверки обновлятора и 39 проверок входа пройдены, сертификат подписи совпадает с test.11. На эмуляторах Android 9 и 13 test.12 устанавливается как обновление поверх test.11 и проверяет обновления без падений. Промежуточные сборки работали в автомобиле BYD (DiLink 5, Android 12): горизонтально, вертикально и в разделённом экране с навигацией, вход по QR, кнопки руля и Spotify Connect.

**Экспериментальная headunit-test.12.** Сам файл релиза в автомобиль ещё не устанавливали. Правый руль, DiLink 5.1 / Android 13 и другие магнитолы не проверены; арабский проверен только на эмуляторе. Spotify держит загрузки выключенными для своего клиента Automotive; эта сборка включает имеющуюся поддержку, поэтому Spotify может изменить их работу.

Android 9+ (API 28). Ритм BYD проверен на DiLink 5 / Android 12; DiLink 5.1 / Android 13 и другие прошивки ещё не проверены. Профили AAOS и GENERIC реализованы; реальные магнитолы других производителей и AAOS ещё не проверены.

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## Как установить

1. Нажмите кнопку скачивания выше, чтобы получить APK, и перенесите его в мультимедийную систему.
2. Нашу предыдущую тестовую сборку с той же подписью обновляйте без удаления: авторизация может сохраниться. Сборки с другой подписью так не обновятся.
3. Откройте Spotify и при необходимости войдите по QR-коду.
4. Для подсветки BYD включите «Музыкальный ритм» в настройках салонной подсветки самого автомобиля. Этот переключатель в настройках машины, а не Spotify.
5. Чтобы добавить другого водителя: Настройки магнитолы → Аккаунты → Добавить аккаунт · QR. Переключение останавливает музыку и перезапускает плеер.
6. Проверьте музыку и управление. Удаляйте старый отдельный интерфейс BYD только после успешной проверки нового APK.

Android 9+ · 55,7 MiB · неофициальная адаптация

## Проверка файла

`Spotify-5.5.0-HeadUnit-test.12.apk` · 58 378 169 bytes

SHA-256 текущей тестовой сборки:

```text
00f82a80d9a40d9d3138c3fde1d4c3c88edf270c380c54a70fa01032ab6982a1
```

Неофициальный экспериментальный проект. Это приложение бесплатное — если вы за него заплатили, требуйте деньги назад. Spotify и BYD — товарные знаки соответствующих владельцев.

## Исходный код и проверки

Наш код Java — интерфейс, интеграция, скрипты патчей и сборки, аудиты и эмуляторные тесты — есть в репозитории. Проприетарный бэкенд Spotify — локальный вход сборки. APK ещё содержит части плеера BYD (Vivid): медиасервис для голосового помощника авто, ресурсы и шрифт; их уберут.

[Java / build](adapter/) · [Что уже проверено](reports/headunit-test.12.md)
