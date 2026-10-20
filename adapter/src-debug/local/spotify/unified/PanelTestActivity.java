package local.spotify.unified;
import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
/**
 * Debug builds only (not compiled into releases): a test host for PanelProviderService in its own
 * process. It opens the block through the same Messenger protocol in its SurfaceView and has
 * buttons for RESIZE (column / strip), THEME (night / day), CLOSE, Open (again, in a new
 * SurfaceView) and Exit (the host process dies, as a crashed host would).
 */
public final class PanelTestActivity extends Activity {
 private Messenger provider;private SurfaceView surface;private FrameLayout card;private TextView status;
 private boolean column=true,night=true,surfaceReady,bound,attached;
 private final Messenger replies=new Messenger(new Handler(Looper.getMainLooper(),this::reply));
 private final ServiceConnection connection=new ServiceConnection(){
  public void onServiceConnected(ComponentName n,IBinder b){provider=new Messenger(b);say("Provider connected");if(surfaceReady && !attached)open();}
  public void onServiceDisconnected(ComponentName n){provider=null;say("Provider disconnected");}
 };
 protected void onCreate(Bundle state){
  super.onCreate(state);
  LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(24),dp(24),dp(24),dp(24));page.setBackgroundColor(0xff3a3a3a);
  status=new TextView(this);status.setTextColor(0xffffffff);status.setTextSize(18);page.addView(status);
  LinearLayout buttons=new LinearLayout(this);page.addView(buttons);
  button(buttons,"Open",v->newSurface());
  button(buttons,"Resize",v->{column=!column;size();surface.post(()->send(PanelProviderService.RESIZE,sizeBundle()));});
  button(buttons,"Theme",v->{night=!night;card.setBackgroundColor(night?0xff000000:0xffffffff);Bundle b=new Bundle();b.putBoolean("night",night);send(PanelProviderService.THEME,b);});
  button(buttons,"Close",v->send(PanelProviderService.CLOSE,new Bundle()));
  button(buttons,"Exit",v->android.os.Process.killProcess(android.os.Process.myPid()));
  card=new FrameLayout(this);card.setBackgroundColor(0xff000000);card.setPadding(dp(16),dp(16),dp(16),dp(16));
  LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.topMargin=dp(16);page.addView(card,cp);
  newSurface();
  setContentView(page);
  bound=bindService(new Intent("local.byd.panel.action.PROVIDER").setPackage(getPackageName()),connection,Context.BIND_AUTO_CREATE);
  say(bound?"Binding…":"Provider not found");
 }
 /**
  * A new SurfaceView for every block: Android 11-12 do not attach a second package to a SurfaceView
  * that already had one. An attached block survives the surface being destroyed and created again
  * (activity stopped and started), so only a new SurfaceView's first surface opens it.
  */
 private void newSurface(){
  card.removeAllViews();attached=false;surfaceReady=false;
  surface=new SurfaceView(this);surface.setZOrderOnTop(true);card.addView(surface);size();
  surface.getHolder().addCallback(new SurfaceHolder.Callback(){
   public void surfaceCreated(SurfaceHolder h){surfaceReady=true;if(provider!=null && !attached)open();}
   public void surfaceChanged(SurfaceHolder h,int f,int w,int ht){}
   public void surfaceDestroyed(SurfaceHolder h){surfaceReady=false;}
  });
 }
 protected void onDestroy(){if(bound)unbindService(connection);super.onDestroy();}
 private void size(){surface.setLayoutParams(new FrameLayout.LayoutParams(dp(column?300:720),dp(column?560:180)));}
 private Bundle sizeBundle(){Bundle b=new Bundle();b.putInt("widthPx",surface.getWidth());b.putInt("heightPx",surface.getHeight());return b;}
 private void open(){
  if(provider==null || !surfaceReady)return;
  Bundle b=sizeBundle();b.putInt("version",PanelProviderService.VERSION);b.putBinder("hostToken",surface.getHostToken());
  b.putInt("displayId",getDisplay().getDisplayId());b.putInt("densityDpi",getResources().getConfiguration().densityDpi);b.putBoolean("night",night);
  send(PanelProviderService.OPEN,b);
 }
 private void send(int what,Bundle data){
  if(provider==null){say("No provider");return;}
  Message m=Message.obtain(null,what);m.setData(data);m.replyTo=replies;
  try{provider.send(m);}catch(RemoteException e){say("Send failed: "+e);}
 }
 private boolean reply(Message m){
  Bundle b=m.getData();
  if(m.what==PanelProviderService.OPENED){
   SurfaceControlViewHost.SurfacePackage p=b.getParcelable("surfacePackage");
   surface.setChildSurfacePackage(p);attached=true;say("Opened (version "+b.getInt("version")+")");
  }else if(m.what==PanelProviderService.ERROR)say("Error "+b.getString("code")+": "+b.getString("message"));
  else if(m.what==PanelProviderService.CLOSED)say("Closed by provider: "+b.getString("reason"));
  return true;
 }
 private void button(LinearLayout row,String label,View.OnClickListener click){Button b=new Button(this);b.setText(label);b.setOnClickListener(click);row.addView(b);}
 private void say(String s){status.setText(s);android.util.Log.i("SpotifyHU","Panel test: "+s);}
 private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
