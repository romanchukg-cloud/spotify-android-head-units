package local.spotify.unified;
import java.net.URI;import java.util.*;import org.json.*;
/** Strict schema-1 record of the shared update server for this project; the APK itself is verified separately. */
final class UpdateFeed {
 static final String URL="https://diagnostics.localcharge.app/v1/updates/spotify-android-head-units";
 static final String PROJECT="spotify-android-head-units",PACKAGE="com.spotify.music";
 /** Release signing certificate (SHA-256 of the DER certificate), the same as every published test build. */
 static final String SIGNER="a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1";
 static final long CODE_BASE=95477,MAX_SIZE=512L*1024*1024;static final int MIN_SDK=28;
 private static final String RELEASES="https://github.com/romanchukg-cloud/spotify-android-head-units/releases/";
 private static final Set<String> KEYS=new HashSet<>(Arrays.asList("schema_version","project_id","build","version","version_code","package_name","apk_url","sha256","size","notes","notes_url","min_sdk"));
 final int build;final long versionCode,size;final String version,url,sha,notes,notesUrl;final JSONObject json;
 UpdateFeed(JSONObject value)throws Exception{
  Set<String> actual=new HashSet<>();Iterator<String> it=value.keys();while(it.hasNext())actual.add(it.next());
  if(!actual.equals(KEYS) || number(value,"schema_version")!=1 || !PROJECT.equals(value.get("project_id")) || !PACKAGE.equals(value.get("package_name")))throw new IllegalArgumentException("Invalid update feed");
  long sequence=number(value,"build");if(sequence<1 || sequence>1000000)throw new IllegalArgumentException("build");build=(int)sequence;
  version=string(value,"version");versionCode=number(value,"version_code");size=number(value,"size");
  if(!version.equals("test."+build) || versionCode!=CODE_BASE+build || size<1 || size>MAX_SIZE || number(value,"min_sdk")!=MIN_SDK)throw new IllegalArgumentException("identity");
  url=string(value,"apk_url");sha=string(value,"sha256");notes=string(value,"notes");notesUrl=string(value,"notes_url");
  String tag="v5.5.0-headunit-"+version;
  if(!url.equals(RELEASES+"download/"+tag+"/Spotify-5.5.0-HeadUnit-"+version+".apk") || !notesUrl.equals(RELEASES+"tag/"+tag) || !sha.matches("[0-9a-f]{64}") || notes.length()>2000)throw new IllegalArgumentException("release");
  json=new JSONObject(value.toString());
 }
 private static long number(JSONObject value,String key)throws Exception{Object n=value.get(key);if(!(n instanceof Integer) && !(n instanceof Long))throw new IllegalArgumentException(key);return ((Number)n).longValue();}
 private static String string(JSONObject value,String key)throws Exception{Object s=value.get(key);if(!(s instanceof String))throw new IllegalArgumentException(key);return (String)s;}
 /** Newer than the running build and the installed package; both numbers only ever increase. */
 boolean newer(long installedCode){return build>HeadUnitBuild.BUILD && versionCode>installedCode && versionCode>HeadUnitBuild.VERSION_CODE;}
 boolean same(UpdateFeed other){return other!=null && other.build==build && other.size==size && other.sha.equals(sha);}
 /** GitHub serves release assets through redirects to its own storage hosts; nothing else is followed. */
 static boolean downloadOrigin(String url){
  try{URI u=new URI(url);return "https".equals(u.getScheme()) && u.getRawUserInfo()==null && (u.getPort()==-1 || u.getPort()==443) && u.getFragment()==null
   && Arrays.asList("github.com","objects.githubusercontent.com","release-assets.githubusercontent.com").contains(u.getHost());}catch(Exception e){return false;}
 }
}
