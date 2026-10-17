package local.spotify.unified;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
/**
 * The first screen of our own interface (no BYD code): content area, our mini-player as its
 * footer and our player, which slides up over everything. Home, Library and Search follow; until
 * then the content area explains that and the BYD-derived interface stays the player.
 */
public final class HuPlayerActivity extends Activity {
 private HuUi ui;private HuNowPlaying player;private boolean expanded;
 protected void attachBaseContext(Context base){super.attachBaseContext(HeadUnitSettings.scaled(base));}
 protected void onCreate(Bundle state){
  super.onCreate(state);ui=new HuUi(this);ui.window();
  boolean accent;try{accent=HeadUnitSettings.call(this,"get",null).getBoolean("cover_accent",true);}catch(RuntimeException e){accent=true;}
  FrameLayout root=new FrameLayout(this);root.setBackgroundColor(ui.dark?0xff000000:0xffe4e4e4);
  int gap=ui.dp(6);
  LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(gap,gap,gap,gap);root.addView(page);
  // Content card (Home, Library and Search come here next).
  LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setGravity(Gravity.CENTER);
  content.setBackground(HuUi.round(ui.card,ui.dp(22)));content.setPadding(ui.dp(32),ui.dp(16),ui.dp(32),ui.dp(16));
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.addView(ui.back());
  TextView title=ui.label(HuText.get("player_preview"),30,ui.text,true);title.setPadding(ui.dp(8),0,0,0);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  content.addView(header,new LinearLayout.LayoutParams(-1,-2));
  TextView hint=ui.label(HuText.get("player_preview_hint"),20,ui.secondary,false);hint.setGravity(Gravity.CENTER);
  content.addView(hint,new LinearLayout.LayoutParams(-1,0,1));
  page.addView(content,new LinearLayout.LayoutParams(-1,0,1));
  HuMiniPlayer mini=new HuMiniPlayer(ui,accent,this::expand);
  LinearLayout.LayoutParams miniParams=new LinearLayout.LayoutParams(-1,-2);miniParams.topMargin=gap;page.addView(mini,miniParams);
  player=new HuNowPlaying(ui,accent,this::collapse);player.setVisibility(View.GONE);root.addView(player,new FrameLayout.LayoutParams(-1,-1));
  setContentView(root);
  // Day/night and other configuration changes recreate the screen; the open player stays open.
  if(state!=null && state.getBoolean("expanded")){expanded=true;player.setVisibility(View.VISIBLE);}
 }
 protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("expanded",expanded);}
 private void expand(){
  if(expanded)return;expanded=true;player.setVisibility(View.VISIBLE);
  player.setTranslationY(player.getHeight()>0?player.getHeight():getResources().getDisplayMetrics().heightPixels);
  player.animate().translationY(0).setDuration(220).start();
 }
 private void collapse(){
  if(!expanded)return;expanded=false;
  player.animate().translationY(player.getHeight()).setDuration(200).withEndAction(()->player.setVisibility(View.GONE)).start();
 }
 public void onBackPressed(){if(expanded)collapse();else super.onBackPressed();}
}
