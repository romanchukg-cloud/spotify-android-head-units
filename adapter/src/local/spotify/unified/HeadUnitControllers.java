package local.spotify.unified;
import android.content.*;
import android.content.pm.*;
import android.os.Process;
import android.util.Log;
public final class HeadUnitControllers {
 public static boolean allow(Context c,String pkg){
  HeadUnitPreferences.init(c);
  try{ApplicationInfo a=c.getPackageManager().getApplicationInfo(pkg,0);
   return a.uid<Process.FIRST_APPLICATION_UID || (a.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0 || a.uid==Process.myUid() || HeadUnitPreferences.external();
  }catch(PackageManager.NameNotFoundException e){return false;}
 }
 public static boolean matches(Context c,int uid,String pkg){
  if(uid<Process.FIRST_APPLICATION_UID)return true;
  try{return c.getPackageManager().getApplicationInfo(pkg,0).uid==uid;}catch(PackageManager.NameNotFoundException e){return false;}
 }
 public static boolean result(String pkg,boolean accepted){if(!accepted)Log.w("SpotifyHU","Controller rejected package="+pkg);return accepted;}
}
