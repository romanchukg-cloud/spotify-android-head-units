package hu.downloads;
import android.app.*;import android.content.*;import android.os.*;import android.support.v4.media.*;import java.util.*;
/**
 * Emulator probe for Spotify Automotive's offline downloads. Connects to the backend's
 * MediaBrowserService as a media host (KEY_ROOT_HINT_MEDIA_HOST_VERSION), lists the browse
 * tree with each item's custom browser actions and, with an action and an item id, sends the
 * action and watches the item's state. Output: logcat tag HU_PROBE.
 *   am start -n hu.downloads/.Probe [--es mode tree|action] [--ez hint false] [--es action download --es id ID] [--ei watch 120]
 *   am start -n hu.downloads/.Probe --es mode tree --es id spotify:playlist:… --ei depth 0   (one node and its item)
 *   am start -n hu.downloads/.Probe --es mode find --es query "imagine dragons"   (browser search results)
 */
public class Probe extends Activity {
 static final String TAG="HU_PROBE",ACTIONS="androidx.media.utils.extras.CUSTOM_BROWSER_ACTION_ID_LIST",ITEM="androidx.media.utils.extras.KEY_CUSTOM_BROWSER_ACTION_MEDIA_ITEM_ID";
 MediaBrowserCompat browser;final Handler main=new Handler(Looper.getMainLooper());final ArrayDeque<String[]> nodes=new ArrayDeque<>();int visited;
 public void onCreate(Bundle b){super.onCreate(b);
  Bundle hints=new Bundle();if(getIntent().getBooleanExtra("hint",true))hints.putString("androidx.car.app.mediaextensions.KEY_ROOT_HINT_MEDIA_HOST_VERSION","1.0.0");
  browser=new MediaBrowserCompat(this,new ComponentName("com.spotify.music","com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService"),new MediaBrowserCompat.ConnectionCallback(){
   public void onConnected(){log("CONNECTED root="+browser.getRoot()+" extras="+dump(browser.getExtras()));
    String mode=getIntent().getStringExtra("mode");
    if("action".equals(mode))action();else if("find".equals(mode))find();else if("search".equals(mode))search();else if("uri".equals(mode))uri();else if("meta".equals(mode))meta();else{String start=getIntent().getStringExtra("id");nodes.add(new String[]{start==null?browser.getRoot():start,"0"});if(start!=null)item(start);next();}}
   public void onConnectionFailed(){log("REJECTED");finish();}
   public void onConnectionSuspended(){log("SUSPENDED");}
  },hints);browser.connect();
 }
 /** Breadth-first walk: depth 3, at most 30 nodes. */
 void next(){
  String[] node=nodes.poll();if(node==null || visited>=30){log("TREE DONE nodes="+visited);return;}
  visited++;final int depth=Integer.parseInt(node[1]);final String id=node[0];
  browser.subscribe(id,new MediaBrowserCompat.SubscriptionCallback(){
   public void onChildrenLoaded(String parent,List<MediaBrowserCompat.MediaItem> children){
    log("NODE depth="+depth+" id="+parent+" children="+children.size());int n=0;
    int limit=getIntent().getIntExtra("depth",2),show=getIntent().getIntExtra("show",12);
    for(MediaBrowserCompat.MediaItem item:children){if(n++<show)log("  "+describe(item));if(item.isBrowsable() && depth<limit && n<=8)nodes.add(new String[]{item.getMediaId(),String.valueOf(depth+1)});}
    browser.unsubscribe(parent);main.post(Probe.this::next);}
   public void onError(String parent){log("NODE ERROR "+parent);main.post(Probe.this::next);}
  });
 }
 void item(String id){browser.getItem(id,new MediaBrowserCompat.ItemCallback(){
   public void onItemLoaded(MediaBrowserCompat.MediaItem item){log("ITEM "+(item==null?"null":describe(item)));}
   public void onError(String i){log("ITEM ERROR "+i);}
  });}
 /** Searches through the browser (MediaBrowserServiceCompat.onSearch) and logs the results. */
 void find(){
  String query=getIntent().getStringExtra("query");int show=getIntent().getIntExtra("show",40);
  browser.search(query,null,new MediaBrowserCompat.SearchCallback(){
   public void onSearchResult(String q,Bundle e,List<MediaBrowserCompat.MediaItem> items){log("FIND "+q+" results="+items.size());int n=0;for(MediaBrowserCompat.MediaItem i:items)if(n++<show)log("  "+describe(i));}
   public void onError(String q,Bundle e){log("FIND ERROR "+q);}
  });
 }
 /** Plays the best match for a spoken-style query through the backend's media session (PLAY_FROM_SEARCH). */
 void search(){
  final String query=getIntent().getStringExtra("query");
  final android.media.browse.MediaBrowser framework=new android.media.browse.MediaBrowser(this,new ComponentName("com.spotify.music","com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService"),new android.media.browse.MediaBrowser.ConnectionCallback(){},null);
  framework.connect();
  main.postDelayed(()->{
   if(!framework.isConnected()){log("SEARCH not connected");return;}
   android.media.session.MediaController c=new android.media.session.MediaController(this,framework.getSessionToken());
   log("SEARCH play query="+query);c.getTransportControls().playFromSearch(query,new Bundle());
   for(int s=3;s<=15;s+=4)main.postDelayed(()->{android.media.MediaMetadata m=c.getMetadata();android.media.session.PlaybackState st=c.getPlaybackState();
    log("SEARCH state="+(st==null?"null":st.getState()+" error="+st.getErrorMessage())+" title="+(m==null?"null":m.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)+" / "+m.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)));},s*1000L);
  },3000);
 }
 /** Plays a Spotify URI (for example a radio station) and logs the queue that follows. */
 void uri(){
  final String value=getIntent().getStringExtra("uri");
  final android.media.browse.MediaBrowser framework=new android.media.browse.MediaBrowser(this,new ComponentName("com.spotify.music","com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService"),new android.media.browse.MediaBrowser.ConnectionCallback(){},null);
  framework.connect();
  main.postDelayed(()->{
   if(!framework.isConnected()){log("URI not connected");return;}
   android.media.session.MediaController c=new android.media.session.MediaController(this,framework.getSessionToken());
   log("URI play "+value);c.getTransportControls().playFromUri(android.net.Uri.parse(value),new Bundle());
   main.postDelayed(()->{android.media.MediaMetadata m=c.getMetadata();android.media.session.PlaybackState st=c.getPlaybackState();
    log("URI state="+(st==null?"null":st.getState()+" error="+st.getErrorMessage())+" title="+(m==null?"null":m.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)+" / "+m.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST))+" queueTitle="+c.getQueueTitle());
    java.util.List<android.media.session.MediaSession.QueueItem> q=c.getQueue();log("URI queue size="+(q==null?0:q.size()));
    if(q!=null)for(int i=0;i<Math.min(6,q.size());i++){android.media.MediaDescription d=q.get(i).getDescription();log("  Q id="+q.get(i).getQueueId()+" media="+d.getMediaId()+" title="+d.getTitle()+" sub="+d.getSubtitle()+" extras="+dump(d.getExtras()));}
   },12000);
  },3000);
 }
 /** Logs every key of the current track's metadata. */
 void meta(){
  final android.media.browse.MediaBrowser framework=new android.media.browse.MediaBrowser(this,new ComponentName("com.spotify.music","com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService"),new android.media.browse.MediaBrowser.ConnectionCallback(){},null);
  framework.connect();
  main.postDelayed(()->{android.media.session.MediaController c=new android.media.session.MediaController(this,framework.getSessionToken());android.media.MediaMetadata m=c.getMetadata();
   if(m==null){log("META null");return;}for(String k:new TreeSet<>(m.keySet())){Object v=m.getString(k);if(v==null)v=m.getLong(k);log("META "+k+"="+v);}
   log("META session extras="+dump(c.getExtras()));},3000);
 }
 void action(){
  final String action=getIntent().getStringExtra("action"),id=getIntent().getStringExtra("id");final int watch=getIntent().getIntExtra("watch",90);
  Bundle extras=new Bundle();extras.putString(ITEM,id);
  browser.sendCustomAction(action,extras,new MediaBrowserCompat.CustomActionCallback(){
   public void onProgressUpdate(String a,Bundle e,Bundle d){log("ACTION PROGRESS "+dump(d));}
   public void onResult(String a,Bundle e,Bundle r){log("ACTION RESULT "+dump(r));}
   public void onError(String a,Bundle e,Bundle d){log("ACTION ERROR "+dump(d));}
  });
  for(int s=0;s<=watch;s+=5)main.postDelayed(()->browser.getItem(id,new MediaBrowserCompat.ItemCallback(){
   public void onItemLoaded(MediaBrowserCompat.MediaItem item){log("ITEM "+(item==null?"null":describe(item)));}
   public void onError(String i){log("ITEM ERROR "+i);}
  }),s*1000L);
  main.postDelayed(()->log("WATCH DONE"),watch*1000L+3000);
 }
 static String describe(MediaBrowserCompat.MediaItem item){
  MediaDescriptionCompat d=item.getDescription();
  return (item.isBrowsable()?"B":"-")+(item.isPlayable()?"P":"-")+" id="+item.getMediaId()+" title="+d.getTitle()+" sub="+d.getSubtitle()+" extras="+dump(d.getExtras());
 }
 static String dump(Bundle b){
  if(b==null)return "null";StringBuilder s=new StringBuilder("{");
  for(String k:new TreeSet<>(b.keySet())){Object v=b.get(k);if(v instanceof Bundle)v=dump((Bundle)v);else if(v instanceof List){List<?> l=(List<?>)v;StringBuilder t=new StringBuilder("[");for(Object o:l)t.append(o instanceof Bundle?dump((Bundle)o):String.valueOf(o)).append(',');v=t.append(']');}
   s.append(k.replace("androidx.media.utils.extras.","")).append('=').append(v).append(' ');}
  return s.append('}').toString();
 }
 void log(String s){for(int i=0;i<s.length();i+=3500)android.util.Log.i(TAG,s.substring(i,Math.min(s.length(),i+3500)));}
 protected void onDestroy(){if(browser!=null)browser.disconnect();super.onDestroy();}
}
