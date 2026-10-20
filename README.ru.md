# Spotify для Android-магнитол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Экспериментальный плеер Spotify Automotive для Android-магнитол с собственным интерфейсом, сделанным для авто: один APK, одна иконка и стандартный вход в Spotify. Бесплатно — никогда не платите за него.

[Скачать APK test.13](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.13/Spotify-5.5.0-HeadUnit-test.13.apk) · [🌐 Spotify для Android-магнитол](https://romanchukg-cloud.github.io/spotify-android-head-units/ru/)

![Главная ночью в автомобиле BYD](docs/screens/car-home-night.jpg)

## Что уже проверено

Headunit-test.13 убирает из APK последние части плеера BYD (Vivid), добавляет переключение треков свайпом, правый руль (на BYD определяется автоматически) и интерфейс до краёв экрана, сохраняет большую обложку в вертикальном разделённом экране и добавляет виджет Spotify, который другие приложения могут показывать в своих панелях (первым будет Google Maps for BYD). Копия, переподписанная другим ключом, теперь сообщает, что её изменили. Собрано полной эталонной сборкой. Release-аудит: 48 PASS / 0 FAIL; 152 проверки обновления, 39 входа и 14 доступа к панели проходят; сертификат подписи совпадает с test.12. На эмуляторах Android 9 и 13 test.13 устанавливается обновлением поверх test.12 и проверяет обновления без падений. Сборки test.13 работали в BYD Tang (DiLink 5, Android 12): свайпы, кнопки руля, голосовая кнопка BYD, подсветка в ритм музыки, машина сообщает левый руль, разделённый экран с Google Maps; в машине стоит именно этот файл.

**Экспериментальная headunit-test.13.** Правый руль ещё не проверен на машине с правым рулём (значение взято из кода BYD); DiLink 5.1 / Android 13 и другие магнитолы не проверены; арабский — только на эмуляторе. Виджет появляется только в приложениях, которые его поддерживают; поддержку в Google Maps for BYD готовят. Spotify отключает загрузки для своего клиента Automotive; эта сборка включает их поддержку, поэтому Spotify может изменить поведение загрузок.

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

Android 9+ · 46,0 MiB · неофициальная адаптация

## Проверка файла

`Spotify-5.5.0-HeadUnit-test.13.apk` · 48 279 511 bytes

SHA-256 текущей тестовой сборки:

```text
d7469ce9383defdc9455e40b7fcbabb101ca7f1d55cfd37ddb8b4d684de3433c
```

Неофициальный экспериментальный проект. Это приложение бесплатное — если вы за него заплатили, требуйте деньги назад. Spotify и BYD — товарные знаки соответствующих владельцев.

## Исходный код и проверки

Наш Java-код — интерфейс, интеграция, скрипты патчей и сборки, аудиты и эмуляторные тесты — в репозитории, на условиях NOTICE.md (бесплатно, без продажи, отметку «бесплатно» не убирать). Проприетарный бэкенд Spotify — локальный вход сборки. С test.13 APK не содержит ни одной части плеера BYD (Vivid).

[Java / build](adapter/) · [Что уже проверено](reports/headunit-test.13.md) · [NOTICE](NOTICE.md)
