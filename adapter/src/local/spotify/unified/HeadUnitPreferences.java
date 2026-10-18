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
  if(reboot){e.putBoolean("boot_restore_pending",p.getBoolean("boot_restore_pending",false) || p.getBoolean("shutdown_playing",p.getBoolean("was_playing",false))).remove("resume_source").remove("resume_started").putLong("resume_generation",p.getLong("resume_generation",0)+1).remove("shutdown_playing").remove("screen_off").remove("screen_playing");c.getSharedPreferences("local_close_gate",0).edit().putBoolean("closed",false).commit();}
  if(!e.commit())throw new IllegalStateException("Head unit preferences unavailable");
 }
 public static SharedPreferences store(){return context.getSharedPreferences("headunit_v1",0);}
 /** Closing Spotify always stops the music (owner decision, test.10); the old "stop_close" choice is ignored. */
 public static boolean stopOnClose(){return true;}
 public static boolean external(){return store().getBoolean("external",HeadUnitProfile.get(context)!=HeadUnitProfile.Kind.BYD);}
 public static boolean resume(){return store().getBoolean("resume",HeadUnitProfile.get(context)==HeadUnitProfile.Kind.GENERIC);}
 public static boolean playOnOpen(){return store().getBoolean("play_open",false);}
 public static boolean coverAccent(){return store().getBoolean("cover_accent",true);}
 /** The driver's side: "auto" (as the car says), "left" or "right" (HeadUnitDriver). */
 public static String driveSide(){String v=store().getString("drive_side",HeadUnitDriver.AUTO);return HeadUnitDriver.LEFT.equals(v) || HeadUnitDriver.RIGHT.equals(v)?v:HeadUnitDriver.AUTO;}
 public static int scale(){return Math.max(80,Math.min(130,store().getInt("scale",100)));}
 public static Bundle read(){Bundle b=new Bundle();b.putString("profile",HeadUnitProfile.get(context).name());b.putBoolean("stop_close",stopOnClose());b.putBoolean("external",external());b.putBoolean("resume",resume());b.putBoolean("play_open",playOnOpen());b.putInt("scale",scale());b.putBoolean("cover_accent",coverAccent());b.putString("drive_side",driveSide());b.putAll(HeadUnitAccounts.summary(context));return b;}
 public static void write(Bundle b){SharedPreferences.Editor e=store().edit();for(String key:new String[]{"external","resume","play_open","cover_accent"})if(b.containsKey(key))e.putBoolean(key,b.getBoolean(key));if(b.containsKey("drive_side"))e.putString("drive_side",b.getString("drive_side"));if(b.containsKey("scale"))e.putInt("scale",Math.max(80,Math.min(130,b.getInt("scale"))));if(b.containsKey("resume") && !b.getBoolean("resume"))e.putBoolean("boot_restore_pending",false).putBoolean("screen_playing",false).remove("shutdown_playing");if(!e.commit())throw new IllegalStateException("Settings save failed");}
}
