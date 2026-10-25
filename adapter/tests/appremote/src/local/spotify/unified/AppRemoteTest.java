package local.spotify.unified;
import java.util.*;
/**
 * Plain-JVM checks of our App Remote: frames and payloads as the Spotify Android SDK 0.8.0 reads
 * them (positions and JSON names taken from its classes), JSON text, paging and the allowed clients.
 */
public final class AppRemoteTest {
 private static int passed;
 private static void ok(boolean condition,String name){if(!condition)throw new AssertionError(name);passed++;}
 @SuppressWarnings("unchecked")
 private static Map<String,Object> obj(Object o){return (Map<String,Object>)o;}
 @SuppressWarnings("unchecked")
 private static List<Object> arr(Object o){return (List<Object>)o;}
 public static void main(String[] args){
  // HELLO as the SDK sends it: [1,"spotify",HelloDetails].
  List<Object> hello=AppRemoteWire.parse("[1,\"spotify\",{\"roles\":{\"subscriber\":{},\"caller\":{}},\"info\":{\"protocol_version\":1,\"id\":\"waze\",\"name\":\"Waze\",\"category\":\"navigation\",\"required_features\":[\"com.spotify.features.v1\"],\"default_image_width\":480},\"authmethods\":[\"appid\"],\"authid\":\"b3b13bd9904a4d6da4116e9c0d17be6d\",\"extras\":{\"redirect_uri\":\"waze://spotify\",\"show_auth_view\":\"false\",\"scopes\":\"app-remote-control\"}}]");
  ok(AppRemoteWire.number(hello,0)==AppRemoteWire.HELLO && "spotify".equals(AppRemoteWire.string(hello,1)),"hello read");
  ok("Waze".equals(obj(obj(hello.get(2)).get("info")).get("name")),"hello details read");
  ok(AppRemoteWire.welcome(7).equals("[2,7,{\"roles\":{\"broker\":{},\"dealer\":{}}}]"),"welcome: details at index 2");
  // RESULT carries its payload at index 3, EVENT at index 5.
  ok(AppRemoteWire.result(5,AppRemoteWire.map("a",1)).equals("[50,5,{},{\"a\":1}]"),"result payload at index 3");
  ok(AppRemoteWire.result(5,null).equals("[50,5,{},{}]"),"empty result");
  ok(AppRemoteWire.event(3,9,AppRemoteWire.map()).equals("[36,3,9,{},[],{}]"),"event payload at index 5");
  ok(AppRemoteWire.error(48,4,AppRemoteWire.INVALID_URI,"x").equals("[8,48,4,{\"message\":\"x\"},\"wamp.error.invalid_uri\"]"),"error: type, request, details, uri");
  ok(AppRemoteWire.subscribed(2,1).equals("[33,2,1]") && AppRemoteWire.unsubscribed(2).equals("[35,2]"),"subscribed and unsubscribed");
  ok(AppRemoteWire.goodbye().equals("[6,{},\"wamp.close.goodbye_and_out\"]"),"goodbye");
  // CALL arguments: kwargs at index 5, else the first of the args at index 4.
  List<Object> call=AppRemoteWire.parse("[48,12,{},\"com.spotify.set_shuffle\",[],{\"shuffle\":true}]");
  ok(AppRemoteWire.number(call,1)==12 && "com.spotify.set_shuffle".equals(AppRemoteWire.string(call,3)),"call read");
  ok(AppRemoteWire.flag(AppRemoteWire.argument(call),"shuffle"),"kwargs argument");
  ok(AppRemoteWire.field(AppRemoteWire.argument(AppRemoteWire.parse("[48,1,{},\"u\",[{\"uri\":\"spotify:track:1\"}]]")),"uri").equals("spotify:track:1"),"args argument");
  ok(AppRemoteWire.argument(AppRemoteWire.parse("[48,1,{},\"com.spotify.skip_next\"]")).isEmpty(),"no argument");
  ok(AppRemoteWire.field(AppRemoteWire.argument(AppRemoteWire.parse("[48,1,{},\"u\",[],{\"position_ms\":-1500}]")),"position_ms",0)==-1500,"negative number");
  ok(AppRemoteWire.field(AppRemoteWire.argument(AppRemoteWire.parse("[48,1,{},\"u\",[],{\"playback_speed\":150}]")),"playback_speed",1)==150,"podcast speed");
  // PlayerState with the SDK's JSON names.
  AppRemoteWire.Player p=new AppRemoteWire.Player();p.title="Song \"1\"";p.artist="Артист";p.artistUri="spotify:artist:a";p.album="Album";p.albumUri="spotify:album:b";
  p.uri="spotify:track:t";p.image="content://com.spotify.music.mediaapi/v1/images/x";p.duration=215000;p.position=42000;p.paused=false;p.shuffle=true;p.repeat=2;
  Map<String,Object> st=AppRemoteWire.playerState(p);
  Map<String,Object> track=obj(st.get("track"));
  ok("Song \"1\"".equals(track.get("name")) && "spotify:track:t".equals(track.get("uri")) && Long.valueOf(215000).equals(track.get("duration_ms")),"track fields");
  ok("Артист".equals(obj(track.get("artist")).get("name")) && arr(track.get("artists")).size()==1 && "spotify:album:b".equals(obj(track.get("album")).get("uri")),"artist, artists and album");
  ok(p.image.equals(track.get("image_id")) && Boolean.FALSE.equals(track.get("is_episode")) && Boolean.FALSE.equals(track.get("is_podcast")),"image and kind");
  ok(Boolean.FALSE.equals(st.get("is_paused")) && Double.valueOf(1.0).equals(st.get("playback_speed")) && Long.valueOf(42000).equals(st.get("playback_position")),"paused, speed and position");
  ok(Boolean.TRUE.equals(obj(st.get("playback_options")).get("shuffle")) && Integer.valueOf(2).equals(obj(st.get("playback_options")).get("repeat")),"options");
  Map<String,Object> r=obj(st.get("playback_restrictions"));
  for(String k:new String[]{"can_skip_next","can_skip_prev","can_repeat_track","can_repeat_context","can_toggle_shuffle","can_seek"})ok(Boolean.TRUE.equals(r.get(k)),"restriction "+k);
  // The JSON text reads back the same.
  List<Object> back=AppRemoteWire.parse(AppRemoteWire.event(1,1,st));
  Map<String,Object> again=obj(back.get(5));
  ok("Артист".equals(obj(obj(again.get("track")).get("artist")).get("name")) && Double.valueOf(1.0).equals(again.get("playback_speed")),"player state round trip");
  ok(AppRemoteWire.json(AppRemoteWire.map("s","a\\b\n\t\u0001\"")).equals("{\"s\":\"a\\\\b\\n\\t\\u0001\\\"\"}"),"escapes");
  ok("a\\b\n\t\u0001\"é".equals(AppRemoteWire.string(AppRemoteWire.parse("[\"a\\\\b\\n\\t\\u0001\\\"\\u00e9\"]"),0)),"unescape");
  // Episodes, paused, nothing playing.
  AppRemoteWire.Player e=new AppRemoteWire.Player();e.uri="spotify:episode:e";e.title="Ep";e.position=-5;
  Map<String,Object> es=AppRemoteWire.playerState(e);
  ok(Boolean.TRUE.equals(obj(es.get("track")).get("is_episode")) && Boolean.TRUE.equals(es.get("is_paused")) && Double.valueOf(0.0).equals(es.get("playback_speed")) && Long.valueOf(0).equals(es.get("playback_position")),"episode, paused");
  ok(AppRemoteWire.playerState(new AppRemoteWire.Player()).get("track")==null,"nothing playing: no track");
  ok(AppRemoteWire.json(AppRemoteWire.playerState(new AppRemoteWire.Player())).startsWith("{\"track\":null,"),"null written");
  // Context, status, library, image.
  ok("playlist".equals(AppRemoteWire.context("spotify:playlist:p",null).get("type")) && "collection".equals(AppRemoteWire.context("spotify:user:u:collection",null).get("type")) && "".equals(AppRemoteWire.context(null,null).get("uri")),"context type");
  ok(Integer.valueOf(0).equals(AppRemoteWire.status(true).get("code")) && Integer.valueOf(1).equals(AppRemoteWire.status(false).get("code")),"status codes");
  ok(Boolean.TRUE.equals(AppRemoteWire.library("u",true,true).get("saved")) && Boolean.TRUE.equals(AppRemoteWire.capabilities().get("can_play_on_demand")),"library and capabilities");
  ok("QUJD".equals(AppRemoteWire.image("QUJD",3,4).get("image_data")) && Integer.valueOf(4).equals(AppRemoteWire.image("QUJD",3,4).get("height")),"image");
  // ListItems paging.
  List<Map<String,Object>> items=new ArrayList<>();
  for(int i=0;i<5;i++)items.add(AppRemoteWire.item("id"+i,"spotify:playlist:"+i,"T"+i,"S",null,true,i%2==0));
  Map<String,Object> pg=AppRemoteWire.page(items,1,2);
  ok(Long.valueOf(2).equals(pg.get("limit")) && Integer.valueOf(1).equals(pg.get("offset")) && Integer.valueOf(5).equals(pg.get("total")) && arr(pg.get("items")).size()==2,"page 1+2");
  ok("id1".equals(obj(arr(pg.get("items")).get(0)).get("id")) && Boolean.FALSE.equals(obj(arr(pg.get("items")).get(0)).get("has_children")),"page items");
  ok(arr(AppRemoteWire.page(items,0,0).get("items")).size()==5 && arr(AppRemoteWire.page(items,9,3).get("items")).isEmpty() && arr(AppRemoteWire.page(items,3,10).get("items")).size()==2,"page bounds");
  Map<String,Object> li=obj(arr(pg.get("items")).get(1));
  for(String k:new String[]{"id","uri","image_id","title","subtitle","playable","has_children"})ok(li.containsKey(k),"list item "+k);
  // Numbers and bad text.
  List<Object> n=AppRemoteWire.parse("[ 1 , -2 , 1.5 , 1e3 , true , false , null , \"\" , [ ] , { } ]");
  ok(Long.valueOf(1).equals(n.get(0)) && Long.valueOf(-2).equals(n.get(1)) && Double.valueOf(1.5).equals(n.get(2)) && Double.valueOf(1000).equals(n.get(3)),"numbers");
  ok(Boolean.TRUE.equals(n.get(4)) && Boolean.FALSE.equals(n.get(5)) && n.get(6)==null && "".equals(n.get(7)),"literals");
  for(String bad:new String[]{"{}","[1,]","[1] x","[\"a","","[1 2]","[tru]"}){boolean threw=false;try{AppRemoteWire.parse(bad);}catch(RuntimeException ex){threw=true;}ok(threw,"rejects "+bad);}
  // Allowed clients: Waze from Google Play and Waze for BYD, one certificate each.
  String play="03637f6c5d8f604e6fdb79a6ffbfa578de4e318f8da22fc6106665247f8807d7",byd="50120cc57caa95d380fdf280e3a9c0853ed09ec8a617fe7bed33893c39b66276";
  ok("com.waze".equals(AppRemoteAccess.client(10100,10050,false,"com.spotify.music",new String[]{"com.waze"},pk->new String[]{play})),"Waze from Google Play");
  ok("com.waze".equals(AppRemoteAccess.client(10100,10050,false,"com.spotify.music",new String[]{"com.waze"},pk->new String[]{byd.toUpperCase(Locale.ROOT)})),"Waze for BYD");
  ok(AppRemoteAccess.client(10100,10050,false,"com.spotify.music",new String[]{"com.waze"},pk->new String[]{"00"})==null,"another certificate");
  ok(AppRemoteAccess.client(10100,10050,false,"com.spotify.music",new String[]{"com.waze"},pk->new String[]{play,byd})==null,"two signers");
  ok(AppRemoteAccess.client(10100,10050,false,"com.spotify.music",new String[]{"com.example"},pk->new String[]{play})==null,"another package");
  ok(AppRemoteAccess.client(10100,10050,false,"com.spotify.music",new String[]{"com.waze"},pk->null)==null,"not visible");
  ok(AppRemoteAccess.client(-1,10050,false,"com.spotify.music",new String[]{"com.waze"},pk->new String[]{play})==null,"no sending uid");
  ok("com.spotify.music".equals(AppRemoteAccess.client(10050,10050,true,"com.spotify.music",null,pk->null)) && AppRemoteAccess.client(10050,10050,false,"com.spotify.music",null,pk->null)==null,"own uid: debug only");
  String own="a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1";
  ok("hu.appremote".equals(AppRemoteAccess.client(10200,10050,true,"com.spotify.music",new String[]{"hu.appremote"},pk->new String[]{own})),"test client in debug builds");
  ok(AppRemoteAccess.client(10200,10050,false,"com.spotify.music",new String[]{"hu.appremote"},pk->new String[]{own})==null,"no test client in release builds");
  System.out.println("App Remote checks passed: "+passed);
 }
}
