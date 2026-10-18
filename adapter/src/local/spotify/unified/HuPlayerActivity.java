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
 * our mini-player as its footer, and our player sliding up over them at full height, leaving the
 * navigation in view. On portrait screens the navigation is a bar along the bottom, under the
 * mini-player, within reach. This is the app's main screen: the launcher entry keeps the
 * BYD-derived activity's name as an alias of it (launcher icons, the notification, profile
 * switching and the BYD media service open it by that name). Signed out, the content shows the
 * backend's sign-in request and opens its sign-in screen.
 */
public final class HuPlayerActivity extends Activity {
 private HuUi ui;private HuSession session;private HuRail rail;private FrameLayout content;private HuNowPlaying player;
 private static String resized;private int[] laid;
 private boolean expanded,queueOpen;private HuQueue queue;private String page="home";private final Map<String,String> ids=new HashMap<>();
 private static final int VOICE=7;
 private HuSearch search;private String searchQuery,pendingVoice;
 /**
  * Navigation: each tab keeps its root page (scroll positions and the search query survive
  * switching tabs); pages opened from cards stack above it with their views, so back is instant.
  * After the screen is rebuilt (rotation, split screen) the stack keeps only its targets and
  * rebuilds those views when they are reached again.
  */
 private static final class Level {final HuDetail.Target target;final View view;Level(HuDetail.Target t,View v){target=t;view=v;}}
 private HuDetail.Target detail;private final ArrayDeque<Level> levels=new ArrayDeque<>();private final Map<String,View> tabs=new HashMap<>();
 private final ArrayList<HuDetail.Target> restored=new ArrayList<>();private float contentWidth,contentHeight;
 protected void attachBaseContext(Context base){super.attachBaseContext(HeadUnitSettings.scaled(base));}
 protected void onCreate(Bundle state){
  super.onCreate(state);local.spotify.close.TaskGuardService.opened(this);ui=new HuUi(this);ui.window();session=HuSession.get(this);
  boolean accent;try{accent=HeadUnitSettings.call(this,"get",null).getBoolean("cover_accent",true);}catch(RuntimeException e){accent=true;}
  if(state!=null){
   page=state.getString("page",page);detail=HuDetail.Target.restore(state);searchQuery=state.getString("search");
   ArrayList<Bundle> saved=state.getParcelableArrayList("trail");if(saved!=null)for(Bundle b:saved){HuDetail.Target t=HuDetail.Target.restore(b);if(t!=null)restored.add(t);}
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
  player=new HuNowPlaying(ui,accent,stageWidth,stageHeight,this::collapse,this::openQueue);player.setVisibility(View.GONE);stage.addView(player,new FrameLayout.LayoutParams(-1,-1));
  queue=new HuQueue(ui,this::closeQueue);queue.setVisibility(View.GONE);stage.addView(queue,new FrameLayout.LayoutParams(-1,-1));
  setContentView(root);
  // The keyboard: on landscape screens it lies over the interface (the search field is at the top),
  // since shrinking the window squeezes the navigation rail; portrait screens make room for it.
  getWindow().setSoftInputMode(bar?WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE:WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
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
  if(state!=null && state.getBoolean("queue")){queueOpen=true;queue.setVisibility(View.VISIBLE);}
  content.addView(HuPages.message(ui,HuText.get("loading")));rail.select(page);
  loadRoot();
  session.add(signInWatch);
 }
 protected void onDestroy(){session.remove(signInWatch);main.removeCallbacksAndMessages(null);super.onDestroy();}
 private final android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());
 /** The browse tree's root: the tabs' nodes, then the current tab (and a restored page above it). */
 private void loadRoot(){
  session.whenReady(()->session.children(session.root(),items->{
   String[][] prefixes={{"home","spotify:home"},{"recents","spotify:recently-played"},{"browse","spotify:browse"},{"library","spotify:library"}};
   for(String[] p:prefixes){MediaItem i=HuBrowse.find(items,p[1]);if(i!=null)ids.put(p[0],HuBrowse.id(i));}
   if(pendingVoice!=null){page="search";detail=null;restored.clear();}
   HuDetail.Target opened=detail;show(page);
   if(opened!=null){
    View tab=content.getChildCount()>0?content.getChildAt(0):null;
    for(HuDetail.Target t:restored)levels.addLast(new Level(t,null));levels.addLast(new Level(null,tab));
    detail=opened;place(detailView(opened),0);
   }
   restored.clear();
  }));
 }
 // Signed out: a panel over the content with the backend's message and its sign-in button; signed
 // in again, the tabs are loaded afresh.
 private View signInPanel;
 private final HuSession.Listener signInWatch=s->{
  boolean needed=s.signIn()!=null;
  if(needed && signInPanel==null){signInPanel=signInPanel();content.addView(signInPanel,new FrameLayout.LayoutParams(-1,-1));}
  else if(!needed && signInPanel!=null){content.removeView(signInPanel);signInPanel=null;tabs.clear();levels.clear();detail=null;search=null;ids.clear();loadRoot();}
 };
 private View signInPanel(){
  LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setGravity(Gravity.CENTER);p.setBackgroundColor(ui.card);p.setPadding(ui.dp(48),ui.dp(32),ui.dp(48),ui.dp(32));p.setClickable(true);
  ImageView logo=new ImageView(this);logo.setImageDrawable(HuControls.icon(this,"encore_icon_spotify_logo_24",ui.text));p.addView(logo,new LinearLayout.LayoutParams(ui.dp(72),ui.dp(72)));
  String message=session.errorMessage();TextView t=ui.label(message!=null && !message.isEmpty()?message:HuText.get("sign_in_hint"),24,ui.text,true);t.setGravity(Gravity.CENTER);t.setPadding(0,ui.dp(24),0,ui.dp(28));p.addView(t);
  String label=session.signInLabel();TextView b=ui.label(label!=null && !label.isEmpty()?label:HuText.get("sign_in"),20,0xff000000,true);b.setGravity(Gravity.CENTER);b.setMinHeight(ui.dp(72));b.setPadding(ui.dp(48),0,ui.dp(48),0);
  b.setBackground(ui.pressable(0xff1ed760,ui.dp(36)));b.setOnClickListener(v->launchSignIn());p.addView(b,new LinearLayout.LayoutParams(-2,ui.dp(72)));
  return p;
 }
 /** Opens the backend's sign-in screen as the BYD-derived screen did (its pending intent). */
 private boolean launchSignIn(){
  android.app.PendingIntent pi=session.signIn();if(pi==null)return false;
  try{
   android.app.ActivityOptions o=android.app.ActivityOptions.makeBasic();if(android.os.Build.VERSION.SDK_INT>=34)o.setPendingIntentBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED);
   int flags=Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP;startIntentSender(pi.getIntentSender(),null,flags,flags,0,o.toBundle());return true;
  }catch(IntentSender.SendIntentException | RuntimeException e){android.util.Log.w("SpotifyHU","Sign-in screen unavailable",e);return false;}
 }
 /** A profile switch asks for the QR sign-in: it opens once the backend asks to sign in (up to 45 s). */
 void signIn(){
  main.post(new Runnable(){int attempts;public void run(){
   if(isFinishing() || isDestroyed() || launchSignIn())return;
   if(++attempts<180)main.postDelayed(this,250);else Toast.makeText(HuPlayerActivity.this,ProfileText.get("loginRetry"),Toast.LENGTH_LONG).show();
  }});
 }
 /**
  * The window's size: the decor view follows rotation and split screens, not the keyboard. (The
  * window metrics of this activity's scaled context keep the size it was created with.)
  */
 private int[] windowSize(){View d=getWindow().getDecorView();return new int[]{d.getWidth(),d.getHeight()};}
 /** Shows a tab at its root page; the current tab, tapped at its root, is built afresh. */
 private void show(String key){
  boolean refresh=key.equals(page) && detail==null && levels.isEmpty();
  page=key;detail=null;levels.clear();rail.select(key);
  if(refresh){View old=tabs.remove(key);if(old!=null && "search".equals(key) && search!=null){searchQuery=search.query();search=null;}}
  place(tab(key),0);
  if("search".equals(key) && search!=null && pendingVoice!=null){search.spoken(pendingVoice);pendingVoice=null;}
 }
 private View tab(String key){
  View v=tabs.get(key);if(v!=null)return v;
  HuPages.Open open=this::open;
  switch(key){
   case "home":v=HuPages.home(ui,ids.get("home"),open);break;
   case "recents":v=HuPages.grid(ui,HuText.get("recents"),ids.get("recents"),open,null);break;
   case "browse":v=HuPages.grid(ui,HuText.get("browse"),ids.get("browse"),open,null);break;
   case "library":v=HuPages.library(ui,ids.get("library"),open);break;
   default:search=new HuSearch(ui,searchQuery,open,this::listen);v=search.view;
  }
  tabs.put(key,v);return v;
 }
 /** A card: pages for browsable items (the current page stays below for back); playable ones play. */
 private void open(MediaItem item){
  if(!item.isBrowsable()){session.playItem(HuBrowse.id(item));return;}
  levels.push(new Level(detail,content.getChildCount()>0?content.getChildAt(0):null));
  detail=HuDetail.Target.of(item);place(detailView(detail),1);
 }
 private View detailView(HuDetail.Target t){
  return HuBrowse.kind(t.id)!=null?HuDetail.page(ui,t,contentWidth,contentHeight,this::back,this::open):HuPages.grid(ui,t.title,t.id,this::open,this::back);
 }
 private void back(){
  Level l=levels.poll();if(l==null){detail=null;place(tab(page),-1);return;}
  detail=l.target;place(l.view!=null?l.view:l.target==null?tab(page):detailView(l.target),-1);
 }
 /** Puts a page in the content card, sliding in from the side it comes from (mirrored in right-to-left). */
 private void place(View v,int direction){
  content.removeAllViews();if(v.getParent() instanceof ViewGroup)((ViewGroup)v.getParent()).removeView(v);
  content.addView(v,new FrameLayout.LayoutParams(-1,-1));
  if(signInPanel!=null)content.addView(signInPanel,new FrameLayout.LayoutParams(-1,-1));
  v.animate().cancel();
  if(direction==0){v.setAlpha(1);v.setTranslationX(0);return;}
  float shift=ui.dp(36)*direction*(content.getLayoutDirection()==View.LAYOUT_DIRECTION_RTL?-1:1);
  v.setAlpha(0);v.setTranslationX(shift);v.animate().alpha(1).translationX(0).setDuration(180).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
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
 protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("expanded",expanded);out.putBoolean("queue",queueOpen);out.putString("page",page);if(detail!=null)detail.save(out);
  out.putString("search",search!=null?search.query():searchQuery);
  ArrayList<Bundle> saved=new ArrayList<>();for(Level l:levels)if(l.target!=null){Bundle b=new Bundle();l.target.save(b);saved.add(b);}out.putParcelableArrayList("trail",saved);
 }
 private void expand(){
  if(expanded)return;expanded=true;player.setVisibility(View.VISIBLE);
  player.setTranslationY(player.getHeight()>0?player.getHeight():getResources().getDisplayMetrics().heightPixels);
  player.animate().translationY(0).setDuration(220).start();
 }
 /** The queue: a card over the player, sliding in from the end side; back closes it. */
 private void openQueue(){
  if(queueOpen)return;queueOpen=true;queue.opened();queue.setVisibility(View.VISIBLE);queue.setAlpha(0);
  float shift=ui.dp(48)*(queue.getLayoutDirection()==View.LAYOUT_DIRECTION_RTL?-1:1);queue.setTranslationX(shift);
  queue.animate().alpha(1).translationX(0).setDuration(200).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
 }
 private void closeQueue(){
  if(!queueOpen)return;queueOpen=false;
  float shift=ui.dp(48)*(queue.getLayoutDirection()==View.LAYOUT_DIRECTION_RTL?-1:1);
  queue.animate().alpha(0).translationX(shift).setDuration(160).withEndAction(()->{if(!queueOpen)queue.setVisibility(View.GONE);}).start();
 }
 private void collapse(){
  if(!expanded)return;expanded=false;
  player.animate().translationY(player.getHeight()).setDuration(200).withEndAction(()->player.setVisibility(View.GONE)).start();
 }
 /** A profile switch or a notification brings the screen back with a new intent (ProfileButton reads it). */
 protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);}
 /** A scale changed in Head unit settings applies when the screen is shown again. */
 protected void onResume(){
  super.onResume();
  local.spotify.close.TaskGuardService.opened(this);
  try{if(HeadUnitSettings.stale(this)){recreate();return;}}catch(RuntimeException ignored){}
 }
 /** Back: closes the queue, collapses the player, then goes back through the pages, then to Home, then leaves. */
 public void onBackPressed(){if(queueOpen)closeQueue();else if(expanded)collapse();else if(detail!=null)back();else if(!"home".equals(page) && !ids.isEmpty())show("home");else super.onBackPressed();}
}
