package android.support.v4.media;
import android.content.*;import android.os.Bundle;
public class MediaBrowserCompat {
 public MediaBrowserCompat(Context c,ComponentName n,ConnectionCallback cb,Bundle b){}
 public void connect(){}public void disconnect(){}public String getRoot(){return null;}
 public static class ConnectionCallback {public void onConnected(){}public void onConnectionFailed(){}public void onConnectionSuspended(){}}
}
