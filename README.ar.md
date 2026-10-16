<div dir="rtl">

# Spotify لوحدات الوسائط العاملة بنظام Android

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

مشغّل Spotify Automotive تجريبي لوحدات الوسائط العاملة بنظام Android. يجمع ملف APK واحد بين واجهة مستندة إلى BYD وخدمة الموسيقى، مع أيقونة واحدة وتسجيل الدخول المعتاد إلى Spotify.

[تنزيل APK test.10](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.10/Spotify-5.5.0-HeadUnit-test.10.apk) · [🌐 Spotify لوحدات الوسائط العاملة بنظام Android](https://romanchukg-cloud.github.io/spotify-android-head-units/ar/)

![الصفحة الرئيسية بالنمط الجديد في سيارة BYD، المظهر النهاري](docs/screens/car-home-day.jpg)

## ما الذي تم اختباره؟

يضيف Headunit-test.10 النمط الجديد مفعّلًا افتراضيًا: بطاقات بزوايا مستديرة، وخط Figtree، ومظهرين نهاري وليلي، ولونًا من صورة الغلاف، ومشغّلًا بترتيب جديد مع زر إعجاب يعمل، وصورة الغلاف ومعادِلًا متحركًا في قائمة الانتظار، وزوايا مستديرة للتطبيق، وإعدادات جديدة للوحدة الرئيسية. يحافظ النمط الكلاسيكي على الواجهة السابقة. أصبح إغلاق Spotify يوقف الموسيقى دائمًا، ولم يعد التبديل بين النهار والليل والمكتبة مفتوحة يتسبب في تعطل المشغّل. ولأن موارد الواجهة تغيّرت، بُني الإصدار بالبناء المرجعي الكامل. أظهر تدقيق الإصدار 45 PASS / 0 FAIL، ونجحت 152 فحصًا للتحديثات و39 فحصًا لتسجيل الدخول، وشهادة التوقيع مطابقة لـ test.9. في سيارة المالك (BYD DiLink 5.0 / Android 12) ثُبِّت test.10 كتحديث فوق test.9: بقي تسجيل الدخول والموسيقى، وفُحص النمط الجديد في المظهر النهاري دون أعطال. على محاكي Android 12 بحساب حقيقي بقي تسجيل الدخول بعد التحديث من test.9، وفُحص النهار والليل، و1024×600 والوضع العمودي، والتبديل بين الكلاسيكي والجديد.

**نسخة headunit-test.10 تجريبية.** لم يُشاهَد بعد أول تحديث من test.9 إلى test.10 عبر التطبيق نفسه على وحدة سيارة. لم يُفحص النمط الجديد بالعربية (من اليمين إلى اليسار) ولا مع تخطيط المقود الأيمن، حيث يبقى ترتيب المشغّل كلاسيكيًا. كما لم يُتحقق بعد من سكون السيارة وإعادة تشغيلها فعليًا، والحسابات A → B → A، والتنزيلات دون اتصال، وأزرار المقود، وDiLink 5.1 / Android 13، ووحدات السيارات الأخرى.

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

Android 9+ · 55.5 MiB · تعديل غير رسمي

## التحقق من الملف

`Spotify-5.5.0-HeadUnit-test.10.apk` · 58 222 521 bytes

بصمة SHA-256 للإصدار التجريبي الحالي:

```text
ccf440e37acd3208f2b076d1023bfec9d5370c626230b65f12980b73f6fcf806
```

مشروع تجريبي غير رسمي. Spotify وBYD علامتان تجاريتان لمالكيهما المعنيين.

## الشيفرة المصدرية والتحقق

تتوفر شيفرة Java الخاصة بالتكامل وسكربتات التعديل والبناء والتدقيق واختبارات المحاكيات في المستودع. خدمة Spotify وواجهة BYD المغلقتان تُستخدمان كمدخلات بناء محلية.

[Java / build](adapter/) · [ما الذي تم اختباره؟](reports/headunit-test.10.md)

</div>
