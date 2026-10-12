package local.spotify.unified;
import android.content.*;
import android.os.*;
import android.util.Log;
/** Playback intent belongs to the backend, not the UI process. */
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
  HeadUnitPreferences.init(c);broadcast(c,i);String action=i==null?null:i.getAction();SharedPreferences p=HeadUnitPreferences.store();
  if(Intent.ACTION_SCREEN_OFF.equals(action)){p.edit().putLong("screen_off",SystemClock.elapsedRealtime()).putBoolean("screen_playing",p.getBoolean("was_playing",false)).commit();}
  else if(Intent.ACTION_SCREEN_ON.equals(action)){
   long off=p.getLong("screen_off",-1);boolean resume=HeadUnitPreferences.resume() && off>=0 && SystemClock.elapsedRealtime()-off>=120000 && p.getBoolean("screen_playing",false);
   p.edit().remove("screen_off").remove("screen_playing").commit();if(resume)requestResume(c,"screen");
  }else if(Intent.ACTION_SHUTDOWN.equals(action)){p.edit().putBoolean("shutdown_playing",p.getBoolean("was_playing",false)).commit();}
  else if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
   if(HeadUnitPreferences.resume() && p.getBoolean("boot_restore_pending",false))requestResume(c,"boot");
  }else Log.w("SpotifyHU","Unknown broadcast action="+action);
 }
 public static void playerOpened(Context c){
  HeadUnitPreferences.init(c);
  if(local.spotify.close.PlaybackGate.accountTransition())return;
  if(HeadUnitPreferences.resume() && HeadUnitPreferences.store().getBoolean("boot_restore_pending",false))requestResume(c,"boot");
  else if(resumeActive())HeadUnitPlaybackService.resume(c);
  else if(HeadUnitPreferences.playOnOpen())requestResume(c,"open");
 }
 public static void requestResume(Context c,String source){
  HeadUnitPreferences.init(c);local.spotify.close.PlaybackGate.init(c);
  if(local.spotify.close.PlaybackGate.accountTransition())return;
  synchronized(HeadUnitRuntime.class){
   SharedPreferences p=HeadUnitPreferences.store();
   if(!resumeActive())p.edit().putLong("resume_generation",p.getLong("resume_generation",0)+1).putString("resume_source",source).putLong("resume_started",SystemClock.elapsedRealtime()).commit();
  }
  Log.i("SpotifyHU","Auto resume requested source="+source);HeadUnitPlaybackService.resume(c);
 }
 public static synchronized boolean resumeActive(){
  SharedPreferences p=HeadUnitPreferences.store();String source=p.getString("resume_source","");if(source.isEmpty())return false;
  boolean enabled=source.equals("open")?HeadUnitPreferences.playOnOpen():HeadUnitPreferences.resume();
  if(!enabled || AutoResumePolicy.expired(SystemClock.elapsedRealtime(),p.getLong("resume_started",-1))){
   p.edit().remove("resume_source").remove("resume_started").putLong("resume_generation",p.getLong("resume_generation",0)+1).commit();Log.i("SpotifyHU","Auto resume ended: "+(enabled?"timeout":"disabled"));return false;
  }return true;
 }
 public static long resumeGeneration(){return HeadUnitPreferences.store().getLong("resume_generation",0);}
 public static synchronized void confirmedPlaying(){
  SharedPreferences p=HeadUnitPreferences.store();boolean pending=p.contains("resume_source") || p.getBoolean("boot_restore_pending",false);
  p.edit().putBoolean("was_playing",true).putBoolean("boot_restore_pending",false).remove("resume_source").remove("resume_started").commit();
  if(pending)Log.i("SpotifyHU","Auto resume confirmed PLAYING");
 }
 public static synchronized void playback(Object value){
  if(context==null)return;int state=0;
  try{if(value!=null)state=(Integer)value.getClass().getMethod("getState").invoke(value);}catch(ReflectiveOperationException e){Log.w("SpotifyHU","Cannot read playback state");return;}
  if(state==lastState)return;lastState=state;
  SharedPreferences.Editor e=HeadUnitPreferences.store().edit().putInt("last_state",state);
  // Initial NONE/STOPPED/CONNECTING must not erase the previous drive's intent.
  if(!local.spotify.close.PlaybackGate.accountTransition()){
   if(state==3)confirmedPlaying();else if(state==2 && !resumeActive())e.putBoolean("was_playing",false);
  }
  e.commit();Log.i("SpotifyHU","Playback state="+state);
  if(AutoResumePolicy.busy(state))HeadUnitPlaybackService.keepAlive(context);
 }
 public static void explicitPause(){if(context!=null)explicitPause(context);}
 public static synchronized void explicitPause(Context c){
  HeadUnitPreferences.init(c);SharedPreferences p=HeadUnitPreferences.store();
  p.edit().putBoolean("was_playing",false).putBoolean("screen_playing",false).putBoolean("boot_restore_pending",false).remove("shutdown_playing").remove("resume_source").remove("resume_started").putLong("resume_generation",p.getLong("resume_generation",0)+1).commit();
 }
}
