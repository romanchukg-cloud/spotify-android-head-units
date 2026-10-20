<div dir="rtl">

# Spotify لوحدات الوسائط العاملة بنظام Android

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

مشغّل Spotify Automotive تجريبي لوحدات السيارات بنظام Android، بواجهة خاصة به مصممة للسيارة: ملف APK واحد وأيقونة واحدة وتسجيل الدخول المعتاد إلى Spotify. مجاني — لا تدفع مقابله أبدًا.

[تنزيل APK test.13](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.13/Spotify-5.5.0-HeadUnit-test.13.apk) · [🌐 Spotify لوحدات الوسائط العاملة بنظام Android](https://romanchukg-cloud.github.io/spotify-android-head-units/ar/)

![الرئيسية ليلًا في سيارة BYD](docs/screens/car-home-night.jpg)

## ما الذي تم اختباره؟

يزيل headunit-test.13 آخر أجزاء مشغّل BYD (Vivid) من ملف APK، ويضيف التنقل بين المقاطع بالسحب، ودعم المقود على اليمين (يُكتشف تلقائيًا في BYD)، وواجهة تمتد حتى حواف الشاشة، ويحتفظ بغلاف كبير في تقسيم الشاشة العمودي، ويضيف أداة Spotify يمكن للتطبيقات الأخرى عرضها في لوحاتها (وسيكون Google Maps for BYD أولها). النسخة المعاد توقيعها بمفتاح آخر تُعلن الآن أنها معدّلة. بُني بالبناء المرجعي الكامل. تدقيق الإصدار: 48 PASS / 0 FAIL، وتنجح 152 فحصًا للتحديث و39 لتسجيل الدخول و14 للوصول إلى اللوحة، وشهادة التوقيع مطابقة لـ test.12. على محاكيَي Android 9 و13 يُثبَّت test.13 كتحديث فوق test.12 ويفحص التحديثات دون أعطال. عملت إصدارات test.13 في BYD Tang (DiLink 5، Android 12): السحب، وأزرار المقود، وزر الصوت في BYD، وإيقاع الإضاءة، وإبلاغ السيارة بأن المقود على اليسار، وتقسيم الشاشة مع Google Maps؛ وهذا الملف نفسه مثبّت في السيارة.

**إصدار تجريبي headunit-test.13.** لم يُجرَّب المقود على اليمين في سيارة بمقود يميني (القيمة مأخوذة من شيفرة BYD)؛ ولم يُتحقق بعد من DiLink 5.1 / Android 13 ووحدات السيارات الأخرى؛ واللغة العربية جُرّبت على المحاكي فقط. لا تظهر الأداة إلا في التطبيقات التي تدعمها، ودعمها في Google Maps for BYD قيد الإعداد. يوقف Spotify التنزيلات لعميل Automotive؛ يفعّل هذا الإصدار دعمها، لذا قد يغيّر Spotify سلوك التنزيلات.

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

Android 9+ · 46.0 MiB · تعديل غير رسمي

## التحقق من الملف

`Spotify-5.5.0-HeadUnit-test.13.apk` · 48 279 511 bytes

بصمة SHA-256 للإصدار التجريبي الحالي:

```text
d7469ce9383defdc9455e40b7fcbabb101ca7f1d55cfd37ddb8b4d684de3433c
```

مشروع تجريبي غير رسمي. هذا التطبيق مجاني — إذا دفعت مقابله فاطلب استرداد أموالك. Spotify وBYD علامتان تجاريتان لمالكيهما المعنيين.

## الشيفرة المصدرية والتحقق

شيفرة Java الخاصة بنا — الواجهة والتكامل وسكربتات الترقيع والبناء والتدقيق واختبارات المحاكي — موجودة في المستودع وفق شروط NOTICE.md (مجانًا، دون بيع، مع إبقاء إشعار المجانية). خلفية Spotify المملوكة مُدخل بناء محلي. منذ test.13 لا يحتوي ملف APK على أي جزء من مشغّل BYD (Vivid).

[Java / build](adapter/) · [ما الذي تم اختباره؟](reports/headunit-test.13.md) · [NOTICE](NOTICE.md)

</div>
