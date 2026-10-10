package local.spotify.close;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.util.Log;
public final class TaskGuardService extends Service {
  private static final Uri CONTROL=Uri.parse("content://com.spotify.music.local.closecontrol");
  private static final IBinder UI_LEASE=new Binder();
  public static void opened(Context c) {
    try {
      Bundle extras=new Bundle(); extras.putBinder("uiLease",UI_LEASE);
      Bundle result=c.getContentResolver().call(CONTROL,"open",null,extras);
      if(result==null || result.getBoolean("closed",true)) throw new IllegalStateException("Spotify gate not opened");
      c.startForegroundService(new Intent(c,TaskGuardService.class));
    } catch(RuntimeException e) {
      Log.e("SpotifyCloseGate","Cannot enable playback: check BYD background startup permissions",e);
    }
  }
  public void onCreate(){
    super.onCreate();
    NotificationManager nm=getSystemService(NotificationManager.class);
    NotificationChannel ch=new NotificationChannel("spotify_task_guard",local.spotify.unified.HuText.get("channel"),NotificationManager.IMPORTANCE_LOW);
    nm.createNotificationChannel(ch);
    Notification n=new Notification.Builder(this,"spotify_task_guard").setSmallIcon(android.R.drawable.ic_media_play)
      .setContentTitle("Spotify").setContentText(local.spotify.unified.HuText.get("background")).setOngoing(true).build();
    startForeground(7044,n);
  }
  public int onStartCommand(Intent i,int f,int id){return START_NOT_STICKY;}
  public IBinder onBind(Intent i){return null;}
  public void onTaskRemoved(Intent root){
    try {
      // Removal belongs to the UI application. Home/navigation does not invoke this.
      Bundle b=getContentResolver().call(CONTROL,"close",null,null);
      Log.i("SpotifyCloseGate","UI task removed; backend closed="+(b!=null && b.getBoolean("closed")));
    } catch(RuntimeException e){Log.e("SpotifyCloseGate","Cannot close backend",e);}
    stopForeground(true); stopSelf();
  }
}
