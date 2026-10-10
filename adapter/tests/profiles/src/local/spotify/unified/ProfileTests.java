package local.spotify.unified;
import android.app.Instrumentation;
import android.content.*;
import android.os.*;
import android.util.Base64;
import org.json.*;
import java.lang.reflect.*;
public final class ProfileTests extends Instrumentation {
 public static final class FakeStore {
  public Object user;public int failures;public boolean unreadable;
  public Object d()throws Exception{if(unreadable)return just("unexpected-storage-result");return just(user==null?Class.forName("cb.e").getField("a").get(null):Class.forName("cb.f").getConstructor(Class.forName("ya.e")).newInstance(user));}
  public Object n(ya.e value)throws Exception{if(failures>0){failures--;throw new IllegalStateException("simulated write failure");}if(user!=null)return just(Class.forName("cb.a").getField("b").get(null));user=value;return success();}
  public Object l()throws Exception{user=null;return success();}
  private Object success()throws Exception{return just(Class.forName("cb.c").getField("a").get(null));}
  private Object just(Object v)throws Exception{return Class.forName("io.reactivex.rxjava3.core.Single").getMethod("just",Object.class).invoke(null,v);}
 }
 public void onCreate(Bundle args){super.onCreate(args);start();}
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 private static final class RestartHost implements ProfileRestart.Host {
  ProfileRestart controller;Runnable queued;int stops,launches,finished,failed;boolean stopped,throwStop,throwLaunch,acknowledge;
  public void stopOldProcesses(){stops++;if(throwStop)throw new SecurityException("simulated process restriction");}
  public boolean oldProcessesStopped(){return stopped;}
  public void launchPlayer(){launches++;if(throwLaunch)throw new IllegalStateException("simulated launch failure");if(acknowledge)controller.acknowledge();}
  public void later(Runnable r,long delay){check(queued==null,"duplicate scheduled restart");queued=r;}
  public void cancel(Runnable r){if(queued==r)queued=null;}
  public void finished(){finished++;}
  public void failed(){failed++;}
  void tick(){check(queued!=null,"restart not scheduled");Runnable r=queued;queued=null;r.run();}
  RestartHost(){controller=new ProfileRestart(this);}
 }
 private static void restartTests(){
  RestartHost h=new RestartHost();h.controller.start();h.controller.start();
  for(int i=0;i<12;i++)h.tick();check(h.stops==1 && h.launches==0,"launched before old processes stopped");
  h.stopped=true;h.tick();check(h.launches==1 && h.finished==0,"launch treated as acknowledgement");
  h.controller.acknowledge();h.controller.acknowledge();check(h.finished==1 && h.failed==0 && h.queued==null,"acknowledgement not terminal");
  h=new RestartHost();h.stopped=true;h.throwLaunch=true;h.controller.start();
  for(int i=0;i<4;i++)h.tick();check(h.launches==3 && h.failed==1 && h.finished==0,"launch failure escaped or closed coordinator");
  h.throwLaunch=false;h.acknowledge=true;h.controller.start();h.tick();check(h.finished==1 && h.queued==null,"retry after launch failure");
  h=new RestartHost();h.stopped=true;h.controller.start();for(int i=0;i<4;i++)h.tick();
  h.controller.acknowledge();check(h.failed==1 && h.finished==0,"unacknowledged UI reported successful");
  h=new RestartHost();h.controller.start();for(int i=0;i<100;i++)h.tick();check(h.failed==1 && h.launches==0,"process death timeout");
  h=new RestartHost();h.throwStop=true;h.controller.start();check(h.failed==1 && h.queued==null,"process restriction escaped");
  h=new RestartHost();h.controller.start();Runnable late=h.queued;h.controller.cancel();h.stopped=true;late.run();check(h.launches==0 && h.finished==0,"destroyed coordinator launched player");
 }
 public void onStart(){Bundle report=new Bundle();try{
  restartTests();
  Context c=getTargetContext();ProfileVault vault=new ProfileVault(c);
  c.getSharedPreferences("driver_profile_transition",0).edit().clear().commit();
  JSONObject input=new JSONObject().put("version",1).put("profiles",new JSONArray().put(new JSONObject().put("user","synthetic-driver").put("credential","synthetic-secret")));
  vault.write(input);check(vault.read().toString().equals(input.toString()),"encrypted round trip");
  SharedPreferences prefs=c.getSharedPreferences("driver_profiles_v1",0);String encrypted=prefs.getString("vault","");
  check(!encrypted.contains("synthetic-driver") && !encrypted.contains("synthetic-secret"),"plaintext leakage");
  byte[] damaged=Base64.decode(encrypted,Base64.NO_WRAP);damaged[damaged.length-1]^=1;
  prefs.edit().putString("vault",Base64.encodeToString(damaged,Base64.NO_WRAP)).commit();
  boolean rejected=false;try{vault.read();}catch(Exception expected){rejected=true;}check(rejected,"tamper detection");
  check(prefs.getString("vault","").equals(Base64.encodeToString(damaged,Base64.NO_WRAP)),"corrupt vault silently overwritten");
  prefs.edit().clear().commit();
  Class<?> blob=Class.forName("ya.c"),info=Class.forName("ya.e");
  FakeStore store=new FakeStore();store.user=info.getConstructor(String.class,Class.forName("ya.a"),blob).newInstance("synthetic-A",null,blob.getConstructor(String.class,byte[].class).newInstance("synthetic-A",new byte[]{1,2,3}));
  DriverProfiles.bindStorage(store);Bundle list=DriverProfiles.call(c,"list",null,null);String id=list.getString("active");
  check(list.getStringArrayList("ids").size()==1 && id!=null,"current account saved");
  check(!list.containsKey("credential") && !list.containsKey("user"),"credentials crossed IPC");
  check(DriverProfiles.call(c,"list",null,null).getStringArrayList("ids").size()==1,"duplicate current account");
  Bundle label=new Bundle();label.putString("name","Driver A");DriverProfiles.call(c,"rename",id,label);
  check(DriverProfiles.call(c,"list",null,null).getStringArrayList("names").get(0).equals("Driver A"),"rename persistence");
  rejected=false;try{DriverProfiles.call(c,"remove",id,null);}catch(Exception expected){rejected=true;}check(rejected,"active removal allowed");
  rejected=false;try{DriverProfiles.call(c,"restore","missing",null);}catch(Exception expected){rejected=true;}check(rejected,"unknown restore allowed");
  check(!DriverProfiles.restartPending(c),"invalid operation started transition");
  store.unreadable=true;rejected=false;try{DriverProfiles.call(c,"add",null,null);}catch(Exception expected){rejected=true;}store.unreadable=false;
  check(rejected && store.user!=null && !DriverProfiles.restartPending(c),"unreadable active account was overwritten");
  c.getSharedPreferences("driver_profile_transition",0).edit().putInt("pid",android.os.Process.myPid()).putLong("started",-1).commit();
  check(!DriverProfiles.restartPending(c),"stale PID treated as live transition");c.getSharedPreferences("driver_profile_transition",0).edit().clear().commit();
  store.failures=1;rejected=false;try{DriverProfiles.call(c,"restore",id,null);}catch(Exception expected){rejected=true;}
  check(rejected && store.user!=null && !DriverProfiles.restartPending(c) && !local.spotify.close.PlaybackGate.blocked(c),"failure recovery");
  store.failures=2;rejected=false;try{DriverProfiles.call(c,"restore",id,null);}catch(Exception expected){rejected=true;}
  check(rejected && local.spotify.close.PlaybackGate.blocked(c) && vault.read().getJSONArray("profiles").length()==1,"failed rollback must keep playback blocked and vault intact");
  Bundle restored=DriverProfiles.call(c,"restore",id,null);check(restored.getBoolean("restart") && !restored.getBoolean("qr"),"restore restart protocol");
  check(local.spotify.close.PlaybackGate.blocked(c),"playback allowed before restart");
  local.spotify.close.PlaybackGate.userPlay();local.spotify.close.PlaybackGate.openForUi(c,new Binder());local.spotify.close.PlaybackGate.change(c,false);
  check(local.spotify.close.PlaybackGate.blocked(c),"UI or steering control reopened account transition");
  check(!HeadUnitPlaybackService.command(c,126,"test"),"cold play accepted during account transition");
  check(!HeadUnitPreferences.store().getBoolean("boot_restore_pending",true),"account transition retained boot resume");
  check(DriverProfiles.call(c,"list",null,null).getBoolean("restart"),"interrupted transition not recovered");
  c.getSharedPreferences("driver_profile_transition",0).edit().clear().commit();
  Bundle added=DriverProfiles.call(c,"add",null,null);check(added.getBoolean("qr") && store.user==null,"QR addition active slot");
  check(vault.read().getJSONArray("profiles").length()==1,"saved account lost during QR addition");
  c.getSharedPreferences("driver_profile_transition",0).edit().clear().commit();
  Bundle returned=DriverProfiles.call(c,"restore",id,null);check(returned.getBoolean("restart") && store.user!=null,"return from cancelled QR");
  c.getSharedPreferences("driver_profile_transition",0).edit().clear().commit();
  store.user=info.getConstructor(String.class,Class.forName("ya.a"),blob).newInstance("synthetic-B",null,blob.getConstructor(String.class,byte[].class).newInstance("synthetic-B",new byte[]{4,5,6}));
  Bundle two=DriverProfiles.call(c,"list",null,null);String second=two.getString("active");check(two.getStringArrayList("ids").size()==2 && !id.equals(second),"second account dedupe");
  DriverProfiles.call(c,"restore",id,null);check(info.getField("j").get(store.user).equals("synthetic-A"),"B to A restore");
  c.getSharedPreferences("driver_profile_transition",0).edit().clear().commit();
  DriverProfiles.call(c,"restore",second,null);check(info.getField("j").get(store.user).equals("synthetic-B"),"A to B restore");
  c.getSharedPreferences("driver_profile_transition",0).edit().clear().commit();
  DriverProfiles.call(c,"remove",id,null);check(vault.read().getJSONArray("profiles").length()==1,"inactive removal");
  report.putString("stream","PASS: delayed process death, UI acknowledgement, failed/blocked launch retry, process restriction, cancelled coordinator, account transition rejects UI/transport/wake resume, encryption, tamper rejection, duplicate prevention, rename, IPC redaction, active removal guard, unknown restore, write failure recovery, restart recovery, QR cancellation return, two synthetic accounts A-B-A and inactive removal. SDK auth store mocked; real two-account switching unverified.\n");finish(-1,report);
 }catch(Throwable failure){report.putString("stream","FAIL: "+failure.getClass().getSimpleName()+": "+failure.getMessage()+"\n");finish(0,report);}}
}
