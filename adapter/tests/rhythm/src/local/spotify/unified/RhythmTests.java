package local.spotify.unified;
import android.app.Instrumentation;
import android.media.session.PlaybackState;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.Field;
import local.spotify.close.PlaybackGate;

/** Synthetic output only: never invokes the car's audio service or changes login. */
public final class RhythmTests extends Instrumentation {
 private int passed;private HandlerThread thread;private Handler worker;
 private void ok(boolean value,String label){if(!value)throw new AssertionError(label);passed++;android.util.Log.i("HU_RHYTHM","PASS "+label);}
 private void drain()throws Exception{CountDownLatch done=new CountDownLatch(1);worker.post(done::countDown);if(!done.await(5,TimeUnit.SECONDS))throw new AssertionError("Worker blocked");}
 private static final class Fake implements BydMusicRhythm.Output {
  final List<Boolean> calls=Collections.synchronizedList(new ArrayList<>());boolean offMain=true;boolean fail;
  public void set(boolean playing)throws Exception{offMain&=Looper.myLooper()!=Looper.getMainLooper();calls.add(playing);if(fail)throw new SecurityException("Synthetic unsupported firmware");}
 }
 private static void state(int value){HeadUnitRuntime.playback(new PlaybackState.Builder().setState(value,0,0).build());}
 public void onCreate(Bundle b){start();}
 public void onStart(){Bundle out=new Bundle();try{
  thread=new HandlerThread("RhythmFixture");thread.start();worker=new Handler(thread.getLooper());
  Fake fake=new Fake();BydMusicRhythm.Controller rhythm=new BydMusicRhythm.Controller(worker,fake);
  rhythm.playback(PlaybackState.STATE_BUFFERING);drain();ok(fake.calls.isEmpty(),"initial buffering does not start rhythm");
  rhythm.playback(PlaybackState.STATE_PLAYING);rhythm.playback(PlaybackState.STATE_PLAYING);drain();ok(fake.calls.equals(Arrays.asList(true)),"PLAYING starts once, repeated updates deduplicated");
  rhythm.playback(PlaybackState.STATE_PAUSED);Thread.sleep(400);rhythm.playback(PlaybackState.STATE_PLAYING);Thread.sleep(2200);drain();ok(fake.calls.equals(Arrays.asList(true)),"short gap followed by PLAYING cancels pending stop");
  rhythm.playback(PlaybackState.STATE_BUFFERING);Thread.sleep(1200);rhythm.playback(PlaybackState.STATE_BUFFERING);Thread.sleep(1100);drain();ok(fake.calls.equals(Arrays.asList(true,false)),"repeated buffering cannot postpone two-second stop");
  rhythm.playback(PlaybackState.STATE_PLAYING);rhythm.stop();drain();ok(fake.calls.equals(Arrays.asList(true,false,true,false)),"explicit stop is immediate and ordered after start");
  rhythm.playback(PlaybackState.STATE_PLAYING);rhythm.playback(PlaybackState.STATE_STOPPED);drain();ok(!fake.calls.get(fake.calls.size()-1),"STOPPED releases without debounce");
  rhythm.playback(PlaybackState.STATE_PLAYING);rhythm.playback(PlaybackState.STATE_ERROR);drain();ok(!fake.calls.get(fake.calls.size()-1),"ERROR releases rhythm");
  rhythm.playback(PlaybackState.STATE_PLAYING);rhythm.playback(PlaybackState.STATE_PAUSED);Thread.sleep(2200);drain();ok(!fake.calls.get(fake.calls.size()-1),"confirmed pause stops after gap deadline");
  ok(fake.offMain,"all output calls run off main thread");
  Fake failed=new Fake();failed.fail=true;BydMusicRhythm.Controller unavailable=new BydMusicRhythm.Controller(worker,failed);
  unavailable.playback(3);unavailable.playback(3);unavailable.stop();drain();ok(failed.calls.equals(Arrays.asList(true,false)),"failed start survives and attempts paired cleanup without callback spam");failed.fail=false;unavailable.stop();drain();ok(!failed.calls.get(failed.calls.size()-1),"failed stop can be retried");

  HeadUnitRuntime.init(getTargetContext());PlaybackGate.init(getTargetContext());
  Field field=BydMusicRhythm.class.getDeclaredField("controller");field.setAccessible(true);
  if(!HeadUnitProfile.isByd())ok(field.get(null)==null,"non-BYD runtime creates no vendor worker");
  field.set(null,rhythm);PlaybackGate.change(getTargetContext(),false);
  state(3);drain();ok(fake.calls.get(fake.calls.size()-1),"actual runtime PLAYING reaches rhythm owner");
  HeadUnitRuntime.explicitPause(getTargetContext());drain();ok(!fake.calls.get(fake.calls.size()-1),"actual explicit pause releases session");
  state(2);state(3);drain();PlaybackGate.change(getTargetContext(),true);drain();int count=fake.calls.size();state(3);drain();ok(!fake.calls.get(count-1) && fake.calls.size()==count,"closed gate stops rhythm and rejects stale PLAYING");
  PlaybackGate.change(getTargetContext(),false);state(2);state(3);drain();PlaybackGate.beginAccountTransition(getTargetContext());drain();count=fake.calls.size();state(3);drain();ok(!fake.calls.get(count-1) && fake.calls.size()==count,"account transition stops rhythm and rejects stale PLAYING");PlaybackGate.abortAccountTransition(getTargetContext());
  state(2);state(3);drain();HeadUnitRuntime.sessionEnded();drain();ok(!fake.calls.get(fake.calls.size()-1),"media-session release stops rhythm");
  state(3);drain();HeadUnitRuntime.receive(getTargetContext(),new android.content.Intent(android.content.Intent.ACTION_SHUTDOWN));drain();ok(!fake.calls.get(fake.calls.size()-1),"shutdown releases rhythm");
  field.set(null,null);out.putString("stream","PASS rhythm fixture checks="+passed+"\n");finish(-1,out);
 }catch(Throwable e){android.util.Log.e("HU_RHYTHM","FAIL",e);out.putString("stream","FAIL "+e+"\n");finish(1,out);}finally{if(thread!=null)thread.quitSafely();}}
}
