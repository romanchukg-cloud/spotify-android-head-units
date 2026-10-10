package hu.tests;
import android.app.*;import android.content.*;import android.os.*;import android.media.session.*;import android.net.Uri;import local.spotify.unified.*;import local.spotify.close.*;
public class Runner extends Instrumentation {
 Bundle args;int passed=0;
 public void onCreate(Bundle b){args=b;start();}
 void ok(boolean value,String label){if(!value)throw new AssertionError(label);passed++;android.util.Log.i("HU_TEST","PASS "+label);}
 public void onStart(){Bundle out=new Bundle();Context c=getTargetContext();try{
  HeadUnitPreferences.init(c);String mode=args.getString("mode","checks");
  if(mode.equals("scale")){Bundle b=new Bundle();b.putInt("scale",Integer.parseInt(args.getString("value","100")));HeadUnitSettings.call(c,"set",b);}
  else if(mode.equals("prepare-reboot")){HeadUnitPreferences.store().edit().putBoolean("was_playing",true).putBoolean("shutdown_playing",true).commit();PlaybackGate.change(c,true);Thread.sleep(30000);}
  else if(mode.equals("after-reboot")){ok(!PlaybackGate.blocked(c),"reboot clears persisted close gate");}
  else if(mode.equals("prime-cold")){MediaSession session=new MediaSession(c,"HU transport fixture");session.setFlags(3);PlaybackGate.register(c,session);PlaybackGate.receiver(session,null);session.setPlaybackState(new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,0,1).setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS|PlaybackState.ACTION_PLAY_PAUSE).build());session.setActive(true);Thread.sleep(3000);android.os.Process.killProcess(android.os.Process.myPid());}
  else if(mode.equals("accounts-ui")){
   if(args.getString("seed","false").equals("true")){
    Class<?> vault=Class.forName("local.spotify.unified.ProfileVault");java.lang.reflect.Constructor<?> ctor=vault.getDeclaredConstructor(Context.class);ctor.setAccessible(true);Object disk=ctor.newInstance(c);
    org.json.JSONArray profiles=new org.json.JSONArray();for(int i=0;i<2;i++)profiles.put(new org.json.JSONObject().put("id","synthetic-"+i).put("name",i==0?"Synthetic driver A — long display name":"Synthetic driver B").put("user","synthetic-"+i).put("credential","AQID"));
    java.lang.reflect.Method write=vault.getDeclaredMethod("write",org.json.JSONObject.class);write.setAccessible(true);write.invoke(disk,new org.json.JSONObject().put("version",1).put("profiles",profiles));
   }
   c.startActivity(new Intent().setClassName(c.getPackageName(),"local.bydui.com.vivid.spotify.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Thread.sleep(4000);c.startActivity(new Intent(c,HeadUnitSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Thread.sleep(60000);}
  else if(mode.equals("settings")){c.startActivity(new Intent(c,HeadUnitSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Thread.sleep(15000);}
  else if(mode.equals("stop-close")){Bundle b=new Bundle();b.putBoolean("stop_close",Boolean.parseBoolean(args.getString("value","false")));HeadUnitSettings.call(c,"set",b);}
  else if(mode.equals("external")){Bundle b=new Bundle();b.putBoolean("external",Boolean.parseBoolean(args.getString("value","true")));HeadUnitSettings.call(c,"set",b);}
  else if(mode.equals("checks")){
   ok(HeadUnitProfile.get(c)==HeadUnitProfile.Kind.GENERIC,"ordinary tablet is GENERIC");
   ok(c.getPackageManager().getComponentEnabledSetting(new ComponentName(c,"local.bydui.com.vivid.spotify.service.MediaManagerService"))==2,"BYD service disabled");
   HeadUnitPreferences.store().edit().clear().commit();HeadUnitPreferences.write(new Bundle());
   ok(!HeadUnitPreferences.stopOnClose()&&HeadUnitPreferences.external()&&HeadUnitPreferences.resume(),"GENERIC defaults");
   Bundle b=new Bundle();b.putBoolean("stop_close",false);HeadUnitSettings.call(c,"set",b);PlaybackGate.change(c,false);
   c.getContentResolver().call(Uri.parse("content://com.spotify.music.local.closecontrol"),"close",null,null);ok(!PlaybackGate.blocked(),"task removal option OFF keeps gate open");
   b.putBoolean("stop_close",true);HeadUnitSettings.call(c,"set",b);c.getContentResolver().call(Uri.parse("content://com.spotify.music.local.closecontrol"),"close",null,null);ok(PlaybackGate.blocked(),"task removal option ON closes gate");
   PlaybackGate.userPlay();ok(!PlaybackGate.blocked(),"explicit PLAY reopens gate");
   PlaybackGate.openForUi(c,new Binder());java.lang.reflect.Field f=PlaybackGate.class.getDeclaredField("uiDeath");f.setAccessible(true);((IBinder.DeathRecipient)f.get(null)).binderDied();ok(!PlaybackGate.blocked(),"binder death leaves gate open");
   HeadUnitPreferences.store().edit().putBoolean("was_playing",true).putBoolean("screen_playing",true).putBoolean("boot_restore_pending",true).commit();HeadUnitRuntime.explicitPause(c);ok(!HeadUnitPreferences.store().getBoolean("was_playing",true)&&!HeadUnitPreferences.store().getBoolean("screen_playing",true)&&!HeadUnitPreferences.store().getBoolean("boot_restore_pending",true),"explicit pause clears wake/reboot resume");
   b=new Bundle();b.putInt("scale",999);HeadUnitSettings.call(c,"set",b);ok(HeadUnitSettings.call(c,"get",null).getInt("scale")==130,"scale upper bound");b.putInt("scale",-10);HeadUnitSettings.call(c,"set",b);ok(HeadUnitSettings.call(c,"get",null).getInt("scale")==80,"scale lower bound");
   b.putInt("scale",100);b.putBoolean("stop_close",false);b.putBoolean("external",false);HeadUnitSettings.call(c,"set",b);ok(HeadUnitControllers.allow(c,"com.android.settings"),"system app bypasses external restriction");ok(!HeadUnitControllers.allow(c,"hu.browser"),"third-party blocked when external OFF");ok(!HeadUnitControllers.matches(c,12345,"hu.browser"),"package UID mismatch rejected");b.putBoolean("external",true);HeadUnitSettings.call(c,"set",b);ok(HeadUnitControllers.allow(c,"hu.browser"),"third-party allowed when external ON");
   HeadUnitPreferences.store().edit().putLong("screen_off",SystemClock.elapsedRealtime()-119000).putBoolean("screen_playing",true).commit();HeadUnitRuntime.receive(c,new Intent(Intent.ACTION_SCREEN_ON));ok(!HeadUnitPreferences.store().contains("screen_off"),"short screen wake consumed without resume request");
   HeadUnitPreferences.store().edit().putLong("screen_off",SystemClock.elapsedRealtime()-121000).putBoolean("screen_playing",false).commit();HeadUnitRuntime.receive(c,new Intent(Intent.ACTION_SCREEN_ON));ok(!HeadUnitPreferences.store().getBoolean("was_playing",false),"paused long sleep stays paused");
   HeadUnitRuntime.receive(c,new Intent("hu.tests.UNKNOWN"));
  }
  out.putString("stream","PASS mode="+mode+" checks="+passed+"\n");finish(-1,out);
 }catch(Throwable e){android.util.Log.e("HU_TEST","FAIL",e);out.putString("stream","FAIL "+e+"\n");finish(1,out);}}
}
