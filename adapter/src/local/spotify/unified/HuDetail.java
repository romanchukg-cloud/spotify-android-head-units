package local.spotify.unified;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * A playlist, album, artist or show page of our interface: cover, type, title, subtitle, the number of
 * songs with their total time, play (pause when this page is what plays), shuffle play and
 * download; then the backend's children: tracks as rows (album track numbers, playlist covers,
 * the playing one highlighted; a tap plays the track in this context) and browsable children,
 * such as an artist's albums, as a row of cards. Wide content keeps the header on the left beside
 * the list; narrow content scrolls it above the list.
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
 private static final class Entry {
  static final int HEADING=0,TRACK=1,CARDS=2;
  final int type,position;final String heading;final MediaItem item;final List<MediaItem> items;
  Entry(int type,String heading,MediaItem item,List<MediaItem> items,int position){this.type=type;this.heading=heading;this.item=item;this.items=items;this.position=position;}
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
  HuCovers.into(cover,t.icon,size);
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

  // The list: the header above it on narrow content, beside it on wide content.
  ListView list=new ListView(ui.activity);list.setDivider(null);list.setSelector(new android.graphics.drawable.ColorDrawable(0));list.setCacheColorHint(0);
  list.setClipToPadding(false);list.setPadding(ui.dp(8),ui.dp(wide?8:0),ui.dp(8),ui.dp(16));
  View page;
  if(wide){
   LinearLayout row=new LinearLayout(ui.activity);ScrollView side=new ScrollView(ui.activity);side.addView(header);
   row.addView(side,new LinearLayout.LayoutParams(0,-1,0.36f));row.addView(list,new LinearLayout.LayoutParams(0,-1,0.64f));page=row;
  }else{list.addHeaderView(header,null,false);page=list;}

  List<Entry> entries=new ArrayList<>();List<MediaItem> tracks=new ArrayList<>();
  BaseAdapter adapter=new BaseAdapter(){
   public int getCount(){return entries.size();}public Object getItem(int p){return entries.get(p);}public long getItemId(int p){return p;}
   public int getViewTypeCount(){return 3;}public int getItemViewType(int p){return entries.get(p).type;}
   public boolean isEnabled(int p){return false;}
   public View getView(int p,View old,ViewGroup parent){
    Entry e=entries.get(p);
    if(e.type==Entry.HEADING){TextView h=old instanceof TextView?(TextView)old:HuPages.heading(ui,"");h.setText(e.heading);return h;}
    if(e.type==Entry.CARDS){
     HorizontalScrollView s=HuPages.row(ui);LinearLayout cards=new LinearLayout(ui.activity);s.addView(cards);
     for(MediaItem i:e.items){HuBrowse.Card c=new HuBrowse.Card(ui,HuPages.card(ui));c.bind(i,v->open.item(i));cards.addView(c);}
     return s;
    }
    Row r=old instanceof Row?(Row)old:new Row(ui);r.bind(e.item,e.position,kind,session.trackUri(),v->session.playItem(HuBrowse.id(e.item)));return r;
   }
  };
  list.setAdapter(adapter);

  // Play: this page's context, or pause when it is what plays. Shuffle play turns shuffle on first.
  Runnable playState=()->{boolean on=here(session,t) && session.playing();
   play.setImageDrawable(HuControls.icon(ui.activity,on?"encore_icon_pause_24":"encore_icon_play_24",0xff000000));play.setContentDescription(HuText.get(on?"pause":"play"));
   shuffle.setImageDrawable(HuControls.icon(ui.activity,"encore_icon_shuffle_24",session.shuffled()?ui.accent:ui.text));};
  play.setOnClickListener(v->{if(here(session,t))session.playPause();else start(session,t,tracks,false);});
  shuffle.setOnClickListener(v->{if(session.shuffle()!=null && !session.shuffled())session.send(session.shuffle());start(session,t,tracks,true);});
  Download d=new Download(ui,session,t.id,download);d.show(state(t.actions));
  String[] playing={session.trackUri()};
  HuSession.Listener listener=s->{playState.run();String now=s.trackUri();if(!Objects.equals(now,playing[0])){playing[0]=now;adapter.notifyDataSetChanged();}};
  page.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
   public void onViewAttachedToWindow(View v){session.add(listener);d.attached(true);}
   public void onViewDetachedFromWindow(View v){session.remove(listener);d.attached(false);}
  });
  playState.run();

  session.children(t.id,items->{
   entries.clear();tracks.clear();long total=0;
   LinkedHashMap<String,List<MediaItem>> groups=HuBrowse.groups(items);boolean headings=groups.size()>1;
   for(Map.Entry<String,List<MediaItem>> g:groups.entrySet()){
    if(!g.getKey().isEmpty() && (headings || "artist".equals(kind)))entries.add(new Entry(Entry.HEADING,g.getKey(),null,null,0));
    List<MediaItem> cards=new ArrayList<>();int n=0;
    for(MediaItem i:g.getValue()){
     if(i.isBrowsable()){cards.add(i);continue;}
     entries.add(new Entry(Entry.TRACK,null,i,null,++n));tracks.add(i);total+=HuBrowse.duration(i);
    }
    if(!cards.isEmpty())entries.add(new Entry(Entry.CARDS,null,null,cards,0));
   }
   // The backend gives at most 200 children (Liked Songs can hold more): no count it cannot vouch for.
   meta.setText(items.isEmpty()?HuText.get("empty"):"artist".equals(kind) || "show".equals(kind) || tracks.isEmpty() || items.size()>=200?"":HuText.songs(tracks.size())+" · "+length(total));
   meta.setVisibility(meta.getText().length()==0?View.GONE:View.VISIBLE);
   // Some items (shows) come without a cover of their own: the first child's stands in.
   if(t.icon==null)for(MediaItem i:items)if(HuBrowse.icon(i)!=null){HuCovers.into(cover,HuBrowse.icon(i),size);break;}
   adapter.notifyDataSetChanged();
  });
  return page;
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

 /** A track row: number or cover, title (accent while it plays), explicit mark, artists, length. */
 private static final class Row extends LinearLayout {
  private final HuUi ui;private final TextView number,title,badge,subtitle,duration;private final ImageView cover;
  Row(HuUi ui){
   super(ui.activity);this.ui=ui;setGravity(Gravity.CENTER_VERTICAL);setMinimumHeight(ui.dp(76));setPaddingRelative(ui.dp(8),ui.dp(8),ui.dp(16),ui.dp(8));
   setBackground(ui.pressable(0x00000000,ui.dp(14)));
   number=ui.label("",17,ui.secondary,false);number.setGravity(Gravity.CENTER);addView(number,new LayoutParams(ui.dp(48),-2));
   cover=new ImageView(ui.activity);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setBackground(HuUi.round(ui.control,ui.dp(6)));cover.setClipToOutline(true);
   LayoutParams c=new LayoutParams(ui.dp(56),ui.dp(56));c.setMarginStart(ui.dp(4));addView(cover,c);
   LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(VERTICAL);texts.setPaddingRelative(ui.dp(16),0,ui.dp(12),0);
   title=ui.label("",19,ui.text,false);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);
   LinearLayout line=new LinearLayout(ui.activity);line.setGravity(Gravity.CENTER_VERTICAL);
   badge=ui.label("E",11,ui.bg,true);badge.setGravity(Gravity.CENTER);badge.setBackground(HuUi.round(ui.secondary,ui.dp(3)));badge.setPadding(ui.dp(5),0,ui.dp(5),0);badge.setContentDescription(HuText.get("explicit"));
   LayoutParams b=new LayoutParams(-2,-2);b.setMarginEnd(ui.dp(6));line.addView(badge,b);
   subtitle=ui.label("",15,ui.secondary,false);subtitle.setSingleLine(true);subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);line.addView(subtitle,new LayoutParams(0,-2,1));
   texts.addView(title);texts.addView(line);addView(texts,new LayoutParams(0,-2,1));
   duration=ui.label("",15,ui.secondary,false);addView(duration);
  }
  void bind(MediaItem item,int position,String kind,String playing,View.OnClickListener click){
   // Episodes carry their date and length in the subtitle: no number and no length column.
   String icon=HuBrowse.icon(item);boolean album="album".equals(kind),show="show".equals(kind),showCover=!album && icon!=null;
   int n=album && HuBrowse.number(item)>0?HuBrowse.number(item):position;
   number.setText(String.valueOf(n));number.setVisibility(show || showCover && !"artist".equals(kind)?GONE:VISIBLE);
   cover.setVisibility(showCover?VISIBLE:GONE);cover.setImageDrawable(null);if(showCover)HuCovers.into(cover,icon,ui.dp(56));
   boolean current=playing!=null && playing.equals(HuBrowse.uri(HuBrowse.id(item)));
   title.setText(HuBrowse.title(item));title.setTextColor(current?ui.accent:ui.text);
   badge.setVisibility(HuBrowse.explicit(item)?VISIBLE:GONE);subtitle.setText(HuBrowse.subtitle(item));
   long d=HuBrowse.duration(item);duration.setText(d>0 && !show?HuControls.time(d):"");title.setMaxLines(show?2:1);title.setSingleLine(!show);
   setContentDescription(HuBrowse.title(item));setOnClickListener(click);
  }
 }

 /**
  * The page's download button, as on the BYD-derived pages (HeadUnitDownloads): download;
  * downloading (tap stops); downloaded (tap asks before removing). Hidden for items without a
  * download action (keeping its place). A requested download can report "download" until Spotify starts it.
  */
 private static final class Download {
  private final HuUi ui;private final HuSession session;private final String id;private final ImageView view;
  private final Handler main=new Handler(Looper.getMainLooper());private final Runnable poll=this::refresh;
  private String state;private boolean attached=true,busy;private long pendingUntil;
  Download(HuUi ui,HuSession session,String id,ImageView view){
   this.ui=ui;this.session=session;this.id=id;this.view=view;
   view.setOnClickListener(v->click());
  }
  void attached(boolean on){attached=on;if(on)refresh();else main.removeCallbacks(poll);}
  void refresh(){session.item(id,i->show(i==null?null:state(HuBrowse.actions(i))));}
  void show(String value){
   if(!attached)return;busy=false;view.setAlpha(1f);
   if(HeadUnitDownloads.DOWNLOAD.equals(value) && SystemClock.elapsedRealtime()<pendingUntil)value=HeadUnitDownloads.DOWNLOADING;
   else if(!HeadUnitDownloads.DOWNLOADING.equals(value))pendingUntil=0;
   state=value;
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
