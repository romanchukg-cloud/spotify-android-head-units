package local.spotify.unified;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import java.util.ArrayList;
/** The sleep timer for our interface: its state and its menu (HeadUnitSleep runs it in the music process). */
final class HuSleep {
 private HuSleep(){}
 private static Bundle state(Context c){try{return HeadUnitSleep.state(c);}catch(RuntimeException e){return new Bundle();}}
 /** Time left in milliseconds, or 0 when the timer is off. */
 static long left(Context c){return state(c).getLong("left");}
 static boolean endOfTrack(Context c){return state(c).getBoolean("end_of_track");}
 /** 15, 30, 45 minutes, an hour, the end of the current track; Off while it runs. */
 static void menu(HuUi ui,HuSession session,Runnable changed){
  Context c=ui.activity;ArrayList<String> labels=new ArrayList<>();ArrayList<Long> delays=new ArrayList<>();
  if(left(c)>0){labels.add(HuText.get("sleep_off"));delays.add(0L);}
  for(int m:new int[]{15,30,45}){labels.add(String.format(HuText.get("minutes"),m));delays.add(m*60000L);}
  labels.add(HuText.get("hour"));delays.add(3600000L);
  long d=session.duration(),track=d>0?Math.max(0,d-session.position())+1000:-1;
  if(track>0){labels.add(HuText.get("end_of_track"));delays.add(-1L);}
  new AlertDialog.Builder(c).setTitle(HuText.get("sleep_timer")).setItems(labels.toArray(new String[0]),(dialog,which)->{
   long delay=delays.get(which);
   try{if(delay==0)HeadUnitSleep.cancel(c);else if(delay<0)HeadUnitSleep.set(c,track,true);else HeadUnitSleep.set(c,delay,false);}catch(RuntimeException e){android.util.Log.w("SpotifyHU","Sleep timer unavailable",e);}
   if(changed!=null)changed.run();
  }).show();
 }
}
