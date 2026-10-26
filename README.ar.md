<div dir="rtl">

# Spotify لوحدات الوسائط العاملة بنظام Android

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

مشغّل Spotify Automotive تجريبي لوحدات السيارات بنظام Android، بواجهة خاصة به مصممة للسيارة: ملف APK واحد وأيقونة واحدة وتسجيل الدخول المعتاد إلى Spotify. مجاني — لا تدفع مقابله أبدًا.

[تنزيل APK test.18](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.18/Spotify-5.5.0-HeadUnit-test.18.apk) · [🌐 Spotify لوحدات الوسائط العاملة بنظام Android](https://romanchukg-cloud.github.io/spotify-android-head-units/ar/)

![الرئيسية ليلًا في سيارة BYD](docs/screens/car-home-night.jpg)

## ما الذي تم اختباره؟

يعرض Headunit-test.18 تطبيق Spotify في لوحة الصوت في Waze: المقطع وعناصر التحكم والقلب والخلط حيث يسمح به Spotify، إضافة إلى الرئيسية والأخيرة والتصفح والمكتبة. ومنذ test.17 أصبح التحميل والتنقل سلسين: عناصر نائبة بشكل المحتوى بدلاً من «جارٍ التحميل…»، ويظهر المحتوى تدريجيًا، وتظهر الأغلفة بتلاشٍ ناعم، وتنزلق الصفحات من الجانب، وتتبدّل علامات التبويب بتلاشٍ ناعم. ومنذ test.16 يعرض أين تُشغَّل الموسيقى وينقلها بين هذه السيارة وأجهزتك الأخرى (Spotify Connect)؛ ويضم المشغّل المصغّر الوقت والخلط والتكرار والأجهزة، وحول الشاشة إطار رفيع، ويُغلق شريط التنقل المشغّل بملء الشاشة. ومنذ test.14 يتضمن أدوات للشاشة الرئيسية للمشغلات التي تتيح إضافة الأدوات (خمسة تخطيطات حسب الحجم، نهاراً وليلاً)، ويفتح روابط Spotify من التطبيقات الأخرى، ويُعلم التطبيقات الأخرى بما يتم تشغيله، ويمكنه تشغيل Spotify في الخلفية عند بدء تشغيل وحدة السيارة (ليس في BYD). أُنشئ بالبناء المرجعي الكامل. تدقيق الإصدار: 56 PASS / 0 FAIL؛ تنجح 152 فحصاً للتحديثات و39 لتسجيل الدخول و14 للوصول إلى الكتلة و13 لرسائل Connect و66 لـ App Remote و19 لبدء التشغيل، وشهادة التوقيع مطابقة لـ test.17. على محاكيات Android 9 و13 يُثبَّت test.18 تحديثاً فوق test.17 ويتحقق من التحديثات دون أعطال؛ ويعمل تسجيل الدخول و«الحسابات» على Android 13. فُحصت الأدوات في المشغلات القياسية لمحاكيات Android 11 و12، وفتحت الروابط هناك قوائم التشغيل والألبومات والفنانين. يعمل إصدار بالشيفرة نفسها في BYD Tang (DiLink 5، Android 12)، وقد فُتح Spotify هناك في لوحة الصوت في Waze for BYD 5.22 مع القلب بجانب المقطع والخلط في Liked Songs، ويعمل القلب في المشغّل؛ وجرى التحقق هناك من الحركة مع test.17، ومن نقل الموسيقى إلى حاسوب محمول وإعادتها مع test.16.

**إصدار تجريبي headunit-test.18.** جرى التحقق من Spotify في Waze مع Waze for BYD 5.22 في سيارة واحدة فقط؛ ويطلب Waze موافقته الخاصة مرة واحدة ولا يعرض زر التكرار له. جرى التحقق من نقل الموسيقى مع حاسوب محمول فقط. لم تعرض الأدوات حتى الآن سوى مقاطع تجريبية، ولا يستطيع مشغل BYD الأصلي عرضها. لم يُتحقق من تشغيل أغنية عبر رابط ولا من تطبيق حقيقي يقرأ ما يتم تشغيله. لم يُختبر المقود الأيمن على سيارة بمقود أيمن؛ ولم يُتحقق من DiLink 5.1 / Android 13 ووحدات السيارات الأخرى؛ وفُحصت العربية على محاكٍ فقط. تظهر الكتلة للوحات فقط في التطبيقات التي تدعمها. يُبقي Spotify التنزيلات معطلة لعميل Automotive؛ هذا الإصدار يفعّل دعمها، لذا قد يغيّر Spotify طريقة عملها.

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

Android 9+ · 46.2 MiB · تعديل غير رسمي

## التحقق من الملف

`Spotify-5.5.0-HeadUnit-test.18.apk` · 48 419 954 bytes

بصمة SHA-256 للإصدار التجريبي الحالي:

```text
114962299a487d7459481567a196f969fe3d76a67e720dca483fce344863467d
```

مشروع تجريبي غير رسمي. هذا التطبيق مجاني — إذا دفعت مقابله فاطلب استرداد أموالك. Spotify وBYD علامتان تجاريتان لمالكيهما المعنيين.

## الشيفرة المصدرية والتحقق

شيفرة Java الخاصة بنا — الواجهة والتكامل وسكربتات الترقيع والبناء والتدقيق واختبارات المحاكي — موجودة في المستودع وفق شروط NOTICE.md (مجانًا، دون بيع، مع إبقاء إشعار المجانية). خلفية Spotify المملوكة مُدخل بناء محلي. منذ test.13 لا يحتوي ملف APK على أي جزء من مشغّل BYD (Vivid).

[Java / build](adapter/) · [ما الذي تم اختباره؟](reports/headunit-test.18.md) · [NOTICE](NOTICE.md)

</div>
