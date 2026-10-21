package local.spotify.unified;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import java.util.*;
import java.util.regex.*;
import org.json.JSONObject;
/**
 * Spotify links other apps open (VIEW on the launcher alias): spotify: URIs, open.spotify.com and
 * the short spotify.link. Songs and episodes play; playlists, albums, artists and shows open their
 * page, with the title and cover from Spotify's public oEmbed (open.spotify.com/oembed: the
 * backend's browser gives no single item), which also turns a short link into the item;
 * spotify:search:… and open.spotify.com/search/… search.
 */
final class HuLinks {
 /** What a link opens: a Spotify URI and its kind, or a search; title and cover when known. */
 static final class Link {
  final String uri,kind,query,title,icon;
  Link(String uri,String kind,String query,String title,String icon){this.uri=uri;this.kind=kind;this.query=query;this.title=title;this.icon=icon;}
  boolean plays(){return "track".equals(kind) || "episode".equals(kind);}
  boolean page(){return HuBrowse.kind(uri)!=null;}
  /** The page's media id: the backend's browser loads a page only with its referer (ref=, one of its own sources), here search. */
  String pageId(){return uri+"?ref=search";}
 }
 interface Done {void link(Link l);}
 private static final List<String> KINDS=Arrays.asList("track","episode","playlist","album","artist","show");
 private static final Pattern EMBED=Pattern.compile("open\\.spotify\\.com/embed/(?:[a-z-]+/)*?(track|episode|playlist|album|artist|show)/([A-Za-z0-9]+)");
 private HuLinks(){}

 /** The link as a Spotify URI or a search, without the network; null for a short link or anything else. */
 static Link parse(Uri u){
  if(u==null)return null;String scheme=u.getScheme();
  List<String> parts=new ArrayList<>();
  if("spotify".equalsIgnoreCase(scheme)){
   String rest=u.getSchemeSpecificPart();if(rest==null)return null;
   // spotify:track:ID, spotify:user:NAME:playlist:ID, spotify:search:QUERY and, rarely, spotify://track/ID.
   if(rest.startsWith("//"))parts.addAll(Arrays.asList(rest.substring(2).split("/")));else parts.addAll(Arrays.asList(rest.split(":",-1)));
  }else if("https".equalsIgnoreCase(scheme) && "open.spotify.com".equalsIgnoreCase(u.getHost()))parts.addAll(u.getPathSegments());
  else return null;
  // open.spotify.com/intl-de/track/ID and open.spotify.com/embed/track/ID.
  while(!parts.isEmpty() && (parts.get(0).startsWith("intl-") || parts.get(0).equals("embed") || parts.get(0).isEmpty()))parts.remove(0);
  if(parts.size()>=2 && parts.get(0).equals("search")){
   String q=String.join(":",parts.subList(1,parts.size())).replace('+',' ').trim();
   return q.isEmpty()?null:new Link(null,"search",q,null,null);
  }
  if(parts.size()>=4 && parts.get(0).equals("user") && parts.get(2).equals("playlist"))parts=parts.subList(2,parts.size());
  if(parts.size()<2 || !KINDS.contains(parts.get(0)) || !parts.get(1).matches("[A-Za-z0-9]+"))return null;
  return new Link("spotify:"+parts.get(0)+":"+parts.get(1),parts.get(0),null,null,null);
 }
 static boolean isShort(Uri u){return u!=null && "https".equalsIgnoreCase(u.getScheme()) && "spotify.link".equalsIgnoreCase(u.getHost());}

 /**
  * What a link opens, on the main thread: a page with its title and cover, a short link as its
  * item; null when it is no Spotify link or a short link cannot be looked up (offline). Songs,
  * episodes and searches need no lookup.
  */
 static void resolve(Uri u,Done done){
  Handler main=new Handler(Looper.getMainLooper());Link parsed=parse(u);
  if(parsed==null && !isShort(u)){done.link(null);return;}
  if(parsed!=null && !parsed.page()){done.link(parsed);return;}
  String web=parsed!=null?"https://open.spotify.com/"+parsed.kind+"/"+parsed.uri.substring(parsed.uri.lastIndexOf(':')+1):u.toString();
  new Thread(()->{
   Link out=parsed;
   try{
    JSONObject o=oembed(web);
    Link item=parsed;
    if(item==null){Matcher m=EMBED.matcher(o.optString("html"));if(m.find())item=new Link("spotify:"+m.group(1)+":"+m.group(2),m.group(1),null,null,null);}
    if(item!=null)out=new Link(item.uri,item.kind,null,o.optString("title",null),o.optString("thumbnail_url",null));
   }catch(Exception e){android.util.Log.w("SpotifyHU","Link details unavailable: "+e.getClass().getSimpleName());}
   Link result=out;main.post(()->done.link(result));
  },"hu-link").start();
 }
 private static JSONObject oembed(String web)throws Exception{
  java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL("https://open.spotify.com/oembed?url="+java.net.URLEncoder.encode(web,"UTF-8")).openConnection();
  c.setConnectTimeout(5000);c.setReadTimeout(5000);
  try{
   if(c.getResponseCode()!=200)throw new java.io.IOException("HTTP "+c.getResponseCode());
   try(java.io.InputStream in=c.getInputStream()){
    java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
    while((n=in.read(buffer))>0 && bytes.size()<262144)bytes.write(buffer,0,n);
    return new JSONObject(bytes.toString("UTF-8"));
   }
  }finally{c.disconnect();}
 }
}
