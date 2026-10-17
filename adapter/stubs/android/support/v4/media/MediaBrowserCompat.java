package android.support.v4.media;
import android.content.*;import android.os.*;import java.util.List;
/** Compile-time stub of the Spotify backend's own support-library browser; the real class ships in the backend DEX. */
public class MediaBrowserCompat {
 public MediaBrowserCompat(Context c,ComponentName n,ConnectionCallback cb,Bundle b){}
 public android.support.v4.media.session.MediaSessionCompat.Token getSessionToken(){return null;}
 public void connect(){}public void disconnect(){}public boolean isConnected(){return false;}public String getRoot(){return null;}public Bundle getExtras(){return null;}
 public void subscribe(String id,SubscriptionCallback cb){}public void unsubscribe(String id){}
 public void getItem(String id,ItemCallback cb){}
 public void sendCustomAction(String action,Bundle extras,CustomActionCallback cb){}
 public void search(String query,Bundle extras,SearchCallback cb){}
 public static class ConnectionCallback {public void onConnected(){}public void onConnectionFailed(){}public void onConnectionSuspended(){}}
 public static abstract class SubscriptionCallback {public void onChildrenLoaded(String parent,List<MediaItem> children){}public void onError(String parent){}}
 public static abstract class ItemCallback {public void onItemLoaded(MediaItem item){}public void onError(String id){}}
 public static abstract class SearchCallback {public void onSearchResult(String query,Bundle extras,List<MediaItem> items){}public void onError(String query,Bundle extras){}}
 public static abstract class CustomActionCallback {public void onProgressUpdate(String action,Bundle extras,Bundle data){}public void onResult(String action,Bundle extras,Bundle result){}public void onError(String action,Bundle extras,Bundle data){}}
 public static class MediaItem implements Parcelable {
  public static final Parcelable.Creator<MediaItem> CREATOR=null;
  public String getMediaId(){return null;}public MediaDescriptionCompat getDescription(){return null;}public boolean isBrowsable(){return false;}public boolean isPlayable(){return false;}
  public int describeContents(){return 0;}public void writeToParcel(Parcel p,int flags){}
 }
}
