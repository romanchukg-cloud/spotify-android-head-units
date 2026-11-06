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
 private static volatile int lastStatus=-1;
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
  SharedPreferences.Editor signedIn=prefs(c).edit().putString("active_user",user).remove("expected_empty");
  // A failed attempt is over once a sign-in has worked.
  if(String.valueOf(prefs(c).getString("last_reason",null)).startsWith("login_"))signedIn.remove("last_reason").remove("last_detail").remove("last_time");
  signedIn.commit();
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

 /** vr.e: the status of a sign-in response, which the SDK drops when it does not succeed. */
 public static void loginStatus(int status){lastStatus=status;}
 /** n5.r.d: a sign-in has failed and the login screen asks for its message. Never in the sign-in's way. */
 public static void loginFailed(Object result){
  Context c=context;if(c==null || result==null)return;
  try{
   String[] facts=loginFacts(result,lastStatus);lastStatus=-1;int status=Integer.parseInt(facts[1]);
   String reason=loginReason(facts[0],status,facts[2]);
   // The login screen's own text already explains the known errors; they are no technical failure.
   if(reason!=null)record(c,reason,!"login_known".equals(reason),loginDetail(reason,status,facts[2]));
  }catch(ReflectiveOperationException | RuntimeException e){Log.w("SpotifyHU","Sign-in error unreadable");}
 }
 /** The result's class, status and server message: ur.b holds a server error (ls.a: status j, fields k); the others only the response's status. */
 static String[] loginFacts(Object result,int response)throws ReflectiveOperationException{
  String type=result.getClass().getName();
  if(!"ur.b".equals(type))return new String[]{type,String.valueOf(response),null};
  Object error=result.getClass().getField("a").get(result);
  Object message=((java.util.Map<?,?>)error.getClass().getField("k").get(error)).get("message");
  return new String[]{type,String.valueOf(error.getClass().getField("j").getInt(error)),message==null?null:String.valueOf(message)};
 }
 /** Which failure it was; null for results that are none (ur.c signed in) or recorded elsewhere (ur.e, deprecated). */
 static String loginReason(String type,int status,String message){
  switch(type){
   case "ur.b":return status==429?"rate_limited".equals(message)?"login_known":"login_unknown_server":status==401 && java.util.Arrays.asList("auth_bad_credentials","login_region_mismatch","auth_network","ap_network_disabled").contains(message)?"login_known":"login_unknown_server";
   case "ur.d":return status>=200 && status<300?"login_not_saved":"login_failed";
   default:return null;
  }
 }
 /** For the settings screen only: the status and a code-like server message, never free text. */
 static String loginDetail(String reason,int status,String message){
  if("login_not_saved".equals(reason) || status<=0)return null;
  return message!=null && message.matches("[A-Za-z0-9_.-]{1,40}")?status+" "+message:String.valueOf(status);
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
 private static void record(Context c,String reason,boolean failure){record(c,reason,failure,null);}
 private static void record(Context c,String reason,boolean failure,String detail){
  prefs(c).edit().putString("last_reason",reason).putString("last_detail",detail).putLong("last_time",System.currentTimeMillis()).apply();
  Log.i("SpotifyHU","Sign-in event: "+reason+(detail==null?"":" ("+detail+")"));
  if(failure)DiagnosticLog.failureCode("auth",code(reason));
 }
 /** Maps a sign-out reason onto the shared diagnostics schema's failure codes. */
 static String code(String reason){
  switch(reason){
   case "invalid_credentials":case "not_allowed":case "forced_logout":case "remote_logout":return "permission_denied";
   case "unsupported_protocol":case "deprecated":case "login_unknown_server":return "schema_mismatch";
   case "account_removed":case "login_not_saved":return "unavailable";
   default:return "internal_error";
  }
 }
 /** Settings screen summary; read in the backend through the private settings provider. */
 static Bundle summary(Context c){
  SharedPreferences p=prefs(c);Bundle b=new Bundle();
  b.putString("auth_reason",p.getString("last_reason",null));b.putString("auth_detail",p.getString("last_detail",null));b.putLong("auth_time",p.getLong("last_time",0));b.putLong("device_id_changed",p.getLong("device_id_changed",0));
  return b;
 }
}
