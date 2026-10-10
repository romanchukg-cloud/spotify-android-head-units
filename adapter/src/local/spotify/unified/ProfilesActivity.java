package local.spotify.unified;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;
/** Independent lightweight process survives restarting backend and UI. */
public final class ProfilesActivity extends Activity {
 private static final Uri ENDPOINT=Uri.parse("content://com.spotify.music.local.profiles");
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final Handler main=new Handler(Looper.getMainLooper());
 private LinearLayout root,rows;private TextView status;private Button retry;private boolean busy,destroyed;private int tries;
 private Bundle pendingRestart;private ProfileRestart restart;
 private String t(String key){return ProfileText.get(key);}
 private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density);}
 public void onCreate(Bundle state){
  super.onCreate(state);if(Build.VERSION.SDK_INT>=30)getWindow().setDecorFitsSystemWindows(true);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);
  if(state!=null)pendingRestart=state.getBundle("profileRestart");
  root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(28),dp(18),dp(28),dp(18));root.setBackgroundColor(0xff121212);setContentView(root);
  TextView title=new TextView(this);title.setText(t("profiles"));title.setTextColor(Color.WHITE);title.setTextSize(30);root.addView(title);
  TextView note=new TextView(this);note.setText(t("note"));note.setTextColor(0xffb3b3b3);note.setTextSize(16);root.addView(note);
  status=new TextView(this);status.setTextColor(0xff1ed760);status.setTextSize(18);status.setPadding(0,dp(8),0,dp(8));root.addView(status);
  ScrollView scroll=new ScrollView(this);rows=new LinearLayout(this);rows.setOrientation(1);scroll.addView(rows);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout footer=new LinearLayout(this);footer.addView(button(t("add"),v->new AlertDialog.Builder(this).setMessage(t("confirmAdd")).setNegativeButton(t("cancel"),null).setPositiveButton(t("yes"),(d,w)->request("add",null,null)).show()));
  footer.addView(button(t("back"),v->{try{startActivity(new Intent().setClassName(getPackageName(),"local.bydui.com.vivid.spotify.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));finish();}catch(RuntimeException e){status.setText(t("launchError"));}}));
  retry=button(t("retry"),v->load());retry.setVisibility(View.GONE);footer.addView(retry);root.addView(footer);
 }
 protected void onResume(){super.onResume();tries=0;if(pendingRestart!=null){if(!busy)resumeRestart();}else load();}
 private Button button(String text,View.OnClickListener click){
  Button b=new Button(this);b.setText(text);b.setTextSize(17);b.setMinWidth(0);b.setMinimumWidth(0);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setMinHeight(dp(54));b.setPadding(dp(16),dp(6),dp(16),dp(6));
  GradientDrawable shape=new GradientDrawable();shape.setColor(0xff282828);shape.setCornerRadius(dp(12));b.setBackground(shape);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(4),dp(6),dp(8),dp(6));b.setLayoutParams(p);b.setOnClickListener(v->{if(!busy){if(pendingRestart!=null)resumeRestart();else click.onClick(v);}});return b;
 }
 private void load(){if(!busy && !destroyed)request("list",null,null);}
 private void request(String method,String id,Bundle args){
  if(busy)return;busy=true;status.setText(t(method.equals("list")?"loading":"switching"));
  worker.execute(()->{
   Bundle result=null;try{result=getContentResolver().call(ENDPOINT,method,id,args);}catch(RuntimeException ignored){}
   final Bundle answer=result;main.post(()->{
    if(destroyed)return;busy=false;
    if(answer==null || answer.getBoolean("error")){status.setText(t("error"));retry.setVisibility(View.VISIBLE);return;}
    if(!answer.getBoolean("ready")){if(++tries<15){status.setText(t("loading"));main.postDelayed(this::load,1000);}else {status.setText(t("error"));retry.setVisibility(View.VISIBLE);}return;}
    if(answer.getBoolean("restart")){restart(answer);return;}
    if(!method.equals("list")){load();return;}render(answer);
   });
  });
 }
 private void render(Bundle data){
  rows.removeAllViews();status.setText("");retry.setVisibility(View.GONE);ArrayList<String> ids=data.getStringArrayList("ids"),names=data.getStringArrayList("names");
  if(ids==null || ids.isEmpty()){status.setText(t("empty"));return;}
  for(int i=0;i<ids.size();i++){
   final String id=ids.get(i),name=names.get(i);boolean active=id.equals(data.getString("active"));
   LinearLayout card=new LinearLayout(this);card.setOrientation(1);
   LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);card.addView(row);
   LinearLayout actions=new LinearLayout(this);card.addView(actions);
   TextView avatar=new TextView(this);avatar.setText(name.isEmpty()?"?":name.substring(0,name.offsetByCodePoints(0,1)).toUpperCase(java.util.Locale.getDefault()));avatar.setGravity(Gravity.CENTER);avatar.setTextSize(24);avatar.setTextColor(Color.BLACK);
   GradientDrawable circle=new GradientDrawable();circle.setShape(GradientDrawable.OVAL);circle.setColor(0xff1ed760);avatar.setBackground(circle);
   LinearLayout.LayoutParams icon=new LinearLayout.LayoutParams(dp(48),dp(48));icon.setMargins(0,0,dp(14),0);row.addView(avatar,icon);
   TextView label=new TextView(this);label.setText(name+(active?"  ·  "+t("active"):""));label.setTextSize(22);label.setTextColor(active?0xff1ed760:Color.WHITE);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
   actions.addView(button(t("choose"),v->request("restore",id,null)));
   actions.addView(button(t("rename"),v->{EditText input=new EditText(this);input.setText(name);input.setSingleLine(true);input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(40)});
    new AlertDialog.Builder(this).setTitle(t("name")).setView(input).setNegativeButton(t("cancel"),null).setPositiveButton(t("save"),(d,w)->{Bundle extras=new Bundle();extras.putString("name",input.getText().toString());request("rename",id,extras);}).show();}));
   if(!active)actions.addView(button(t("remove"),v->new AlertDialog.Builder(this).setMessage(t("remove")+": "+name+"?").setNegativeButton(t("cancel"),null).setPositiveButton(t("remove"),(d,w)->request("remove",id,null)).show()));
   rows.addView(card,new LinearLayout.LayoutParams(-1,-2));
  }
 }
 private void restart(Bundle result){
  pendingRestart=new Bundle(result);resumeRestart();
 }
 private java.util.List<ActivityManager.RunningAppProcessInfo> running(){
  java.util.List<ActivityManager.RunningAppProcessInfo> processes=getSystemService(ActivityManager.class).getRunningAppProcesses();
  if(processes==null)throw new IllegalStateException("Process state unavailable");return processes;
 }
 private boolean oldProcess(ActivityManager.RunningAppProcessInfo p){
  return p.uid==android.os.Process.myUid() && p.pid!=android.os.Process.myPid() &&
   ((p.pid==pendingRestart.getInt("uiPid") && p.processName.equals(getPackageName()+":bydui")) ||
    (p.pid==pendingRestart.getInt("backendPid") && p.processName.equals(getPackageName())));
 }
 private void resumeRestart(){
  if(pendingRestart==null || busy || destroyed)return;
  busy=true;retry.setVisibility(View.GONE);status.setText(t("switching"));
  if(restart!=null)restart.cancel();
  restart=new ProfileRestart(new ProfileRestart.Host(){
   public void stopOldProcesses(){for(ActivityManager.RunningAppProcessInfo p:running())if(oldProcess(p))android.os.Process.killProcess(p.pid);}
   public boolean oldProcessesStopped(){for(ActivityManager.RunningAppProcessInfo p:running())if(oldProcess(p))return false;return true;}
   public void launchPlayer(){
    Intent intent=new Intent().setClassName(getPackageName(),"local.bydui.com.vivid.spotify.MainActivity");
    intent.putExtra("local.profile.qr",pendingRestart.getBoolean("qr"));
    final ProfileRestart attempt=restart;
    intent.putExtra(ProfileButton.ACK,new ResultReceiver(main){protected void onReceiveResult(int code,Bundle data){if(!destroyed && code==1 && restart==attempt)attempt.acknowledge();}});
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivity(intent);
   }
   public void later(Runnable task,long delay){main.postDelayed(task,delay);}
   public void cancel(Runnable task){main.removeCallbacks(task);}
   public void finished(){boolean qr=pendingRestart.getBoolean("qr");pendingRestart=null;busy=false;status.setText("");if(!qr)finish();}
   public void failed(){busy=false;status.setText(t("launchError"));retry.setVisibility(View.VISIBLE);android.util.Log.w("SpotifyProfiles","Player launch not acknowledged; coordinator retained");}
  });
  restart.start();
 }
 protected void onSaveInstanceState(Bundle state){if(pendingRestart!=null)state.putBundle("profileRestart",pendingRestart);super.onSaveInstanceState(state);}
 protected void onDestroy(){destroyed=true;if(restart!=null)restart.cancel();main.removeCallbacksAndMessages(null);worker.shutdown();super.onDestroy();}
 public void onBackPressed(){if(!busy)super.onBackPressed();}
}
