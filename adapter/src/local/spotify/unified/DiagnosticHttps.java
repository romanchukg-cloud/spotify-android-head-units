package local.spotify.unified;
import javax.net.ssl.HttpsURLConnection;import java.net.URL;import java.io.*;import org.json.*;import java.text.*;import java.util.*;
/** Fixed HTTPS origin, platform certificate validation, no redirects or credentials beyond a per-report token. */
public final class DiagnosticHttps implements DiagnosticTransport {
 private volatile HttpsURLConnection active;
 public Reply call(String method,String receipt,String key,String token,String body)throws Exception{
  if(!Arrays.asList("POST","GET","DELETE").contains(method) || (!"POST".equals(method) && !DiagnosticsConfig.uuid(receipt)))throw new IOException("Invalid request");
  return request(method,"/v1/reports"+("POST".equals(method)?"":"/"+receipt),key,token,body);
 }
 public Reply capabilities()throws Exception{return request("GET","/","","","");}
 private Reply request(String method,String path,String key,String token,String body)throws Exception{
  HttpsURLConnection c=(HttpsURLConnection)new URL(DiagnosticsConfig.BASE_URL+path).openConnection();active=c;
  try{c.setInstanceFollowRedirects(false);c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setRequestMethod(method);c.setRequestProperty("Accept","application/json");if(!token.isEmpty())c.setRequestProperty("X-Report-Token",token);c.setRequestProperty("Cache-Control","no-store");
   if("POST".equals(method)){DiagnosticSchema.report(body);byte[] bytes=body.getBytes("UTF-8");c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Idempotency-Key",key);c.setDoOutput(true);c.setFixedLengthStreamingMode(bytes.length);try(OutputStream out=c.getOutputStream()){out.write(bytes);}}
   int status=c.getResponseCode();long retry=retryAfter(c.getHeaderField("Retry-After"),System.currentTimeMillis());InputStream stream=status>=400?c.getErrorStream():c.getInputStream();ByteArrayOutputStream bytes=new ByteArrayOutputStream();
   if(stream!=null)try(InputStream in=stream){byte[] buf=new byte[2048];int n;while((n=in.read(buf))!=-1){if(bytes.size()+n>16384)throw new IOException("Response too large");bytes.write(buf,0,n);}}
   JSONObject json;try{json=new JSONObject(bytes.toString("UTF-8"));}catch(JSONException bad){json=new JSONObject();}return new Reply(status,json,retry);
  }finally{active=null;c.disconnect();}
 }
 public void cancel(){HttpsURLConnection c=active;if(c!=null)c.disconnect();}
 static long retryAfter(String value,long now){if(value==null)return 0;try{if(value.trim().matches("[0-9]+")){long seconds=Long.parseLong(value.trim());return seconds>86400?86400000:seconds*1000;}SimpleDateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);format.setLenient(false);return Math.min(86400000,Math.max(0,format.parse(value).getTime()-now));}catch(Exception invalid){return 0;}}
}
