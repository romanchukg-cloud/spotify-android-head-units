package local.spotify.unified;
import android.content.*;
/** Private target of the PackageInstaller status PendingIntent; the action is a per-session nonce. */
public final class UpdateResultReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){PendingResult pending=goAsync();try{AppUpdates.result(c,i,pending::finish);}catch(Throwable t){pending.finish();}}
}
