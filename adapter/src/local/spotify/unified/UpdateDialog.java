package local.spotify.unified;
import android.app.*;import android.content.*;import android.content.pm.*;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.view.ContextThemeWrapper;import android.widget.*;import java.util.concurrent.*;
/** Update screens for any app process. State and work belong to the backend (AppUpdates via the settings provider). */
public final class UpdateDialog {
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static boolean alive(Activity a){return a!=null && !a.isFinishing() && !a.isDestroyed();}
 private static Context theme(Activity a){return new ContextThemeWrapper(a,android.R.style.Theme_DeviceDefault_Dialog_Alert);}
 private static Bundle call(Context c,String method,Bundle extras){return HeadUnitSettings.call(c,method,extras);}
 private static void quietly(Context c,String method,Bundle extras){new Thread(()->{try{call(c,method,extras);}catch(Throwable ignored){}},"SpotifyUpdate").start();}
 /** On a player launch: one offer per verified build, only while music is not playing. */
 public static void maybeOffer(Activity player){main.postDelayed(()->{
  if(!alive(player) || !player.hasWindowFocus())return;
  new Thread(()->{try{Bundle s=call(player,"update_state",null);if(s.getBoolean("offer"))player.runOnUiThread(()->{try{if(alive(player) && player.hasWindowFocus())offer(player,s);}catch(RuntimeException ignored){}});}catch(Throwable ignored){}},"SpotifyUpdateOffer").start();
 },2500);}
 private static void offer(Activity a,Bundle s){
  quietly(a,"update_offered",null);
  new AlertDialog.Builder(theme(a)).setTitle(UpdateText.format("ready_title",s.getString("candidate",""))).setMessage(UpdateText.get("restart"))
   .setNegativeButton(UpdateText.get("later"),null).setPositiveButton(UpdateText.get("install"),(d,w)->install(a)).show();
 }
 /** Prepares the session in the backend, then opens Android's own Update confirmation from this foreground activity. */
 static void install(Activity a){new Thread(()->{try{
  Bundle s=call(a,"update_install",null);long until=SystemClock.elapsedRealtime()+120000;
  while(alive(a) && SystemClock.elapsedRealtime()<until){
   String status=s.getString("status",""),confirm=s.getString("confirmation");
   if(confirm!=null){a.runOnUiThread(()->launch(a,confirm));return;}
   if("permission".equals(status)){a.runOnUiThread(()->permission(a));return;}
   if(!s.getBoolean("preparing") && !"preparing".equals(status)){Bundle shown=s;if(!s.getBoolean("ready") || "install_failed".equals(status) || "cancelled".equals(status))a.runOnUiThread(()->{if(alive(a))Toast.makeText(a,UpdateText.status(shown),Toast.LENGTH_LONG).show();});return;}
   Thread.sleep(500);s=call(a,"update_state",null);
  }
 }catch(Throwable ignored){}},"SpotifyUpdateInstall").start();}
 private static void launch(Activity a,String raw){
  if(!alive(a))return;
  try{
   Intent confirm=Intent.parseUri(raw,0);confirm.setSelector(null);
   ResolveInfo r=a.getPackageManager().resolveActivity(confirm,PackageManager.MATCH_SYSTEM_ONLY);
   if(r==null || r.activityInfo==null || (r.activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0)throw new IllegalStateException("Not a system installer");
   confirm.setComponent(new ComponentName(r.activityInfo.packageName,r.activityInfo.name));a.startActivity(confirm);
  }catch(Exception e){Toast.makeText(a,UpdateText.get("install_failed"),Toast.LENGTH_LONG).show();}
 }
 private static void permission(Activity a){
  if(!alive(a))return;
  new AlertDialog.Builder(theme(a)).setTitle(UpdateText.get("permission_title")).setMessage(UpdateText.get("permission_message")).setNegativeButton(UpdateText.get("later"),null)
   .setPositiveButton(UpdateText.get("allow"),(d,w)->{quietly(a,"update_permission_wait",null);
    try{a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+a.getPackageName())));}catch(RuntimeException e){Toast.makeText(a,UpdateText.get("permission"),Toast.LENGTH_LONG).show();}}).show();
 }
 /** Head unit settings → Updates. */
 public static void show(Activity a){
  if(!alive(a))return;
  Context c=theme(a);int pad=Math.round(20*a.getResources().getDisplayMetrics().density);
  LinearLayout p=new LinearLayout(c);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(pad,pad/2,pad,pad/2);
  TextView current=new TextView(c);current.setTextSize(18);p.addView(current);
  Switch[] options={new Switch(c),new Switch(c),new Switch(c)};String[] keys={"automatic_check","automatic_download","wifi_only"},labels={"auto_check","auto_download","wifi_only"};
  for(int i=0;i<3;i++){options[i].setText(UpdateText.get(labels[i]));options[i].setTextSize(16);options[i].setMinHeight(pad*3);options[i].setEnabled(false);p.addView(options[i]);}
  TextView message=new TextView(c);message.setTextSize(16);message.setPadding(0,pad/2,0,pad/2);p.addView(message);
  Button check=button(c,p,"check"),download=button(c,p,"download"),install=button(c,p,"install"),pause=button(c,p,"pause");
  TextView note=new TextView(c);note.setText(UpdateText.get("note"));note.setPadding(0,pad/2,0,0);p.addView(note);
  ScrollView scroll=new ScrollView(c);scroll.addView(p);
  AlertDialog dialog=new AlertDialog.Builder(c).setTitle(UpdateText.get("updates")).setView(scroll).setPositiveButton(UpdateText.get("done"),null).create();
  ExecutorService io=Executors.newSingleThreadExecutor();boolean[] bound={false};
  check.setOnClickListener(v->quietly(a,"update_check",null));download.setOnClickListener(v->quietly(a,"update_download",null));
  install.setOnClickListener(v->install(a));pause.setOnClickListener(v->quietly(a,"update_pause",null));
  Runnable refresh=new Runnable(){public void run(){
   if(!dialog.isShowing() || !alive(a))return;
   try{io.execute(()->{Bundle s;try{s=call(a,"update_state",null);}catch(Throwable e){s=null;}Bundle state=s;main.post(()->{
    if(!dialog.isShowing())return;
    if(state==null){message.setText(UpdateText.get("unavailable"));for(Button b:new Button[]{check,download,install,pause})b.setEnabled(false);}
    else{
     current.setText(UpdateText.format("current",state.getString("version","")));message.setText(UpdateText.status(state));
     if(!bound[0]){bound[0]=true;for(int i=0;i<3;i++){String key=keys[i];options[i].setChecked(state.getBoolean(key,true));options[i].setEnabled(true);
      options[i].setOnCheckedChangeListener((s2,on)->{Bundle b=new Bundle();b.putBoolean(key,on);quietly(a,"update_set",b);});}}
     boolean busy=state.getBoolean("downloading") || state.getBoolean("preparing");
     check.setEnabled(!busy);download.setEnabled(state.containsKey("candidate") && !state.getBoolean("ready") && !busy);install.setEnabled(state.getBoolean("ready") && !busy);pause.setEnabled(state.getBoolean("downloading"));
    }
    main.postDelayed(this,700);
   });});}catch(RejectedExecutionException ignored){}
  }};
  dialog.setOnDismissListener(d->{main.removeCallbacks(refresh);io.shutdown();});
  dialog.show();main.post(refresh);
 }
 private static Button button(Context c,LinearLayout p,String key){Button b=new Button(c);b.setText(UpdateText.get(key));b.setAllCaps(false);b.setEnabled(false);p.addView(b);return b;}
 private UpdateDialog(){}
}
