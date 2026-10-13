package local.spotify.unified;
import android.content.Context;import android.os.Bundle;import android.util.AtomicFile;import android.util.Base64;
import org.json.*;import java.io.*;import java.security.SecureRandom;import java.util.*;import java.util.concurrent.locks.ReentrantLock;

/** All mutations run in the backend. Bodies, keys and tokens are durable before any POST. */
public final class DiagnosticsQueue {
 static final long RETRY_WINDOW=86400000L,CONTENT_RETENTION=7*RETRY_WINDOW;
 private static final Object disk=new Object();private static final ReentrantLock runner=new ReentrantLock();
 private static AtomicFile file(Context c){return new AtomicFile(new File(c.getFilesDir(),"diagnostics-pending-v1.json"));}
 private static JSONArray load(Context c)throws Exception{
  AtomicFile f=file(c);if(!f.getBaseFile().exists() && !new File(f.getBaseFile().getPath()+".bak").exists())return new JSONArray();byte[] bytes=f.readFully();if(bytes.length>2*1024*1024)throw new IOException("Queue unavailable");JSONArray rows=new JSONObject(new String(bytes,"UTF-8")).getJSONArray("reports");if(rows.length()>3)throw new IOException("Queue unavailable");return rows;
 }
 private static void save(Context c,JSONArray rows)throws Exception{AtomicFile f=file(c);FileOutputStream out=null;try{out=f.startWrite();out.write(new JSONObject().put("reports",rows).toString().getBytes("UTF-8"));f.finishWrite(out);}catch(Exception e){if(out!=null)f.failWrite(out);throw e;}}
 private static JSONObject find(JSONArray rows,String key)throws JSONException{for(int i=0;i<rows.length();i++)if(key.equals(rows.getJSONObject(i).optString("key")))return rows.getJSONObject(i);return null;}
 private static JSONArray without(JSONArray rows,String key)throws JSONException{JSONArray next=new JSONArray();for(int i=0;i<rows.length();i++)if(!key.equals(rows.getJSONObject(i).optString("key")))next.put(rows.get(i));return next;}
 private static boolean purgeContent(JSONArray rows,long now)throws JSONException{boolean changed=false;for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);if(now-r.optLong("created")>=CONTENT_RETENTION && !r.optString("body").isEmpty()){r.put("body","");changed=true;}}return changed;}
 static boolean terminal(String state){return Arrays.asList("submitted","failed","cancelled","cancel_uncertain","expired").contains(state);}
 public static String approve(Context c,String body,boolean publicConsent)throws Exception{return approve(c,body,publicConsent,System.currentTimeMillis());}
 static String approve(Context c,String body,boolean consent,long now)throws Exception{
  if(!consent)throw new SecurityException("Public-report consent required");JSONObject validated=DiagnosticSchema.report(body);if(validated.getInt("schema_version")==2 && !"manual".equals(validated.getString("report_source")))throw new SecurityException("Manual source required");
  synchronized(disk){JSONArray rows=load(c);if(rows.length()>=3)throw new IllegalStateException("Queue full: remove a report first");byte[] random=new byte[32];new SecureRandom().nextBytes(random);String key=UUID.randomUUID().toString();
   JSONObject row=new JSONObject().put("key",key).put("token",Base64.encodeToString(random,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING)).put("body",body).put("origin","manual").put("created",now).put("next",now).put("state","pending").put("receipt","").put("issue","").put("error","").put("attempts",0).put("attempted",false).put("cancel",false);
   rows.put(row);save(c,rows);return key;
  }
 }
 static String automatic(Context c,String stage,String code,long wall,long elapsed)throws Exception{
  synchronized(disk){JSONArray rows=load(c),keep=new JSONArray();for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);if("automatic".equals(r.optString("origin"))){if(!terminal(r.optString("state")))return null;}else keep.put(r);}if(keep.length()>=3 || !AutomaticDiagnostics.canTransmit(c))return null;
   String body=DiagnosticReport.automatic(c,stage,code);if(!AutomaticDiagnostics.reserve(c,stage,code,wall,elapsed))return null;
   byte[] random=new byte[32];new SecureRandom().nextBytes(random);String key=UUID.randomUUID().toString();keep.put(new JSONObject().put("key",key).put("token",Base64.encodeToString(random,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING)).put("body",body).put("origin","automatic").put("created",wall).put("next",wall).put("state","pending").put("receipt","").put("issue","").put("error","").put("attempts",0).put("attempted",false).put("cancel",false));save(c,keep);return key;
  }
 }
 public static Bundle summary(Context c)throws Exception{
  synchronized(disk){JSONArray rows=load(c),safe=new JSONArray();long now=System.currentTimeMillis();boolean changed=false;
   for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);long age=now-r.optLong("created");if((age<0 || age>RETRY_WINDOW) && !terminal(r.optString("state"))){r.put("state","expired").put("error","retry_window_expired");changed=true;}if(age>CONTENT_RETENTION && !r.optString("body").isEmpty()){r.put("body","");changed=true;}
    safe.put(new JSONObject().put("id",r.getString("key")).put("source",r.optString("origin","manual")).put("status",r.optBoolean("cancel") && !terminal(r.getString("state"))?"cancelling":r.getString("state")).put("issue_url",DiagnosticsConfig.issueUrl(r.optString("issue"))?r.optString("issue"):"").put("error",r.optString("error")).put("can_cancel",!r.optBoolean("cancel") && !terminal(r.optString("state")) && !Arrays.asList("delivering","delivery_uncertain").contains(r.optString("state"))).put("can_remove",terminal(r.optString("state"))).put("can_export",!r.optString("body").isEmpty()));
   }if(changed)save(c,rows);Bundle b=AutomaticDiagnostics.state(c);b.putString("reports",safe.toString());b.putString("endpoint",DiagnosticsConfig.BASE_URL);b.putInt("api_version",1);b.putInt("schema_version",DiagnosticsConfig.SCHEMA_VERSION);return b;
  }
 }
 public static String body(Context c,String key)throws Exception{synchronized(disk){JSONArray rows=load(c);if(purgeContent(rows,System.currentTimeMillis()))save(c,rows);JSONObject r=find(rows,key);if(r==null || r.optString("body").isEmpty())throw new IOException("Report unavailable");return r.getString("body");}}
 public static void cancel(Context c,String key)throws Exception{
  synchronized(disk){JSONArray rows=load(c);JSONObject r=find(rows,key);if(r==null)return;if(terminal(r.getString("state")))return;
   if(!r.optBoolean("attempted") && r.optString("receipt").isEmpty()){save(c,without(rows,key));return;}
   r.put("cancel",true).put("next",System.currentTimeMillis());save(c,rows);
  }DiagnosticUploadJob.schedule(c);
 }
 public static void remove(Context c,String key)throws Exception{synchronized(disk){JSONArray rows=load(c);JSONObject r=find(rows,key);if(r!=null && !terminal(r.getString("state")))throw new IllegalStateException("Cancel pending report first");save(c,without(rows,key));}}
 public static void cancelAutomatic(Context c){try{synchronized(disk){JSONArray rows=load(c),next=new JSONArray();for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);if("automatic".equals(r.optString("origin"))){if(!r.optBoolean("attempted") && r.optString("receipt").isEmpty())continue;r.put("body","").put("cancel",true).put("next",System.currentTimeMillis());}next.put(r);}save(c,next);}DiagnosticUploadJob.schedule(c);}catch(Exception ignored){}}
 static long next(Context c){try{synchronized(disk){long due=Long.MAX_VALUE;JSONArray rows=load(c);for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);if(!terminal(r.optString("state")))due=Math.min(due,r.optLong("next"));}return due;}}catch(Exception ignored){return Long.MAX_VALUE;}}
 static long maintenance(Context c){try{synchronized(disk){long due=next(c);JSONArray rows=load(c);for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);if(!r.optString("body").isEmpty())due=Math.min(due,r.optLong("created")+CONTENT_RETENTION);}return due;}}catch(Exception ignored){return Long.MAX_VALUE;}}
 public static void process(Context c,DiagnosticTransport transport){process(c,transport,System.currentTimeMillis());}
 static void process(Context c,DiagnosticTransport transport,long now){process(c,transport,now,true);}
 static void process(Context c,DiagnosticTransport transport,long now,boolean schedule){
  if(!runner.tryLock())return;String key="";
  try{JSONObject work=null;
   synchronized(disk){JSONArray rows=load(c);if(purgeContent(rows,now))save(c,rows);for(int pass=0;pass<2 && work==null;pass++)for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);long age=now-r.optLong("created");if(!terminal(r.optString("state")) && (r.optLong("next")<=now || age<0 || age>RETRY_WINDOW) && (pass==1 || r.optBoolean("cancel"))){work=r;break;}}
    if(work==null)return;key=work.getString("key");long age=now-work.getLong("created");
    if(age<0 || age>RETRY_WINDOW){work.put("state","expired").put("error","retry_window_expired");save(c,rows);return;}
    if(work.optBoolean("cancel") && work.optString("receipt").isEmpty()){if(!work.optBoolean("attempted"))save(c,without(rows,key));else{work.put("state","cancel_uncertain").put("error","acceptance_unknown");save(c,rows);}return;}
    if("automatic".equals(work.optString("origin")) && !AutomaticDiagnostics.canTransmit(c)){work.put("cancel",true);if(work.optString("receipt").isEmpty()){work.put("state",work.optBoolean("attempted")?"cancel_uncertain":"cancelled");save(c,rows);return;}}
    work.put("attempts",work.optInt("attempts")+1);save(c,rows);work=new JSONObject(work.toString());
   }
   String receipt=work.getString("receipt"),method=receipt.isEmpty()?"POST":work.optBoolean("cancel")?"DELETE":"GET";
   if("POST".equals(method) && "automatic".equals(work.optString("origin"))){DiagnosticTransport.Reply caps=transport.capabilities();if(!AutomaticDiagnostics.capability(caps)){synchronized(disk){JSONArray rows=load(c);JSONObject r=find(rows,key);if(r!=null){retry(r,now,Math.max(60000,caps.retryAfterMs));r.put("error","automatic_support_unavailable");save(c,rows);}}return;}}
   if("POST".equals(method)){synchronized(disk){JSONArray rows=load(c);JSONObject r=find(rows,key);if(r==null)return;if(r.optBoolean("cancel") || ("automatic".equals(r.optString("origin")) && !AutomaticDiagnostics.canTransmit(c))){if(!r.optBoolean("attempted"))save(c,without(rows,key));else{r.put("state","cancel_uncertain").put("error","acceptance_unknown");save(c,rows);}return;}r.put("attempted",true);save(c,rows);}}
   DiagnosticTransport.Reply reply=transport.call(method,receipt,key,work.getString("token"),work.getString("body"));
   synchronized(disk){JSONArray rows=load(c);JSONObject r=find(rows,key);if(r==null)return;apply(r,method,reply,now);save(c,rows);}
  }catch(Exception failure){try{synchronized(disk){JSONArray rows=load(c);JSONObject r=find(rows,key);if(r!=null){retry(r,now,0);save(c,rows);}}}catch(Exception ignored){}}
  finally{runner.unlock();if(schedule)DiagnosticUploadJob.schedule(c);}
 }
 private static void retry(JSONObject row,long now,long minimum)throws JSONException{
  long[] delay={10000,30000,120000,600000,3600000};long backoff=delay[Math.min(delay.length-1,Math.max(0,row.optInt("attempts")-1))];long jitter=(long)(backoff*Math.random()*0.25);
  row.put("next",now+Math.max(minimum,backoff+jitter)).put("error","waiting_to_retry");
 }
 private static void apply(JSONObject r,String method,DiagnosticTransport.Reply reply,long now)throws Exception{
  int http=reply.http;JSONObject json=reply.json;
  if(http==429 || http>=500){retry(r,now,reply.retryAfterMs);return;}
  if("DELETE".equals(method) && http==409){r.put("cancel",false).put("state","delivering").put("error","delivery_cannot_be_recalled").put("next",now+10000);return;}
  if(http==400 || http==404 || http==409 || http==413 || http==415 || http==422 || (http!=200 && http!=202)){r.put("state","failed").put("error",http==409?"report_conflict":http==404?"report_or_project_unavailable":"request_rejected");return;}
  String receipt=json.optString("report_id"),state=json.optString("status");
  if(!DiagnosticsConfig.uuid(receipt) || (!r.optString("receipt").isEmpty() && !receipt.equals(r.optString("receipt"))) || !Arrays.asList("queued","delivering","submitted","delivery_uncertain","failed","cancelled").contains(state)){r.put("state","failed").put("error","invalid_server_response");return;}
  r.put("receipt",receipt).put("state",state).put("error","");
  if("submitted".equals(state)){String url=json.optString("issue_url");if(!DiagnosticsConfig.issueUrl(url)){r.put("state","failed").put("error","invalid_issue_link");return;}r.put("issue",url);}
  long hint=Math.max(10,Math.min(3600,json.optInt("poll_after_seconds",10)))*1000L;
  r.put("next",now+Math.max(hint,reply.retryAfterMs));if(r.optBoolean("cancel") && "queued".equals(state))r.put("next",now);
 }
}
