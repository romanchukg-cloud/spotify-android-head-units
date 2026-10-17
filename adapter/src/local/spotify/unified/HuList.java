package local.spotify.unified;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * The list of a page or of search results: the backend's items in their groups (group titles as
 * headings), browsable items as a row of cards and tracks or episodes as rows (number or cover,
 * explicit mark, artists, length; the playing one in the accent colour; a tap plays it in its
 * context). kind: playlist, album, artist, show or search, for how rows are numbered.
 */
final class HuList {
 private static final class Entry {
  static final int HEADING=0,TRACK=1,CARDS=2;
  final int type,position;final String heading;final MediaItem item;final List<MediaItem> items;
  Entry(int type,String heading,MediaItem item,List<MediaItem> items,int position){this.type=type;this.heading=heading;this.item=item;this.items=items;this.position=position;}
 }
 final ListView view;final List<MediaItem> tracks=new ArrayList<>();
 private final List<Entry> entries=new ArrayList<>();private final BaseAdapter adapter;private String playing;

 HuList(HuUi ui,String kind,HuPages.Open open){
  HuSession session=HuSession.get(ui.activity);playing=session.trackUri();
  view=new ListView(ui.activity);view.setDivider(null);view.setSelector(new android.graphics.drawable.ColorDrawable(0));view.setCacheColorHint(0);view.setClipToPadding(false);
  adapter=new BaseAdapter(){
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
    Row r=old instanceof Row?(Row)old:new Row(ui);r.bind(e.item,e.position,kind,playing,v->open.item(e.item));return r;
   }
  };
  view.setAdapter(adapter);
  HuSession.Listener listener=s->{String now=s.trackUri();if(!Objects.equals(now,playing)){playing=now;adapter.notifyDataSetChanged();}};
  view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
   public void onViewAttachedToWindow(View v){session.add(listener);}
   public void onViewDetachedFromWindow(View v){session.remove(listener);}
  });
 }
 /**
  * Shows the items: in each group a row of cards first, then the tracks; the same item twice in a
  * group (search's top results) once. headings: group titles even when there is only one group.
  * Returns the total length of the tracks.
  */
 long fill(List<MediaItem> items,boolean headings){
  entries.clear();tracks.clear();long total=0;
  LinkedHashMap<String,List<MediaItem>> groups=HuBrowse.groups(items);boolean titles=headings || groups.size()>1;
  for(Map.Entry<String,List<MediaItem>> g:groups.entrySet()){
   if(!g.getKey().isEmpty() && titles)entries.add(new Entry(Entry.HEADING,g.getKey(),null,null,0));
   List<MediaItem> cards=new ArrayList<>(),rows=new ArrayList<>();Set<String> seen=new HashSet<>();
   for(MediaItem i:g.getValue()){if(!seen.add(String.valueOf(HuBrowse.uri(HuBrowse.id(i)))))continue;(i.isBrowsable()?cards:rows).add(i);}
   if(!cards.isEmpty())entries.add(new Entry(Entry.CARDS,null,null,cards,0));
   int n=0;for(MediaItem i:rows){entries.add(new Entry(Entry.TRACK,null,i,null,++n));tracks.add(i);total+=HuBrowse.duration(i);}
  }
  adapter.notifyDataSetChanged();return total;
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
   String icon=HuBrowse.icon(item);boolean album="album".equals(kind),show="show".equals(kind) || String.valueOf(HuBrowse.uri(HuBrowse.id(item))).startsWith("spotify:episode:"),showCover=!album && icon!=null;
   int n=album && HuBrowse.number(item)>0?HuBrowse.number(item):position;
   number.setText(String.valueOf(n));number.setVisibility(show || "search".equals(kind) || showCover && !"artist".equals(kind)?GONE:VISIBLE);
   cover.setVisibility(showCover?VISIBLE:GONE);cover.setImageDrawable(null);if(showCover)HuCovers.into(cover,icon,ui.dp(56));
   boolean current=playing!=null && playing.equals(HuBrowse.uri(HuBrowse.id(item)));
   title.setText(HuBrowse.title(item));title.setTextColor(current?ui.accent:ui.text);
   badge.setVisibility(HuBrowse.explicit(item)?VISIBLE:GONE);subtitle.setText(HuBrowse.subtitle(item));
   long d=HuBrowse.duration(item);duration.setText(d>0 && !show?HuControls.time(d):"");title.setMaxLines(show?2:1);title.setSingleLine(!show);
   setContentDescription(HuBrowse.title(item));setOnClickListener(click);
  }
 }
}
