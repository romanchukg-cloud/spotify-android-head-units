package hu.resume;
import android.app.*;import android.content.*;import android.os.*;import android.media.session.*;
import local.spotify.unified.*;import local.spotify.close.PlaybackGate;
import com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService;
public class ResumeTests extends Instrumentation {
 private int passed;private Context c;
 private void ok(boolean value,String label){if(!value)throw new AssertionError(label);passed++;android.util.Log.i("HU_RESUME","PASS "+label);}
 private void waitFor(java.util.concurrent.Callable<Boolean> condition,long timeout)throws Exception{long end=SystemClock.elapsedRealtime()+timeout;while(!condition.call()){if(SystemClock.elapsedRealtime()>=end)throw new AssertionError("Timed out waiting for fixture");Thread.sleep(100);}}
 private void state(int value,long actions)throws Exception{AutomotiveMediaBrowserService.set(value,actions);Thread.sleep(700);}
 public void onCreate(Bundle b){start();}
 public void onStart(){Bundle out=new Bundle();c=getTargetContext();try{
  HeadUnitPreferences.init(c);HeadUnitRuntime.init(c);PlaybackGate.init(c);HeadUnitPreferences.store().edit().clear().putBoolean("resume",true).putBoolean("boot_restore_pending",true).putBoolean("was_playing",true).commit();
  ok(!HeadUnitPreferences.playOnOpen(),"opening autoplay is opt-in");
  HeadUnitRuntime.playback(new PlaybackState.Builder().setState(0,0,0).build());
  ok(HeadUnitPreferences.store().getBoolean("was_playing",false),"startup NONE preserves previous playback intent");
  HeadUnitRuntime.playback(new PlaybackState.Builder().setState(8,0,0).build());
  ok(HeadUnitPreferences.store().getBoolean("boot_restore_pending",false),"CONNECTING does not acknowledge boot resume");
  HeadUnitRuntime.requestResume(c,"boot");waitFor(()->AutomotiveMediaBrowserService.session!=null,10000);Thread.sleep(1000);
  ok(HeadUnitPreferences.store().getBoolean("boot_restore_pending",false),"accepted foreground service keeps boot request pending");
  ok(AutomotiveMediaBrowserService.plays==0,"empty session receives no premature PLAY");
  state(2,0);ok(AutomotiveMediaBrowserService.plays==0,"paused session without PLAY capability waits");
  state(2,PlaybackState.ACTION_PLAY);waitFor(()->AutomotiveMediaBrowserService.plays>=1,5000);
  ok(HeadUnitRuntime.resumeActive(),"first unacknowledged PLAY remains pending");
  waitFor(()->AutomotiveMediaBrowserService.plays>=2,8000);ok(true,"lost first PLAY retried after readiness");
  state(6,PlaybackState.ACTION_PLAY);int before=AutomotiveMediaBrowserService.plays;Thread.sleep(5500);
  ok(AutomotiveMediaBrowserService.plays==before && HeadUnitPreferences.store().getBoolean("boot_restore_pending",false),"BUFFERING waits without retry or false acknowledgement");
  state(3,PlaybackState.ACTION_PAUSE);waitFor(()->!HeadUnitRuntime.resumeActive(),3000);
  ok(!HeadUnitPreferences.store().getBoolean("boot_restore_pending",true),"only PLAYING acknowledges boot restore");
  state(2,PlaybackState.ACTION_PLAY);int previousPlays=before;HeadUnitRuntime.requestResume(c,"boot");waitFor(()->AutomotiveMediaBrowserService.plays>previousPlays,5000);
  HeadUnitRuntime.explicitPause(c);before=AutomotiveMediaBrowserService.plays;Thread.sleep(6000);
  ok(AutomotiveMediaBrowserService.plays==before && !HeadUnitRuntime.resumeActive(),"explicit pause cancels all further automatic PLAY");
  HeadUnitPreferences.store().edit().putString("resume_source","boot").putLong("resume_started",SystemClock.elapsedRealtime()-AutoResumePolicy.WINDOW_MS).putBoolean("boot_restore_pending",true).commit();
  ok(!HeadUnitRuntime.resumeActive() && HeadUnitPreferences.store().getBoolean("boot_restore_pending",false),"bounded timeout retains boot intent for visible launch fallback");
  HeadUnitPreferences.store().edit().putString("resume_source","boot").putLong("resume_started",SystemClock.elapsedRealtime()).putBoolean("resume",false).commit();ok(!HeadUnitRuntime.resumeActive(),"disabled setting cancels pending retry");
  HeadUnitPreferences.store().edit().putBoolean("resume",true).putString("resume_source","boot").putLong("resume_started",SystemClock.elapsedRealtime()).commit();PlaybackGate.beginAccountTransition(c);
  ok(!HeadUnitRuntime.resumeActive(),"account transition clears automatic playback");PlaybackGate.abortAccountTransition(c);
  HeadUnitPreferences.store().edit().putBoolean("was_playing",false).commit();HeadUnitRuntime.playback(new PlaybackState.Builder().setState(8,0,0).build());
  ok(!HeadUnitPreferences.store().getBoolean("was_playing",true),"logged-out CONNECTING is not remembered as playback");
  Bundle disabled=new Bundle();disabled.putBoolean("resume",false);HeadUnitPreferences.store().edit().putBoolean("boot_restore_pending",true).commit();HeadUnitPreferences.write(disabled);
  ok(!HeadUnitPreferences.store().getBoolean("boot_restore_pending",true),"disabling boot resume clears stale boot intent");
  ok(!AutoResumePolicy.shouldPlay(7,4,10000,-1) && !AutoResumePolicy.shouldPlay(0,4,10000,-1),"ERROR and NONE never dispatch automatic PLAY");
  ok(!AutoResumePolicy.shouldPlay(2,4,12000,10000) && AutoResumePolicy.shouldPlay(2,4,15000,10000),"retry respects five-second backoff");
  ok(AutoResumePolicy.expired(500,1000),"monotonic clock reset expires old requests");
  out.putString("stream","PASS resume fixture checks="+passed+"\n");finish(-1,out);
 }catch(Throwable e){android.util.Log.e("HU_RESUME","FAIL",e);out.putString("stream","FAIL "+e+"\n");finish(1,out);}}
}
