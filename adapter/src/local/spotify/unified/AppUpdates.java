package local.spotify.unified;
import android.app.*;import android.app.job.*;import android.content.*;import android.content.pm.*;import android.net.*;import android.os.*;
import java.io.*;import java.net.URL;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.*;import java.util.concurrent.*;
import javax.net.ssl.HttpsURLConnection;import org.json.*;
/**
 * Backend-owned updates: a daily check of the shared update feed, a private resumable download, and a
 * verified PackageInstaller session that only Android's own Update confirmation can complete.
 * All work runs on one worker thread; failures only change the reported status.
 */
public final class AppUpdates {
 static final int JOB_ID=9158;
 private static final long DAY=86400000,HOUR=3600000,WIFI_RETRY=1800000;
 private static final ExecutorService worker=Executors.newSingleThreadExecutor();
 private static Context app;private static SharedPreferences prefs;private static UpdateDownload downloader;
 private static volatile UpdateFeed candidate;
 private static volatile boolean downloading,preparing,ready,cancel,automaticJob,jobStopped;
 private static volatile String status="idle";private static volatile int percent;

 /** Updater work never reaches the uncaught-exception handler of the music process. */
 private static void run(Runnable task){worker.execute(()->{try{task.run();}catch(Throwable t){status="unavailable";downloading=false;preparing=false;automaticJob=false;}
  try{schedule();}catch(Throwable ignored){}
  UpdateFeed info=candidate;android.util.Log.i("SpotifyHU","Update status="+status+(info==null?"":" candidate="+info.version+(ready?" verified":"")));});}
 static synchronized void init(Context c){
  if(app!=null)return;app=c.getApplicationContext();
  prefs=app.getSharedPreferences("headunit_updates_v1",Context.MODE_PRIVATE);
  downloader=new UpdateDownload(new File(app.getNoBackupFilesDir(),"local-updater"),AppUpdates::open);
  run(AppUpdates::restore);
 }
 private static void restore(){
  try{
   long installed=installedCode();int previous=prefs.getInt("last_code",0);
   if(previous>0 && previous<HeadUnitBuild.VERSION_CODE)status="installed";
   prefs.edit().putInt("last_code",HeadUnitBuild.VERSION_CODE).commit();
   int session=prefs.getInt("session",-1);
   if(session>=0 && (installed>=prefs.getLong("target_code",Long.MAX_VALUE) || app.getPackageManager().getPackageInstaller().getSessionInfo(session)==null))clearSession();
   String saved=prefs.getString("candidate","");
   if(!saved.isEmpty()){
    UpdateFeed info=new UpdateFeed(new JSONObject(saved));
    if(!info.newer(installed))forget();
    else{candidate=info;if(downloader.ready().isFile()){try{verify(downloader.ready(),info);ready=true;}catch(Exception bad){downloader.clear();}}
     if(prefs.getInt("session",-1)>=0 && !prefs.getString("confirmation","").isEmpty())status="confirm";
     else if(!"installed".equals(status))status=ready?"ready":"available";}
   }
  }catch(Exception e){forget();}
 }
 static boolean owns(int jobId){return jobId==JOB_ID;}
 static boolean startJob(JobService service,JobParameters params){
  init(service);jobStopped=false;
  run(()->{try{automatic();}finally{try{service.jobFinished(params,false);}catch(RuntimeException ignored){}}});
  return true;
 }
 static void stopJob(){jobStopped=true;if(automaticJob)cancel=true;}
 /** One persisted job: the next daily check or the next automatic download attempt, whichever is earlier. */
 static void schedule(){
  try{
   JobScheduler jobs=app.getSystemService(JobScheduler.class);long now=System.currentTimeMillis(),next=Long.MAX_VALUE;
   if(prefs.getBoolean("automatic_check",true)){long last=prefs.getLong("checked",0);next=last>now?now:last+DAY;}
   if(candidate!=null && !ready && prefs.getBoolean("automatic_download",true) && prefs.getInt("rejected",-1)!=candidate.build)next=Math.min(next,prefs.getLong("retry_at",0));
   if(next==Long.MAX_VALUE){jobs.cancel(JOB_ID);return;}
   jobs.schedule(new JobInfo.Builder(JOB_ID,new ComponentName(app,DiagnosticUploadJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setMinimumLatency(Math.max(60000,next-now)).build());
  }catch(RuntimeException ignored){}
 }
 private static void automatic(){
  if(downloading || preparing || prefs.getInt("session",-1)>=0)return;
  long now=System.currentTimeMillis(),last=prefs.getLong("checked",0);
  if(prefs.getBoolean("automatic_check",true) && (now<last || now-last>=DAY))check();
  else if(candidate!=null && now>=prefs.getLong("retry_at",0))download(false);
 }
 private static void check(){
  if(downloading || preparing || prefs.getInt("session",-1)>=0)return;
  status="checking";long now=System.currentTimeMillis(),retry=0;
  try{
   HttpsURLConnection c=(HttpsURLConnection)new URL(UpdateFeed.URL).openConnection();JSONObject json=null;int code;
   try{
    c.setInstanceFollowRedirects(false);c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setUseCaches(false);c.setRequestProperty("Accept","application/json");
    code=c.getResponseCode();retry=DiagnosticHttps.retryAfter(c.getHeaderField("Retry-After"),now);
    if(code==200)try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
     byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>16384)throw new IOException("response");out.write(buffer,0,n);}
     json=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
    }
   }finally{c.disconnect();}
   if(code==404){prefs.edit().putLong("checked",now).commit();forget();status="up_to_date";return;} // No published record.
   if(json==null)throw new IOException("http "+code);
   UpdateFeed info=new UpdateFeed(json);prefs.edit().putLong("checked",now).commit();
   if(!info.newer(installedCode())){forget();status="up_to_date";return;}
   if(!info.same(candidate)){ready=false;prefs.edit().remove("retry_at").commit();}
   candidate=info;if(!prefs.edit().putString("candidate",info.json.toString()).commit())throw new IOException("storage");
   if(ready){status="ready";return;}
   status="available";
   if(prefs.getBoolean("automatic_download",true) && prefs.getInt("rejected",-1)!=info.build)download(false);
  }catch(Exception e){prefs.edit().putLong("checked",now-DAY+Math.max(HOUR,retry)).apply();status="check_failed";}
 }
 private static void download(boolean manual){
  UpdateFeed info=candidate;if(info==null || ready || downloading || preparing || prefs.getInt("session",-1)>=0)return;
  long now=System.currentTimeMillis();
  if(!manual && (!prefs.getBoolean("automatic_download",true) || prefs.getInt("rejected",-1)==info.build))return;
  if(!manual && !networkAllowed()){status="wifi_wait";prefs.edit().putLong("retry_at",now+WIFI_RETRY).apply();return;}
  downloading=true;automaticJob=!manual;cancel=false;percent=0;status="downloading";
  try{
   File apk=downloader.download(info,new UpdateDownload.Progress(){
    long checked;boolean allowed=true;
    public boolean cancelled(){
     if(automaticJob && SystemClock.elapsedRealtime()-checked>=1000){checked=SystemClock.elapsedRealtime();allowed=prefs.getBoolean("automatic_download",true) && networkAllowed();}
     return cancel || (automaticJob && !allowed);
    }
    public void update(long count,long total){percent=(int)(count*100/total);}
   });
   status="verifying";
   try{verify(apk,info);}catch(Exception bad){downloader.clear();prefs.edit().putInt("rejected",info.build).commit();throw bad;}
   ready=true;status="ready";prefs.edit().remove("retry_at").remove("rejected").commit();
  }catch(InterruptedIOException e){status=automaticJob && !cancel?"wifi_wait":"paused";prefs.edit().putLong("retry_at",System.currentTimeMillis()+(jobStopped?60000:automaticJob?WIFI_RETRY:HOUR)).apply();}
  catch(Exception e){
   status="storage".equals(e.getMessage())?"storage":"download_failed";
   if("hash_mismatch".equals(e.getMessage()))prefs.edit().putInt("rejected",info.build).commit();
   prefs.edit().putLong("retry_at",System.currentTimeMillis()+HOUR).apply();
  }finally{downloading=false;automaticJob=false;}
 }
 /** The file must be exactly the feed's APK, this package, a newer versionCode and the pinned release certificate. */
 private static void verify(File file,UpdateFeed info)throws Exception{
  if(file.length()!=info.size || !UpdateDownload.hash(file).equals(info.sha))throw new IOException("hash");
  PackageManager pm=app.getPackageManager();
  // Android 9 collects archive certificates only when GET_SIGNATURES is requested.
  PackageInfo archive=pm.getPackageArchiveInfo(file.getAbsolutePath(),PackageManager.GET_SIGNATURES|PackageManager.GET_SIGNING_CERTIFICATES);
  if(archive==null || !UpdateFeed.PACKAGE.equals(archive.packageName) || !app.getPackageName().equals(archive.packageName) || archive.getLongVersionCode()!=info.versionCode
   || !info.newer(installedCode()) || archive.applicationInfo==null || archive.applicationInfo.minSdkVersion>Build.VERSION.SDK_INT)throw new IOException("identity");
  Signature[] signers=archive.signingInfo!=null && !archive.signingInfo.hasMultipleSigners()?archive.signingInfo.getApkContentsSigners():archive.signatures;
  if(signers==null || signers.length!=1 || !UpdateFeed.SIGNER.equals(UpdateDownload.hex(MessageDigest.getInstance("SHA-256").digest(signers[0].toByteArray()))))throw new IOException("signer");
 }
 private static void install(){
  int id=-1;boolean committed=false;
  try{
   UpdateFeed info=candidate;if(info==null || !ready)throw new IOException("not_ready");
   PackageInstaller installer=app.getPackageManager().getPackageInstaller();
   int previous=prefs.getInt("session",-1);if(previous>=0){try{installer.abandonSession(previous);}catch(RuntimeException ignored){}clearSession();}
   File file=downloader.ready();verify(file,info);
   PackageInstaller.SessionParams params=new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
   params.setAppPackageName(app.getPackageName());params.setSize(info.size);
   if(Build.VERSION.SDK_INT>=31)params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED);
   id=installer.createSession(params);String nonce=app.getPackageName()+".LOCAL_UPDATE."+UUID.randomUUID();
   if(!prefs.edit().putInt("session",id).putLong("target_code",info.versionCode).putString("nonce",nonce).remove("confirmation").commit())throw new IOException("storage");
   try(PackageInstaller.Session session=installer.openSession(id)){
    try(InputStream in=new FileInputStream(file);OutputStream out=session.openWrite("base.apk",0,info.size)){byte[] buffer=new byte[65536];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);session.fsync(out);}
    // Explicit, mutable and private: only the installer fills in the status of this session.
    Intent result=new Intent(app,UpdateResultReceiver.class).setAction(nonce);
    PendingIntent sender=PendingIntent.getBroadcast(app,id,result,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0));
    session.commit(sender.getIntentSender());committed=true;
   }
  }catch(Exception e){
   if(!committed && id>=0)try{app.getPackageManager().getPackageInstaller().abandonSession(id);}catch(RuntimeException ignored){}
   if(!committed){clearSession();status="install_failed";}
  }finally{preparing=false;}
 }
 static void result(Context c,Intent result,Runnable finished){
  init(c);
  run(()->{try{
   String expected=prefs.getString("nonce","");
   if(expected.isEmpty() || !expected.equals(result.getAction()))return;
   if(result.getIntExtra(PackageInstaller.EXTRA_SESSION_ID,prefs.getInt("session",-1))!=prefs.getInt("session",-1))return;
   int code=result.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);
   if(code==PackageInstaller.STATUS_PENDING_USER_ACTION){
    Intent confirm=result.getParcelableExtra(Intent.EXTRA_INTENT);
    if(confirm==null || !prefs.edit().putString("confirmation",confirm.toUri(0)).commit()){clearSession();status="install_failed";}else status="confirm";
   }else if(code==PackageInstaller.STATUS_SUCCESS){clearSession();forget();status="installed";}
   else{clearSession();status=code==PackageInstaller.STATUS_FAILURE_ABORTED?"cancelled":"install_failed";}
  }finally{finished.run();}});
 }
 /** Provider entry point; the UI in any process only reads state and requests actions. */
 static Bundle call(Context c,String method,Bundle extras){
  init(c);
  switch(method){
   case "update_state":break;
   case "update_set":
    SharedPreferences.Editor edit=prefs.edit();
    for(String key:new String[]{"automatic_check","automatic_download","wifi_only"})if(extras!=null && extras.containsKey(key))edit.putBoolean(key,extras.getBoolean(key));
    edit.commit();if(automaticJob && (!prefs.getBoolean("automatic_download",true)))cancel=true;schedule();break;
   case "update_check":if(!downloading && !preparing){status="checking";run(AppUpdates::check);}break;
   case "update_download":if(candidate!=null && !ready && !downloading && !preparing){status="downloading";percent=0;run(()->download(true));}break;
   case "update_pause":if(downloading)cancel=true;break;
   case "update_install":
    if(!ready || downloading || preparing || !prefs.getString("confirmation","").isEmpty())break;
    if(!app.getPackageManager().canRequestPackageInstalls()){status="permission";break;}
    preparing=true;status="preparing";run(AppUpdates::install);break;
   case "update_offered":if(candidate!=null)prefs.edit().putInt("offered",candidate.build).remove("permission_wait").apply();break;
   case "update_permission_wait":if(candidate!=null)prefs.edit().putInt("permission_wait",candidate.build).apply();break;
   default:throw new IllegalArgumentException("Unknown update operation");
  }
  return state();
 }
 private static Bundle state(){
  Bundle b=new Bundle();UpdateFeed info=candidate;
  b.putString("version","test."+HeadUnitBuild.BUILD);b.putString("status",status);b.putInt("percent",percent);
  if(info!=null){b.putString("candidate",info.version);b.putLong("size",info.size);b.putString("notes",info.notes);}
  for(String key:new String[]{"automatic_check","automatic_download","wifi_only"})b.putBoolean(key,prefs.getBoolean(key,true));
  b.putBoolean("downloading",downloading);b.putBoolean("preparing",preparing);b.putBoolean("ready",ready);b.putLong("checked",prefs.getLong("checked",0));
  String confirmation=prefs.getString("confirmation","");if(!confirmation.isEmpty())b.putString("confirmation",confirmation);
  boolean quiet=!AutoResumePolicy.busy(HeadUnitRuntime.diagnosticState()) && !noticePending();
  b.putBoolean("offer",info!=null && ready && !downloading && !preparing && (prefs.getInt("permission_wait",-1)==info.build || (prefs.getInt("offered",-1)!=info.build && quiet)));
  return b;
 }
 private static boolean noticePending(){try{return AutomaticDiagnostics.state(app).getBoolean("notice_required");}catch(RuntimeException e){return true;}}
 private static boolean networkAllowed(){
  if(!prefs.getBoolean("wifi_only",true))return true;
  try{ConnectivityManager m=app.getSystemService(ConnectivityManager.class);NetworkCapabilities caps=m.getNetworkCapabilities(m.getActiveNetwork());
   return caps!=null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));}catch(RuntimeException e){return false;}
 }
 private static long installedCode(){try{return app.getPackageManager().getPackageInfo(app.getPackageName(),0).getLongVersionCode();}catch(Exception e){return HeadUnitBuild.VERSION_CODE;}}
 private static void clearSession(){prefs.edit().remove("session").remove("nonce").remove("confirmation").remove("target_code").commit();}
 private static void forget(){candidate=null;ready=false;prefs.edit().remove("candidate").remove("retry_at").remove("rejected").remove("permission_wait").commit();downloader.clear();}
 private static UpdateDownload.Response open(String original,long offset)throws Exception{
  String url=original;
  for(int redirects=0;redirects<=3;redirects++){
   if(!UpdateFeed.downloadOrigin(url))throw new IOException("download_origin");
   HttpsURLConnection c=(HttpsURLConnection)new URL(url).openConnection();
   try{
    c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setUseCaches(false);c.setRequestProperty("Accept-Encoding","identity");
    if(offset>0)c.setRequestProperty("Range","bytes="+offset+"-");
    int code=c.getResponseCode();
    if(Arrays.asList(301,302,303,307,308).contains(code)){String next=c.getHeaderField("Location");if(next==null)throw new IOException("redirect");url=new URL(new URL(url),next).toString();c.disconnect();continue;}
    InputStream in=code==200 || code==206?c.getInputStream():null;
    return new UpdateDownload.Response(code,c.getHeaderField("Content-Range"),in,c::disconnect);
   }catch(Exception e){c.disconnect();throw e;}
  }
  throw new IOException("too_many_redirects");
 }
 private AppUpdates(){}
}
