package hu.browser;
import android.app.*;import android.content.*;import android.os.*;import android.widget.*;import android.support.v4.media.MediaBrowserCompat;
public class Main extends Activity {
 MediaBrowserCompat browser;TextView text;boolean finished;
 public void onCreate(Bundle b){super.onCreate(b);text=new TextView(this);text.setText("Connecting independent MediaBrowserCompat…");text.setTextSize(22);setContentView(text);
  try{getContentResolver().call(android.net.Uri.parse("content://com.spotify.music.local.headunit"),"get",null,null);report("FAIL private settings accessible");}catch(SecurityException expected){android.util.Log.i("HU_BROWSER","PASS private settings inaccessible");}
  if(getIntent().hasExtra("key")){int key=getIntent().getIntExtra("key",126);sendBroadcast(new Intent(Intent.ACTION_MEDIA_BUTTON).setClassName("com.spotify.music","local.spotify.unified.HeadUnitMediaReceiver").putExtra(Intent.EXTRA_KEY_EVENT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,key)));report("SENT media key="+key);return;}
  browser=new MediaBrowserCompat(this,new ComponentName("com.spotify.music","com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService"),new MediaBrowserCompat.ConnectionCallback(){
   public void onConnected(){report("CONNECTED root="+browser.getRoot());}
   public void onConnectionFailed(){report("REJECTED connection");}
   public void onConnectionSuspended(){report("SUSPENDED connection");}
  },null);browser.connect();new Handler().postDelayed(()->{if(!finished)report("TIMEOUT connection");},20000);
 }
 void report(String s){finished=true;text.setText(s);android.util.Log.i("HU_BROWSER",s);}
 protected void onDestroy(){if(browser!=null)browser.disconnect();super.onDestroy();}
}
