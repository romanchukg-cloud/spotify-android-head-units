package local.spotify.unified;
import android.app.Application;import android.content.Context;import android.os.*;import android.util.Log;
import org.json.*;import java.io.*;import java.nio.channels.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicInteger;
/** Shared playback event allowlist only. Four process slots, 1 MiB total. */
public final class DiagnosticLog {
 static final int MAX_BYTES=64*1024,MAX_PENDING=128;static final long WINDOW_MS=600000;
 private static volatile Handler writer;private static File current;private static Context context;
 private static final Object disk=new Object();private static final AtomicInteger pending=new AtomicInteger();private static volatile boolean truncated;
 static File directory(Context c){return new File(c.getFilesDir(),"headunit-diagnostics");}
 static boolean logName(String n){return n.matches("(main|settings|profiles|bydui)\\.jsonl(\\.1)?");}
 public static synchronized void init(Context c){
  if(writer!=null)return;context=c.getApplicationContext();File dir=directory(context);dir.mkdirs();
  String n=Application.getProcessName();String slot=n!=null && n.endsWith(":settings")?"settings":n!=null && n.endsWith(":profiles")?"profiles":n!=null && n.endsWith(":bydui")?"bydui":"main";
  current=new File(dir,slot+".jsonl");HandlerThread t=new HandlerThread("SpotifyDiagnostics");t.start();writer=new Handler(t.getLooper());
  writer.post(()->pruneAll(context));lifecycle("started");focus("unknown");
  writer.postDelayed(new Runnable(){public void run(){pruneAll(context);writer.postDelayed(this,60000);}},60000);
 }
 public static void lifecycle(String s){try{record("lifecycle",new JSONObject().put("state",s));}catch(JSONException ignored){}}
 public static void playback(int state,boolean connected){try{record("playback",new JSONObject().put("state",state==3?"playing":state==2?"paused":state==1?"stopped":"unknown").put("bridge_connected",connected));}catch(JSONException ignored){}}
 public static void focus(String s){try{record("audio_focus",new JSONObject().put("state",s));}catch(JSONException ignored){}}
 public static void wheel(int action,int key,int scan,boolean consumed){if(action!=0 && action!=1)return;try{record("wheel_key",new JSONObject().put("action",action==0?"down":"up").put("key_code",key).put("scan_code",scan).put("consumed",consumed));}catch(JSONException ignored){}}
 public static void failure(String stage,Throwable problem){
  Throwable cause=problem;for(int i=0;i<6 && cause.getCause()!=null && cause.getCause()!=cause;i++)cause=cause.getCause();
  String code=cause instanceof SecurityException?"permission_denied":cause instanceof java.util.concurrent.TimeoutException?"timeout":cause instanceof android.os.RemoteException?"connection_lost":cause instanceof ReflectiveOperationException || cause instanceof LinkageError || cause instanceof IllegalStateException?"unavailable":"internal_error";
  failureCode(stage,code);
 }
 public static void failureCode(String stage,String code){try{record("failure",new JSONObject().put("stage",stage).put("code",code));}catch(JSONException ignored){}}
 static boolean record(String type,JSONObject data){
  Handler h=writer;if(h==null)return false;try{DiagnosticSchema.eventData(type,data);}catch(JSONException invalid){return false;}
  if(pending.incrementAndGet()>MAX_PENDING){pending.decrementAndGet();truncated=true;return false;}
  try{JSONObject r=new JSONObject().put("time_ms",System.currentTimeMillis()).put("uptime_ms",SystemClock.elapsedRealtime()).put("type",type).put("data",new JSONObject(data.toString()));
   if(!h.post(()->{try{append(r.toString());if("failure".equals(type))AutomaticDiagnostics.observe(context,data.optString("stage"),data.optString("code"));}finally{pending.decrementAndGet();}})){pending.decrementAndGet();return false;}return true;
  }catch(JSONException|RuntimeException e){pending.decrementAndGet();return false;}
 }
 private static final class SlotLock implements AutoCloseable {
  final RandomAccessFile file;final FileLock lock;
  SlotLock(File log)throws IOException{file=new RandomAccessFile(new File(log.getParentFile(),log.getName().split("\\.")[0]+".lock"),"rw");FileLock held=null;try{held=file.getChannel().tryLock();}catch(OverlappingFileLockException busy){}catch(IOException e){file.close();throw e;}lock=held;}
  public void close()throws IOException{try{if(lock!=null)lock.release();}finally{file.close();}}
 }
 static void append(String text){synchronized(disk){try(SlotLock held=new SlotLock(current)){
  if(held.lock==null){truncated=true;return;}
  JSONObject r=new JSONObject(text);DiagnosticSchema.record(r);byte[] bytes=(r+"\n").getBytes("UTF-8");if(bytes.length>MAX_BYTES)return;
  if(current.length()+bytes.length>MAX_BYTES){File old=new File(current.getPath()+".1");if(old.exists() && !old.delete())throw new IOException();if(current.exists() && !current.renameTo(old))throw new IOException();truncated=true;}
  try(FileOutputStream out=new FileOutputStream(current,true)){out.write(bytes);}
 }catch(Exception e){Log.w("SpotifyDiagnostics","Private event write unavailable");}}}
 static boolean recent(JSONObject r,long wall,long elapsed){try{DiagnosticSchema.record(r);long age=wall-r.getLong("time_ms"),up=elapsed-r.getLong("uptime_ms");return age>=0 && age<=WINDOW_MS && up>=0 && up<=WINDOW_MS;}catch(Exception invalid){return false;}}
 static void pruneAll(Context c){synchronized(disk){long wall=System.currentTimeMillis(),elapsed=SystemClock.elapsedRealtime();for(String slot:new String[]{"main","settings","profiles","bydui"}){
  File log=new File(directory(c),slot+".jsonl");try(SlotLock held=new SlotLock(log)){if(held.lock==null)continue;
   for(File f:new File[]{log,new File(log.getPath()+".1")}){new File(f.getPath()+".tmp").delete();if(!f.isFile())continue;
    ByteArrayOutputStream bytes=new ByteArrayOutputStream();boolean changed=f.length()>MAX_BYTES;
    if(!changed)try(BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"))){String s;while((s=in.readLine())!=null)try{JSONObject r=new JSONObject(s);if(recent(r,wall,elapsed))bytes.write((r+"\n").getBytes("UTF-8"));else changed=true;}catch(JSONException invalid){changed=true;}}
    if(!changed)continue;if(bytes.size()==0){f.delete();continue;}
    File tmp=new File(f.getPath()+".tmp");try(FileOutputStream out=new FileOutputStream(tmp)){bytes.writeTo(out);}if(!tmp.renameTo(f))tmp.delete();
   }
  }catch(IOException ignored){}
 }}}
 static boolean flush(){Handler h=writer;if(h==null || Looper.myLooper()==h.getLooper())return true;CountDownLatch done=new CountDownLatch(1);h.post(done::countDown);try{return done.await(3,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}}
 static List<JSONObject> snapshot(Context c){flush();pruneAll(c);List<JSONObject> rows=new ArrayList<>();File[] files=directory(c).listFiles();long wall=System.currentTimeMillis(),elapsed=SystemClock.elapsedRealtime();
  if(files!=null)for(File f:files){if(!logName(f.getName()) || f.length()>MAX_BYTES)continue;try(BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"))){String s;while((s=in.readLine())!=null)try{JSONObject r=new JSONObject(s);if(recent(r,wall,elapsed))rows.add(r);}catch(JSONException invalid){}}catch(IOException ignored){}}
  rows.sort((a,b)->Long.compare(a.optLong("uptime_ms"),b.optLong("uptime_ms")));return rows;
 }
 static boolean wasTruncated(){return truncated;}
}
