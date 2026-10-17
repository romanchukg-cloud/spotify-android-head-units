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
 * Library, Search; playlist, album, artist and category pages opened from their cards) with
 * our mini-player as its footer, and our player sliding up over
 * them at full height, leaving the navigation in view. On portrait screens the navigation is a
 * bar along the bottom, under the mini-player, within reach. Opened from Head unit settings
 * while the BYD-derived interface stays the main one.
 */
public final class HuPlayerActivity extends Activity {
 private HuUi ui;private HuSession session;private HuRail rail;private FrameLayout content;private HuNowPlaying player;
 private static String resized;private int[] laid;
 private boolean expanded;private String page="home";private final Map<String,String> ids=new HashMap<>();
 private static final int VOICE=7;
 private HuSearch search;private String searchQuery,pendingVoice;
 private HuDetail.Target detail;private final ArrayDeque<HuDetail.Target> trail=new ArrayDeque<>();private float contentWidth,contentHeight;
 protected void attachBaseContext(Context base){super.attachBaseContext(HeadUnitSettings.scaled(base));}
 protected void onCreate(Bundle state){
  super.onCreate(state);ui=new HuUi(this);ui.window();session=HuSession.get(this);
  boolean accent;try{accent=HeadUnitSettings.call(this,"get",null).getBoolean("cover_accent",true);}catch(RuntimeException e){accent=true;}
  if(state!=null){
   page=state.getString("page",page);detail=HuDetail.Target.restore(state);searchQuery=state.getString("search");
   ArrayList<Bundle> saved=state.getParcelableArrayList("trail");if(saved!=null)for(Bundle b:saved){HuDetail.Target t=HuDetail.Target.restore(b);if(t!=null)trail.addLast(t);}
  }
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
  float stageWidth=cf.screenWidthDp-12-(bar?0:126),stageHeight=cf.screenHeightDp-12-(bar?110:0);contentWidth=stageWidth;contentHeight=stageHeight-110;
  player=new HuNowPlaying(ui,accent,stageWidth,stageHeight,this::collapse);player.setVisibility(View.GONE);stage.addView(player,new FrameLayout.LayoutParams(-1,-1));
  setContentView(root);
  // Splitting the screen resizes the window without recreating the screen when the new size
  // crosses none of the app's resource size buckets; our layout depends on the size, so it is
  // built again (once per size). The window's own size counts: the keyboard leaves it alone.
  root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
   int[] now=windowSize();if(now[0]<=0 || now[1]<=0)return;
   if(laid==null){laid=now;return;}
   int limit=ui.dp(48);String size=now[0]+"x"+now[1];
   if((Math.abs(now[0]-laid[0])>limit || Math.abs(now[1]-laid[1])>limit) && !size.equals(resized) && !isFinishing()){resized=size;v.post(this::recreate);}
  });
  // Day/night and other configuration changes recreate the screen; the open player stays open.
  if(state!=null && state.getBoolean("expanded")){expanded=true;player.setVisibility(View.VISIBLE);}
  content.addView(HuPages.message(ui,HuText.get("loading")));rail.select(page);
  session.whenReady(()->session.children(session.root(),items->{
   String[][] prefixes={{"home","spotify:home"},{"recents","spotify:recently-played"},{"browse","spotify:browse"},{"library","spotify:library"}};
   for(String[] p:prefixes){MediaItem i=HuBrowse.find(items,p[1]);if(i!=null)ids.put(p[0],HuBrowse.id(i));}
   if(pendingVoice!=null){page="search";detail=null;trail.clear();}
   HuDetail.Target opened=detail;ArrayList<HuDetail.Target> path=new ArrayList<>(trail);show(page);
   if(opened!=null){trail.addAll(path);detail=opened;showDetail();}
  }));
 }
 /**
  * The window's size: the decor view follows rotation and split screens, not the keyboard. (The
  * window metrics of this activity's scaled context keep the size it was created with.)
  */
 private int[] windowSize(){View d=getWindow().getDecorView();return new int[]{d.getWidth(),d.getHeight()};}
 /** Shows a page of the rail. */
 private void show(String key){
  if(search!=null){searchQuery=search.query();search=null;}
  page=key;detail=null;trail.clear();rail.select(key);content.removeAllViews();
  HuPages.Open open=this::open;
  View v;
  switch(key){
   case "home":v=HuPages.home(ui,ids.get("home"),open);break;
   case "recents":v=HuPages.grid(ui,HuText.get("recents"),ids.get("recents"),open,null);break;
   case "browse":v=HuPages.grid(ui,HuText.get("browse"),ids.get("browse"),open,null);break;
   case "library":v=HuPages.library(ui,ids.get("library"),open);break;
   default:
    search=new HuSearch(ui,searchQuery,open,this::listen);v=search.view;
    if(pendingVoice!=null){search.spoken(pendingVoice);pendingVoice=null;}
  }
  content.addView(v,new FrameLayout.LayoutParams(-1,-1));
 }
 /** A card: pages for browsable items (the current page is kept for back); playable ones play. */
 private void open(MediaItem item){
  if(item.isBrowsable()){if(detail!=null)trail.push(detail);detail=HuDetail.Target.of(item);showDetail();}
  else session.playItem(HuBrowse.id(item));
 }
 private void showDetail(){
  content.removeAllViews();
  View v=HuBrowse.kind(detail.id)!=null?HuDetail.page(ui,detail,contentWidth,contentHeight,this::back,this::open):HuPages.grid(ui,detail.title,detail.id,this::open,this::back);
  content.addView(v,new FrameLayout.LayoutParams(-1,-1));
 }
 /** The microphone: the head unit's speech recognition; the text comes back to the search page. */
 private void listen(){try{startActivityForResult(HeadUnitVoice.recognizer(),VOICE);}catch(ActivityNotFoundException e){android.util.Log.w("SpotifyHU","Voice search unavailable",e);}}
 protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(request!=VOICE || result!=RESULT_OK || data==null)return;
  ArrayList<String> heard=data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
  String q=heard==null || heard.isEmpty() || heard.get(0)==null?"":heard.get(0).trim();if(q.isEmpty())return;
  if(search!=null && detail==null && "search".equals(page))search.spoken(q);
  else{pendingVoice=q;if(!ids.isEmpty())show("search");}
 }
 private void back(){if(!trail.isEmpty()){detail=trail.pop();showDetail();}else show(page);}
 protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("expanded",expanded);out.putString("page",page);if(detail!=null)detail.save(out);
  out.putString("search",search!=null?search.query():searchQuery);
  ArrayList<Bundle> saved=new ArrayList<>();for(HuDetail.Target t:trail){Bundle b=new Bundle();t.save(b);saved.add(b);}out.putParcelableArrayList("trail",saved);
 }
 private void expand(){
  if(expanded)return;expanded=true;player.setVisibility(View.VISIBLE);
  player.setTranslationY(player.getHeight()>0?player.getHeight():getResources().getDisplayMetrics().heightPixels);
  player.animate().translationY(0).setDuration(220).start();
 }
 private void collapse(){
  if(!expanded)return;expanded=false;
  player.animate().translationY(player.getHeight()).setDuration(200).withEndAction(()->player.setVisibility(View.GONE)).start();
 }
 public void onBackPressed(){if(expanded)collapse();else if(detail!=null)back();else super.onBackPressed();}
}
