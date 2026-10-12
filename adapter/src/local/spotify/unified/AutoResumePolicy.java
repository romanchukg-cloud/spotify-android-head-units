package local.spotify.unified;
/** SDK-independent decisions, shared by the service and deterministic tests. */
public final class AutoResumePolicy {
 public static final long WINDOW_MS=120000, RETRY_MS=5000;
 public static boolean playing(int state){return state==3;}
 public static boolean busy(int state){return state==3 || state==6 || state==8 || state==9 || state==10 || state==11;}
 public static boolean expired(long now,long started){return started<0 || now<started || now-started>=WINDOW_MS;}
 public static boolean shouldPlay(int state,long actions,long now,long lastAttempt){
  // NONE, ERROR and connection/buffering states do not establish a restored queue.
  return (state==1 || state==2) && (actions&(4L|512L))!=0 && (lastAttempt<0 || now-lastAttempt>=RETRY_MS);
 }
 private AutoResumePolicy(){}
}
