package local.spotify.unified;
import java.util.*;
/**
 * Spotify's App Remote protocol as the Spotify app serves it, for apps built on the Spotify
 * Android SDK (Waze): WAMP messages as JSON arrays (HELLO, WELCOME, CALL, RESULT, SUBSCRIBE,
 * EVENT, ERROR, GOODBYE), with the SDK's own JSON names for the player state, list items, images
 * and the rest. HuAppRemoteService carries them; this part is plain Java, so the plain-JVM tests
 * (tests/appremote) check it.
 *
 * Frames, as the SDK writes and reads them: HELLO [1,"spotify",details]; WELCOME
 * [2,session,details]; CALL [48,request,{},uri(,[args](,{kwargs}))]; RESULT [50,request,{},payload]
 * (the SDK reads the payload at index 3); SUBSCRIBE [32,request,{},topic]; SUBSCRIBED
 * [33,request,subscription]; EVENT [36,subscription,publication,{},[],payload] (payload at index
 * 5); ERROR [8,type,request,{"message":…},uri]; UNSUBSCRIBE [34,request,subscription].
 */
final class AppRemoteWire {
 private AppRemoteWire(){}
 static final int HELLO=1,WELCOME=2,ABORT=3,GOODBYE=6,ERROR=8,SUBSCRIBE=32,SUBSCRIBED=33,UNSUBSCRIBE=34,UNSUBSCRIBED=35,EVENT=36,CALL=48,CANCEL=49,RESULT=50;
 static final String PLAYER_STATE="com.spotify.player_state",CONTEXT="com.spotify.current_context",STATUS="com.spotify.status",CAPABILITIES="com.spotify.capabilities";
 static final String INVALID_URI="wamp.error.invalid_uri",INVALID_ARGUMENT="wamp.error.invalid_argument",FAILED="wamp.error";

 static String welcome(long session){return json(list(WELCOME,session,map("roles",map("broker",map(),"dealer",map()))));}
 static String result(long request,Object payload){return json(list(RESULT,request,map(),payload==null?map():payload));}
 static String error(int type,long request,String uri,String message){return json(list(ERROR,type,request,map("message",message==null?"":message),uri));}
 static String subscribed(long request,long subscription){return json(list(SUBSCRIBED,request,subscription));}
 static String unsubscribed(long request){return json(list(UNSUBSCRIBED,request));}
 static String event(long subscription,long publication,Object payload){return json(list(EVENT,subscription,publication,map(),list(),payload));}
 static String goodbye(){return json(list(GOODBYE,map(),"wamp.close.goodbye_and_out"));}

 /** What plays, from our media session. */
 static final class Player {
  String title,artist,artistUri,album,albumUri,uri,image;long duration,position;boolean paused=true,shuffle;int repeat;
 }
 /** PlayerState: track (null when nothing plays), paused, speed, position, options and restrictions. */
 static Map<String,Object> playerState(Player p){
  boolean episode=p.uri!=null && p.uri.startsWith("spotify:episode:");
  Object track=null;
  if(p.uri!=null || p.title!=null){
   Map<String,Object> artist=map("name",text(p.artist),"uri",text(p.artistUri));
   track=map("name",text(p.title),"uri",text(p.uri),"artist",artist,"artists",list(artist),"album",map("name",text(p.album),"uri",text(p.albumUri)),
    "duration_ms",Math.max(0,p.duration),"image_id",p.image,"is_episode",episode,"is_podcast",episode);
  }
  return map("track",track,"is_paused",p.paused,"playback_speed",p.paused?0.0:1.0,"playback_position",Math.max(0,p.position),
   "playback_options",map("shuffle",p.shuffle,"repeat",p.repeat),
   "playback_restrictions",map("can_skip_next",true,"can_skip_prev",true,"can_repeat_track",true,"can_repeat_context",true,"can_toggle_shuffle",true,"can_seek",true));
 }
 /** PlayerContext: what the music plays from (a playlist, album…). */
 static Map<String,Object> context(String uri,String title){
  String u=text(uri),type="";
  for(String k:new String[]{"playlist","album","artist","show","episode","track","collection"})if(u.startsWith("spotify:"+k)){type=k;break;}
  if(u.contains(":collection"))type="collection";
  return map("uri",u,"title",text(title),"subtitle","","type",type);
 }
 static Map<String,Object> status(boolean signedIn){return signedIn?map("code",0,"short_text","","long_text",""):map("code",1,"short_text","Not logged in","long_text","Sign in to Spotify on the head unit.");}
 static Map<String,Object> capabilities(){return map("can_play_on_demand",true);}
 static Map<String,Object> library(String uri,boolean saved,boolean canSave){return map("uri",text(uri),"saved",saved,"can_save",canSave);}
 static Map<String,Object> image(String base64,int width,int height){return map("image_data",base64,"width",width,"height",height);}
 static Map<String,Object> crossfade(){return map("isEnabled",false,"duration",0);}
 /** A browse item for ListItems. */
 static Map<String,Object> item(String id,String uri,String title,String subtitle,String image,boolean playable,boolean children){
  return map("id",text(id),"uri",text(uri),"image_id",image,"title",text(title),"subtitle",text(subtitle),"playable",playable,"has_children",children);
 }
 /** ListItems: the page [offset, offset+limit) of all items (a limit of 0 or less takes the rest). */
 static Map<String,Object> page(List<Map<String,Object>> all,long offset,long limit){
  int from=(int)Math.max(0,Math.min(all.size(),offset)),to=limit<=0?all.size():(int)Math.min(all.size(),from+limit);
  return map("limit",limit<=0?all.size():limit,"offset",from,"total",all.size(),"items",new ArrayList<Object>(all.subList(from,to)));
 }

 // A message's parts.
 static long number(List<Object> m,int i){Object o=i<m.size()?m.get(i):null;return o instanceof Number?((Number)o).longValue():-1;}
 static String string(List<Object> m,int i){Object o=i<m.size()?m.get(i):null;return o instanceof String?(String)o:null;}
 /** The call's argument object: its kwargs (index 5), or else the first of its args (index 4). */
 @SuppressWarnings("unchecked")
 static Map<String,Object> argument(List<Object> m){
  if(m.size()>5 && m.get(5) instanceof Map)return (Map<String,Object>)m.get(5);
  if(m.size()>4 && m.get(4) instanceof List && !((List<Object>)m.get(4)).isEmpty() && ((List<Object>)m.get(4)).get(0) instanceof Map)return (Map<String,Object>)((List<Object>)m.get(4)).get(0);
  return new LinkedHashMap<>();
 }
 static String field(Map<String,Object> m,String key){Object o=m.get(key);return o==null?null:String.valueOf(o);}
 static long field(Map<String,Object> m,String key,long fallback){Object o=m.get(key);return o instanceof Number?((Number)o).longValue():o instanceof Boolean?(((Boolean)o)?1:0):fallback;}
 static boolean flag(Map<String,Object> m,String key){Object o=m.get(key);return o instanceof Boolean?(Boolean)o:o instanceof Number && ((Number)o).intValue()!=0;}

 private static String text(String s){return s==null?"":s;}
 static List<Object> list(Object... items){return new ArrayList<>(Arrays.asList(items));}
 static Map<String,Object> map(Object... pairs){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i+1<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]);return m;}

 // JSON: maps, lists, strings, numbers, booleans and null.
 static String json(Object o){StringBuilder b=new StringBuilder();write(b,o);return b.toString();}
 private static void write(StringBuilder b,Object o){
  if(o==null){b.append("null");return;}
  if(o instanceof String){quote(b,(String)o);return;}
  if(o instanceof Boolean || o instanceof Integer || o instanceof Long){b.append(o);return;}
  if(o instanceof Number){double d=((Number)o).doubleValue();if(Double.isNaN(d) || Double.isInfinite(d))b.append('0');else if(d==Math.rint(d) && Math.abs(d)<1e15)b.append((long)d).append(".0");else b.append(d);return;}
  if(o instanceof Map){b.append('{');boolean first=true;for(Map.Entry<?,?> e:((Map<?,?>)o).entrySet()){if(!first)b.append(',');first=false;quote(b,String.valueOf(e.getKey()));b.append(':');write(b,e.getValue());}b.append('}');return;}
  if(o instanceof List){b.append('[');boolean first=true;for(Object e:(List<?>)o){if(!first)b.append(',');first=false;write(b,e);}b.append(']');return;}
  quote(b,String.valueOf(o));
 }
 private static void quote(StringBuilder b,String s){
  b.append('"');
  for(int i=0;i<s.length();i++){char c=s.charAt(i);
   switch(c){case '"':b.append("\\\"");break;case '\\':b.append("\\\\");break;case '\n':b.append("\\n");break;case '\r':b.append("\\r");break;case '\t':b.append("\\t");break;case '\b':b.append("\\b");break;case '\f':b.append("\\f");break;
    default:if(c<0x20)b.append(String.format(Locale.ROOT,"\\u%04x",(int)c));else b.append(c);}
  }
  b.append('"');
 }
 /** A message: a JSON array. */
 @SuppressWarnings("unchecked")
 static List<Object> parse(String text){Reader r=new Reader(text);Object o=r.value();r.space();if(r.p!=text.length() || !(o instanceof List))throw new IllegalArgumentException("Not a message");return (List<Object>)o;}
 private static final class Reader {
  final String s;int p;Reader(String s){this.s=s;}
  void space(){while(p<s.length() && Character.isWhitespace(s.charAt(p)))p++;}
  char next(){if(p>=s.length())throw new IllegalArgumentException("End of text");return s.charAt(p++);}
  Object value(){
   space();char c=next();
   if(c=='{'){Map<String,Object> m=new LinkedHashMap<>();space();if(s.startsWith("}",p)){p++;return m;}
    while(true){space();if(next()!='"')throw new IllegalArgumentException("Key expected");String k=string();space();if(next()!=':')throw new IllegalArgumentException("Colon expected");m.put(k,value());space();char d=next();if(d=='}')return m;if(d!=',')throw new IllegalArgumentException("Comma expected");}}
   if(c=='['){List<Object> l=new ArrayList<>();space();if(s.startsWith("]",p)){p++;return l;}
    while(true){l.add(value());space();char d=next();if(d==']')return l;if(d!=',')throw new IllegalArgumentException("Comma expected");}}
   if(c=='"')return string();
   if(s.startsWith("true",p-1)){p+=3;return Boolean.TRUE;}
   if(s.startsWith("false",p-1)){p+=4;return Boolean.FALSE;}
   if(s.startsWith("null",p-1)){p+=3;return null;}
   if(c=='-' || c>='0' && c<='9'){int start=p-1;while(p<s.length() && "+-0123456789.eE".indexOf(s.charAt(p))>=0)p++;String n=s.substring(start,p);
    if(n.indexOf('.')<0 && n.indexOf('e')<0 && n.indexOf('E')<0)try{return Long.parseLong(n);}catch(NumberFormatException ignored){}
    return Double.parseDouble(n);}
   throw new IllegalArgumentException("Unexpected "+c);
  }
  String string(){
   StringBuilder b=new StringBuilder();
   while(true){char c=next();if(c=='"')return b.toString();
    if(c!='\\'){b.append(c);continue;}
    char e=next();
    switch(e){case 'n':b.append('\n');break;case 'r':b.append('\r');break;case 't':b.append('\t');break;case 'b':b.append('\b');break;case 'f':b.append('\f');break;
     case 'u':if(p+4>s.length())throw new IllegalArgumentException("Bad escape");b.append((char)Integer.parseInt(s.substring(p,p+4),16));p+=4;break;
     default:b.append(e);}
   }
  }
 }
}
