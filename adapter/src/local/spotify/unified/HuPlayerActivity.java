package local.spotify.unified;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * Our own interface (no BYD code): the navigation rail, the content card (Home, Recents, Browse,
 * Library; Search next) with our mini-player as its footer, and our player sliding up over
 * them at full height, leaving the navigation in view. On portrait screens the navigation is a
 * bar along the bottom, under the mini-player, within reach. Opened from Head unit settings
 * while the BYD-derived interface stays the main one.
 */
public final class HuPlayerActivity extends Activity {
 private HuUi ui;private HuSession session;private HuRail rail;private FrameLayout content;private HuNowPlaying player;
 private static String resized;
 private boolean expanded;private String page="home";private final Map<String,String> ids=new HashMap<>();
 protected void attachBaseContext(Context base){super.attachBaseContext(HeadUnitSettings.scaled(base));}
 protected void onCreate(Bundle state){
  super.onCreate(state);ui=new HuUi(this);ui.window();session=HuSession.get(this);
  boolean accent;try{accent=HeadUnitSettings.call(this,"get",null).getBoolean("cover_accent",true);}catch(RuntimeException e){accent=true;}
  if(state!=null)page=state.getString("page",page);
  FrameLayout root=new FrameLayout(this);root.setBackgroundColor(ui.dark?0xff000000:0xffe4e4e4);
  int gap=ui.dp(6);android.content.res.Configuration cf=getResources().getConfiguration();boolean bar=cf.screenWidthDp<cf.screenHeightDp;
  LinearLayout shell=new LinearLayout(this);shell.setOrientation(bar?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);shell.setPadding(gap,gap,gap,gap);root.addView(shell);
  rail=new HuRail(ui,bar,this::show,()->startActivity(new Intent(this,HeadUnitSettingsActivity.class)));
  // The stage holds the content card with the mini-player and, over both, the player: it covers
  // them at full height and leaves the navigation in view.
  FrameLayout stage=new FrameLayout(this);
  if(bar){shell.addView(stage,new LinearLayout.LayoutParams(-1,0,1));LinearLayout.LayoutParams railParams=new LinearLayout.LayoutParams(-1,ui.dp(104));railParams.topMargin=gap;shell.addView(rail,railParams);}
  else{shell.addView(rail,new LinearLayout.LayoutParams(ui.dp(120),-1));LinearLayout.LayoutParams stageParams=new LinearLayout.LayoutParams(0,-1,1);stageParams.setMarginStart(gap);shell.addView(stage,stageParams);}
  LinearLayout column=new LinearLayout(this);column.setOrientation(LinearLayout.VERTICAL);stage.addView(column,new FrameLayout.LayoutParams(-1,-1));
  content=new FrameLayout(this);content.setBackground(HuUi.round(ui.card,ui.dp(22)));content.setClipToOutline(true);content.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
  column.addView(content,new LinearLayout.LayoutParams(-1,0,1));
  HuMiniPlayer mini=new HuMiniPlayer(ui,accent,this::expand);
  LinearLayout.LayoutParams miniParams=new LinearLayout.LayoutParams(-1,-2);miniParams.topMargin=gap;column.addView(mini,miniParams);
  float stageWidth=cf.screenWidthDp-12-(bar?0:126),stageHeight=cf.screenHeightDp-12-(bar?110:0);
  player=new HuNowPlaying(ui,accent,stageWidth,stageHeight,this::collapse);player.setVisibility(View.GONE);stage.addView(player,new FrameLayout.LayoutParams(-1,-1));
  setContentView(root);
  // Splitting the screen resizes the window without recreating the screen when the new size
  // crosses none of the app's resource size buckets; our layout depends on the size, so it is
  // built again (once per size, should the measured size never match the configuration).
  int laidWidth=cf.screenWidthDp,laidHeight=cf.screenHeightDp;
  root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
   float d=getResources().getDisplayMetrics().density;int w=Math.round(v.getWidth()/d),h=Math.round(v.getHeight()/d);String size=w+"x"+h;
   if(w>0 && h>0 && (Math.abs(w-laidWidth)>48 || Math.abs(h-laidHeight)>48) && !size.equals(resized) && !isFinishing()){resized=size;v.post(this::recreate);}
  });
  // Day/night and other configuration changes recreate the screen; the open player stays open.
  if(state!=null && state.getBoolean("expanded")){expanded=true;player.setVisibility(View.VISIBLE);}
  content.addView(HuPages.message(ui,HuText.get("loading")));rail.select(page);
  session.whenReady(()->session.children(session.root(),items->{
   String[][] prefixes={{"home","spotify:home"},{"recents","spotify:recently-played"},{"browse","spotify:browse"},{"library","spotify:library"}};
   for(String[] p:prefixes){MediaItem i=HuBrowse.find(items,p[1]);if(i!=null)ids.put(p[0],HuBrowse.id(i));}
   show(page);
  }));
 }
 /** Shows a page of the rail. */
 private void show(String key){
  page=key;rail.select(key);content.removeAllViews();
  HuPages.Open open=item->session.play(HuBrowse.id(item));
  View v;
  switch(key){
   case "home":v=HuPages.home(ui,ids.get("home"),open);break;
   case "recents":v=HuPages.grid(ui,HuText.get("recents"),ids.get("recents"),open);break;
   case "browse":v=HuPages.grid(ui,HuText.get("browse"),ids.get("browse"),open);break;
   case "library":v=HuPages.library(ui,ids.get("library"),open);break;
   default:v=HuPages.message(ui,HuText.get("search_next"));
  }
  content.addView(v,new FrameLayout.LayoutParams(-1,-1));
 }
 protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("expanded",expanded);out.putString("page",page);}
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
