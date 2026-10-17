<div dir="rtl">

# Spotify لوحدات الوسائط العاملة بنظام Android

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

مشغّل Spotify Automotive تجريبي لوحدات الوسائط العاملة بنظام Android. يجمع ملف APK واحد بين واجهة مستندة إلى BYD وخدمة الموسيقى، مع أيقونة واحدة وتسجيل الدخول المعتاد إلى Spotify.

[تنزيل APK test.11](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.11/Spotify-5.5.0-HeadUnit-test.11.apk) · [🌐 Spotify لوحدات الوسائط العاملة بنظام Android](https://romanchukg-cloud.github.io/spotify-android-head-units/ar/)

![الصفحة الرئيسية بالنمط الجديد في سيارة BYD، المظهر النهاري](docs/screens/car-home-day.jpg)

## ما الذي تم اختباره؟

يضيف Headunit-test.11 التنزيلات دون اتصال (ميزة تجريبية، Spotify Premium)، والبحث الصوتي إذا كانت الوحدة الرئيسية تدعم التعرف على الكلام، ومحرر قائمة الانتظار مع راديو الأغنية والفنان، وJam مع رمز QR للركاب. كما يصلح تعطل عملية الموسيقى عند إزالة مقطع من قائمة الانتظار أو إضافته إليها. ولأن الواجهة تغيّرت، بُني الإصدار بالبناء المرجعي الكامل. أظهر تدقيق الإصدار 45 PASS / 0 FAIL، ونجحت 152 فحصًا للتحديثات و39 فحصًا لتسجيل الدخول، وشهادة التوقيع مطابقة لـ test.10. على محاكيات Android 9 و13 يُثبَّت test.11 كتحديث فوق test.10 ويتحقق من التحديثات دون أعطال. على محاكي Android 12 بحساب Premium، شُغّل المحتوى المنزّل دون اتصال (حتى بعد التشغيل البارد)، وعمل البحث الصوتي مع أداة تعرّف اختبارية، وعمل تحرير قائمة الانتظار والراديو وJam.

**نسخة headunit-test.11 تجريبية.** لم يُفحص test.11 في سيارة بعد، ولا يُعرف ما إذا كانت BYD DiLink تملك أداة للتعرف على الكلام لزر الميكروفون. يُبقي Spotify التنزيلات معطلة لعميله في السيارات؛ وهذا الإصدار يفعّل الدعم الموجود في العميل، لذا قد يغيّر Spotify سلوكها. لم يُتحقق بعد من النمط الجديد بالعربية (من اليمين إلى اليسار)، وتخطيطات المقود الأيمن، وDiLink 5.1 / Android 13، ووحدات السيارات الأخرى.

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

Android 9+ · 55.6 MiB · تعديل غير رسمي

## التحقق من الملف

`Spotify-5.5.0-HeadUnit-test.11.apk` · 58 251 193 bytes

بصمة SHA-256 للإصدار التجريبي الحالي:

```text
6e1cf2566026f08859861fc63ab1a6a53eba8c84d1329c29985708e4e7030fe7
```

مشروع تجريبي غير رسمي. Spotify وBYD علامتان تجاريتان لمالكيهما المعنيين.

## الشيفرة المصدرية والتحقق

تتوفر شيفرة Java الخاصة بالتكامل وسكربتات التعديل والبناء والتدقيق واختبارات المحاكيات في المستودع. خدمة Spotify وواجهة BYD المغلقتان تُستخدمان كمدخلات بناء محلية.

[Java / build](adapter/) · [ما الذي تم اختباره؟](reports/headunit-test.11.md)

</div>
