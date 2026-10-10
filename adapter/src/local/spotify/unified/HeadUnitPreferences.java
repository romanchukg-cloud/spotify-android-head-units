package local.spotify.unified;
import android.content.*;
import android.os.*;
import android.provider.Settings;
/** Accessed in the backend process only; UI reads/writes through a private provider. */
public final class HeadUnitPreferences {
 private static Context context;
 public static synchronized void init(Context c){
  if(context!=null)return;context=c.getApplicationContext();
  SharedPreferences p=store();long boot=System.currentTimeMillis()-SystemClock.elapsedRealtime();
  int count=Settings.Global.getInt(c.getContentResolver(),"boot_count",-1);
  long old=p.getLong("boot_epoch",0);int previous=p.getInt("boot_count",-1);
  boolean reboot=old==0 || (count>=0 && previous>=0 ? count!=previous : Math.abs(boot-old)>5000);
  SharedPreferences.Editor e=p.edit().putLong("boot_epoch",boot).putInt("boot_count",count);
  if(reboot){e.putBoolean("boot_restore_pending",p.getBoolean("shutdown_playing",p.getBoolean("was_playing",false))).remove("shutdown_playing").remove("screen_off").remove("screen_playing");c.getSharedPreferences("local_close_gate",0).edit().putBoolean("closed",false).commit();}
  if(!e.commit())throw new IllegalStateException("Head unit preferences unavailable");
 }
 public static SharedPreferences store(){return context.getSharedPreferences("headunit_v1",0);}
 public static boolean stopOnClose(){return store().getBoolean("stop_close",HeadUnitProfile.get(context)==HeadUnitProfile.Kind.BYD);}
 public static boolean external(){return store().getBoolean("external",HeadUnitProfile.get(context)!=HeadUnitProfile.Kind.BYD);}
 public static boolean resume(){return store().getBoolean("resume",HeadUnitProfile.get(context)==HeadUnitProfile.Kind.GENERIC);}
 public static int scale(){return Math.max(80,Math.min(130,store().getInt("scale",100)));}
 public static Bundle read(){Bundle b=new Bundle();b.putString("profile",HeadUnitProfile.get(context).name());b.putBoolean("stop_close",stopOnClose());b.putBoolean("external",external());b.putBoolean("resume",resume());b.putInt("scale",scale());return b;}
 public static void write(Bundle b){SharedPreferences.Editor e=store().edit();for(String key:new String[]{"stop_close","external","resume"})if(b.containsKey(key))e.putBoolean(key,b.getBoolean(key));if(b.containsKey("scale"))e.putInt("scale",Math.max(80,Math.min(130,b.getInt("scale"))));if(!e.commit())throw new IllegalStateException("Settings save failed");}
}
