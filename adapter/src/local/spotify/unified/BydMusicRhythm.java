package local.spotify.unified;

import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import java.lang.reflect.Method;

/** Owns one BYD output-mix rhythm session in the Spotify backend process. */
public final class BydMusicRhythm {
 private static Controller controller;
 static final long GAP_MS=2000;
 public static synchronized void init(){
  if(controller!=null || !HeadUnitProfile.isByd())return;
  HandlerThread thread=new HandlerThread("SpotifyCarRhythm");thread.start();
  controller=new Controller(new Handler(thread.getLooper()),new DirectOutput());
 }
 public static synchronized void playback(int state){if(controller!=null)controller.playback(state);}
 public static synchronized void stop(){if(controller!=null)controller.stop();}

 interface Output {void set(boolean playing)throws Exception;}
 /** All state and Binder calls are serialized on the worker, including delayed stop. */
 static final class Controller {
  private final Handler worker;private final Output output;
  private boolean requested,owned,stopPending;
  private final Runnable endGap=()->{stopPending=false;requested=false;apply(false);};
  Controller(Handler worker,Output output){this.worker=worker;this.output=output;}
  void playback(int state){worker.post(()->{
   if(state==PlaybackState.STATE_PLAYING){
    worker.removeCallbacks(endGap);stopPending=false;requested=true;apply(true);
   }else if(state==PlaybackState.STATE_PAUSED || state==PlaybackState.STATE_BUFFERING || state==PlaybackState.STATE_CONNECTING || state==PlaybackState.STATE_SKIPPING_TO_NEXT || state==PlaybackState.STATE_SKIPPING_TO_PREVIOUS || state==PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM){
    // Repeated paused/buffering notifications must not extend this deadline.
    if(requested && !stopPending){stopPending=true;worker.postDelayed(endGap,GAP_MS);}
   }else stopOnWorker();
  });}
  void stop(){worker.post(this::stopOnWorker);}
  private void stopOnWorker(){worker.removeCallbacks(endGap);stopPending=false;requested=false;apply(false);}
  private void apply(boolean playing){
   if(playing==owned)return;
   // Even an uncertain start must be paired with a stop using the same token.
   if(playing)owned=true;
   try{output.set(playing);owned=playing;Log.i("SpotifyCarRhythm",playing?"start accepted":"stop accepted");}
   catch(Exception|LinkageError e){Log.w("SpotifyCarRhythm",(playing?"start":"stop")+" unavailable",e);}
  }
 }

 private static final class DirectOutput implements Output {
  private static final String MEDIA_CENTRE="com.byd.mediacenter";
  private final IBinder token=new Binder();
  public void set(boolean playing)throws Exception{
   IBinder binder=(IBinder)Class.forName("android.os.ServiceManager").getMethod("getService",String.class).invoke(null,"audio");
   if(binder==null)throw new IllegalStateException("Audio service unavailable");
   Object audio=Class.forName("android.media.IAudioService$Stub").getMethod("asInterface",IBinder.class).invoke(null,binder);
   if(audio==null)throw new IllegalStateException("Audio interface unavailable");
   Method method=audio.getClass().getMethod(playing?"startAudioOutput":"stopAudioOutput",String.class,IBinder.class);
   // The vehicle owns mode, colours and brightness. Only acquire/release its session.
   method.invoke(audio,MEDIA_CENTRE,token);
  }
 }
}
