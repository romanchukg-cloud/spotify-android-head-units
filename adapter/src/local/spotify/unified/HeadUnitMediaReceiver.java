package local.spotify.unified;
import android.content.*;
import android.view.KeyEvent;
import android.util.Log;
public final class HeadUnitMediaReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){
  HeadUnitRuntime.broadcast(c,i);
  if(i!=null && Intent.ACTION_MEDIA_BUTTON.equals(i.getAction())){
   KeyEvent e=i.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
   if(e==null){Log.w("SpotifyHU","Media broadcast without key event");return;}
   Log.i("SpotifyHU","Media key="+e.getKeyCode()+" action="+e.getAction()+" repeat="+e.getRepeatCount());
   if(e.getAction()==KeyEvent.ACTION_DOWN && e.getRepeatCount()==0)HeadUnitPlaybackService.command(c,e.getKeyCode(),"media-button");
  }else if(i!=null && Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()))HeadUnitRuntime.receive(c,i);
  else Log.w("SpotifyHU","Unknown manifest broadcast action="+(i==null?"null":i.getAction()));
 }
}
