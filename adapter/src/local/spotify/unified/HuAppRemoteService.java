package local.spotify.unified;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.*;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static local.spotify.unified.AppRemoteWire.*;
/**
 * Spotify's App Remote for apps built on the Spotify Android SDK, such as Waze's audio player:
 * the Spotify app's own service, which the Automotive backend lacks. A client starts and binds it
 * (START_APP_PROTOCOL_SERVICE, or BIND_PROTOCOL_SERVICE in newer SDKs), sends a handshake with
 * its messenger and then WAMP messages (AppRemoteWire) as UTF-8 JSON under MESSAGE_BODY. Each
 * client gets a messenger of its own in the handshake's reply, so its messages are told apart;
 * only allowed clients get one (AppRemoteAccess). The answers come from our media session
 * (HuSession): what plays, the controls, the browse tree and its covers (HuCovers); saving
 * works for the playing track. The SDK starts the service in the foreground, so it keeps a
 * notification while a client is connected. Everything runs on the main thread.
 */
public final class HuAppRemoteService extends Service {
 private static final int HANDSHAKE=1,DATA=2,NOTIFICATION=7046;private static final String BODY="MESSAGE_BODY",CHANNEL="spotify_hu_apps";
 private final List<Client> clients=new ArrayList<>();private Messenger messenger;private HuSession session;private boolean foreground;
 public void onCreate(){super.onCreate();session=HuSession.get(this);messenger=new Messenger(new Handler(Looper.getMainLooper(),this::handshake));}
 /**
  * The SDK starts the service with startForegroundService: every start has to put it in the
  * foreground at once, even when it is there already. Started without a client connecting soon,
  * it stops again; a client that leaves often comes straight back (Waze reconnects when it opens),
  * so the last one's leaving stops it only a little later.
  */
 public int onStartCommand(Intent intent,int flags,int id){lastStart=id;foreground=false;notification(null);main.removeCallbacks(idle);main.postDelayed(idle,20000);return START_NOT_STICKY;}
 private final Handler main=new Handler(Looper.getMainLooper());
 private final Runnable idle=()->{if(clients.isEmpty())stop();};private int lastStart;
 /**
  * Stops unless a client is connected or a newer start has come meanwhile (Android ends the
  * process when a service is brought down before a pending foreground start is answered).
  */
 private void stop(){
  if(!clients.isEmpty())return;
  if(foreground){stopForeground(STOP_FOREGROUND_REMOVE);foreground=false;}
  if(lastStart!=0)stopSelfResult(lastStart);else stopSelf();
 }
 public IBinder onBind(Intent intent){return messenger.getBinder();}
 public boolean onUnbind(Intent intent){for(Client c:new ArrayList<>(clients))c.close();return false;}
 public void onDestroy(){main.removeCallbacks(idle);for(Client c:new ArrayList<>(clients))c.close();super.onDestroy();}

 /** A client's first message carries its messenger; the reply carries ours for it, or none when it is refused. */
 private boolean handshake(Message m){
  Messenger reply=m.replyTo;if(reply==null)return true;
  String client=caller(m.sendingUid);Message answer=Message.obtain(null,HANDSHAKE);
  if(client==null)Log.w("SpotifyHU","App Remote: uid "+m.sendingUid+" refused");
  else{Client c=new Client(client,reply);clients.add(c);answer.replyTo=c.messenger;Log.i("SpotifyHU","App Remote: "+client+" connected");if(foreground)notification(label(client));}
  try{reply.send(answer);}catch(RemoteException e){Log.w("SpotifyHU","App Remote: handshake undelivered");}
  return true;
 }
 private String caller(int uid){
  PackageManager pm=getPackageManager();String[] packages=uid<0?null:pm.getPackagesForUid(uid);
  return AppRemoteAccess.client(uid,android.os.Process.myUid(),HeadUnitBuild.DEBUG,getPackageName(),packages,p->PanelProviderService.signers(pm,p));
 }
 private String label(String pkg){try{PackageManager pm=getPackageManager();return pm.getApplicationLabel(pm.getApplicationInfo(pkg,0)).toString();}catch(PackageManager.NameNotFoundException e){return pkg;}}
 /** The foreground notification: connecting, or connected to an app. */
 private void notification(String app){
  try{
   NotificationManager nm=getSystemService(NotificationManager.class);
   nm.createNotificationChannel(new NotificationChannel(CHANNEL,HuText.get("apps_channel"),NotificationManager.IMPORTANCE_LOW));
   String text=app==null?HuText.get("apps_connecting"):String.format(HuText.get("apps_connected"),app);
   Notification n=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Spotify").setContentText(text).setOngoing(true).build();
   if(foreground)nm.notify(NOTIFICATION,n);else{startForeground(NOTIFICATION,n);foreground=true;}
  }catch(RuntimeException e){Log.w("SpotifyHU","App Remote: no foreground ("+e.getClass().getSimpleName()+")");}
 }
 private void closed(Client c){clients.remove(c);if(clients.isEmpty()){main.removeCallbacks(idle);main.postDelayed(idle,3000);}}

 /** One connected app: its subscriptions and what they were last sent. */
 private final class Client implements HuSession.Listener,IBinder.DeathRecipient {
  final String pkg;final Messenger out,messenger;
  private final Map<Long,String> topics=new LinkedHashMap<>(),sent=new HashMap<>();private long subscriptions,publications;private boolean hello,closed;
  Client(String pkg,Messenger out){
   this.pkg=pkg;this.out=out;messenger=new Messenger(new Handler(Looper.getMainLooper(),this::receive));
   try{out.getBinder().linkToDeath(this,0);}catch(RemoteException e){new Handler(Looper.getMainLooper()).post(this::close);}
  }
  public void binderDied(){new Handler(Looper.getMainLooper()).post(this::close);}
  void close(){
   if(closed)return;closed=true;session.remove(this);
   try{out.getBinder().unlinkToDeath(this,0);}catch(RuntimeException ignored){}
   Log.i("SpotifyHU","App Remote: "+pkg+" disconnected");closed(this);
  }
  private boolean receive(Message m){
   Bundle b=m.getData();byte[] body=b==null?null:b.getByteArray(BODY);if(body==null || closed)return true;
   List<Object> message;
   try{message=parse(new String(body,StandardCharsets.UTF_8));}catch(RuntimeException e){Log.w("SpotifyHU","App Remote: unreadable message from "+pkg);return true;}
   try{dispatch(message);}catch(RuntimeException e){Log.w("SpotifyHU","App Remote: message failed",e);}
   return true;
  }
  private void dispatch(List<Object> m){
   int type=(int)number(m,0);
   switch(type){
    case HELLO:if(!hello){hello=true;Log.i("SpotifyHU","App Remote: hello from "+pkg+" "+describe(m));send(welcome(1+new Random().nextInt(1<<30)));session.add(this);}break;
    case GOODBYE:Log.i("SpotifyHU","App Remote: goodbye from "+pkg+" ("+string(m,2)+")");send(goodbye());close();break;
    case SUBSCRIBE:{long sub=++subscriptions;String topic=string(m,3);Log.i("SpotifyHU","App Remote: subscribe "+topic);topics.put(sub,topic==null?"":topic);send(subscribed(number(m,1),sub));publish(sub);break;}
    case UNSUBSCRIBE:{long sub=number(m,2);topics.remove(sub);sent.remove(sub);send(unsubscribed(number(m,1)));break;}
    case CALL:Log.i("SpotifyHU","App Remote: call "+number(m,1)+" "+string(m,3)+" "+argument(m).keySet()+(String.valueOf(string(m,3)).contains("root_items")?" "+argument(m):""));call(number(m,1),string(m,3),argument(m));break;
    case CANCEL:break;
    default:Log.w("SpotifyHU","App Remote: message type "+type+" ignored");
   }
  }
  /** The client's name, category, SDK version and required features from its HELLO (no account data). */
  @SuppressWarnings("unchecked")
  private String describe(List<Object> m){
   Object d=m.size()>2?m.get(2):null;Object info=d instanceof Map?((Map<String,Object>)d).get("info"):null;
   if(!(info instanceof Map))return "";Map<String,Object> i=(Map<String,Object>)info;
   return i.get("name")+" / "+i.get("category")+" / "+i.get("version")+" / "+i.get("required_features");
  }
  public void changed(HuSession s){if(!closed)for(Long sub:new ArrayList<>(topics.keySet()))publish(sub);}
  /** The subscription's present value, when it differs from the last one sent. */
  private void publish(long sub){
   String topic=topics.get(sub);if(topic==null || !session.ready())return;
   Object payload;
   switch(topic){
    case PLAYER_STATE:payload=playerState(player());break;
    case CONTEXT:payload=context(session.contextUri(),"");break;
    case STATUS:payload=status(session.signIn()==null);break;
    case CAPABILITIES:payload=capabilities();break;
    default:return;
   }
   String key=json(payload);if(key.equals(sent.get(sub)))return;sent.put(sub,key);
   send(event(sub,++publications,payload));
  }
  private Player player(){
   Player p=new Player();p.title=session.title();p.artist=session.artist();p.artistUri=session.artistUri();p.album=session.album();p.albumUri=session.albumUri();
   p.uri=session.trackUri();p.image=session.art();p.duration=session.duration();p.position=session.position();p.paused=!session.playing();p.shuffle=session.shuffled();p.repeat=session.repeat();
   return p;
  }
  private void call(long id,String uri,Map<String,Object> a){
   if(uri==null){send(error(CALL,id,INVALID_URI,"No procedure"));return;}
   switch(uri){
    case "com.spotify.get_player_state":ready(()->send(result(id,playerState(player()))));break;
    case "com.spotify.skip_next":ready(()->{session.next();done(id);});break;
    case "com.spotify.skip_previous":ready(()->{session.previous();done(id);});break;
    // Pause and resume are speeds 0 and 1; a podcast speed (50–300 %) only resumes.
    case "com.spotify.set_playback_speed":{long speed=field(a,"playback_speed",1);ready(()->{if(speed==0)session.pause();else session.resume();done(id);});break;}
    case "com.spotify.play_spotify_uri":case "com.spotify.play_spotify_uri_option_extras":{String u=field(a,"uri");if(u==null)bad(id);else ready(()->{session.playUri(u);done(id);});break;}
    case "com.spotify.play_item":{String item=field(a,"id");if(item==null)bad(id);else ready(()->{if(item.startsWith("spotify:") && item.indexOf('?')<0)session.playUri(item);else session.playItem(item);done(id);});break;}
    case "com.spotify.set_shuffle":{boolean on=flag(a,"shuffle");ready(()->{if(on!=session.shuffled())session.send(session.shuffle());done(id);});break;}
    case "com.spotify.toggle_shuffle":ready(()->{session.send(session.shuffle());done(id);});break;
    case "com.spotify.set_repeat":{int mode=(int)field(a,"repeat",0);ready(()->{session.setRepeat(mode);done(id);});break;}
    case "com.spotify.toggle_repeat":ready(()->{session.cycleRepeat();done(id);});break;
    case "com.spotify.set_playback_position":{long position=field(a,"position_ms",-1);if(position<0)bad(id);else ready(()->{session.seek(position);done(id);});break;}
    case "com.spotify.seek_to_relative_position":{long by=field(a,"position_ms",0);ready(()->{session.seek(Math.max(0,session.position()+by));done(id);});break;}
    case "com.spotify.get_saved":{String u=field(a,"id");ready(()->{boolean here=current(u);send(result(id,library(u,here && session.liked(),here && session.like()!=null)));});break;}
    case "com.spotify.set_saved":{String u=field(a,"uri");boolean saved=flag(a,"saved");ready(()->{
     if(!current(u) || session.like()==null){send(error(CALL,id,FAILED,"Only the playing track can be saved"));return;}
     if(saved!=session.liked())session.send(session.like());done(id);});break;}
    case "com.spotify.get_capabilities":send(result(id,capabilities()));break;
    case "com.spotify.get_crossfade":send(result(id,crossfade()));break;
    case "com.spotify.get_image":image(id,a,360);break;
    case "com.spotify.get_thumbnail_image":image(id,a,144);break;
    case "com.spotify.get_recommended_root_items":ready(()->session.children(session.root(),items->send(result(id,page(items(items),0,0)))));break;
    case "com.spotify.get_children_of_item":{String parent=field(a,"parent_id");long offset=field(a,"offset",0),limit=field(a,"limit",0);
     if(parent==null)bad(id);else ready(()->session.children(parent,items->send(result(id,page(items(items),offset,limit)))));break;}
    default:send(error(CALL,id,INVALID_URI,"Not supported: "+uri));
   }
  }
  private void ready(Runnable r){session.whenReady(()->{if(!closed)r.run();});}
  private boolean current(String uri){return uri!=null && uri.equals(session.trackUri());}
  private void done(long id){send(result(id,map()));}
  private void bad(long id){send(error(CALL,id,INVALID_ARGUMENT,"Missing argument"));}
  private List<Map<String,Object>> items(List<MediaItem> list){
   List<Map<String,Object>> out=new ArrayList<>();
   for(MediaItem i:list){String id=HuBrowse.id(i);out.add(item(id,HuBrowse.uri(id),HuBrowse.title(i),HuBrowse.subtitle(i),HuBrowse.icon(i),i.isPlayable(),i.isBrowsable()));}
   return out;
  }
  /**
   * A cover as JPEG, at most about the asked size (the session's and the browse tree's covers are
   * content or https URIs); the backend's own pictures (android.resource, often vectors) as PNG.
   */
  private void image(long id,Map<String,Object> a,int fallback){
   String uri=field(a,"id");int w=(int)field(a,"width",fallback),h=(int)field(a,"height",w),size=Math.max(64,Math.min(720,Math.max(w,h)));
   if(uri==null || uri.isEmpty()){bad(id);return;}
   if(uri.startsWith("android.resource://")){
    Bitmap picture=picture(android.net.Uri.parse(uri),size);
    if(picture==null){send(error(CALL,id,FAILED,"Image unavailable"));return;}
    java.io.ByteArrayOutputStream o=new java.io.ByteArrayOutputStream();picture.compress(Bitmap.CompressFormat.PNG,100,o);
    send(result(id,AppRemoteWire.image(Base64.getEncoder().encodeToString(o.toByteArray()),picture.getWidth(),picture.getHeight())));
    return;
   }
   HuCovers.load(HuAppRemoteService.this,uri,size,(b,color)->{
    if(closed)return;
    if(b==null){send(error(CALL,id,FAILED,"Image unavailable"));return;}
    Bitmap s=b.getWidth()>size*5/4?Bitmap.createScaledBitmap(b,size,Math.max(1,Math.round(b.getHeight()*size/(float)b.getWidth())),true):b;
    java.io.ByteArrayOutputStream o=new java.io.ByteArrayOutputStream();s.compress(Bitmap.CompressFormat.JPEG,90,o);
    send(result(id,AppRemoteWire.image(Base64.getEncoder().encodeToString(o.toByteArray()),s.getWidth(),s.getHeight())));
   });
  }
  /** One of our package's drawables (android.resource://package/type/name) drawn at the size. */
  private Bitmap picture(android.net.Uri u,int size){
   List<String> path=u.getPathSegments();if(!getPackageName().equals(u.getAuthority()) || path.size()!=2)return null;
   int res=getResources().getIdentifier(path.get(1),path.get(0),getPackageName());if(res==0)return null;
   try{
    android.graphics.drawable.Drawable d=getDrawable(res);if(d==null)return null;
    Bitmap b=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);d.setBounds(0,0,size,size);d.draw(new android.graphics.Canvas(b));return b;
   }catch(RuntimeException e){return null;}
  }
  private void send(String json){
   if(closed)return;
   if(json.startsWith("[8,"))Log.w("SpotifyHU","App Remote: error sent "+json);
   Bundle b=new Bundle();b.putByteArray(BODY,json.getBytes(StandardCharsets.UTF_8));Message m=Message.obtain(null,DATA);m.setData(b);
   try{out.send(m);}catch(RemoteException e){close();}
  }
 }
}
