package local.spotify.unified;
import android.content.*;
import android.os.*;
import android.util.Log;
import java.lang.reflect.*;
public final class HeadUnitRuntime {
 private static Context context;private static int lastState=-1;
 public static synchronized void init(Context c){
  if(context!=null)return;context=c.getApplicationContext();HeadUnitPreferences.init(context);
  IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_SCREEN_ON);f.addAction(Intent.ACTION_SCREEN_OFF);f.addAction(Intent.ACTION_SHUTDOWN);
  BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){receive(c,i);}};
  if(Build.VERSION.SDK_INT>=33)context.registerReceiver(receiver,f,Context.RECEIVER_NOT_EXPORTED);else context.registerReceiver(receiver,f);
 }
 public static void broadcast(Context c,Intent i){Log.i("SpotifyHU","Broadcast action="+(i==null?"null":i.getAction()));}
 public static void receive(Context c,Intent i){
  HeadUnitPreferences.init(c);broadcast(c,i);String action=i==null?null:i.getAction();
  android.content.SharedPreferences p=HeadUnitPreferences.store();
  if(Intent.ACTION_SCREEN_OFF.equals(action)){p.edit().putLong("screen_off",SystemClock.elapsedRealtime()).putBoolean("screen_playing",p.getBoolean("was_playing",false)).commit();}
  else if(Intent.ACTION_SCREEN_ON.equals(action)){
   long off=p.getLong("screen_off",-1);boolean resume=HeadUnitPreferences.resume() && off>=0 && SystemClock.elapsedRealtime()-off>=120000 && p.getBoolean("screen_playing",false);
   p.edit().remove("screen_off").remove("screen_playing").commit();if(resume)HeadUnitPlaybackService.command(c,android.view.KeyEvent.KEYCODE_MEDIA_PLAY,"screen-resume");
  }else if(Intent.ACTION_SHUTDOWN.equals(action)){p.edit().putBoolean("shutdown_playing",p.getBoolean("was_playing",false)).commit();}
  else if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
   if(HeadUnitPreferences.resume() && p.getBoolean("boot_restore_pending",false)){
    if(HeadUnitPlaybackService.command(c,android.view.KeyEvent.KEYCODE_MEDIA_PLAY,"boot-resume"))p.edit().putBoolean("boot_restore_pending",false).commit();
   }
  }else Log.w("SpotifyHU","Unknown broadcast action="+action);
 }
 public static synchronized void playback(Object value){
  if(context==null)return;int state=0;
  try{if(value!=null)state=(Integer)value.getClass().getMethod("getState").invoke(value);}catch(ReflectiveOperationException e){Log.w("SpotifyHU","Cannot read playback state");return;}
  if(state==lastState)return;lastState=state;
  boolean playing=state==3 || state==6 || state==8 || state==9 || state==10 || state==11;
  HeadUnitPreferences.store().edit().putBoolean("was_playing",playing).putInt("last_state",state).commit();Log.i("SpotifyHU","Playback state="+state);
  if(playing)HeadUnitPlaybackService.keepAlive(context);
 }
 public static void explicitPause(){if(context!=null)explicitPause(context);}
 public static void explicitPause(Context c){HeadUnitPreferences.init(c);HeadUnitPreferences.store().edit().putBoolean("was_playing",false).putBoolean("screen_playing",false).putBoolean("boot_restore_pending",false).remove("shutdown_playing").commit();}
}
