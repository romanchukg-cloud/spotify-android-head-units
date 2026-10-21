# Spotify для Android-магнитол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Экспериментальный плеер Spotify Automotive для Android-магнитол с собственным интерфейсом, сделанным для авто: один APK, одна иконка и стандартный вход в Spotify. Бесплатно — никогда не платите за него.

[Скачать APK test.14](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.14/Spotify-5.5.0-HeadUnit-test.14.apk) · [🌐 Spotify для Android-магнитол](https://romanchukg-cloud.github.io/spotify-android-head-units/ru/)

![Главная ночью в автомобиле BYD](docs/screens/car-home-night.jpg)

## Что уже проверено

Headunit-test.14 добавляет виджеты рабочего стола для лаунчеров, в которых можно добавлять виджеты (пять вариантов по размеру, день и ночь), открывает ссылки Spotify из других приложений, сообщает другим приложениям, что играет, и может запускать Spotify в фоне при запуске магнитолы (не на BYD). Сделано полной эталонной сборкой. Аудит релиза: 51 PASS / 0 FAIL; проходят 152 проверки обновлений, 39 входа, 14 доступа к блоку и 19 запуска воспроизведения; сертификат подписи тот же, что в test.13. На эмуляторах Android 9 и 13 test.14 устанавливается обновлением поверх test.13 и без сбоев проверяет обновления; вход и «Аккаунты» проходят на Android 13. Виджеты проверены в стандартных лаунчерах эмуляторов Android 11 и 12; ссылки открывали там плейлисты, альбомы и исполнителей. Именно этот файл установлен в BYD Tang (DiLink 5, Android 12).

**Экспериментальная headunit-test.14.** Виджеты пока показывали только выдуманные треки, а штатный лаунчер BYD их не показывает. Воспроизведение песни по ссылке и настоящее приложение, читающее «что играет», не проверены. Правый руль не видели на машине с правым рулём; DiLink 5.1 / Android 13 и другие магнитолы не проверены; арабский проверен только на эмуляторе. Блок для панелей появляется только в приложениях с его поддержкой. Spotify держит загрузки выключенными для своего Automotive-клиента; эта сборка включает их поддержку, поэтому Spotify может изменить их работу.

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

Android 9+ · 46,1 MiB · неофициальная адаптация

## Проверка файла

`Spotify-5.5.0-HeadUnit-test.14.apk` · 48 350 322 bytes

SHA-256 текущей тестовой сборки:

```text
9a6408d84b2d193762c72efe5a082627e771c696ac74748251456294109cf905
```

Неофициальный экспериментальный проект. Это приложение бесплатное — если вы за него заплатили, требуйте деньги назад. Spotify и BYD — товарные знаки соответствующих владельцев.

## Исходный код и проверки

Наш Java-код — интерфейс, интеграция, скрипты патчей и сборки, аудиты и эмуляторные тесты — в репозитории, на условиях NOTICE.md (бесплатно, без продажи, отметку «бесплатно» не убирать). Проприетарный бэкенд Spotify — локальный вход сборки. С test.13 APK не содержит ни одной части плеера BYD (Vivid).

[Java / build](adapter/) · [Что уже проверено](reports/headunit-test.14.md) · [NOTICE](NOTICE.md)
