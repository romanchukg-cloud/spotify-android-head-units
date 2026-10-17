package local.spotify.unified;
import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
public final class HeadUnitSettings {
 public static Bundle call(Context c,String method,Bundle args){Uri uri=Uri.parse("content://"+c.getPackageName()+".local.headunit");Bundle b=c.getContentResolver().call(uri,method,null,args);if(b==null)throw new IllegalStateException("Settings service unavailable");return b;}
 public static void open(Activity a){a.startActivity(new Intent(a,HeadUnitSettingsActivity.class));}
 public static int density(Context c,int scale){DisplayMetrics d=new DisplayMetrics();c.getSystemService(WindowManager.class).getDefaultDisplay().getRealMetrics(d);return Math.round(d.densityDpi*scale/100f);}
 /** Applies the UI scale and the Classic/New style to the player's base context. */
 public static Context scaled(Context c){
  Bundle b;
  try{b=call(c,"get",null);}catch(RuntimeException e){android.util.Log.w("SpotifyHU","Head unit settings unavailable; using device density");b=new Bundle();}
  try{
   Configuration config=new Configuration(c.getResources().getConfiguration());
   if(b.containsKey("scale")){int old=config.densityDpi;config.densityDpi=density(c,b.getInt("scale"));if(old>0){config.screenWidthDp=Math.round(config.screenWidthDp*old/(float)config.densityDpi);config.screenHeightDp=Math.round(config.screenHeightDp*old/(float)config.densityDpi);config.smallestScreenWidthDp=Math.round(config.smallestScreenWidthDp*old/(float)config.densityDpi);}}
   HeadUnitStyle.apply(config);
   return c.createConfigurationContext(config);
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","UI scale unavailable; using device density");return c;}
 }
 /**
  * Restarts the interface process with a fresh player. Recreating the BYD-derived activity
  * restores fragments whose delayed callbacks outlive their views (CategoryFragment,
  * MainFragment); playback runs in the backend process and continues.
  */
 public static void restartPlayer(Activity a){
  a.startActivity(new Intent(a,a.getClass()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
  a.finish();android.os.Process.killProcess(android.os.Process.myPid());
 }
 /** True when the activity's resources no longer match the saved scale or the style. */
 public static boolean stale(Activity a){
  Bundle b=call(a,"get",null);Configuration config=a.getResources().getConfiguration();
  return config.densityDpi!=density(a,b.getInt("scale",100)) || !HeadUnitStyle.applied(config);
 }
}
