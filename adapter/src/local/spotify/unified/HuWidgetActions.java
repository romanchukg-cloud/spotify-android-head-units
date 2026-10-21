package local.spotify.unified;
import android.content.*;
/**
 * The widgets' buttons (not exported: only our own PendingIntents reach it): previous,
 * play or pause, next, through the playback service as the media keys go, so a pause counts as
 * the user's and the backend starts if it is not running.
 */
public final class HuWidgetActions extends BroadcastReceiver {
 static final String ACTION="local.spotify.widget.KEY";
 public void onReceive(Context c,Intent i){
  if(i==null || !ACTION.equals(i.getAction()))return;
  HeadUnitPlaybackService.command(c,i.getIntExtra("key",0),"widget");
 }
}
