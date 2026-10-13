package local.spotify.unified;
import android.app.*;import android.content.*;import android.graphics.Color;import android.os.*;import android.widget.*;import java.util.concurrent.*;
/** A first-use choice on a foreground player launch, never from playback/error callbacks. */
public final class DiagnosticNoticeActivity extends Activity {
 private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private boolean destroyed;
 public static void maybeShow(Activity player){new Handler(Looper.getMainLooper()).postDelayed(()->{
  if(player.isFinishing() || player.isDestroyed() || !player.hasWindowFocus())return;
  new Thread(()->{try{Bundle state=HeadUnitSettings.call(player,"diagnostics_notice",null);if(state.getBoolean("notice_required"))player.runOnUiThread(()->{if(!player.isFinishing() && !player.isDestroyed() && player.hasWindowFocus())player.startActivity(new Intent(player,DiagnosticNoticeActivity.class));});}catch(RuntimeException ignored){}},"SpotifyDiagnosticsChoice").start();
 },500);}
 public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff121212);LinearLayout list=new LinearLayout(this);list.setOrientation(1);int p=Math.round(20*getResources().getDisplayMetrics().density);list.setPadding(p,p,p,p);scroll.addView(list);setContentView(scroll);
  label(list,"Help improve Spotify",26);label(list,DiagnosticsConfig.DISCLOSURE,18);label(list,"After Continue, automatic reports cover measured technical failures only, at most one new report per 24 hours. Server support is checked before upload. You can turn this off in Settings > Support; manual sending remains available.",18);
  CheckBox choice=new CheckBox(this);choice.setText("Automatically send diagnostics");choice.setTextColor(Color.WHITE);choice.setTextSize(20);choice.setChecked(true);choice.setEnabled(false);list.addView(choice);
  TextView status=label(list,"",16);Button next=new Button(this);next.setText("Continue");next.setAllCaps(false);next.setEnabled(false);list.addView(next);
  worker.execute(()->{try{Bundle b=HeadUnitSettings.call(this,"diagnostics_notice",null);main.post(()->{if(!destroyed){choice.setChecked(b.getBoolean("auto_choice",true));choice.setEnabled(true);next.setEnabled(true);}});}catch(RuntimeException e){main.post(()->{if(!destroyed)status.setText("Choice could not be loaded. Reopen Support to try again.");});}});
  next.setOnClickListener(v->{next.setEnabled(false);boolean selected=choice.isChecked();worker.execute(()->{try{Bundle b=new Bundle();b.putBoolean("auto_choice",selected);HeadUnitSettings.call(this,"diagnostics_continue",b);main.post(()->{if(!destroyed)finish();});}catch(RuntimeException e){main.post(()->{if(!destroyed){next.setEnabled(true);status.setText("Choice could not be saved. Try again.");}});}});});
 }
 private TextView label(LinearLayout list,String text,int size){TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(0,12,0,12);list.addView(t);return t;}
 protected void onDestroy(){destroyed=true;worker.shutdown();main.removeCallbacksAndMessages(null);super.onDestroy();}
}
