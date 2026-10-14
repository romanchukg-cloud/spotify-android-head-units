package local.spotify.unified;
import android.app.job.*;import android.content.*;import java.util.concurrent.*;
/** Native persisted scheduler; no permanent service or playback notification. */
public final class DiagnosticUploadJob extends JobService {
 private static final int JOB_ID=9157;private static final ExecutorService immediate=Executors.newSingleThreadExecutor();private final ExecutorService worker=Executors.newSingleThreadExecutor();private volatile boolean stopped;private DiagnosticHttps transport;
 public static void kick(Context c){Context app=c.getApplicationContext();immediate.execute(()->DiagnosticsQueue.process(app,new DiagnosticHttps()));}
 public static void schedule(Context c){try{JobScheduler jobs=c.getSystemService(JobScheduler.class);long next=DiagnosticsQueue.next(c),due=DiagnosticsQueue.maintenance(c);if(due==Long.MAX_VALUE){jobs.cancel(JOB_ID);return;}long delay=Math.max(10000,due-System.currentTimeMillis());jobs.schedule(new JobInfo.Builder(JOB_ID,new ComponentName(c,DiagnosticUploadJob.class)).setRequiredNetworkType(next==Long.MAX_VALUE?JobInfo.NETWORK_TYPE_NONE:JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setMinimumLatency(delay).build());}catch(RuntimeException ignored){}}
 public boolean onStartJob(JobParameters p){if(AppUpdates.owns(p.getJobId())){try{return AppUpdates.startJob(this,p);}catch(Throwable t){return false;}}stopped=false;transport=new DiagnosticHttps();worker.execute(()->{DiagnosticsQueue.process(this,transport,System.currentTimeMillis(),false);new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{if(!stopped)jobFinished(p,false);new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->schedule(getApplicationContext()),200);});});return true;}
 public boolean onStopJob(JobParameters p){if(AppUpdates.owns(p.getJobId())){AppUpdates.stopJob();return false;}stopped=true;if(transport!=null)transport.cancel();return false;}
 public void onDestroy(){stopped=true;if(transport!=null)transport.cancel();worker.shutdown();super.onDestroy();}
}
