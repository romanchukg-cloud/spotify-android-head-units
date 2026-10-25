package local.spotify.unified;
import android.app.Service;
import android.content.*;
import android.content.pm.*;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.*;
import android.util.Log;
import android.view.*;
import java.util.*;
/**
 * The panel provider (protocol: PANEL_PROVIDER.md): another app's panel shows our compact player
 * (PanelPlayerView), drawn here and embedded in the host's SurfaceView through
 * SurfaceControlViewHost (Android 11+). Touches reach the block through the system. The host
 * talks to us with Messenger messages; each one is checked against the allowed hosts
 * (PanelAccess). One block per host uid; everything runs on the main thread and is released on
 * CLOSE, unbinding or the host's death. Binding opens no screen, plays nothing and changes no
 * setting: the interface process starts without the backend, which the block's session joins
 * only once it runs.
 */
public final class PanelProviderService extends Service {
 static final int VERSION=1;
 static final int OPEN=1,RESIZE=2,THEME=3,CLOSE=4,OPENED=101,CLOSED=102,ERROR=199;
 private final Map<Integer,Session> sessions=new HashMap<>();
 private Messenger messenger;
 public void onCreate(){super.onCreate();messenger=new Messenger(new Handler(Looper.getMainLooper(),this::handle));}
 public IBinder onBind(Intent intent){return messenger.getBinder();}
 public boolean onUnbind(Intent intent){closeAll(null);return false;}
 public void onDestroy(){closeAll("provider_stopped");super.onDestroy();}

 private boolean handle(Message m){
  Messenger reply=m.replyTo;String host=caller(m.sendingUid);
  if(host==null){Log.w("SpotifyHU","Panel: message "+m.what+" from uid "+m.sendingUid+" refused");error(reply,"forbidden","Not an allowed host");return true;}
  if(Build.VERSION.SDK_INT<30){error(reply,"unsupported","The panel needs Android 11 or newer");return true;}
  Bundle b=m.getData();Session s=sessions.get(m.sendingUid);
  try{
   switch(m.what){
    case OPEN:open(m.sendingUid,host,reply,b);break;
    case RESIZE:if(s!=null)s.resize(b.getInt("widthPx"),b.getInt("heightPx"));break;
    case THEME:if(s!=null)s.view.theme(b.getBoolean("night"));break;
    case CLOSE:close(m.sendingUid,null);break;
    default:error(reply,"bad_request","Unknown message "+m.what);
   }
  }catch(RuntimeException e){Log.w("SpotifyHU","Panel: message "+m.what+" failed",e);close(m.sendingUid,"error");error(reply,"internal",String.valueOf(e.getMessage()));}
  return true;
 }
 /** The allowed host package a uid belongs to, or null. */
 private String caller(int uid){
  PackageManager pm=getPackageManager();String[] packages=uid<0?null:pm.getPackagesForUid(uid);
  return PanelAccess.host(uid,android.os.Process.myUid(),HeadUnitBuild.DEBUG,getPackageName(),packages,p->signers(pm,p));
 }
 /** SHA-256 of the package's current signing certificates; null when it is not visible. */
 static String[] signers(PackageManager pm,String pkg){
  try{
   SigningInfo info=pm.getPackageInfo(pkg,PackageManager.GET_SIGNING_CERTIFICATES).signingInfo;if(info==null)return null;
   Signature[] all;
   if(info.hasMultipleSigners())all=info.getApkContentsSigners();
   else{Signature[] history=info.getSigningCertificateHistory();all=history==null || history.length==0?null:new Signature[]{history[history.length-1]};}
   if(all==null)return null;
   String[] out=new String[all.length];java.security.MessageDigest sha=java.security.MessageDigest.getInstance("SHA-256");
   for(int i=0;i<all.length;i++)out[i]=UpdateDownload.hex(sha.digest(all[i].toByteArray()));
   return out;
  }catch(PackageManager.NameNotFoundException | java.security.NoSuchAlgorithmException e){return null;}
 }

 private void open(int uid,String host,Messenger reply,Bundle b){
  if(reply==null)return;
  if(b.getInt("version")!=VERSION){error(reply,"unsupported_version","Protocol version "+VERSION+" expected");return;}
  IBinder token=b.getBinder("hostToken");int w=b.getInt("widthPx"),h=b.getInt("heightPx");
  if(token==null || w<=0 || h<=0){error(reply,"bad_request","hostToken, widthPx and heightPx are required");return;}
  Display display=getSystemService(DisplayManager.class).getDisplay(b.getInt("displayId"));
  if(display==null){error(reply,"bad_display","No display "+b.getInt("displayId"));return;}
  close(uid,null);
  Session s=new Session(uid,reply,display,token,w,h,b.getInt("densityDpi"),b.getBoolean("night"),b.getInt("cornerRadiusPx",-1));sessions.put(uid,s);s.opened();
  Log.i("SpotifyHU","Panel opened for "+host+" "+w+"x"+h);
 }
 private void close(int uid,String reason){
  Session s=sessions.remove(uid);if(s==null)return;
  s.release();if(reason!=null){Bundle out=new Bundle();out.putString("reason",reason);send(s.reply,CLOSED,out);}
  Log.i("SpotifyHU","Panel closed"+(reason==null?"":" ("+reason+")"));
 }
 private void closeAll(String reason){for(Integer uid:new ArrayList<>(sessions.keySet()))close(uid,reason);}
 private static void error(Messenger reply,String code,String message){Bundle b=new Bundle();b.putString("code",code);b.putString("message",message);send(reply,ERROR,b);}
 private static void send(Messenger to,int what,Bundle data){
  if(to==null)return;Message m=Message.obtain(null,what);m.setData(data);
  try{to.send(m);}catch(RemoteException e){Log.i("SpotifyHU","Panel host gone");}
 }
 /** A tap on the block opens Spotify; allowed while a visible host is bound (BIND_ALLOW_ACTIVITY_STARTS on Android 14+). */
 private void openSpotify(){
  try{startActivity(new Intent().setClassName(getPackageName(),"local.bydui.com.vivid.spotify.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
  catch(RuntimeException e){Log.w("SpotifyHU","Panel: Spotify could not be opened",e);}
 }

 /** One host's block: its SurfaceControlViewHost, the player view and the host's death watch. */
 private final class Session implements IBinder.DeathRecipient {
  final int uid;final Messenger reply;final SurfaceControlViewHost viewHost;final PanelPlayerView view;
  Session(int uid,Messenger reply,Display display,IBinder token,int w,int h,int densityDpi,boolean night,int radiusPx){
   this.uid=uid;this.reply=reply;
   // The host's display, density and day/night decide sizes and colours.
   Context c=createDisplayContext(display);Configuration config=new Configuration(c.getResources().getConfiguration());
   if(densityDpi>0)config.densityDpi=densityDpi;
   config.uiMode=(config.uiMode&~Configuration.UI_MODE_NIGHT_MASK)|(night?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO);
   c=new ContextThemeWrapper(c.createConfigurationContext(config),android.R.style.Theme_Material_NoActionBar);
   view=new PanelPlayerView(c,night,w,h,radiusPx,PanelProviderService.this::openSpotify);
   viewHost=new SurfaceControlViewHost(c,display,token);viewHost.setView(view,w,h);
   try{reply.getBinder().linkToDeath(this,0);}catch(RemoteException e){binderDied();}
  }
  /** Sends the host its surface; our copy of the package is released once it is sent. */
  void opened(){
   SurfaceControlViewHost.SurfacePackage surface=viewHost.getSurfacePackage();
   Bundle out=new Bundle();out.putInt("version",VERSION);out.putParcelable("surfacePackage",surface);
   send(reply,OPENED,out);surface.release();
  }
  void resize(int w,int h){if(w<=0 || h<=0)return;view.size(w,h);viewHost.relayout(w,h);}
  void release(){
   try{reply.getBinder().unlinkToDeath(this,0);}catch(RuntimeException ignored){}
   view.release();viewHost.release();
  }
  public void binderDied(){new Handler(Looper.getMainLooper()).post(()->{if(sessions.get(uid)==this)close(uid,null);});}
 }
}
