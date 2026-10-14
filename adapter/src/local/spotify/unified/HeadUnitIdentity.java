package local.spotify.unified;
import android.content.*;
import android.provider.Settings;
import android.util.Log;
/** Keeps the Spotify device ID stable. Saved logins are tied to it, so a firmware
 * that regenerates ANDROID_ID would otherwise invalidate them and sign the user out. */
public final class HeadUnitIdentity {
 private static volatile Context context;
 private static String pinned;
 public static void init(Context c){if(context==null)context=c.getApplicationContext();}
 /** Drop-in for Settings.Secure.getString at the SDK's device-ID call sites. */
 public static String secureString(ContentResolver resolver,String name){
  String current=Settings.Secure.getString(resolver,name);
  if(!Settings.Secure.ANDROID_ID.equals(name))return current;
  return androidId(current);
 }
 private static synchronized String androidId(String current){
  if(pinned!=null)return pinned;
  Context c=context;if(c==null)return current;
  try{
   SharedPreferences p=c.getSharedPreferences("headunit_identity_v1",0);
   String[] result=pin(p.getString("android_id",null),current);
   if(result[1]!=null && !p.edit().putString("android_id",result[1]).commit())return current;
   if(result[2]!=null){Log.w("SpotifyHU","System ANDROID_ID changed; keeping the device ID used for sign-in");HeadUnitAccounts.deviceIdChanged(c);}
   pinned=result[0];return pinned;
  }catch(RuntimeException e){Log.w("SpotifyHU","Device ID store unavailable");return current;}
 }
 /** Returns {value, value to save or null, non-null when the system ID changed}. */
 static String[] pin(String stored,String current){
  boolean present=current!=null && !current.isEmpty();
  if(stored==null || stored.isEmpty())return new String[]{current,present?current:null,null};
  return new String[]{stored,null,present && !stored.equals(current)?"changed":null};
 }
}
