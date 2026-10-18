package local.spotify.unified;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * A playlist, album, artist or show page of our interface: cover, type, title, subtitle, the
 * number of songs with their total time, shuffle play, play (pause when this page is what plays)
 * and download; then the backend's children in a HuList: tracks that play in this context and
 * browsable children, such as an artist's albums, as a row of cards. Wide content keeps the
 * header on the left beside the list; narrow content scrolls it above the list.
 */
final class HuDetail {
 private HuDetail(){}
 private static final long POLL=2000,PENDING=30*60*1000L;

 /** What a page shows before its children arrive: taken from the card that opened it. */
 static final class Target {
  final String id,title,subtitle,icon;final ArrayList<String> actions;
  Target(String id,String title,String subtitle,String icon,ArrayList<String> actions){this.id=id;this.title=title==null?"":title;this.subtitle=subtitle==null?"":subtitle;this.icon=icon;this.actions=actions==null?new ArrayList<>():actions;}
  static Target of(MediaItem i){return new Target(HuBrowse.id(i),HuBrowse.title(i),HuBrowse.subtitle(i),HuBrowse.icon(i),HuBrowse.actions(i));}
  void save(Bundle b){b.putString("detail_id",id);b.putString("detail_title",title);b.putString("detail_subtitle",subtitle);b.putString("detail_icon",icon);b.putStringArrayList("detail_actions",actions);}
  static Target restore(Bundle b){String id=b==null?null:b.getString("detail_id");return id==null?null:new Target(id,b.getString("detail_title"),b.getString("detail_subtitle"),b.getString("detail_icon"),b.getStringArrayList("detail_actions"));}
 }
 /** width and height: the content card's size in dp. */
 static View page(HuUi ui,Target t,float width,float height,Runnable back,HuPages.Open open){
  HuSession session=HuSession.get(ui.activity);
  String kind=HuBrowse.kind(t.id);boolean round="artist".equals(kind),wide=width>=760;
  // Header: back, cover, texts and buttons.
  LinearLayout header=new LinearLayout(ui.activity);header.setOrientation(LinearLayout.VERTICAL);header.setPadding(ui.dp(wide?24:16),ui.dp(8),ui.dp(wide?24:16),ui.dp(16));
  // Back shares its row with the page type, so the cover can be larger.
  LinearLayout top=new LinearLayout(ui.activity);top.setGravity(Gravity.CENTER_VERTICAL);
  ImageView backButton=HuControls.button(ui,"encore_icon_chevron_left_24",HuText.get("back"),ui.dp(64),v->back.run());
  if(backButton.getDrawable()!=null)backButton.getDrawable().setAutoMirrored(true);
  top.addView(backButton);header.addView(top);
  int size=ui.dp(Math.max(120,wide?Math.min(260,Math.min(height-330,width*0.36f-32)):Math.min(220,width*0.5f)));
  ImageView cover=new ImageView(ui.activity);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setBackground(HuUi.round(ui.control,round?size/2f:ui.dp(12)));cover.setClipToOutline(true);
  LinearLayout.LayoutParams coverParams=new LinearLayout.LayoutParams(size,size);coverParams.gravity=Gravity.CENTER_HORIZONTAL;coverParams.topMargin=ui.dp(4);header.addView(cover,coverParams);
  // The header panel (Backdrop) shows the cover blurred; the cover and its colour arrive together.
  Integer[] tone={null};android.graphics.Bitmap[] art={null};Runnable[] paint={()->{}};
  java.util.function.Consumer<String> showCover=uri->{cover.setTag(uri);HuCovers.load(ui.activity,uri,size,(b,c)->{if(!Objects.equals(uri,cover.getTag()))return;cover.setImageBitmap(b);art[0]=b;tone[0]=c;paint[0].run();});};
  if(t.icon!=null)showCover.accept(t.icon);
  // Shows are podcasts and audiobooks alike in the backend, so they get no type label.
  TextView type=ui.label(kind==null || "show".equals(kind)?"":HuText.get(kind),16,ui.secondary,true);type.setPaddingRelative(ui.dp(8),0,0,0);top.addView(type);
  // Cover, texts and buttons are centred in the header; back and the page type stay at the top start.
  TextView title=HuControls.title(ui,wide?30:28,2);title.setText(t.title);title.setPadding(0,ui.dp(14),0,0);title.setGravity(Gravity.CENTER_HORIZONTAL);
  TextView subtitle=ui.label(t.subtitle,17,ui.secondary,false);subtitle.setMaxLines(2);subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);subtitle.setGravity(Gravity.CENTER_HORIZONTAL);if(t.subtitle.isEmpty())subtitle.setVisibility(View.GONE);
  TextView meta=ui.label(HuText.get("loading"),15,ui.secondary,false);meta.setPadding(0,ui.dp(6),0,0);meta.setGravity(Gravity.CENTER_HORIZONTAL);
  header.addView(title);header.addView(subtitle);header.addView(meta);
  LinearLayout buttons=new LinearLayout(ui.activity);buttons.setGravity(Gravity.CENTER);buttons.setPadding(0,ui.dp(16),0,0);
  int playSize=ui.dp(72);ImageView play=new ImageView(ui.activity);play.setScaleType(ImageView.ScaleType.FIT_CENTER);int pad=playSize*28/100;play.setPadding(pad,pad,pad,pad);
  play.setBackground(ui.pressable(0xff1ed760,playSize/2f));
  // Shuffle, play in the middle, download; a button a page lacks keeps its place, so play stays centred.
  ImageView shuffle=HuControls.button(ui,"encore_icon_shuffle_24",HuText.get("shuffle_play"),ui.dp(64),null);buttons.addView(shuffle);
  if("show".equals(kind))shuffle.setVisibility(View.INVISIBLE);
  LinearLayout.LayoutParams playParams=new LinearLayout.LayoutParams(playSize,playSize);playParams.setMargins(ui.dp(20),0,ui.dp(20),0);buttons.addView(play,playParams);
  ImageView download=HuControls.button(ui,"encore_icon_download_24",HuText.get("download"),ui.dp(64),null);download.setVisibility(View.INVISIBLE);buttons.addView(download);
  header.addView(buttons);
  // Download progress, counted from the tracks' downloaded marks while the page downloads.
  LinearLayout progressBox=new LinearLayout(ui.activity);progressBox.setOrientation(LinearLayout.VERTICAL);progressBox.setGravity(Gravity.CENTER_HORIZONTAL);progressBox.setPadding(0,ui.dp(14),0,0);progressBox.setVisibility(View.GONE);
  TextView progressText=ui.label("",15,ui.secondary,false);progressText.setGravity(Gravity.CENTER_HORIZONTAL);progressBox.addView(progressText);
  HuList.Progress progressBar=new HuList.Progress(ui);LinearLayout.LayoutParams pb=new LinearLayout.LayoutParams(ui.dp(240),ui.dp(4));pb.topMargin=ui.dp(8);progressBox.addView(progressBar,pb);
  header.addView(progressBox);

  // The list: the header above it on narrow content, beside it on wide content.
  HuList listing=new HuList(ui,kind,open);ListView list=listing.view;List<MediaItem> tracks=listing.tracks;
  list.setPadding(ui.dp(8),ui.dp(wide?8:0),ui.dp(8),ui.dp(16));
  View page;
  if(wide){
   LinearLayout row=new LinearLayout(ui.activity);ScrollView side=new ScrollView(ui.activity);side.addView(header);
   // A cover from the cache has arrived already: the panel paints it at once.
   Backdrop panel=new Backdrop(ui,side);paint[0]=()->panel.paint(art[0],tone[0]);paint[0].run();
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,0.36f);int gap=ui.dp(8);lp.topMargin=gap;lp.bottomMargin=gap;lp.setMarginStart(gap);
   row.addView(panel,lp);row.addView(list,new LinearLayout.LayoutParams(0,-1,0.64f));page=row;
  }else{
   Backdrop panel=new Backdrop(ui,header);paint[0]=()->panel.paint(art[0],tone[0]);paint[0].run();
   FrameLayout holder=new FrameLayout(ui.activity);int gap=ui.dp(8);holder.setPadding(0,gap,0,gap);holder.addView(panel,new FrameLayout.LayoutParams(-1,-2));
   list.addHeaderView(holder,null,false);page=list;
  }

  // Play: this page's context, or pause when it is what plays. Shuffle play turns shuffle on first.
  Runnable playState=()->{boolean on=here(session,t) && session.playing();
   play.setImageDrawable(HuControls.icon(ui.activity,on?"encore_icon_pause_24":"encore_icon_play_24",0xff000000));play.setContentDescription(HuText.get(on?"pause":"play"));
   shuffle.setImageDrawable(HuControls.icon(ui.activity,"encore_icon_shuffle_24",session.shuffled()?ui.accent:ui.text));};
  play.setOnClickListener(v->{if(here(session,t))session.playPause();else start(session,t,tracks,false);});
  shuffle.setOnClickListener(v->{if(session.shuffle()!=null && !session.shuffled())session.send(session.shuffle());start(session,t,tracks,true);});
  // While downloading, the list is reloaded every few seconds for the tracks' marks and the count.
  boolean[] full={false};
  Runnable progress=()->{
   int done=listing.downloadedCount(),all=listing.tracks.size();
   progressText.setText(full[0] || all==0?HuText.get("downloading_all"):String.format(Locale.getDefault(),HuText.get("downloaded_count"),done,all));
   progressBar.set(all==0?0:done/(float)all);
  };
  Runnable[] reload={null};
  reload[0]=()->session.children(t.id,items->{listing.fill(items,"artist".equals(kind));full[0]=items.size()>=200;progress.run();});
  Handler ticker=new Handler(Looper.getMainLooper());
  Runnable tick=new Runnable(){public void run(){reload[0].run();ticker.postDelayed(this,4000);}};
  String[] last={null};
  // The button's state is polled every 2 s: the reload timer starts and stops only when it changes.
  Download d=new Download(ui,session,t.id,download,value->{
   if(Objects.equals(value,last[0]))return;
   boolean on=HeadUnitDownloads.DOWNLOADING.equals(value),was=HeadUnitDownloads.DOWNLOADING.equals(last[0]);last[0]=value;
   listing.downloading(on);progressBox.setVisibility(on?View.VISIBLE:View.GONE);
   ticker.removeCallbacks(tick);if(on){progress.run();ticker.postDelayed(tick,4000);}
   // Finished: the list once more, so every track shows its mark.
   if(was && !on)reload[0].run();
  });
  d.show(state(t.actions));
  HuSession.Listener listener=s->playState.run();
  page.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
   public void onViewAttachedToWindow(View v){session.add(listener);d.attached(true);}
   public void onViewDetachedFromWindow(View v){session.remove(listener);d.attached(false);ticker.removeCallbacks(tick);last[0]=null;}
  });
  playState.run();

  session.children(t.id,items->{
   long total=listing.fill(items,"artist".equals(kind));full[0]=items.size()>=200;progress.run();
   // The backend gives at most 200 children (Liked Songs can hold more): no count it cannot vouch for.
   meta.setText(items.isEmpty()?HuText.get("empty"):"artist".equals(kind) || "show".equals(kind) || tracks.isEmpty() || items.size()>=200?"":HuText.songs(tracks.size())+" · "+length(total));
   meta.setVisibility(meta.getText().length()==0?View.GONE:View.VISIBLE);
   // Some items (shows) come without a cover of their own: the first child's stands in.
   if(t.icon==null)for(MediaItem i:items)if(HuBrowse.icon(i)!=null){showCover.accept(HuBrowse.icon(i));break;}
  });
  return page;
 }
 /**
  * The header's panel, so it stands apart from the track list: the page's cover, blurred and
  * dimmed (lightened by day), in a rounded panel 8 dp inside the content card (14 dp corners,
  * concentric with the card's 22 dp). Before Android 12 (no blur) it is a panel in the cover's
  * colour; without a cover, a slightly lighter grey.
  */
 private static final class Backdrop extends FrameLayout {
  private final HuUi ui;private final ImageView blur;private final View scrim;
  Backdrop(HuUi ui,View content){
   super(ui.activity);this.ui=ui;
   setBackground(HuUi.round(ui.dark?0xff262626:0xfff1f1f1,ui.dp(14)));setOutlineProvider(ViewOutlineProvider.BACKGROUND);setClipToOutline(true);
   blur=new ImageView(ui.activity);blur.setScaleType(ImageView.ScaleType.CENTER_CROP);blur.setVisibility(GONE);addView(blur,new LayoutParams(-1,-1));
   scrim=new View(ui.activity);scrim.setBackgroundColor(ui.dark?0x99000000:0x99ffffff);scrim.setVisibility(GONE);addView(scrim,new LayoutParams(-1,-1));
   addView(content,new LayoutParams(-1,content instanceof ScrollView?-1:-2));
  }
  void paint(android.graphics.Bitmap art,Integer color){
   if(art!=null && android.os.Build.VERSION.SDK_INT>=31){
    blur.setImageBitmap(art);blur.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(ui.dp(40),ui.dp(40),android.graphics.Shader.TileMode.CLAMP));
    blur.setVisibility(VISIBLE);scrim.setVisibility(VISIBLE);
   }else if(color!=null)((android.graphics.drawable.GradientDrawable)getBackground()).setColor(CoverAccent.tone(color,ui.dark,ui.dark?0.45f:1f));
  }
 }
 private static boolean here(HuSession s,Target t){String c=s.contextUri();return c!=null && c.equals(HuBrowse.uri(t.id));}
 /** Plays the page: its own play action, or (artists) its first track, a random one when shuffling. */
 private static void start(HuSession s,Target t,List<MediaItem> tracks,boolean shuffle){
  if(t.actions.contains("play") || tracks.isEmpty()){s.play(t.id);return;}
  s.playItem(HuBrowse.id(tracks.get(shuffle?new Random().nextInt(tracks.size()):0)));
 }
 private static String length(long ms){
  long minutes=Math.max(1,Math.round(ms/60000.0));
  return minutes>=60?String.format(Locale.getDefault(),HuText.get("hours_minutes"),minutes/60,minutes%60):String.format(Locale.getDefault(),HuText.get("minutes"),minutes);
 }
 /** The download state among the item's browser actions, or null when it cannot be downloaded. */
 static String state(List<String> actions){
  for(String a:actions)if(HeadUnitDownloads.DOWNLOAD.equals(a) || HeadUnitDownloads.DOWNLOADING.equals(a) || HeadUnitDownloads.DOWNLOADED.equals(a))return a;
  return null;
 }

 /**
  * The page's download button, as on the BYD-derived pages (HeadUnitDownloads): download;
  * downloading (tap stops); downloaded (tap asks before removing). Hidden for items without a
  * download action (keeping its place). A requested download can report "download" until Spotify starts it.
  */
 private static final class Download {
  private final HuUi ui;private final HuSession session;private final String id;private final ImageView view;
  private final Handler main=new Handler(Looper.getMainLooper());private final Runnable poll=this::refresh;
  private String state;private boolean attached=true,busy;private long pendingUntil;private final java.util.function.Consumer<String> changed;
  Download(HuUi ui,HuSession session,String id,ImageView view,java.util.function.Consumer<String> changed){
   this.ui=ui;this.session=session;this.id=id;this.view=view;this.changed=changed;
   view.setOnClickListener(v->click());
  }
  void attached(boolean on){attached=on;if(on)refresh();else main.removeCallbacks(poll);}
  void refresh(){session.item(id,i->show(i==null?null:state(HuBrowse.actions(i))));}
  void show(String value){
   if(!attached)return;busy=false;view.setAlpha(1f);
   if(HeadUnitDownloads.DOWNLOAD.equals(value) && SystemClock.elapsedRealtime()<pendingUntil)value=HeadUnitDownloads.DOWNLOADING;
   else if(!HeadUnitDownloads.DOWNLOADING.equals(value))pendingUntil=0;
   state=value;changed.accept(value);
   if(value==null){view.setVisibility(View.INVISIBLE);return;}
   int res=ui.activity.getResources().getIdentifier("ic_action_"+value,"drawable",ui.activity.getPackageName());
   Drawable d=res==0?null:ui.activity.getDrawable(res);if(d==null){view.setVisibility(View.INVISIBLE);return;}
   d=d.mutate();d.setTint(HeadUnitDownloads.DOWNLOADED.equals(value)?ui.accent:ui.text);view.setImageDrawable(d);view.setVisibility(View.VISIBLE);
   view.setContentDescription(HuText.get(HeadUnitDownloads.DOWNLOAD.equals(value)?"download":HeadUnitDownloads.DOWNLOADING.equals(value)?"stop_download":"remove_download"));
   main.removeCallbacks(poll);if(HeadUnitDownloads.DOWNLOADING.equals(value))main.postDelayed(poll,POLL);
  }
  void click(){
   if(state==null || busy)return;
   if(HeadUnitDownloads.DOWNLOADED.equals(state)){
    new AlertDialog.Builder(ui.activity).setTitle(HuText.get("remove_ask")).setPositiveButton(HuText.get("remove"),(x,w)->send(HeadUnitDownloads.DOWNLOADED)).setNegativeButton(HuText.get("cancel"),null).show();
    return;
   }
   pendingUntil=HeadUnitDownloads.DOWNLOAD.equals(state)?SystemClock.elapsedRealtime()+PENDING:0;send(state);
   if(pendingUntil>0)show(HeadUnitDownloads.DOWNLOADING);
  }
  void send(String action){
   busy=true;view.setAlpha(.5f);
   session.action(action,id,error->{
    if(error!=null){Toast.makeText(ui.activity,error.isEmpty()?HuText.get("download_error"):error,Toast.LENGTH_LONG).show();pendingUntil=0;}
    refresh();
   });
  }
 }
}
