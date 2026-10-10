package local.spotify.close;
import android.app.*;
import android.content.*;
import android.media.session.*;
import android.os.*;
import android.util.Log;
import java.util.*;
public final class PlaybackGate {
  private static Context context;
  private static volatile boolean closed,accountTransition;
  private static final Map<MediaSession,PendingIntent> sessions = new WeakHashMap<>();
  private static IBinder uiLease;
  private static IBinder.DeathRecipient uiDeath;
  public static synchronized void openForUi(Context c, final IBinder token) {
    if(token==null) throw new IllegalArgumentException("Missing UI lifetime token");
    if(uiLease!=token) {
      if(uiLease!=null && uiDeath!=null) uiLease.unlinkToDeath(uiDeath,0);
      uiLease=token;
      uiDeath=new IBinder.DeathRecipient(){ public void binderDied(){
        synchronized(PlaybackGate.class){
          if(uiLease!=token) return;
          uiLease=null; uiDeath=null;

          // A crash or LMK is not an explicit task removal.
          Log.i("SpotifyHU","UI binder died; playback left unchanged");
        }
      }};
      try { token.linkToDeath(uiDeath,0); }
      catch(RemoteException e){ uiLease=null; uiDeath=null; Log.i("SpotifyHU","UI binder already dead; playback left unchanged"); return; }
    }
    change(c,false);
  }
  public static synchronized void init(Context c) {
    if(context==null) { context=c.getApplicationContext(); local.spotify.unified.HeadUnitPreferences.init(context); closed=context.getSharedPreferences("local_close_gate",0).getBoolean("closed",false); }
  }
  public static boolean trustedLocalUi(Context c,int uid,String name) {
    if(!local.spotify.unified.HeadUnitProfile.isByd() || !"com.vivid.music.byd".equals(name)) return false;
    try {
      android.content.pm.PackageManager pm=c.getPackageManager();
      return pm.getApplicationInfo(name,0).uid==uid &&
        pm.checkSignatures(android.os.Process.myUid(),uid)==android.content.pm.PackageManager.SIGNATURE_MATCH;
    } catch(android.content.pm.PackageManager.NameNotFoundException e) { return false; }
  }
  public static boolean trustedLocalUi(Context c,String name) {
    if(!local.spotify.unified.HeadUnitProfile.isByd() || !"com.vivid.music.byd".equals(name)) return false;
    try { return trustedLocalUi(c,c.getPackageManager().getApplicationInfo(name,0).uid,name); }
    catch(android.content.pm.PackageManager.NameNotFoundException e) { return false; }
  }
  public static boolean accountTransition() { return accountTransition || (context!=null && local.spotify.unified.DriverProfiles.restartPending(context)); }
  public static synchronized void beginAccountTransition(Context c) { init(c);accountTransition=true;local.spotify.unified.HeadUnitRuntime.explicitPause(c);change(c,true); }
  public static synchronized void abortAccountTransition(Context c) { accountTransition=false;change(c,false); }
  public static boolean blocked() { return closed || accountTransition(); }
  public static boolean blocked(Context c) { init(c); return closed || accountTransition(); }
  public static synchronized void register(Context c,MediaSession session) { init(c); sessions.put(session,mediaReceiver(c)); }
  public static synchronized void active(MediaSession session,boolean value) { session.setActive(value); }
  public static synchronized void receiver(MediaSession session,PendingIntent receiver) { if(receiver==null)receiver=mediaReceiver(context); sessions.put(session,receiver); session.setMediaButtonReceiver(receiver); }
  public static PendingIntent mediaReceiver(Context c) {
    Intent i=new Intent(Intent.ACTION_MEDIA_BUTTON).setClass(c,local.spotify.unified.HeadUnitMediaReceiver.class);
    return PendingIntent.getBroadcast(c,73,i,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0));
  }
  public static void userPlay() { if(!accountTransition() && context!=null && closed) change(context,false); }
  public static void mediaButton(Intent i) {
    android.view.KeyEvent e=i==null?null:i.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
    if(e==null)return;Log.i("SpotifyHU","Session key="+e.getKeyCode()+" action="+e.getAction());
    if(e.getAction()==android.view.KeyEvent.ACTION_DOWN && local.spotify.unified.HeadUnitPlaybackService.supported(e.getKeyCode()) && e.getKeyCode()!=android.view.KeyEvent.KEYCODE_MEDIA_PAUSE)userPlay();
  }
  public static synchronized void change(Context c,boolean value) {
    init(c);
    if(!value && accountTransition())return;
    // Commit before acknowledging the synchronous cross-package call.
    if(!context.getSharedPreferences("local_close_gate",0).edit().putBoolean("closed",value).commit()) throw new IllegalStateException("Cannot persist playback gate");
    closed=value;
    for(Map.Entry<MediaSession,PendingIntent> item:sessions.entrySet()) {
      MediaSession s=item.getKey();
      if(value) { s.getController().getTransportControls().pause(); }
      else { s.setMediaButtonReceiver(item.getValue()); s.setActive(true); }
    }
    Log.i("SpotifyCloseGate",value?"CLOSED: paused; media controls remain available":"OPEN: playback commands enabled");
  }
}
