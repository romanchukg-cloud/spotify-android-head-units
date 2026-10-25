package local.spotify.unified;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
/**
 * Spotify in Waze's audio panel, the way Waze asks for it (its Audio SDK): when the driver picks
 * Spotify, Waze sends com.waze.sdk.audio.ACTION_INIT with a session token, and the app registers
 * with Waze's SdkService (com.waze.sdk.ISdkService, call 1: the token, its settings and a
 * messenger). Waze then browses and controls the music through the app's media browser service,
 * the backend's own (AutomotiveMediaBrowserService), which keeps this process alive while Waze
 * uses it. Without the registration Waze tries App Remote instead and shows "Can't connect"
 * after 15 seconds. Only a Waze that AppRemoteAccess allows is answered. Waze's navigation
 * messages to the messenger are ignored; its "disconnected" message ends the link.
 */
public final class HuWazeLink extends BroadcastReceiver {
 static final String INIT="com.waze.sdk.audio.ACTION_INIT",WAZE="com.waze";
 private static final String SERVICE="com.waze.sdk.ISdkService";private static final int REGISTER=IBinder.FIRST_CALL_TRANSACTION,DISCONNECTED=502;
 private static Context app;private static ServiceConnection link;private static Messenger inbox;
 public void onReceive(Context context,Intent intent){
  if(!INIT.equals(intent.getAction()))return;
  String token=intent.getStringExtra("token");app=context.getApplicationContext();
  if(token==null || !allowed()){Log.w("SpotifyHU","Waze: init ignored");return;}
  connect(token);
 }
 private static boolean allowed(){
  PackageManager pm=app.getPackageManager();
  try{int uid=pm.getPackageUid(WAZE,0);return WAZE.equals(AppRemoteAccess.client(uid,android.os.Process.myUid(),HeadUnitBuild.DEBUG,app.getPackageName(),pm.getPackagesForUid(uid),p->PanelProviderService.signers(pm,p)));}
  catch(PackageManager.NameNotFoundException e){return false;}
 }
 /** A new token replaces the link before it. */
 private static void connect(String token){
  release();
  if(inbox==null)inbox=new Messenger(new Handler(Looper.getMainLooper(),HuWazeLink::received));
  ServiceConnection c=new ServiceConnection(){
   public void onServiceConnected(ComponentName n,IBinder b){if(link==this)register(b,token);}
   public void onServiceDisconnected(ComponentName n){if(link==this){Log.i("SpotifyHU","Waze: gone");release();}}
  };
  link=c;
  if(!app.bindService(new Intent().setComponent(new ComponentName(WAZE,"com.waze.sdk.SdkService")),c,Context.BIND_AUTO_CREATE)){Log.w("SpotifyHU","Waze: SDK service unavailable");release();}
 }
 private static void release(){
  ServiceConnection c=link;link=null;
  if(c!=null)try{app.unbindService(c);}catch(RuntimeException ignored){}
 }
 private static void register(IBinder b,String token){
  Parcel data=Parcel.obtain(),reply=Parcel.obtain();
  try{
   data.writeInterfaceToken(SERVICE);data.writeString(token);
   data.writeInt(1);settings().writeToParcel(data,0);
   data.writeInt(1);inbox.writeToParcel(data,0);
   b.transact(REGISTER,data,reply,0);reply.readException();
   boolean accepted=reply.readInt()!=0;
   Log.i("SpotifyHU","Waze: "+(accepted?"connected":"refused"));if(!accepted)release();
  }catch(RemoteException|RuntimeException e){Log.w("SpotifyHU","Waze: registering failed ("+e.getClass().getSimpleName()+")");release();}
  finally{data.recycle();reply.recycle();}
 }
 private static boolean received(Message m){
  if(m.what==DISCONNECTED){Log.i("SpotifyHU","Waze: disconnected ("+m.getData().getInt("reason",-1)+")");release();}
  return true;
 }
 /**
  * openMeIntent opens the app from Waze's panel; themeColor tints the panel. From versionCode 10
  * the panel has two buttons beside the track (the heart, and shuffle) instead of one.
  */
 private static Bundle settings(){
  Bundle s=new Bundle();
  Intent open=app.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
  if(open!=null)s.putParcelable("openMeIntent",PendingIntent.getActivity(app,0,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT));
  s.putInt("themeColor",0xFF1ED760);s.putInt("versionCode",10);
  return s;
 }
}
