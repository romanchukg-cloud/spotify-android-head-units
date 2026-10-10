package local.spotify.unified;

/** A visible coordinator waits for process death and an actual UI acknowledgement. */
public final class ProfileRestart implements Runnable {
 public interface Host {
  void stopOldProcesses();
  boolean oldProcessesStopped();
  void launchPlayer();
  void later(Runnable task,long delay);
  void cancel(Runnable task);
  void finished();
  void failed();
 }
 private final Host host;
 private boolean running;
 private int waits,launches;
 public ProfileRestart(Host host){this.host=host;}
 public void start(){
  if(running)return;
  running=true;waits=0;launches=0;
  try{host.stopOldProcesses();host.later(this,100);}catch(RuntimeException e){fail();}
 }
 public void run(){
  if(!running)return;
  try{
   if(!host.oldProcessesStopped()){
    if(++waits>=100){fail();return;}
    host.later(this,100);return;
   }
   if(launches>=3){fail();return;}
   launches++;
   try{host.launchPlayer();}catch(RuntimeException e){/* Keep the coordinator available for retry. */}
   if(running)host.later(this,10000);
  }catch(RuntimeException e){fail();}
 }
 public void acknowledge(){if(running){cancel();host.finished();}}
 public void cancel(){running=false;host.cancel(this);}
 private void fail(){cancel();host.failed();}
}
