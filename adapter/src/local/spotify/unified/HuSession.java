package local.spotify.unified;
import android.content.*;
import android.os.*;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import java.util.ArrayList;
import java.util.List;
/**
 * Our interface's connection to the Spotify backend's media session: one per process, through
 * the backend's own support-library classes (stubs in adapter/stubs), so repeat and shuffle
 * modes are available too. Listeners hear every change on the main thread.
 */
final class HuSession {
 interface Listener {void changed(HuSession session);}
 static final int STATE_PLAYING=3,STATE_BUFFERING=6,STATE_ERROR=7,REPEAT_NONE=0,REPEAT_ONE=1,REPEAT_ALL=2;
 private static final String SERVICE="com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService";
 private static HuSession instance;
 static HuSession get(Context c){if(instance==null)instance=new HuSession(c.getApplicationContext());return instance;}

 private final Context app;private final Handler main=new Handler(Looper.getMainLooper());
 private final List<Listener> listeners=new ArrayList<>();private final List<Runnable> waiting=new ArrayList<>();
 private MediaBrowserCompat browser;private MediaControllerCompat controller;
 private MediaMetadataCompat metadata;private PlaybackStateCompat state;private int repeat;
 private List<android.support.v4.media.session.MediaSessionCompat.QueueItem> queue;
 private final MediaControllerCompat.Callback callback=new MediaControllerCompat.Callback(){
  public void onPlaybackStateChanged(PlaybackStateCompat s){state=s;notifyListeners();}
  public void onMetadataChanged(MediaMetadataCompat m){metadata=m;notifyListeners();}
  public void onRepeatModeChanged(int mode){repeat=mode;notifyListeners();}
  public void onQueueChanged(List<android.support.v4.media.session.MediaSessionCompat.QueueItem> q){queue=q;notifyListeners();}
  public void onShuffleModeChanged(int mode){notifyListeners();}
  public void onSessionDestroyed(){disconnect();main.postDelayed(HuSession.this::connect,2000);}
 };
 private HuSession(Context app){this.app=app;}

 void add(Listener l){if(!listeners.contains(l))listeners.add(l);connect();l.changed(this);}
 void remove(Listener l){listeners.remove(l);}
 private void notifyListeners(){for(Listener l:new ArrayList<>(listeners))l.changed(this);}
 private void connect(){
  if(browser!=null)return;
  browser=new MediaBrowserCompat(app,new ComponentName(app.getPackageName(),SERVICE),new MediaBrowserCompat.ConnectionCallback(){
   public void onConnected(){
    try{
     controller=new MediaControllerCompat(app,browser.getSessionToken());controller.registerCallback(callback,main);
     metadata=controller.getMetadata();state=controller.getPlaybackState();repeat=controller.getRepeatMode();queue=controller.getQueue();
    }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Player session unavailable",e);}
    List<Runnable> tasks=new ArrayList<>(waiting);waiting.clear();for(Runnable r:tasks)r.run();
    for(String parent:new ArrayList<>(pending.keySet()))load(parent,1);
    notifyListeners();
   }
   public void onConnectionFailed(){disconnect();main.postDelayed(HuSession.this::connect,3000);}
   public void onConnectionSuspended(){disconnect();main.postDelayed(HuSession.this::connect,2000);}
  },null);
  browser.connect();
 }
 private void disconnect(){
  if(controller!=null)try{controller.unregisterCallback(callback);}catch(RuntimeException ignored){}
  if(browser!=null)try{browser.disconnect();}catch(RuntimeException ignored){}
  controller=null;browser=null;
 }

 boolean ready(){return controller!=null;}
 /** Runs the task once connected (main thread). */
 void whenReady(Runnable task){if(ready())task.run();else{waiting.add(task);connect();}}
 /** The browse tree's root (spotify:navigation). */
 String root(){return browser==null?null:browser.getRoot();}
 /**
  * Loads the children of a browse node once; done gets an empty list on errors. Pages asking for
  * the same node share one request; a request the backend leaves unanswered is sent once more,
  * and requests still open when the connection drops are sent again after it returns.
  */
 void children(String parent,java.util.function.Consumer<List<MediaBrowserCompat.MediaItem>> done){
  whenReady(()->{
   if(browser==null || parent==null){done.accept(new ArrayList<>());return;}
   List<java.util.function.Consumer<List<MediaBrowserCompat.MediaItem>>> w=pending.get(parent);
   if(w!=null){w.add(done);return;}
   w=new ArrayList<>();w.add(done);pending.put(parent,w);load(parent,1);
  });
 }
 private final java.util.Map<String,List<java.util.function.Consumer<List<MediaBrowserCompat.MediaItem>>>> pending=new java.util.HashMap<>();
 private void load(String parent,int attempt){
  MediaBrowserCompat b=browser;if(b==null)return;
  Runnable timeout=new Runnable(){public void run(){
   if(!pending.containsKey(parent) || browser!=b)return;
   try{b.unsubscribe(parent);}catch(RuntimeException ignored){}
   if(attempt<2)load(parent,attempt+1);else deliver(parent,new ArrayList<>());
  }};
  b.subscribe(parent,new MediaBrowserCompat.SubscriptionCallback(){
   public void onChildrenLoaded(String id,List<MediaBrowserCompat.MediaItem> items){main.removeCallbacks(timeout);if(browser!=null)browser.unsubscribe(id);deliver(parent,items==null?new ArrayList<>():items);}
   public void onError(String id){main.removeCallbacks(timeout);if(browser!=null)browser.unsubscribe(id);deliver(parent,new ArrayList<>());}
  });
  main.postDelayed(timeout,10000);
 }
 private void deliver(String parent,List<MediaBrowserCompat.MediaItem> items){
  List<java.util.function.Consumer<List<MediaBrowserCompat.MediaItem>>> w=pending.remove(parent);
  if(w!=null)for(java.util.function.Consumer<List<MediaBrowserCompat.MediaItem>> c:w)c.accept(items);
 }
 /** Plays a browse item through the backend's "play" browser action, as a media host does. */
 void play(String mediaId){
  whenReady(()->{
   if(browser==null || mediaId==null)return;Bundle b=new Bundle();b.putString("androidx.media.utils.extras.KEY_CUSTOM_BROWSER_ACTION_MEDIA_ITEM_ID",mediaId);
   browser.sendCustomAction("play",b,null);
  });
 }
 /** Plays a playable item (a track in its playlist, album or artist) as a media host does. */
 void playItem(String mediaId){MediaControllerCompat.TransportControls t=controls();if(t!=null && mediaId!=null)t.playFromMediaId(mediaId,new Bundle());}
 /** Plays the best match for a query, as a voice request does (PLAY_FROM_SEARCH). */
 void playSearch(String query){MediaControllerCompat.TransportControls t=controls();if(t!=null && query!=null)t.playFromSearch(query,new Bundle());}
 /** Searches the backend (its media browser search); done gets the results, empty on errors. */
 void search(String query,java.util.function.Consumer<List<MediaBrowserCompat.MediaItem>> done){
  whenReady(()->{
   if(browser==null || query==null){done.accept(new ArrayList<>());return;}
   browser.search(query,null,new MediaBrowserCompat.SearchCallback(){
    public void onSearchResult(String q,Bundle e,List<MediaBrowserCompat.MediaItem> items){done.accept(items==null?new ArrayList<>():items);}
    public void onError(String q,Bundle e){done.accept(new ArrayList<>());}
   });
  });
 }
 /** Loads one item (its current download state, for example); done gets null on errors. */
 void item(String id,java.util.function.Consumer<MediaBrowserCompat.MediaItem> done){
  whenReady(()->{
   if(browser==null || id==null){done.accept(null);return;}
   browser.getItem(id,new MediaBrowserCompat.ItemCallback(){
    public void onItemLoaded(MediaBrowserCompat.MediaItem i){done.accept(i);}
    public void onError(String i){done.accept(null);}
   });
  });
 }
 /** Sends an item's browser action (download, downloading, downloaded); done gets null or the error message. */
 void action(String action,String id,java.util.function.Consumer<String> done){
  whenReady(()->{
   if(browser==null || id==null){done.accept("");return;}
   Bundle b=new Bundle();b.putString("androidx.media.utils.extras.KEY_CUSTOM_BROWSER_ACTION_MEDIA_ITEM_ID",id);
   browser.sendCustomAction(action,b,new MediaBrowserCompat.CustomActionCallback(){
    public void onResult(String a,Bundle e,Bundle r){done.accept(null);}
    public void onError(String a,Bundle e,Bundle d){String m=d==null?null:d.getString("androidx.media.utils.extras.KEY_CUSTOM_BROWSER_ACTION_RESULT_MESSAGE");done.accept(m==null?"":m);}
   });
  });
 }
 String text(String key){if(metadata==null)return null;CharSequence t=metadata.getText(key);return t==null?metadata.getString(key):t.toString();}
 String title(){return text("android.media.metadata.TITLE");}
 String artist(){return text("android.media.metadata.ARTIST");}
 String art(){return metadata==null?null:metadata.getString("android.media.metadata.ALBUM_ART_URI");}
 String contextUri(){return metadata==null?null:metadata.getString("com.spotify.music.extra.CONTEXT_URI");}
 String trackUri(){return metadata==null?null:metadata.getString("android.media.metadata.MEDIA_URI");}
 String artistUri(){return metadata==null?null:metadata.getString("com.spotify.music.extra.ARTIST_URI");}
 long duration(){return metadata==null?0:Math.max(0,metadata.getLong("android.media.metadata.DURATION"));}
 boolean playing(){return state!=null && (state.getState()==STATE_PLAYING || state.getState()==STATE_BUFFERING);}
 /** The position now, from the last reported position, its time and the playback speed. */
 long position(){
  if(state==null)return 0;long p=state.getPosition();
  if(state.getState()==STATE_PLAYING)p+=(long)((SystemClock.elapsedRealtime()-state.getLastPositionUpdateTime())*state.getPlaybackSpeed());
  long d=duration();return Math.max(0,d>0?Math.min(p,d):p);
 }
 int repeat(){return repeat;}
 /** The backend's queue: the current track, then the queued tracks and the rest of the context. */
 List<android.support.v4.media.session.MediaSessionCompat.QueueItem> queue(){return queue==null?new ArrayList<>():queue;}
 long activeQueueItem(){return state==null?-1:state.getActiveQueueItemId();}
 void skipTo(long queueId){MediaControllerCompat.TransportControls t=controls();if(t!=null)t.skipToQueueItem(queueId);}
 /** Removes a queue entry (the support-library command the backend turns into a new queue). */
 void removeAt(int index){
  if(controller==null)return;Bundle b=new Bundle();b.putInt("android.support.v4.media.session.command.ARGUMENT_INDEX",index);
  controller.sendCommand("android.support.v4.media.session.command.REMOVE_QUEUE_ITEM_AT",b,null);
 }
 /** Plays a Spotify URI (song or artist radio: spotify:station:…). */
 void playUri(String uri){MediaControllerCompat.TransportControls t=controls();if(t!=null && uri!=null)t.playFromUri(android.net.Uri.parse(uri),new Bundle());}
 String albumUri(){return metadata==null?null:metadata.getString("com.spotify.music.extra.ALBUM_URI");}
 /**
  * Signed out: the backend's error state carries the sign-in screen (QR sign-in) as its resolution
  * intent, which is returned here; null while signed in. Any error with a resolution counts, as in
  * other media hosts: the error code (AUTHENTICATION_EXPIRED) only reaches this process once the
  * session's extra binder is connected, and reads as unknown before.
  */
 android.app.PendingIntent signIn(){
  if(state==null || state.getState()!=STATE_ERROR || state.getExtras()==null)return null;
  Object pi=state.getExtras().getParcelable("android.media.extras.ERROR_RESOLUTION_ACTION_INTENT");
  return pi instanceof android.app.PendingIntent?(android.app.PendingIntent)pi:null;
 }
 /** A playback error other than signing in (Premium required, not available, no connection), or null. */
 String playbackError(){
  if(state==null || state.getState()!=STATE_ERROR || signIn()!=null)return null;
  CharSequence m=state.getErrorMessage();return m==null || m.length()==0?null:m.toString();
 }
 /** The backend's own texts for signing in: its message and the button label (either may be null). */
 String errorMessage(){CharSequence m=state==null?null:state.getErrorMessage();return m==null?null:m.toString();}
 String signInLabel(){Bundle e=state==null?null:state.getExtras();return e==null?null:e.getString("android.media.extras.ERROR_RESOLUTION_ACTION_LABEL");}
 /** A session custom action by its ids (for example "like" / "unlike"), or null. */
 PlaybackStateCompat.CustomAction custom(String... ids){
  if(state==null || state.getCustomActions()==null)return null;
  for(PlaybackStateCompat.CustomAction a:state.getCustomActions())for(String id:ids)if(id.equals(a.getAction()))return a;
  return null;
 }
 /** Liked: the session offers "unlike" for a track in Liked Songs. */
 PlaybackStateCompat.CustomAction like(){return custom("like","unlike");}
 boolean liked(){PlaybackStateCompat.CustomAction a=like();return a!=null && "unlike".equals(a.getAction());}
 /** Shuffle is on when the session offers "disable-shuffle". */
 PlaybackStateCompat.CustomAction shuffle(){return custom("shuffle","disable-shuffle");}
 boolean shuffled(){PlaybackStateCompat.CustomAction a=shuffle();return a!=null && "disable-shuffle".equals(a.getAction());}

 private MediaControllerCompat.TransportControls controls(){return controller==null?null:controller.getTransportControls();}
 void playPause(){MediaControllerCompat.TransportControls t=controls();if(t==null)return;if(playing()){HeadUnitRuntime.explicitPause(app);t.pause();}else t.play();}
 void next(){MediaControllerCompat.TransportControls t=controls();if(t!=null)t.skipToNext();}
 void previous(){MediaControllerCompat.TransportControls t=controls();if(t!=null)t.skipToPrevious();}
 void seek(long position){MediaControllerCompat.TransportControls t=controls();if(t!=null)t.seekTo(position);}
 void send(PlaybackStateCompat.CustomAction a){MediaControllerCompat.TransportControls t=controls();if(t!=null && a!=null)t.sendCustomAction(a.getAction(),a.getExtras()==null?new Bundle():a.getExtras());}
 void cycleRepeat(){MediaControllerCompat.TransportControls t=controls();if(t!=null)t.setRepeatMode(repeat==REPEAT_NONE?REPEAT_ALL:repeat==REPEAT_ALL?REPEAT_ONE:REPEAT_NONE);}
}
