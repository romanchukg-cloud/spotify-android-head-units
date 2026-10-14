# Spotify для Android-магнитол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Экспериментальный плеер Spotify Automotive для Android-магнитол. Интерфейс на основе BYD и музыкальный сервис объединены в одном APK с одним значком и стандартным входом Spotify.

[Скачать APK test.9](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.9/Spotify-5.5.0-HeadUnit-test.9.apk) · [🌐 Spotify для Android-магнитол](https://romanchukg-cloud.github.io/spotify-android-head-units/ru/)

## Что уже проверено

Headunit-test.9 добавляет обновления в самом приложении: раз в день Spotify спрашивает сервер обновлений проекта о новой тестовой сборке, загружает её в фоне (по умолчанию только по Wi-Fi), проверяет размер, SHA-256, пакет, версию и сертификат подписи и устанавливает только после вашего подтверждения в окне «Обновить» Android. Собрано из опубликованного test.8: изменились только DEX адаптера и манифест (versionCode, разрешение на установку и один приватный приёмник). Release-аудит: 45 PASS / 0 FAIL, пройдены 152 проверки обновлений и 39 проверок входа, а на эмуляторах Android 9 и 13 test.9 устанавливается обновлением поверх test.8, запускается и проверяет обновления на настоящем сервере без сбоев.

**Экспериментальная headunit-test.9.** Загрузку и установку более новой сборки через приложение можно будет проверить только после выхода test.10. Test.9 ещё не устанавливали в машину, как и изменения входа из test.8. Также не проверены реальный сон/перезагрузка машины, аккаунты A → B → A, офлайн-загрузки, кнопки на руле и другие магнитолы.

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

`Spotify-5.5.0-HeadUnit-test.9.apk` · 58 011 648 bytes

SHA-256 текущей тестовой сборки:

```text
f78026ba05fa52c67082851e710379358a05027dfcc47c404b8146147c880cd3
```

Неофициальный экспериментальный проект. Spotify и BYD — товарные знаки соответствующих владельцев.

## Исходный код и проверки

Наш Java-код интеграции, патчи, скрипты сборки, аудит и тесты эмуляторов доступны в репозитории. Закрытый SDK Spotify и интерфейс BYD — локальные входные файлы сборки.

[Java / build](adapter/) · [Что уже проверено](reports/headunit-test.9.md)
