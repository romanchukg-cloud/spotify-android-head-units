package local.spotify.unified;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.concurrent.*;
public final class HeadUnitSettingsActivity extends Activity {
 private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());
 private ScrollView scroll;private LinearLayout list;private TextView status;private boolean destroyed;
 public void onCreate(Bundle state){super.onCreate(state);
  scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);setContentView(scroll);
  worker.execute(()->{try{Bundle b=HeadUnitSettings.call(this,"get",null);main.post(()->{if(!destroyed)render(b);});}catch(RuntimeException e){main.post(()->{if(!destroyed)failed();});}});
 }
 private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
 private void render(Bundle b){modern(b);}
 /** The provider is unavailable: the header, the error and the Accounts button. */
 private void failed(){
  HuUi ui=new HuUi(this);ui.window();scroll.setBackgroundColor(ui.bg);list.setPadding(ui.dp(20),ui.dp(4),ui.dp(32),ui.dp(24));
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setMinimumHeight(ui.dp(84));
  header.addView(ui.back());TextView title=ui.label(HuText.get("settings"),34,ui.text,true);title.setPadding(ui.dp(8),0,0,0);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));list.addView(header);
  status=ui.label(HuText.get("error"),17,ui.accent,true);status.setPadding(ui.dp(12),0,0,ui.dp(8));list.addView(status);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.leftMargin=ui.dp(12);list.addView(ui.tile(ProfileText.get("profiles"),v->openAccounts()),p);
 }
 private void openAccounts(){startActivity(new Intent(this,ProfilesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));finish();}
 private void openSpotify(){startActivity(new Intent().setClassName(getPackageName(),"com.spotify.automotive.settingspage.SettingsPageActivity"));}
 private void footer(Bundle b,int color){
  String reason=b.getString("auth_reason");
  if(reason!=null){TextView signIn=new TextView(this);signIn.setText(SignInText.get("label")+": "+SignInText.get(reason)+" · "+java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT).format(new java.util.Date(b.getLong("auth_time"))));signIn.setTextColor(color);signIn.setPadding(0,dp(16),0,0);list.addView(signIn);}
  if(b.getLong("device_id_changed")>0){TextView id=new TextView(this);id.setText(SignInText.get("device_id"));id.setTextColor(color);id.setPadding(0,dp(8),0,0);list.addView(id);}
  TextView profile=new TextView(this);profile.setText("5.5.0 · "+HeadUnitBuild.LABEL+" · "+HeadUnitBuild.VERSION_CODE+" · "+b.getString("profile"));profile.setTextColor(color);profile.setPadding(0,dp(16),0,0);list.addView(profile);
 }
 /** Grouped cards, 84 dp rows and large controls. */
 private void modern(Bundle b){
  HuUi ui=new HuUi(this);ui.window();scroll.setBackgroundColor(ui.bg);list.setPadding(ui.dp(20),ui.dp(4),ui.dp(32),ui.dp(24));
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setMinimumHeight(ui.dp(84));
  header.addView(ui.back());TextView title=ui.label(HuText.get("settings"),34,ui.text,true);title.setPadding(ui.dp(8),0,0,0);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  list.addView(header);status=ui.label("",17,ui.accent,true);status.setPadding(ui.dp(12),0,0,ui.dp(8));list.addView(status);
  boolean cover=b.getBoolean("cover_accent",true);
  LinearLayout appearance=ui.card(HuText.get("appearance"));
  HuUi.Toggle coverToggle=new HuUi.Toggle(ui,cover);
  appearance.addView(ui.switchRow(HuText.get("cover"),HuText.get("cover_hint"),coverToggle,v->{coverToggle.set(!coverToggle.on());Bundle change=new Bundle();change.putBoolean("cover_accent",coverToggle.on());save(change);}));
  ui.divider(appearance);
  int[] scale={b.getInt("scale",100)};TextView value=ui.label(scale[0]+"%",24,ui.text,true);value.setGravity(Gravity.CENTER);value.setMinWidth(ui.dp(84));
  LinearLayout stepper=new LinearLayout(this);stepper.setGravity(Gravity.CENTER_VERTICAL);
  stepper.addView(ui.circle("−",HuText.get("scale")+" −",v->{if(scale[0]>80){scale[0]-=5;value.setText(scale[0]+"%");Bundle change=new Bundle();change.putInt("scale",scale[0]);save(change);}}));
  stepper.addView(value);
  stepper.addView(ui.circle("+",HuText.get("scale")+" +",v->{if(scale[0]<130){scale[0]+=5;value.setText(scale[0]+"%");Bundle change=new Bundle();change.putInt("scale",scale[0]);save(change);}}));
  appearance.addView(ui.row(HuText.get("scale"),null,stepper));
  LinearLayout playback=ui.card(HuText.get("channel"));
  String[] keys={"resume","play_open","external"};
  for(int i=0;i<keys.length;i++){
   String key=keys[i];if(i>0)ui.divider(playback);
   HuUi.Toggle toggle=new HuUi.Toggle(ui,b.getBoolean(key));
   playback.addView(ui.switchRow(HuText.get(key.equals("stop_close")?"close":key),null,toggle,v->{toggle.set(!toggle.on());Bundle change=new Bundle();change.putBoolean(key,toggle.on());save(change);}));
  }
  boolean wide=HuUi.wide(this);
  LinearLayout cards=new LinearLayout(this);cards.setBaselineAligned(false);cards.setOrientation(wide?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
  LinearLayout.LayoutParams first=wide?new LinearLayout.LayoutParams(0,-2,1):new LinearLayout.LayoutParams(-1,-2);LinearLayout.LayoutParams second=wide?new LinearLayout.LayoutParams(0,-2,1):new LinearLayout.LayoutParams(-1,-2);
  if(wide){second.leftMargin=ui.dp(16);}else{second.topMargin=ui.dp(16);}
  cards.addView(appearance,first);cards.addView(playback,second);
  LinearLayout.LayoutParams cardsParams=new LinearLayout.LayoutParams(-1,-2);cardsParams.leftMargin=ui.dp(12);list.addView(cards,cardsParams);
  LinearLayout tiles=new LinearLayout(this);tiles.setBaselineAligned(false);tiles.setOrientation(wide?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
  TextView[] items={ui.tile(ProfileText.get("profiles"),v->openAccounts()),ui.tile(UpdateText.get("updates"),v->UpdateDialog.show(this)),ui.tile(DiagnosticText.get("title"),v->startActivity(new Intent(this,DiagnosticActivity.class))),ui.tile(HuText.get("spotify"),v->openSpotify()),ui.tile(HuText.get("about"),v->HuFree.show(this,String.format(HuText.get("version"),"5.5.0 · "+HeadUnitBuild.LABEL+" · "+HeadUnitBuild.VERSION_CODE)))};
  for(int i=0;i<items.length;i++){LinearLayout.LayoutParams p=wide?new LinearLayout.LayoutParams(0,-1,1):new LinearLayout.LayoutParams(-1,-2);if(i>0){if(wide)p.leftMargin=ui.dp(16);else p.topMargin=ui.dp(12);}tiles.addView(items[i],p);}
  LinearLayout.LayoutParams tilesParams=new LinearLayout.LayoutParams(-1,-2);tilesParams.topMargin=ui.dp(16);tilesParams.leftMargin=ui.dp(12);list.addView(tiles,tilesParams);
  LinearLayout foot=list;int before=foot.getChildCount();footer(b,ui.secondary);for(int i=before;i<foot.getChildCount();i++)foot.getChildAt(i).setPadding(ui.dp(12),foot.getChildAt(i).getPaddingTop(),0,0);
 }
 private void save(Bundle b){worker.execute(()->{try{HeadUnitSettings.call(this,"set",b);main.post(()->{if(!destroyed && status!=null)status.setText(HuText.get("saved"));});}catch(RuntimeException e){main.post(()->{if(!destroyed && status!=null)status.setText(HuText.get("error"));});}});}
 protected void onDestroy(){destroyed=true;worker.shutdown();main.removeCallbacksAndMessages(null);super.onDestroy();}
}
