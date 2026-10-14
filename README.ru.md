# Spotify для Android-магнитол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Экспериментальный плеер Spotify Automotive для Android-магнитол. Интерфейс на основе BYD и музыкальный сервис объединены в одном APK с одним значком и стандартным входом Spotify.

[Скачать APK test.8](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.8/Spotify-5.5.0-HeadUnit-test.8.apk) · [🌐 Spotify для Android-магнитол](https://romanchukg-cloud.github.io/spotify-android-head-units/ru/)

## Что уже проверено

Headunit-test.8 сохраняет вход в Spotify: стабильный ID устройства, без выхода после удаления старого неактивного аккаунта в настройках Android и однократное автоматическое восстановление входа, потерянного на магнитоле. Собрано из опубликованного test.7: изменились только DEX Spotify, DEX адаптера и versionCode; ресурсы, интерфейс BYD и нативные библиотеки побайтно те же. Все 8 новых патчей совпали по одному разу, 39 проверок логики входа и аудит совместимости пройдены, release-аудит: 42 PASS / 0 FAIL, сертификат подписи совпадает с test.7.

**Экспериментальная headunit-test.8.** Изменения входа ещё не проверены с реальным аккаунтом на эмуляторе или в машине; настоящая смена ANDROID_ID и восстановление после отключения питания не проверялись. Также не проверены реальный сон/перезагрузка машины, аккаунты A → B → A, офлайн-загрузки, кнопки на руле и другие магнитолы. Эмуляторные тесты test.7 для test.8 повторно не запускались.

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

Android 9+ · 55,3 MiB · неофициальная адаптация

## Проверка файла

`Spotify-5.5.0-HeadUnit-test.8.apk` · 57 962 496 bytes

SHA-256 текущей тестовой сборки:

```text
caa026818b27aa08de38c4c76dab35a02a6ec975095ed5cba9ed0d57c7dc8a93
```

Неофициальный экспериментальный проект. Spotify и BYD — товарные знаки соответствующих владельцев.

## Исходный код и проверки

Наш Java-код интеграции, патчи, скрипты сборки, аудит и тесты эмуляторов доступны в репозитории. Закрытый SDK Spotify и интерфейс BYD — локальные входные файлы сборки.

[Java / build](adapter/) · [Что уже проверено](reports/headunit-test.8.md)
