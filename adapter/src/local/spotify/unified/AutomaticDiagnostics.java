package local.spotify.unified;
import android.content.*;import android.os.*;import java.security.MessageDigest;import java.nio.charset.StandardCharsets;import java.util.concurrent.*;import org.json.*;
/** Backend-owned consent, failure limits and automatic enqueue. No dialogs or raw exceptions. */
public final class AutomaticDiagnostics {
 private static final ExecutorService worker=Executors.newSingleThreadExecutor();
 private static final java.util.concurrent.atomic.AtomicBoolean handling=new java.util.concurrent.atomic.AtomicBoolean();
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("diagnostics_consent_v1",0);}
 public static Bundle state(Context c){SharedPreferences p=prefs(c);Bundle b=new Bundle();b.putBoolean("notice_required",!p.getBoolean("notice_accepted",false));b.putBoolean("auto_choice",p.getBoolean("auto_choice",true));b.putBoolean("automatic_available",DiagnosticsConfig.automaticSupported());return b;}
 public static void accept(Context c,boolean choice){if(!prefs(c).edit().putBoolean("notice_accepted",true).putBoolean("auto_choice",choice).commit())throw new IllegalStateException("Choice could not be saved");if(!choice)DiagnosticsQueue.cancelAutomatic(c);}
 public static void choice(Context c,boolean choice){if(!prefs(c).edit().putBoolean("auto_choice",choice).commit())throw new IllegalStateException("Choice could not be saved");if(!choice)DiagnosticsQueue.cancelAutomatic(c);}
 public static boolean canTransmit(Context c){return prefs(c).getBoolean("notice_accepted",false) && prefs(c).getBoolean("auto_choice",true) && DiagnosticsConfig.automaticSupported();}
 static void observe(Context c,String stage,String code){if("upload".equals(stage))return;try{Bundle b=new Bundle();b.putString("stage",stage);b.putString("code",code);HeadUnitSettings.call(c,"diagnostics_failure",b);}catch(RuntimeException ignored){}}
 static void observeBackend(Context c,String stage,String code){if(!canTransmit(c) || !handling.compareAndSet(false,true))return;Context app=c.getApplicationContext();worker.execute(()->{try{if(enqueue(app,stage,code,System.currentTimeMillis(),SystemClock.elapsedRealtime())!=null){DiagnosticUploadJob.schedule(app);DiagnosticUploadJob.kick(app);}}catch(Exception ignored){}finally{handling.set(false);}});}
 static synchronized String enqueue(Context c,String stage,String code,long wall,long elapsed)throws Exception{
  fingerprint(stage,code);if(!canTransmit(c))return null;return DiagnosticsQueue.automatic(c,stage,code,wall,elapsed);
 }
 static boolean capability(DiagnosticTransport.Reply reply){if(reply.http!=200 || !Boolean.TRUE.equals(reply.json.opt("automatic_reports")) || !"project_build_failure_utc_day".equals(reply.json.optString("automatic_grouping")))return false;JSONArray versions=reply.json.optJSONArray("schema_versions");if(versions==null)return false;for(int i=0;i<versions.length();i++)if(versions.opt(i) instanceof Integer && ((Integer)versions.opt(i))==2)return true;return false;}
 static String fingerprint(String stage,String code)throws Exception{
  if(!DiagnosticSchema.STAGES.contains(stage) || !DiagnosticSchema.CODES.contains(code) || "upload".equals(stage))throw new IllegalArgumentException("Not an automatic trigger");
  byte[] digest=MessageDigest.getInstance("SHA-256").digest((DiagnosticsConfig.PROJECT_ID+"|"+HeadUnitBuild.LABEL+"|"+stage+"|"+code).getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte b:digest)s.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return s.toString();
 }
 /** Reserve before enqueue; a crash may suppress one report, but cannot bypass the daily limit. */
 static synchronized boolean reserve(Context c,String stage,String code,long wall,long elapsed)throws Exception{
  String f=fingerprint(stage,code);SharedPreferences p=c.getSharedPreferences("diagnostics_auto_limits",0);long last=p.getLong("last_wall",-1),up=p.getLong("last_elapsed",-1);
  if(last>=0){long age=wall-last;if(age<86400000 || age<0 || (elapsed>=up && Math.abs((wall-elapsed)-(last-up))<5000 && elapsed-up<86400000))return false;}
  long previous=p.getLong("fp_"+f,-1);if(previous>=0 && wall-previous<86400000)return false;
  SharedPreferences.Editor edit=p.edit();for(String key:p.getAll().keySet())if(key.startsWith("fp_") && wall-p.getLong(key,wall)>86400000)edit.remove(key);
  return edit.putLong("last_wall",wall).putLong("last_elapsed",elapsed).putLong("fp_"+f,wall).commit();
 }
}
