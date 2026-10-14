<div dir="rtl">

# Spotify لوحدات الوسائط العاملة بنظام Android

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

مشغّل Spotify Automotive تجريبي لوحدات الوسائط العاملة بنظام Android. يجمع ملف APK واحد بين واجهة مستندة إلى BYD وخدمة الموسيقى، مع أيقونة واحدة وتسجيل الدخول المعتاد إلى Spotify.

[تنزيل APK test.7](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.7/Spotify-5.5.0-HeadUnit-test.7.apk) · [🌐 Spotify لوحدات الوسائط العاملة بنظام Android](https://romanchukg-cloud.github.io/spotify-android-head-units/ar/)

## ما الذي تم اختباره؟

Headunit-test.7: APK audit 39 PASS / 0 FAIL. Android 9, 12 and 13 emulators each passed 94 diagnostic logic checks, 7 Support/notice UI checks, 8 process-restart persistence checks, 18 synthetic rhythm checks and 19 playback-resume checks. Diagnostics delivery was tested with a fake transport; no production Spotify report was submitted.

Experimental headunit-test.7. This version has not been installed on the car. The earlier test.6 lighting integration was verified on a BYD Tang EV with DiLink 5 / Android 12. Physical DiLink 5.1 / Android 13 lighting, real vehicle sleep/reboot, real account switching and other head units remain unverified.

Android 9+ (API 28). BYD, AAOS and GENERIC profiles are implemented; compatibility depends on vehicle firmware.

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## طريقة التثبيت

1. اضغط على زر التنزيل أعلاه للحصول على ملف APK، ثم انقله إلى نظام الوسائط في السيارة.
2. يمكن تحديث نسختنا التجريبية السابقة التي تحمل التوقيع نفسه دون إزالتها، وقد تبقى جلسة تسجيل الدخول محفوظة. لا يمكن تحديث النسخ ذات التوقيع المختلف بهذه الطريقة.
3. افتح Spotify وسجّل الدخول عبر رمز QR عند الحاجة.
4. لإضاءة BYD، فعّل «إيقاع الموسيقى» في إعدادات الإضاءة المحيطية للسيارة نفسها. يوجد هذا المفتاح في إعدادات السيارة، وليس في Spotify.
5. لإضافة سائق آخر: إعدادات الوحدة الرئيسية ← الحسابات ← إضافة حساب · QR. يوقف التبديل الموسيقى ويعيد تشغيل المشغل.
6. تحقّق من تشغيل الموسيقى وأدوات التحكم. لا تزل تطبيق واجهة BYD القديم المنفصل إلا بعد نجاح اختبار ملف APK الجديد.

Android 9+ · 55.3 MiB · تعديل غير رسمي

## التحقق من الملف

`Spotify-5.5.0-HeadUnit-test.7.apk` · 57 946 112 bytes

بصمة SHA-256 للإصدار التجريبي الحالي:

```text
9b127d9e9f9b05dfa210207370e9f21dd9ca5eb61ec6e4d6c6bb748d069f4da7
```

مشروع تجريبي غير رسمي. Spotify وBYD علامتان تجاريتان لمالكيهما المعنيين.

## الشيفرة المصدرية والتحقق

تتوفر شيفرة Java الخاصة بالتكامل وسكربتات التعديل والبناء والتدقيق واختبارات المحاكيات في المستودع. خدمة Spotify وواجهة BYD المغلقتان تُستخدمان كمدخلات بناء محلية.

[Java / build](adapter/) · [ما الذي تم اختباره؟](reports/headunit-test.7.md)

</div>
