<div dir="rtl">

# Spotify لوحدات الوسائط العاملة بنظام Android

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

مشغّل Spotify Automotive تجريبي لوحدات الوسائط العاملة بنظام Android. يجمع ملف APK واحد بين واجهة مستندة إلى BYD وخدمة الموسيقى، مع أيقونة واحدة وتسجيل الدخول المعتاد إلى Spotify.

[تنزيل APK test.8](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.8/Spotify-5.5.0-HeadUnit-test.8.apk) · [🌐 Spotify لوحدات الوسائط العاملة بنظام Android](https://romanchukg-cloud.github.io/spotify-android-head-units/ar/)

## ما الذي تم اختباره؟

يحافظ Headunit-test.8 على تسجيل الدخول إلى Spotify: معرّف جهاز ثابت، وعدم تسجيل الخروج عند إزالة حساب قديم غير نشط من إعدادات Android، واستعادة تلقائية لمرة واحدة لتسجيل دخول فُقد على وحدة السيارة. بُني من test.7 المنشور: لم يتغير سوى DEX الخاص بـ Spotify وDEX المحوِّل وversionCode؛ أما الموارد وواجهة BYD والمكتبات الأصلية فمطابقة بايتًا ببايت. تطابقت التعديلات الثمانية الجديدة مرة واحدة لكل منها، ونجحت 39 فحصًا لمنطق تسجيل الدخول وتدقيق التوافق، وأظهر تدقيق الإصدار 42 PASS / 0 FAIL، وشهادة التوقيع مطابقة لـ test.7.

**نسخة headunit-test.8 تجريبية.** لم تُختبر تغييرات تسجيل الدخول بحساب حقيقي على محاكي أو في سيارة؛ ولم يُختبر تغيير ANDROID_ID فعليًا ولا الاستعادة بعد انقطاع الكهرباء. كما لم يُتحقق بعد من سكون السيارة وإعادة تشغيلها فعليًا، والحسابات A → B → A، والتنزيلات دون اتصال، وأزرار المقود، ووحدات السيارات الأخرى. لم تُعد اختبارات المحاكي الخاصة بـ test.7 للإصدار test.8.

Android 9 أو أحدث (API 28). تم التحقق من إيقاع BYD على DiLink 5 وAndroid 12؛ لم يتم التحقق بعد من DiLink 5.1 وAndroid 13 والبرامج الثابتة الأخرى. تم تنفيذ ملفات AAOS وGENERIC؛ لم يتم التحقق من وحدات الشركات الأخرى أو AAOS الفعلية.

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

`Spotify-5.5.0-HeadUnit-test.8.apk` · 57 962 496 bytes

بصمة SHA-256 للإصدار التجريبي الحالي:

```text
caa026818b27aa08de38c4c76dab35a02a6ec975095ed5cba9ed0d57c7dc8a93
```

مشروع تجريبي غير رسمي. Spotify وBYD علامتان تجاريتان لمالكيهما المعنيين.

## الشيفرة المصدرية والتحقق

تتوفر شيفرة Java الخاصة بالتكامل وسكربتات التعديل والبناء والتدقيق واختبارات المحاكيات في المستودع. خدمة Spotify وواجهة BYD المغلقتان تُستخدمان كمدخلات بناء محلية.

[Java / build](adapter/) · [ما الذي تم اختباره؟](reports/headunit-test.8.md)

</div>
