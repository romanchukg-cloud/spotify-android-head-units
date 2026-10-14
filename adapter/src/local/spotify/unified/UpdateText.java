package local.spotify.unified;
import android.os.Bundle;import java.util.Locale;
public final class UpdateText {
 private static final String[] KEYS={"updates","current","auto_check","auto_download","wifi_only","check","download","install","pause","done","later","allow","note","ready_title","restart","permission_title","permission_message",
  "idle","checked","checking","up_to_date","available","wifi_wait","downloading","verifying","ready","check_failed","download_failed","paused","storage","preparing","confirm","installed","install_failed","cancelled","permission","unavailable"};
 private static final String[][] TEXT={
  {"Updates","Installed version: %1$s","Automatically check for updates","Automatically download updates","Automatic downloads on Wi-Fi only","Check for updates","Download update","Install update","Pause download","Done","Later","Open Android settings",
   "Updates are this project's published test builds. Spotify checks once a day while it runs and verifies every download. Installing restarts Spotify and stops music briefly; Android asks you to confirm Update. Your sign-in and settings are kept.",
   "Spotify update %1$s is ready","Spotify restarts during the update and music stops briefly. Android asks you to confirm Update. Your sign-in and settings are kept.",
   "Allow updates","Allow Spotify to install updates in the Android settings that open next, then return and tap Install update. Android still asks you to confirm every update.",
   "Not checked yet.","Last check: %1$s","Checking for updates…","You have the latest version.","Available: %1$s (%2$d MB)","Available: %1$s (%2$d MB). Waiting for Wi-Fi, or tap Download update.","Downloading %1$s: %2$d%%","Checking the downloaded update…","Ready to install %1$s",
   "Could not check for updates. Spotify tries again later.","The download failed or did not pass the check. Tap Download update to retry.","Download paused. Tap Download update to resume.","Not enough free space for the update.","Preparing the Android Update window…","Confirm Update in the Android window.","Updated to %1$s.",
   "Android did not install the update. Tap Install update to retry.","Update cancelled. Tap Install update when ready.","Allow Spotify to install updates, then tap Install update.","Updates are unavailable right now. Try again later."},
  {"Оновлення","Встановлена версія: %1$s","Автоматично перевіряти оновлення","Автоматично завантажувати оновлення","Автоматично завантажувати лише через Wi-Fi","Перевірити оновлення","Завантажити оновлення","Встановити оновлення","Призупинити завантаження","Готово","Пізніше","Відкрити налаштування Android",
   "Оновлення — це опубліковані тестові збірки проєкту. Spotify перевіряє їх раз на день, поки працює, і перевіряє кожне завантаження. Під час встановлення Spotify перезапускається, і музика ненадовго зупиняється; Android попросить підтвердити «Оновити». Вхід і налаштування зберігаються.",
   "Оновлення Spotify %1$s готове","Під час оновлення Spotify перезапуститься, і музика ненадовго зупиниться. Android попросить підтвердити «Оновити». Вхід і налаштування зберігаються.",
   "Дозвольте оновлення","У налаштуваннях Android, які зараз відкриються, дозвольте Spotify встановлювати оновлення, потім поверніться й натисніть «Встановити оновлення». Android однаково проситиме підтвердити кожне оновлення.",
   "Ще не перевірялося.","Остання перевірка: %1$s","Перевірка оновлень…","У вас найновіша версія.","Доступно: %1$s (%2$d МБ)","Доступно: %1$s (%2$d МБ). Очікування Wi-Fi, або натисніть «Завантажити оновлення».","Завантаження %1$s: %2$d%%","Перевірка завантаженого оновлення…","Готово до встановлення: %1$s",
   "Не вдалося перевірити оновлення. Spotify спробує пізніше.","Завантаження не вдалося або файл не пройшов перевірку. Натисніть «Завантажити оновлення», щоб повторити.","Завантаження призупинено. Натисніть «Завантажити оновлення», щоб продовжити.","Недостатньо вільного місця для оновлення.","Підготовка вікна оновлення Android…","Підтвердьте «Оновити» у вікні Android.","Оновлено до %1$s.",
   "Android не встановив оновлення. Натисніть «Встановити оновлення», щоб повторити.","Оновлення скасовано. Натисніть «Встановити оновлення», коли будете готові.","Дозвольте Spotify встановлювати оновлення, потім натисніть «Встановити оновлення».","Оновлення зараз недоступні. Спробуйте пізніше."},
  {"Обновления","Установленная версия: %1$s","Автоматически проверять обновления","Автоматически загружать обновления","Автоматически загружать только по Wi-Fi","Проверить обновления","Загрузить обновление","Установить обновление","Приостановить загрузку","Готово","Позже","Открыть настройки Android",
   "Обновления — это опубликованные тестовые сборки проекта. Spotify проверяет их раз в день, пока работает, и проверяет каждую загрузку. При установке Spotify перезапускается, и музыка ненадолго останавливается; Android попросит подтвердить «Обновить». Вход и настройки сохраняются.",
   "Обновление Spotify %1$s готово","Во время обновления Spotify перезапустится, и музыка ненадолго остановится. Android попросит подтвердить «Обновить». Вход и настройки сохраняются.",
   "Разрешите обновления","В настройках Android, которые сейчас откроются, разрешите Spotify устанавливать обновления, затем вернитесь и нажмите «Установить обновление». Android всё равно попросит подтвердить каждое обновление.",
   "Ещё не проверялось.","Последняя проверка: %1$s","Проверка обновлений…","У вас последняя версия.","Доступно: %1$s (%2$d МБ)","Доступно: %1$s (%2$d МБ). Ожидание Wi-Fi, или нажмите «Загрузить обновление».","Загрузка %1$s: %2$d%%","Проверка загруженного обновления…","Готово к установке: %1$s",
   "Не удалось проверить обновления. Spotify попробует позже.","Загрузка не удалась или файл не прошёл проверку. Нажмите «Загрузить обновление», чтобы повторить.","Загрузка приостановлена. Нажмите «Загрузить обновление», чтобы продолжить.","Недостаточно свободного места для обновления.","Подготовка окна обновления Android…","Подтвердите «Обновить» в окне Android.","Обновлено до %1$s.",
   "Android не установил обновление. Нажмите «Установить обновление», чтобы повторить.","Обновление отменено. Нажмите «Установить обновление», когда будете готовы.","Разрешите Spotify устанавливать обновления, затем нажмите «Установить обновление».","Обновления сейчас недоступны. Попробуйте позже."},
  {"التحديثات","الإصدار المثبت: %1$s","التحقق من التحديثات تلقائيًا","تنزيل التحديثات تلقائيًا","التنزيل التلقائي عبر Wi-Fi فقط","التحقق من التحديثات","تنزيل التحديث","تثبيت التحديث","إيقاف التنزيل مؤقتًا","تم","لاحقًا","فتح إعدادات Android",
   "التحديثات هي إصدارات الاختبار المنشورة لهذا المشروع. يتحقق Spotify منها مرة يوميًا أثناء تشغيله ويفحص كل ملف يتم تنزيله. يؤدي التثبيت إلى إعادة تشغيل Spotify وتتوقف الموسيقى لفترة قصيرة، ويطلب Android تأكيد «تحديث». يتم الاحتفاظ بتسجيل الدخول والإعدادات.",
   "تحديث Spotify %1$s جاهز","يُعاد تشغيل Spotify أثناء التحديث وتتوقف الموسيقى لفترة قصيرة. يطلب Android تأكيد «تحديث». يتم الاحتفاظ بتسجيل الدخول والإعدادات.",
   "السماح بالتحديثات","اسمح لـ Spotify بتثبيت التحديثات في إعدادات Android التي ستفتح الآن، ثم عُد واضغط «تثبيت التحديث». سيظل Android يطلب تأكيد كل تحديث.",
   "لم يتم التحقق بعد.","آخر تحقق: %1$s","جارٍ التحقق من التحديثات…","لديك أحدث إصدار.","متوفر: %1$s (%2$d ميغابايت)","متوفر: %1$s (%2$d ميغابايت). بانتظار Wi-Fi، أو اضغط «تنزيل التحديث».","جارٍ تنزيل %1$s: %2$d%%","جارٍ فحص التحديث الذي تم تنزيله…","جاهز للتثبيت: %1$s",
   "تعذر التحقق من التحديثات. سيحاول Spotify لاحقًا.","فشل التنزيل أو لم يجتز الملف الفحص. اضغط «تنزيل التحديث» لإعادة المحاولة.","تم إيقاف التنزيل مؤقتًا. اضغط «تنزيل التحديث» للمتابعة.","لا توجد مساحة كافية للتحديث.","جارٍ تجهيز نافذة التحديث في Android…","أكّد «تحديث» في نافذة Android.","تم التحديث إلى %1$s.",
   "لم يثبّت Android التحديث. اضغط «تثبيت التحديث» لإعادة المحاولة.","تم إلغاء التحديث. اضغط «تثبيت التحديث» عندما تكون جاهزًا.","اسمح لـ Spotify بتثبيت التحديثات، ثم اضغط «تثبيت التحديث».","التحديثات غير متاحة الآن. حاول لاحقًا."},
  {"更新","已安装版本：%1$s","自动检查更新","自动下载更新","仅在 Wi-Fi 下自动下载","检查更新","下载更新","安装更新","暂停下载","完成","稍后","打开 Android 设置",
   "更新为本项目已发布的测试版本。Spotify 运行时每天检查一次，并校验每个下载的文件。安装时 Spotify 会重启，音乐会短暂停止；Android 会要求你确认“更新”。登录状态和设置会保留。",
   "Spotify 更新 %1$s 已就绪","更新期间 Spotify 会重启，音乐会短暂停止。Android 会要求你确认“更新”。登录状态和设置会保留。",
   "允许更新","请在接下来打开的 Android 设置中允许 Spotify 安装更新，然后返回并点按“安装更新”。Android 仍会要求你确认每次更新。",
   "尚未检查。","上次检查：%1$s","正在检查更新…","已是最新版本。","可用：%1$s（%2$d MB）","可用：%1$s（%2$d MB）。正在等待 Wi-Fi，或点按“下载更新”。","正在下载 %1$s：%2$d%%","正在校验下载的更新…","可以安装：%1$s",
   "无法检查更新。Spotify 稍后会重试。","下载失败或文件未通过校验。点按“下载更新”重试。","下载已暂停。点按“下载更新”继续。","可用空间不足，无法更新。","正在准备 Android 更新窗口…","请在 Android 窗口中确认“更新”。","已更新到 %1$s。",
   "Android 未安装更新。点按“安装更新”重试。","更新已取消。准备好后点按“安装更新”。","请允许 Spotify 安装更新，然后点按“安装更新”。","暂时无法更新，请稍后再试。"}
 };
 private static int row(){String lang=Locale.getDefault().getLanguage();return lang.equals("uk")?1:lang.equals("ru")?2:lang.equals("ar")?3:lang.equals("zh")?4:0;}
 public static String get(String key){int row=row();for(int i=0;i<KEYS.length;i++)if(KEYS[i].equals(key))return TEXT[row][i];return TEXT[row][KEYS.length-1];}
 public static String format(String key,Object... args){return String.format(Locale.ROOT,get(key),args);}
 /** The backend reports a status key; the text is chosen in the viewer's language. */
 public static String status(Bundle s){
  String key=s.getString("status","idle"),candidate=s.getString("candidate","");long mb=Math.max(1,s.getLong("size")/1000000);
  switch(key){
   case "available":case "wifi_wait":return format(key,candidate,mb);
   case "downloading":return format(key,candidate,s.getInt("percent"));
   case "ready":return format(key,candidate);
   case "installed":return format(key,s.getString("version",""));
   case "idle":long checked=s.getLong("checked");return checked>0?format("checked",java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT).format(new java.util.Date(checked))):get("idle");
   default:return get(key);
  }
 }
 static int rows(){return TEXT.length;}static int columns(int row){return TEXT[row].length;}static int keys(){return KEYS.length;}
 private UpdateText(){}
}
