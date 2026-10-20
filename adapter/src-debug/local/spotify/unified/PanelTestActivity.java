package local.spotify.unified;
import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
/**
 * Debug builds only (not compiled into releases): a test host for PanelProviderService in its own
 * process. It opens the block through the same Messenger protocol in its SurfaceView and has
 * buttons for RESIZE (through the block sizes of the Maps panel on a BYD Tang, then the
 * minimum), THEME (night / day), CLOSE, Open (again, in a new
 * SurfaceView) and Exit (the host process dies, as a crashed host would).
 */
public final class PanelTestActivity extends Activity {
 private Messenger provider;private SurfaceView surface;private FrameLayout card;private TextView status;
 /** Block sizes in dp: the Maps panel on the car (landscape 1/3, 1/2, 2/3; portrait 1/3, 1/2) and the minimum. */
 private static final int[][] SIZES={{378,588},{592,588},{805,588},{704,340},{704,534},{280,160}};
 private int sizeIndex;private boolean night=true,surfaceReady,bound,attached;
 private final Messenger replies=new Messenger(new Handler(Looper.getMainLooper(),this::reply));
 private final ServiceConnection connection=new ServiceConnection(){
  public void onServiceConnected(ComponentName n,IBinder b){provider=new Messenger(b);say("Provider connected");if(surfaceReady && !attached)open();}
  public void onServiceDisconnected(ComponentName n){provider=null;say("Provider disconnected");}
 };
 protected void onCreate(Bundle state){
  super.onCreate(state);
  // Buttons on the left, the card at the top right, so that 588 dp tall blocks fit.
  LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.HORIZONTAL);page.setPadding(dp(16),dp(8),dp(16),dp(8));page.setBackgroundColor(0xff3a3a3a);
  LinearLayout buttons=new LinearLayout(this);buttons.setOrientation(LinearLayout.VERTICAL);page.addView(buttons,new LinearLayout.LayoutParams(dp(150),-2));
  status=new TextView(this);status.setTextColor(0xffffffff);status.setTextSize(14);buttons.addView(status);
  button(buttons,"Open",v->newSurface());
  button(buttons,"Resize",v->{sizeIndex=(sizeIndex+1)%SIZES.length;size();surface.post(()->send(PanelProviderService.RESIZE,sizeBundle()));});
  button(buttons,"Theme",v->{night=!night;paintCard();Bundle b=new Bundle();b.putBoolean("night",night);send(PanelProviderService.THEME,b);});
  button(buttons,"Close",v->send(PanelProviderService.CLOSE,new Bundle()));
  button(buttons,"Exit",v->android.os.Process.killProcess(android.os.Process.myPid()));
  // A rounded card (22 dp) with an 8 dp inset; the block gets the concentric 14 dp radius. The card
  // is a shade lighter than the block, so that the block's transparent corners show.
  card=new FrameLayout(this);paintCard();card.setPadding(dp(8),dp(8),dp(8),dp(8));
  LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.setMarginStart(dp(16));page.addView(card,cp);
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
  surface=new SurfaceView(this);surface.setZOrderOnTop(true);surface.getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);card.addView(surface);size();
  surface.getHolder().addCallback(new SurfaceHolder.Callback(){
   public void surfaceCreated(SurfaceHolder h){surfaceReady=true;if(provider!=null && !attached)open();}
   public void surfaceChanged(SurfaceHolder h,int f,int w,int ht){}
   public void surfaceDestroyed(SurfaceHolder h){surfaceReady=false;}
  });
 }
 protected void onDestroy(){if(bound)unbindService(connection);super.onDestroy();}
 private void paintCard(){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(night?0xff262626:0xffe0e0e0);d.setCornerRadius(dp(22));card.setBackground(d);}
 private void size(){int[] z=SIZES[sizeIndex];surface.setLayoutParams(new FrameLayout.LayoutParams(dp(z[0]),dp(z[1])));say("Size "+z[0]+"×"+z[1]+" dp");}
 private Bundle sizeBundle(){Bundle b=new Bundle();b.putInt("widthPx",surface.getWidth());b.putInt("heightPx",surface.getHeight());return b;}
 private void open(){
  if(provider==null || !surfaceReady)return;
  Bundle b=sizeBundle();b.putInt("version",PanelProviderService.VERSION);b.putBinder("hostToken",surface.getHostToken());
  b.putInt("cornerRadiusPx",dp(14));b.putInt("displayId",getDisplay().getDisplayId());b.putInt("densityDpi",getResources().getConfiguration().densityDpi);b.putBoolean("night",night);
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
