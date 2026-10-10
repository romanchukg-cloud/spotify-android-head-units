package local.spotify.unified;
import java.util.Locale;
public final class HuText {
 public static String get(String key){
  String[] keys={"settings","close","external","resume","scale","spotify","saved","channel","background","guard","error","apply"};
  String[][] text={
   {"Head unit settings","Stop music when Spotify is closed","Allow other apps to control Spotify","Resume playback after head unit starts","Interface scale","Spotify account and audio settings","Changes saved. Scale applies when you return to the player.","Spotify playback","Music playback and media controls","Stop on close is enabled","Settings could not be saved. Please try again.","Save"},
   {"Налаштування магнітоли","Зупиняти музику при закритті Spotify","Дозволити іншим застосункам керувати Spotify","Продовжувати відтворення після старту магнітоли","Масштаб інтерфейсу","Акаунт і налаштування звуку Spotify","Зміни збережено. Масштаб застосовується після повернення до плеєра.","Відтворення Spotify","Відтворення музики та медіакерування","Зупинку при закритті увімкнено","Не вдалося зберегти налаштування. Спробуй ще раз.","Зберегти"},
   {"Настройки магнитолы","Останавливать музыку при закрытии Spotify","Разрешить другим приложениям управлять Spotify","Продолжать воспроизведение после запуска магнитолы","Масштаб интерфейса","Аккаунт и настройки звука Spotify","Изменения сохранены. Масштаб применится после возврата в плеер.","Воспроизведение Spotify","Воспроизведение музыки и медиакнопки","Остановка при закрытии включена","Не удалось сохранить настройки. Попробуй ещё раз.","Сохранить"},
   {"إعدادات وحدة السيارة","إيقاف الموسيقى عند إغلاق Spotify","السماح للتطبيقات الأخرى بالتحكم في Spotify","استئناف التشغيل بعد بدء تشغيل وحدة السيارة","حجم الواجهة","إعدادات حساب Spotify والصوت","تم الحفظ. يطبق الحجم عند العودة إلى المشغل.","تشغيل Spotify","تشغيل الموسيقى وأزرار التحكم","الإيقاف عند الإغلاق مفعّل","تعذر حفظ الإعدادات. حاول مجدداً.","حفظ"},
   {"车机设置","关闭 Spotify 时停止音乐","允许其他应用控制 Spotify","车机启动后继续播放","界面缩放","Spotify 账号和音频设置","已保存。返回播放器后应用缩放。","Spotify 播放","音乐播放和媒体控制","已启用关闭时停止","无法保存设置，请重试。","保存"}
  };
  String lang=Locale.getDefault().getLanguage();int row=lang.equals("uk")?1:lang.equals("ru")?2:lang.equals("ar")?3:lang.equals("zh")?4:0;
  for(int i=0;i<keys.length;i++)if(key.equals(keys[i]))return text[row][i];return key;
 }
}
