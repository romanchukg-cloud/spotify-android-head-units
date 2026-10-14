# Spotify для Android-магнитол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Экспериментальный плеер Spotify Automotive для Android-магнитол. Интерфейс на основе BYD и музыкальный сервис объединены в одном APK с одним значком и стандартным входом Spotify.

[Скачать APK test.7](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.7/Spotify-5.5.0-HeadUnit-test.7.apk) · [🌐 Spotify для Android-магнитол](https://romanchukg-cloud.github.io/spotify-android-head-units/ru/)

## Что уже проверено

Headunit-test.7: APK audit 39 PASS / 0 FAIL. Android 9, 12 and 13 emulators each passed 94 diagnostic logic checks, 7 Support/notice UI checks, 8 process-restart persistence checks, 18 synthetic rhythm checks and 19 playback-resume checks. Diagnostics delivery was tested with a fake transport; no production Spotify report was submitted.

Experimental headunit-test.7. This version has not been installed on the car. The earlier test.6 lighting integration was verified on a BYD Tang EV with DiLink 5 / Android 12. Physical DiLink 5.1 / Android 13 lighting, real vehicle sleep/reboot, real account switching and other head units remain unverified.

Android 9+ (API 28). BYD, AAOS and GENERIC profiles are implemented; compatibility depends on vehicle firmware.

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## Как установить

1. Нажмите кнопку скачивания выше, чтобы получить APK, и перенесите его в мультимедийную систему.
2. Нашу предыдущую тестовую сборку с той же подписью обновляйте без удаления: авторизация может сохраниться. Сборки с другой подписью так не обновятся.
3. Откройте Spotify и при необходимости войдите по QR-коду.
4. Для подсветки BYD включите «Музыкальный ритм» в настройках салонной подсветки самого автомобиля. Этот переключатель в настройках машины, а не Spotify.
5. Чтобы добавить другого водителя: Настройки магнитолы → Аккаунты → Добавить аккаунт · QR. Переключение останавливает музыку и перезапускает плеер.
6. Проверьте музыку и управление. Удаляйте старый отдельный интерфейс BYD только после успешной проверки нового APK.

Android 9+ · 55,2 MiB · неофициальная адаптация

## Проверка файла

`Spotify-5.5.0-HeadUnit-test.7.apk` · 57 946 112 bytes

SHA-256 текущей тестовой сборки:

```text
9b127d9e9f9b05dfa210207370e9f21dd9ca5eb61ec6e4d6c6bb748d069f4da7
```

Неофициальный экспериментальный проект. Spotify и BYD — товарные знаки соответствующих владельцев.

## Исходный код и проверки

Наш Java-код интеграции, патчи, скрипты сборки, аудит и тесты эмуляторов доступны в репозитории. Закрытый SDK Spotify и интерфейс BYD — локальные входные файлы сборки.

[Java / build](adapter/) · [Что уже проверено](reports/headunit-test.7.md)
