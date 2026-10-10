package local.spotify.unified;
import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
public final class HeadUnitSettings {
 private static final Uri URI=Uri.parse("content://com.spotify.music.local.headunit");
 public static Bundle call(Context c,String method,Bundle args){Bundle b=c.getContentResolver().call(URI,method,null,args);if(b==null)throw new IllegalStateException("Settings service unavailable");return b;}
 public static void open(Activity a){a.startActivity(new Intent(a,HeadUnitSettingsActivity.class));}
 public static int density(Context c,int scale){DisplayMetrics d=new DisplayMetrics();c.getSystemService(WindowManager.class).getDefaultDisplay().getRealMetrics(d);return Math.round(d.densityDpi*scale/100f);}
 public static Context scaled(Context c){
  try{int scale=call(c,"get",null).getInt("scale",100);Configuration config=new Configuration(c.getResources().getConfiguration());int old=config.densityDpi;config.densityDpi=density(c,scale);if(old>0){config.screenWidthDp=Math.round(config.screenWidthDp*old/(float)config.densityDpi);config.screenHeightDp=Math.round(config.screenHeightDp*old/(float)config.densityDpi);config.smallestScreenWidthDp=Math.round(config.smallestScreenWidthDp*old/(float)config.densityDpi);}return c.createConfigurationContext(config);}catch(RuntimeException e){android.util.Log.w("SpotifyHU","UI scale unavailable; using device density");return c;}
 }
}
