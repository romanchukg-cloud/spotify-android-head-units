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
 private LinearLayout list;private TextView status;private boolean destroyed;
 public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff121212);list=new LinearLayout(this);list.setOrientation(1);int p=dp(20);list.setPadding(p,p,p,p);scroll.addView(list);setContentView(scroll);
  TextView title=new TextView(this);title.setText(HuText.get("settings"));title.setTextSize(26);title.setTextColor(Color.WHITE);list.addView(title);
  status=new TextView(this);status.setTextColor(0xff1ed760);status.setTextSize(16);list.addView(status);
  Button accounts=new Button(this);accounts.setAllCaps(false);accounts.setText(ProfileText.get("profiles"));accounts.setTextSize(22);accounts.setMinHeight(dp(64));list.addView(accounts,new LinearLayout.LayoutParams(-1,-2));
  accounts.setOnClickListener(v->{startActivity(new Intent(this,ProfilesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));finish();});
  worker.execute(()->{try{Bundle b=HeadUnitSettings.call(this,"get",null);main.post(()->{if(!destroyed)render(b);});}catch(RuntimeException e){main.post(()->status.setText(HuText.get("error")));}});
 }
 private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
 private void render(Bundle b){
  for(String key:new String[]{"stop_close","external","resume"}){
   Switch row=new Switch(this);row.setText(HuText.get(key.equals("stop_close")?"close":key));row.setTextColor(Color.WHITE);row.setTextSize(18);row.setMinHeight(dp(60));row.setChecked(b.getBoolean(key));list.addView(row,new LinearLayout.LayoutParams(-1,-2));
   row.setOnCheckedChangeListener((button,value)->{Bundle change=new Bundle();change.putBoolean(key,value);save(change);});
  }
  TextView label=new TextView(this);label.setText(HuText.get("scale")+": "+b.getInt("scale")+" %");label.setTextSize(18);label.setTextColor(Color.WHITE);label.setPadding(0,dp(12),0,0);list.addView(label);
  SeekBar scale=new SeekBar(this);scale.setMax(50);scale.setProgress(b.getInt("scale")-80);list.addView(scale,new LinearLayout.LayoutParams(-1,dp(52)));
  scale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int p,boolean user){label.setText(HuText.get("scale")+": "+(p+80)+" %");}public void onStopTrackingTouch(SeekBar s){Bundle change=new Bundle();change.putInt("scale",s.getProgress()+80);save(change);}});
  Button audio=new Button(this);audio.setAllCaps(false);audio.setText(HuText.get("spotify"));audio.setTextSize(18);audio.setMinHeight(dp(56));list.addView(audio,new LinearLayout.LayoutParams(-1,-2));
  audio.setOnClickListener(v->startActivity(new Intent().setClassName(getPackageName(),"com.spotify.automotive.settingspage.SettingsPageActivity")));
  TextView profile=new TextView(this);profile.setText("5.5.0 · headunit-test.4 · "+b.getString("profile"));profile.setTextColor(0xffb3b3b3);profile.setPadding(0,dp(16),0,0);list.addView(profile);
 }
 private void save(Bundle b){worker.execute(()->{try{HeadUnitSettings.call(this,"set",b);main.post(()->{if(!destroyed)status.setText(HuText.get("saved"));});}catch(RuntimeException e){main.post(()->{if(!destroyed)status.setText(HuText.get("error"));});}});}
 protected void onDestroy(){destroyed=true;worker.shutdown();main.removeCallbacksAndMessages(null);super.onDestroy();}
}
