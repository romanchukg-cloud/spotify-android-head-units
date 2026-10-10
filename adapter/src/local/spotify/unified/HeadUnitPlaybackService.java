package local.spotify.unified;
import android.app.*;
import android.content.*;
import android.media.browse.MediaBrowser;
import android.media.session.*;
import android.os.*;
import android.util.Log;
import android.view.KeyEvent;
import java.util.*;
/** Holds a framework browser connection and routes cold-start media commands. */
public final class HeadUnitPlaybackService extends Service {
 private final Handler main=new Handler(Looper.getMainLooper());
 private final ArrayDeque<Integer> commands=new ArrayDeque<>();
 private MediaBrowser browser;private MediaController controller;private long connectedAt;
 public static boolean supported(int key){return key==85 || key==87 || key==88 || key==126 || key==127 || key==79;}
 public static boolean command(Context c,int key,String source){
  local.spotify.close.PlaybackGate.init(c);if(local.spotify.close.PlaybackGate.accountTransition())return false;
  if(!supported(key)){Log.w("SpotifyHU","Unsupported media key="+key);return false;}
  Log.i("SpotifyHU","Command key="+key+" source="+source);
  if(key==KeyEvent.KEYCODE_MEDIA_PAUSE)HeadUnitRuntime.explicitPause(c);
  return start(c,new Intent(c,HeadUnitPlaybackService.class).putExtra("key",key));
 }
 public static void keepAlive(Context c){start(c,new Intent(c,HeadUnitPlaybackService.class));}
 private static boolean start(Context c,Intent intent){
  try{c.startForegroundService(intent);return true;}catch(RuntimeException e){Log.w("SpotifyHU","Playback start blocked ("+e.getClass().getSimpleName()+")");return false;}
 }
 public void onCreate(){
  super.onCreate();HeadUnitPreferences.init(this);HeadUnitRuntime.init(this);
  NotificationManager nm=getSystemService(NotificationManager.class);NotificationChannel channel=new NotificationChannel("spotify_hu_playback",HuText.get("channel"),NotificationManager.IMPORTANCE_LOW);nm.createNotificationChannel(channel);
  Intent open=new Intent().setClassName(getPackageName(),"local.bydui.com.vivid.spotify.MainActivity");
  PendingIntent pending=PendingIntent.getActivity(this,72,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  startForeground(7045,new Notification.Builder(this,"spotify_hu_playback").setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Spotify").setContentText(HuText.get("background")).setContentIntent(pending).setOngoing(true).build());
  browser=new MediaBrowser(this,new ComponentName(getPackageName(),"com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService"),new MediaBrowser.ConnectionCallback(){
   public void onConnected(){controller=new MediaController(HeadUnitPlaybackService.this,browser.getSessionToken());connectedAt=SystemClock.elapsedRealtime();controller.registerCallback(callback,main);flush();main.postDelayed(idle,30000);Log.i("SpotifyHU","Playback browser connected");}
   public void onConnectionFailed(){Log.w("SpotifyHU","Playback browser rejected connection");stopSelf();}
   public void onConnectionSuspended(){controller=null;Log.w("SpotifyHU","Playback browser suspended");stopSelf();}
  },null);browser.connect();main.postDelayed(()->{if(controller==null){Log.w("SpotifyHU","Playback browser timed out");stopSelf();}},30000);
 }
 private final MediaController.Callback callback=new MediaController.Callback(){public void onPlaybackStateChanged(PlaybackState state){main.removeCallbacks(idle);main.postDelayed(idle,30000);}};
 private final Runnable idle=()->{
  PlaybackState s=controller==null?null:controller.getPlaybackState();int state=s==null?0:s.getState();
  if(state!=3 && state!=6 && state!=8 && state!=9 && state!=10 && state!=11)stopSelf();
 };
 private void flush(){
  if(local.spotify.close.PlaybackGate.accountTransition()){commands.clear();return;}
  if(controller==null)return;
  while(!commands.isEmpty()){
   int key=commands.removeFirst();
   if(key==KeyEvent.KEYCODE_MEDIA_PAUSE)HeadUnitRuntime.explicitPause(this);else local.spotify.close.PlaybackGate.userPlay();
   if(key==126)controller.getTransportControls().play();else if(key==127)controller.getTransportControls().pause();else if(key==87)controller.getTransportControls().skipToNext();else if(key==88)controller.getTransportControls().skipToPrevious();
   else {long now=SystemClock.uptimeMillis();controller.dispatchMediaButtonEvent(new KeyEvent(now,now,KeyEvent.ACTION_DOWN,key,0));controller.dispatchMediaButtonEvent(new KeyEvent(now,now,KeyEvent.ACTION_UP,key,0));}
  }
 }
 public int onStartCommand(Intent i,int flags,int id){if(i!=null && i.hasExtra("key"))commands.add(i.getIntExtra("key",0));flush();return START_NOT_STICKY;}
 public void onDestroy(){main.removeCallbacksAndMessages(null);if(controller!=null)controller.unregisterCallback(callback);if(browser!=null)browser.disconnect();stopForeground(true);super.onDestroy();}
 public IBinder onBind(Intent i){return null;}
}
