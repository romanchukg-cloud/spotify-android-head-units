# Spotify для Android-магнитол

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

Экспериментальный плеер Spotify Automotive для Android-магнитол. Интерфейс на основе BYD и музыкальный сервис объединены в одном APK с одним значком и стандартным входом Spotify.

[Скачать APK test.11](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.11/Spotify-5.5.0-HeadUnit-test.11.apk) · [🌐 Spotify для Android-магнитол](https://romanchukg-cloud.github.io/spotify-android-head-units/ru/)

![Главная в Новом стиле в машине BYD, дневная тема](docs/screens/car-home-day.jpg)

## Что уже проверено

Headunit-test.11 добавляет офлайн-загрузки (экспериментально, Spotify Premium), голосовой поиск, если в магнитоле есть распознавание речи, редактор очереди с радио песни и исполнителя и Jam с QR-кодом для пассажиров. Также исправлено падение процесса музыки при удалении трека из очереди или добавлении в неё. Поскольку изменился интерфейс, сборка сделана полной эталонной сборкой. Release-аудит: 45 PASS / 0 FAIL, пройдены 152 проверки обновлений и 39 проверок входа, сертификат подписи совпадает с test.10. На эмуляторах Android 9 и 13 test.11 устанавливается обновлением поверх test.10 и проверяет обновления без сбоев. На эмуляторе Android 12 с аккаунтом Premium скачанное играло без сети (в том числе после холодного старта), голосовой поиск работал с тестовым распознавателем, редактирование очереди, радио и Jam работали.

**Экспериментальная headunit-test.11.** Test.11 ещё не проверяли в машине, и неизвестно, есть ли в BYD DiLink распознавание речи для кнопки микрофона. Spotify держит загрузки выключенными для своего автомобильного клиента; эта сборка включает имеющуюся в клиенте поддержку, поэтому Spotify может изменить их поведение. Новый стиль на арабском (справа налево), раскладки для правого руля, DiLink 5.1 / Android 13 и другие магнитолы ещё не проверены.

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

Android 9+ · 55,6 MiB · неофициальная адаптация

## Проверка файла

`Spotify-5.5.0-HeadUnit-test.11.apk` · 58 251 193 bytes

SHA-256 текущей тестовой сборки:

```text
6e1cf2566026f08859861fc63ab1a6a53eba8c84d1329c29985708e4e7030fe7
```

Неофициальный экспериментальный проект. Spotify и BYD — товарные знаки соответствующих владельцев.

## Исходный код и проверки

Наш Java-код интеграции, патчи, скрипты сборки, аудит и тесты эмуляторов доступны в репозитории. Закрытый SDK Spotify и интерфейс BYD — локальные входные файлы сборки.

[Java / build](adapter/) · [Что уже проверено](reports/headunit-test.11.md)
