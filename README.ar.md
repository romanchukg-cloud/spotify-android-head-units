<div dir="rtl">

# Spotify لوحدات الوسائط العاملة بنظام Android

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

مشغّل Spotify Automotive تجريبي لوحدات السيارات بنظام Android، بواجهة خاصة به مصممة للسيارة: ملف APK واحد وأيقونة واحدة وتسجيل الدخول المعتاد إلى Spotify. مجاني — لا تدفع مقابله أبدًا.

[تنزيل APK test.12](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.12/Spotify-5.5.0-HeadUnit-test.12.apk) · [🌐 Spotify لوحدات الوسائط العاملة بنظام Android](https://romanchukg-cloud.github.io/spotify-android-head-units/ar/)

![الرئيسية ليلًا في سيارة BYD](docs/screens/car-home-night.jpg)

## ما الذي تم اختباره؟

يستبدل Headunit-test.12 الواجهة المستندة إلى BYD بواجهة خاصة به: الرئيسية والمكتبة والبحث، وصفحات لقوائم التشغيل والألبومات والفنانين والبودكاست، ومشغّل بملء الشاشة وقائمة انتظار على الغلاف الضبابي، وتقدّم التنزيلات، ومؤقت النوم، ورسالة بأن التطبيق مجاني. أُنشئ بالبناء المرجعي الكامل. تدقيق الإصدار: 46 PASS / 0 FAIL، ونجحت 152 فحصًا للمحدّث و39 فحصًا لتسجيل الدخول، وشهادة التوقيع مطابقة لـ test.11. على محاكيات Android 9 و13 يُثبَّت test.12 كتحديث فوق test.11 ويتحقق من التحديثات دون تعطل. عملت إصدارات تمهيدية في سيارة BYD (DiLink 5، Android 12): أفقيًا وعموديًا وفي تقسيم الشاشة مع الملاحة، وتسجيل الدخول عبر QR، وأزرار المقود، وSpotify Connect.

**إصدار تجريبي headunit-test.12.** لم يُثبَّت ملف الإصدار نفسه في سيارة بعد. لم يُتحقق من تصميمات المقود الأيمن وDiLink 5.1 / Android 13 ووحدات السيارات الأخرى؛ وتم فحص العربية على محاكٍ فقط. يُبقي Spotify التنزيلات معطلة لعميل Automotive؛ هذا الإصدار يفعّل الدعم الموجود، لذا قد يغيّر Spotify طريقة عملها.

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

Android 9+ · 55.7 MiB · تعديل غير رسمي

## التحقق من الملف

`Spotify-5.5.0-HeadUnit-test.12.apk` · 58 378 169 bytes

بصمة SHA-256 للإصدار التجريبي الحالي:

```text
00f82a80d9a40d9d3138c3fde1d4c3c88edf270c380c54a70fa01032ab6982a1
```

مشروع تجريبي غير رسمي. هذا التطبيق مجاني — إذا دفعت مقابله فاطلب استرداد أموالك. Spotify وBYD علامتان تجاريتان لمالكيهما المعنيين.

## الشيفرة المصدرية والتحقق

شيفرة Java الخاصة بنا — الواجهة والتكامل وسكربتات التعديل والبناء والتدقيق واختبارات المحاكي — متاحة في المستودع. خادم Spotify الخلفي المملوك مُدخل بناء محلي. لا يزال ملف APK يحتوي على أجزاء من مشغّل BYD (Vivid): خدمة الوسائط للمساعد الصوتي في السيارة والموارد وخط؛ وستُزال لاحقًا.

[Java / build](adapter/) · [ما الذي تم اختباره؟](reports/headunit-test.12.md)

</div>
