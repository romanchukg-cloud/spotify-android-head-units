package local.spotify.unified;
import android.accounts.AccountManager;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import java.util.Locale;
import java.util.concurrent.*;
/** Backend-only sign-in bookkeeping, called from SDK hooks. It records why Spotify signed out,
 * ignores removal of Android accounts that are not the active user, and restores the last
 * active saved login once when the SDK's stored user vanished without any sign-out. */
public final class HeadUnitAccounts {
 private static volatile Context context;
 private static volatile String tokenError;
 private static volatile boolean serverRejected,removalPending,removalEcho;
 private static boolean recoveryTried;
 private static final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor();
 public static void init(Context c){if(context==null)context=c.getApplicationContext();}
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("headunit_auth_v1",0);}

 /** zb.a.b: Spotify adds its Android account after a fresh sign-in. */
 public static void androidAccount(String name){Context c=context;if(c!=null && name!=null)prefs(c).edit().putString("android_name",name).apply();}
 /** zb.a.c: a user is signed in, at startup or after a fresh sign-in. */
 public static void loggedIn(String user){
  Context c=context;if(c==null || user==null || user.isEmpty())return;
  tokenError=null;serverRejected=false;removalPending=false;removalEcho=false;
  prefs(c).edit().putString("active_user",user).remove("expected_empty").commit();
  // Keep the saved copy current so a later storage loss can be repaired.
  worker.schedule(()->DriverProfiles.syncActive(c),20,TimeUnit.SECONDS);
 }
 /** ur.a.d entry (ac.j, vr.e): every sign-out request, before the stored user is removed. */
 public static void logoutRequested(Object reason){
  Context c=context;if(c==null)return;String r=String.valueOf(reason);
  // Spotify removes its own Android account after it has already signed out.
  if(removalEcho){removalEcho=false;return;}
  String detail=classify(r,removalPending,serverRejected,tokenError);
  removalPending=false;serverRejected=false;signedOut(c,detail);
 }
 /** zb.a.d: only the startup "no stored user" result is new information here. */
 public static void loggedOut(Object reason){
  Context c=context;if(c==null || !"NOT_LOGGED_IN".equals(String.valueOf(reason)))return;
  SharedPreferences p=prefs(c);
  if(p.getBoolean("expected_empty",false))p.edit().remove("expected_empty").commit();
  else if(p.getString("active_user",null)!=null)signedOut(c,"storage_missing");
 }
 /** zb.a.a: Spotify's remote minimum version is above this build; it removes the login itself. */
 public static void deprecated(){Context c=context;if(c!=null)signedOut(c,"deprecated");}
 /** za.d: the login5 error behind a rejected access-token request. */
 public static void tokenError(Object code){tokenError=String.valueOf(code);}
 /** is.e: the SDK is about to force a sign-out because the saved login was rejected. */
 public static void serverRejected(){serverRejected=true;}
 static String classify(String reason,boolean removal,boolean server,String code){
  if(removal)return "account_removed";
  if(server)return "INVALID_CREDENTIALS".equals(code)?"invalid_credentials":"NOT_ALLOWED".equals(code)?"not_allowed":"UNSUPPORTED_LOGIN_PROTOCOL".equals(code)?"unsupported_protocol":"forced_logout";
  return "USER_INITIATED".equals(reason)?"user_logout":reason.toLowerCase(Locale.ROOT);
 }

 /** AccountRemovedBroadcastReceiver: true skips Spotify's sign-out for this broadcast. */
 public static boolean ignoreRemoval(Context c,Intent intent){
  try{
   if(intent==null || !AccountManager.ACTION_ACCOUNT_REMOVED.equals(intent.getAction()))return false;
   init(c);
   if(local.spotify.close.PlaybackGate.accountTransition()){Log.i("SpotifyHU","Ignored Android account removal during an account switch");return true;}
   SharedPreferences p=prefs(c);String active=p.getString("active_user",null);
   boolean ignore=stale(intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME),active,p.getString("android_name",null));
   Log.i("SpotifyHU",ignore?"Ignored removal of an inactive Spotify Android account":"Spotify Android account removed; signing out");
   if(!ignore){if(active!=null)removalPending=true;else removalEcho=true;}
   return ignore;
  }catch(RuntimeException e){return false;}
 }
 /** A removal is stale only when the active user is known and the removed name is not one of its names. */
 static boolean stale(String removed,String active,String android){
  return removed!=null && active!=null && !removed.equals(active) && !removed.equals(android);
 }

 /** ac.j/vr.e constructors, just before Spotify reads its stored user at startup. */
 public static void beforeStartupRead(Object store){
  Context c=context;if(c==null || store==null)return;
  synchronized(HeadUnitAccounts.class){if(recoveryTried)return;recoveryTried=true;}
  try{
   SharedPreferences p=prefs(c);String user=p.getString("active_user",null),boot=bootId(c);
   if(!recoverable(user,p.getBoolean("expected_empty",false),p.getString("recovery_boot",null),boot))return;
   if(DriverProfiles.hasStoredUser(store))return;
   if(!p.edit().putString("recovery_boot",boot).commit())return;
   if(DriverProfiles.restoreSaved(c,store,user)){Log.w("SpotifyHU","Saved Spotify login was missing; restored the last active account");record(c,"storage_restored",false);}
  }catch(Exception e){Log.w("SpotifyHU","Sign-in recovery unavailable ("+e.getClass().getSimpleName()+")");}
 }
 /** One attempt per boot, only for a user that never signed out and was not being switched away from. */
 static boolean recoverable(String user,boolean expectedEmpty,String lastBoot,String boot){
  return user!=null && !user.isEmpty() && !expectedEmpty && !boot.equals(lastBoot);
 }
 private static String bootId(Context c){
  int count=Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,-1);
  return count>=0?"count:"+count:"epoch:"+(System.currentTimeMillis()-SystemClock.elapsedRealtime())/60000;
 }

 /** DriverProfiles: the active SDK user is about to change; a QR switch leaves no stored user on purpose. */
 static void switching(Context c,boolean qr){
  SharedPreferences.Editor e=prefs(c).edit().remove("android_name");
  if(qr)e.remove("active_user").putBoolean("expected_empty",true);
  e.commit();
 }
 static void deviceIdChanged(Context c){prefs(c).edit().putLong("device_id_changed",System.currentTimeMillis()).apply();}
 private static void signedOut(Context c,String reason){
  prefs(c).edit().remove("active_user").remove("android_name").commit();
  // User actions stay local; automatic reports cover measured technical failures only.
  record(c,reason,!"user_logout".equals(reason) && !"account_removed".equals(reason));
 }
 private static void record(Context c,String reason,boolean failure){
  prefs(c).edit().putString("last_reason",reason).putLong("last_time",System.currentTimeMillis()).apply();
  Log.i("SpotifyHU","Sign-in event: "+reason);
  if(failure)DiagnosticLog.failureCode("auth",code(reason));
 }
 /** Maps a sign-out reason onto the shared diagnostics schema's failure codes. */
 static String code(String reason){
  switch(reason){
   case "invalid_credentials":case "not_allowed":case "forced_logout":case "remote_logout":return "permission_denied";
   case "unsupported_protocol":case "deprecated":return "schema_mismatch";
   case "account_removed":return "unavailable";
   default:return "internal_error";
  }
 }
 /** Settings screen summary; read in the backend through the private settings provider. */
 static Bundle summary(Context c){
  SharedPreferences p=prefs(c);Bundle b=new Bundle();
  b.putString("auth_reason",p.getString("last_reason",null));b.putLong("auth_time",p.getLong("last_time",0));b.putLong("device_id_changed",p.getLong("device_id_changed",0));
  return b;
 }
}
