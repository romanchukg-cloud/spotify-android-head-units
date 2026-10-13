package local.spotify.unified;
import android.content.Context;import android.content.pm.PackageInfo;import android.net.Uri;import android.os.*;
import org.json.*;import java.io.*;import java.util.*;import java.util.zip.*;
/** A frozen allowlisted JSON preview; export never includes private log files or queue tokens. */
public final class DiagnosticReport {
 static final long EXPIRY_MS=86400000L;
 static File directory(Context c){return new File(c.getCacheDir(),"diagnostic-reports");}
 public static String preview(Context c,String category)throws Exception{
  if(!DiagnosticSchema.CATEGORIES.contains(category))throw new JSONException("Invalid category");
  DiagnosticLog.init(c);DiagnosticLog.playback(HeadUnitRuntime.diagnosticState(),HeadUnitPlaybackService.connected());
  List<JSONObject> rows=DiagnosticLog.snapshot(c);long end=SystemClock.elapsedRealtime(),start=Math.max(0,end-DiagnosticLog.WINDOW_MS);
  JSONArray events=new JSONArray();boolean shortened=DiagnosticLog.wasTruncated();List<JSONObject> selected=new ArrayList<>();
  for(JSONObject r:rows){long t=r.getLong("uptime_ms");if(t>=start && t<=end)selected.add(new JSONObject().put("elapsed_ms",t-start).put("type",r.getString("type")).put("data",r.getJSONObject("data")));}
  if(selected.size()>DiagnosticSchema.MAX_EVENTS){selected=selected.subList(selected.size()-DiagnosticSchema.MAX_EVENTS,selected.size());shortened=true;}
  for(JSONObject e:selected)events.put(e);
  String version="unknown";try{PackageInfo p=c.getPackageManager().getPackageInfo(c.getPackageName(),0);version=p.versionName+" ("+p.getLongVersionCode()+")";}catch(Exception ignored){}
  JSONObject report=new JSONObject().put("schema_version",DiagnosticsConfig.SCHEMA_VERSION).put("report_source","manual").put("project_id",DiagnosticsConfig.PROJECT_ID).put("consent","public_issue");
  report.put("app",new JSONObject().put("version",DiagnosticSchema.safe(version,80)).put("build",HeadUnitBuild.LABEL));
  JSONObject device=new JSONObject().put("android_api",Build.VERSION.SDK_INT).put("android_version",DiagnosticSchema.safe(Build.VERSION.RELEASE,160)).put("manufacturer",DiagnosticSchema.safe(Build.MANUFACTURER,160)).put("model",DiagnosticSchema.safe(Build.MODEL,160)).put("firmware",DiagnosticSchema.safe(Build.DISPLAY,160));
  report.put("device",device).put("problem",new JSONObject().put("category",category)).put("settings",new JSONObject()).put("components",new JSONArray());
  report.put("capture",new JSONObject().put("duration_seconds",(int)Math.min(600,(end-start+999)/1000)).put("truncated",shortened)).put("events",events);
  while(report.toString().getBytes("UTF-8").length>DiagnosticSchema.MAX_BODY && events.length()>0){JSONArray tail=new JSONArray();for(int i=1;i<events.length();i++)tail.put(events.get(i));events=tail;report.put("events",events);report.getJSONObject("capture").put("truncated",true);}
  String body=report.toString();DiagnosticSchema.report(body);return body;
 }
 static String automatic(Context c,String stage,String code)throws Exception{
  AutomaticDiagnostics.fingerprint(stage,code);String category="wheel".equals(stage)?"wheel_buttons":"auth".equals(stage)?"authentication":"adapter".equals(stage)?"other":"playback";
  JSONObject r=new JSONObject(preview(c,category));r.put("report_source","automatic");JSONArray events=r.getJSONArray("events");if(events.length()>=DiagnosticSchema.MAX_EVENTS){events.remove(0);r.getJSONObject("capture").put("truncated",true);}
  events.put(new JSONObject().put("elapsed_ms",r.getJSONObject("capture").getInt("duration_seconds")*1000).put("type","failure").put("data",new JSONObject().put("stage",stage).put("code",code)));String body=r.toString();DiagnosticSchema.report(body);return body;
 }
 public static synchronized Bundle create(Context c,Bundle input)throws Exception{
  String body=input==null?null:input.getString("body");if(body==null)body=preview(c,input==null?"other":input.getString("category","other"));DiagnosticSchema.report(body);
  String id=UUID.randomUUID().toString();File dir=directory(c);if(!dir.isDirectory() && !dir.mkdirs())throw new IOException("Report storage unavailable");purge(dir);
  File target=new File(dir,id+".zip"),temp=new File(dir,id+".tmp");try{
   try(ZipOutputStream out=new ZipOutputStream(new FileOutputStream(temp))){add(out,"report.json",body.getBytes("UTF-8"));add(out,"README.txt",("Spotify diagnostics export. This file has NOT been uploaded by exporting it.\n"+DiagnosticsConfig.DISCLOSURE+"\nThe shared API accepts JSON, not this ZIP. A successful integration call does not prove visible lighting or cluster output.\n").getBytes("UTF-8"));}
   if(!temp.renameTo(target))throw new IOException("Cannot finalize report");
  }finally{if(temp.exists())temp.delete();}
  Bundle result=new Bundle();result.putString("report_id",id);result.putString("uri",new Uri.Builder().scheme("content").authority(c.getPackageName()+".local.diagnostics").appendPath("report").appendPath(target.getName()).build().toString());result.putLong("size",target.length());return result;
 }
 private static void add(ZipOutputStream out,String name,byte[] data)throws IOException{out.putNextEntry(new ZipEntry(name));out.write(data);out.closeEntry();}
 private static void purge(File dir){File[] files=dir.listFiles();if(files==null)return;ArrayList<File> reports=new ArrayList<>();long now=System.currentTimeMillis();
  for(File f:files)if(f.getName().matches("[0-9a-f-]{36}\\.(zip|tmp)")){if(now-f.lastModified()>EXPIRY_MS || f.getName().endsWith(".tmp"))f.delete();else reports.add(f);}
  reports.sort((a,b)->Long.compare(b.lastModified(),a.lastModified()));for(int i=3;i<reports.size();i++)reports.get(i).delete();
 }
}
