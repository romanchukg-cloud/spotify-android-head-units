package android.support.v4.media.session;
import android.os.Bundle;import java.util.List;
/** Compile-time stub of the Spotify backend's own support-library playback state. */
public class PlaybackStateCompat {
 public int getState(){return 0;}public long getPosition(){return 0;}public long getLastPositionUpdateTime(){return 0;}
 public float getPlaybackSpeed(){return 0;}public long getActions(){return 0;}public long getActiveQueueItemId(){return 0;}
 public List<CustomAction> getCustomActions(){return null;}
 public int getErrorCode(){return 0;}public CharSequence getErrorMessage(){return null;}public Bundle getExtras(){return null;}
 public static final class CustomAction {
  public String getAction(){return null;}public CharSequence getName(){return null;}public int getIcon(){return 0;}public Bundle getExtras(){return null;}
 }
}
