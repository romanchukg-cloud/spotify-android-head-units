package local.spotify.unified;
import org.json.*;import java.util.*;
public final class DiagnosticSchema {
 public static final int MAX_BODY=262144,MAX_EVENTS=500;
 static final Set<String> CATEGORIES=set("playback","cluster_media","wheel_buttons","authentication","other");
 static final Set<String> STAGES=set("wheel","playback","auth","upload");
 static final Set<String> CODES=set("unavailable","permission_denied","schema_mismatch","timeout","connection_lost","internal_error");
 static Set<String> set(String... values){return new HashSet<>(Arrays.asList(values));}
 static void keys(JSONObject o,String... expected)throws JSONException{if(o.length()!=expected.length)throw new JSONException("Unexpected fields");for(String k:expected)if(!o.has(k))throw new JSONException("Missing field");}
 static void text(JSONObject o,String key,int max)throws JSONException{Object v=o.get(key);if(!(v instanceof String) || ((String)v).isEmpty() || ((String)v).length()>max)throw new JSONException("Invalid string");}
 static int number(JSONObject o,String key,int min,int max)throws JSONException{Object v=o.get(key);if(!(v instanceof Integer) && !(v instanceof Long))throw new JSONException("Invalid integer");long n=((Number)v).longValue();if(n<min || n>max)throw new JSONException("Invalid range");return (int)n;}
 static void bool(JSONObject o,String key)throws JSONException{if(!(o.get(key) instanceof Boolean))throw new JSONException("Invalid Boolean");}
 static void choice(JSONObject o,String key,Set<String> values)throws JSONException{text(o,key,80);if(!values.contains(o.getString(key)))throw new JSONException("Invalid enum");}
 static void eventData(String type,JSONObject d)throws JSONException{
  switch(type){
   case "lifecycle":keys(d,"state");choice(d,"state",set("started","stopped"));break;
   case "playback":keys(d,"state","bridge_connected");choice(d,"state",set("playing","paused","stopped","unknown"));bool(d,"bridge_connected");break;
   case "audio_focus":keys(d,"state");choice(d,"state",set("gained","lost","ducked","unknown"));break;
   case "wheel_key":keys(d,"action","key_code","scan_code","consumed");choice(d,"action",set("down","up"));number(d,"key_code",0,65535);number(d,"scan_code",0,65535);bool(d,"consumed");break;
   case "failure":keys(d,"stage","code");choice(d,"stage",STAGES);choice(d,"code",CODES);break;
   default:throw new JSONException("Event is not in playback profile");
  }
 }
 static void record(JSONObject r)throws JSONException{keys(r,"time_ms","uptime_ms","type","data");for(String k:new String[]{"time_ms","uptime_ms"}){Object v=r.get(k);if((!(v instanceof Integer) && !(v instanceof Long)) || ((Number)v).longValue()<0)throw new JSONException("Missing clock");}eventData(r.getString("type"),r.getJSONObject("data"));}
 public static JSONObject report(String body)throws JSONException{
  if(body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>MAX_BODY)throw new JSONException("Report too large");JSONObject r=new JSONObject(body);
  int version=number(r,"schema_version",1,2);Set<String> top=set("schema_version","project_id","consent","app","device","problem","settings","components","capture","events");if(r.has("vehicle"))top.add("vehicle");if(version==2)top.add("report_source");keys(r,top.toArray(new String[0]));
  if(version==2)choice(r,"report_source",set("manual","automatic"));if(!DiagnosticsConfig.PROJECT_ID.equals(r.get("project_id")) || !"public_issue".equals(r.get("consent")))throw new JSONException("Wrong project/consent");
  JSONObject a=r.getJSONObject("app");keys(a,"version","build");text(a,"version",80);text(a,"build",80);
  JSONObject d=r.getJSONObject("device");keys(d,"android_api","android_version","manufacturer","model","firmware");number(d,"android_api",1,100);for(String k:new String[]{"android_version","manufacturer","model","firmware"})text(d,k,160);
  if(r.has("vehicle")){JSONObject v=r.getJSONObject("vehicle");keys(v,"car_model","dilink_version");text(v,"car_model",80);text(v,"dilink_version",40);}
  JSONObject p=r.getJSONObject("problem");keys(p,"category");choice(p,"category",CATEGORIES);
  JSONObject s=r.getJSONObject("settings");Iterator<String> it=s.keys();while(it.hasNext()){String k=it.next();if(!set("bridge_enabled","cluster_media_enabled").contains(k))throw new JSONException("Wrong profile setting");bool(s,k);}
  if(r.getJSONArray("components").length()!=0)throw new JSONException("Components are not registered");
  JSONObject capture=r.getJSONObject("capture");keys(capture,"duration_seconds","truncated");int duration=number(capture,"duration_seconds",0,600);bool(capture,"truncated");
  JSONArray e=r.getJSONArray("events");if(e.length()>MAX_EVENTS)throw new JSONException("Too many events");boolean failure=false;for(int i=0;i<e.length();i++){JSONObject row=e.getJSONObject(i);keys(row,"elapsed_ms","type","data");number(row,"elapsed_ms",0,duration*1000);eventData(row.getString("type"),row.getJSONObject("data"));if("failure".equals(row.getString("type")) && !"upload".equals(row.getJSONObject("data").optString("stage")))failure=true;}if(version==2 && "automatic".equals(r.getString("report_source")) && !failure)throw new JSONException("Automatic report requires an integration failure");return r;
 }
 static String safe(String v,int max){if(v==null || v.trim().isEmpty())return "unknown";v=v.replaceAll("[\\p{Cntrl}]"," ").replace('@','＠').replace('`','\'').replaceAll("(?i)(?:https?://|www\\.)\\S+","[redacted]");return v.length()>max?v.substring(0,max):v;}
}
