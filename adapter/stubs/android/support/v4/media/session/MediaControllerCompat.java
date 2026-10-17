package android.support.v4.media.session;
import android.content.Context;import android.os.*;import android.support.v4.media.MediaMetadataCompat;import java.util.List;
/** Compile-time stub of the Spotify backend's own support-library controller. */
public class MediaControllerCompat {
 public MediaControllerCompat(Context c,MediaSessionCompat.Token token){}
 public MediaMetadataCompat getMetadata(){return null;}public PlaybackStateCompat getPlaybackState(){return null;}
 public int getRepeatMode(){return 0;}public int getShuffleMode(){return 0;}
 public TransportControls getTransportControls(){return null;}
 public void registerCallback(Callback cb,Handler h){}public void unregisterCallback(Callback cb){}
 public static abstract class TransportControls {
  public abstract void play();public abstract void pause();public abstract void skipToNext();public abstract void skipToPrevious();
  public abstract void seekTo(long position);public abstract void setRepeatMode(int mode);public abstract void setShuffleMode(int mode);
  public abstract void skipToQueueItem(long id);public abstract void sendCustomAction(String action,Bundle extras);
  public abstract void playFromMediaId(String mediaId,Bundle extras);public abstract void playFromSearch(String query,Bundle extras);
 }
 public static abstract class Callback {
  public void onSessionReady(){}public void onSessionDestroyed(){}
  public void onPlaybackStateChanged(PlaybackStateCompat state){}public void onMetadataChanged(MediaMetadataCompat metadata){}
  public void onQueueChanged(List<?> queue){}public void onRepeatModeChanged(int mode){}public void onShuffleModeChanged(int mode){}
 }
}
