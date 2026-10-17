package local.spotify.unified;
import android.content.*;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.*;
/**
 * Sleep timer: pauses Spotify after 15–60 minutes or at the end of the current track. The timer
 * runs in the music process, which stays alive while music plays (an inexact alarm could fire
 * minutes late, and exact alarms need a permission on Android 12+). The queue editor sets it
 * through the private settings provider (sleep_set, sleep_cancel, sleep_get); at the time it
 * pauses like the user would (HeadUnitRuntime.explicitPause, so no automatic resume follows).
 */
public final class HeadUnitSleep {
 private static final String SERVICE="com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService";
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static long at;private static boolean endOfTrack;private static Context app;
 private static final Runnable fire=()->{at=0;endOfTrack=false;pause();};
 private HeadUnitSleep(){}

 /** Provider calls in the music process. */
 static Bundle call(Context c,String method,Bundle extras){
  app=c.getApplicationContext();
  if("sleep_set".equals(method) && extras!=null){
   long delay=Math.max(1000,extras.getLong("delay"));main.removeCallbacks(fire);
   at=SystemClock.elapsedRealtime()+delay;endOfTrack=extras.getBoolean("end_of_track");main.postDelayed(fire,delay);
   android.util.Log.i("SpotifyHU","Sleep timer in "+delay/1000+" s"+(endOfTrack?" (end of track)":""));
  }else if("sleep_cancel".equals(method)){main.removeCallbacks(fire);at=0;endOfTrack=false;android.util.Log.i("SpotifyHU","Sleep timer off");}
  Bundle out=new Bundle();long left=Math.max(0,at-SystemClock.elapsedRealtime());
  out.putLong("left",left);out.putBoolean("end_of_track",left>0 && endOfTrack);return out;
 }
 /** Interface side: these go through the provider to the music process. */
 static Bundle set(Context c,long delay,boolean endOfTrack){Bundle b=new Bundle();b.putLong("delay",delay);b.putBoolean("end_of_track",endOfTrack);return HeadUnitSettings.call(c,"sleep_set",b);}
 static Bundle cancel(Context c){return HeadUnitSettings.call(c,"sleep_cancel",null);}
 static Bundle state(Context c){return HeadUnitSettings.call(c,"sleep_get",null);}
 /** Time to the end of the current track from the session's metadata and playback state, or -1. */
 static long trackLeft(MediaController controller){
  MediaMetadata m=controller==null?null:controller.getMetadata();PlaybackState s=controller==null?null:controller.getPlaybackState();
  if(m==null || s==null)return -1;long duration=m.getLong(MediaMetadata.METADATA_KEY_DURATION);if(duration<=0)return -1;
  long position=s.getPosition();
  if(s.getState()==PlaybackState.STATE_PLAYING)position+=(long)((SystemClock.elapsedRealtime()-s.getLastPositionUpdateTime())*s.getPlaybackSpeed());
  return Math.max(0,duration-position)+1000;
 }
 private static void pause(){
  if(app==null)return;android.util.Log.i("SpotifyHU","Sleep timer: pause");
  HeadUnitRuntime.explicitPause(app);
  final MediaBrowser[] holder=new MediaBrowser[1];
  holder[0]=new MediaBrowser(app,new ComponentName(app.getPackageName(),SERVICE),new MediaBrowser.ConnectionCallback(){
   public void onConnected(){
    try{new MediaController(app,holder[0].getSessionToken()).getTransportControls().pause();}catch(RuntimeException e){android.util.Log.w("SpotifyHU","Sleep timer pause failed",e);}
    main.post(holder[0]::disconnect);
   }
  },null);
  holder[0].connect();
 }
}
