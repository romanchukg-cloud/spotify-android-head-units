package local.spotify.unified;
import android.os.*;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * The queue in our interface, a card over the player: the current track, the queued tracks and
 * the rest of the context in the backend's groups (Now playing, Next in queue, Next from …). A
 * row plays that track; its minus button removes it. The actions above the list start a song or
 * artist radio from the current track, set the sleep timer, open Jam and clear the queued tracks.
 * Back closes the queue.
 */
final class HuQueue extends LinearLayout implements HuSession.Listener {
 private static final String GROUP="android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT";
 private final HuUi ui;private final HuSession session;private final ListView list;private final TextView empty,songRadio,artistRadio,clear,sleep;
 private final List<Object> rows=new ArrayList<>();private final BaseAdapter adapter;private final Set<Long> removing=new HashSet<>();
 private final Runnable sleepLabel=this::updateSleep;private List<QueueItem> shown;private long shownActive=-2;
 HuQueue(HuUi ui,Runnable close){
  super(ui.activity);this.ui=ui;session=HuSession.get(ui.activity);setOrientation(VERTICAL);setClickable(true);
  setBackground(HuUi.round(ui.card,ui.dp(22)));setClipToOutline(true);setOutlineProvider(ViewOutlineProvider.BACKGROUND);setPaddingRelative(ui.dp(12),ui.dp(8),ui.dp(16),0);
  LinearLayout top=new LinearLayout(ui.activity);top.setGravity(Gravity.CENTER_VERTICAL);
  ImageView back=HuControls.button(ui,"encore_icon_chevron_left_24",HuText.get("back"),ui.dp(64),v->close.run());if(back.getDrawable()!=null)back.getDrawable().setAutoMirrored(true);top.addView(back);
  TextView title=ui.label(HuText.get("queue"),28,ui.text,true);title.setPaddingRelative(ui.dp(8),0,0,0);top.addView(title,new LayoutParams(0,-2,1));
  addView(top);
  // Actions on their own row, scrollable when the card is narrow.
  HorizontalScrollView actionsScroll=HuPages.row(ui);LinearLayout actions=new LinearLayout(ui.activity);actions.setPadding(ui.dp(4),ui.dp(4),ui.dp(4),ui.dp(8));actionsScroll.addView(actions);
  songRadio=pill(HuText.get("song_radio"),v->radio(false));artistRadio=pill(HuText.get("artist_radio"),v->radio(true));sleep=pill(HuText.get("sleep_timer"),v->HuSleep.menu(ui,session,this::updateSleep));
  clear=pill(HuText.get("clear_queue"),v->clearQueued());
  actions.addView(songRadio);actions.addView(artistRadio);actions.addView(sleep);actions.addView(pill(HuText.get("jam"),v->HeadUnitJam.open(getContext())));actions.addView(clear);
  addView(actionsScroll);
  FrameLayout body=new FrameLayout(ui.activity);addView(body,new LayoutParams(-1,0,1));
  list=new ListView(ui.activity);list.setDivider(null);list.setSelector(new android.graphics.drawable.ColorDrawable(0));list.setCacheColorHint(0);list.setClipToPadding(false);list.setPadding(0,0,0,ui.dp(16));
  empty=HuPages.message(ui,HuText.get("queue_empty"));
  body.addView(list,new FrameLayout.LayoutParams(-1,-1));body.addView(empty,new FrameLayout.LayoutParams(-1,-2));
  adapter=new BaseAdapter(){
   public int getCount(){return rows.size();}public Object getItem(int p){return rows.get(p);}public long getItemId(int p){return p;}
   public int getViewTypeCount(){return 2;}public int getItemViewType(int p){return rows.get(p) instanceof String?0:1;}
   public boolean isEnabled(int p){return false;}
   public View getView(int p,View old,ViewGroup parent){
    Object o=rows.get(p);
    if(o instanceof String){TextView h=old instanceof TextView?(TextView)old:heading();h.setText((String)o);return h;}
    Row r=old instanceof Row?(Row)old:new Row();r.bind((Entry)o);return r;
   }
  };
  list.setAdapter(adapter);
 }
 private TextView heading(){TextView h=ui.label("",15,ui.secondary,true);h.setLetterSpacing(0.06f);h.setAllCaps(true);h.setPaddingRelative(ui.dp(12),ui.dp(18),0,ui.dp(6));return h;}
 private TextView pill(String text,View.OnClickListener click){
  TextView t=ui.label(text,17,ui.text,true);t.setGravity(Gravity.CENTER);t.setPadding(ui.dp(22),0,ui.dp(22),0);
  t.setBackground(ui.pressable(ui.control,ui.dp(30)));t.setOnClickListener(click);
  LayoutParams p=new LayoutParams(-2,ui.dp(60));p.setMarginEnd(ui.dp(10));t.setLayoutParams(p);return t;
 }
 protected void onAttachedToWindow(){super.onAttachedToWindow();session.add(this);updateSleep();}
 /** The card is kept while hidden: opening it refreshes the sleep timer's label and goes to the top. */
 void opened(){updateSleep();list.setSelection(0);}
 protected void onDetachedFromWindow(){session.remove(this);removeCallbacks(sleepLabel);super.onDetachedFromWindow();}

 private static final class Entry {final QueueItem item;final int index;final boolean playing;Entry(QueueItem i,int n,boolean p){item=i;index=n;playing=p;}}
 public void changed(HuSession s){
  songRadio.setVisibility(uri(s.trackUri(),"spotify:track:")!=null?VISIBLE:GONE);artistRadio.setVisibility(uri(s.artistUri(),"spotify:artist:")!=null?VISIBLE:GONE);
  List<QueueItem> queue=s.queue();long active=s.activeQueueItem();
  if(queue==shown && active==shownActive)return;shown=queue;shownActive=active;removing.clear();
  rows.clear();boolean queued=false;String group=null;
  for(int i=0;i<queue.size();i++){
   QueueItem item=queue.get(i);MediaDescriptionCompat d=item.getDescription();boolean playing=item.getQueueId()==active;
   String g=playing?HuText.get("now_playing"):d==null || d.getExtras()==null?null:d.getExtras().getString(GROUP);
   if(g!=null){g=g.trim();if(g.endsWith(":"))g=g.substring(0,g.length()-1);}
   if(isQueuedGroup(g))queued=true;
   if(g!=null && !g.isEmpty() && !g.equals(group)){rows.add(g);group=g;}
   rows.add(new Entry(item,i,playing));
  }
  clear.setVisibility(queued?VISIBLE:GONE);empty.setVisibility(queue.isEmpty()?VISIBLE:GONE);
  adapter.notifyDataSetChanged();
 }
 /** A queue row: cover, title (accent while it plays), artists and, for the others, a remove button. */
 private final class Row extends LinearLayout {
  final ImageView cover,remove;final TextView title,subtitle;Entry entry;
  Row(){
   super(ui.activity);setGravity(Gravity.CENTER_VERTICAL);setMinimumHeight(ui.dp(80));setPaddingRelative(ui.dp(8),ui.dp(6),ui.dp(4),ui.dp(6));setBackground(ui.pressable(0x00000000,ui.dp(14)));
   cover=new HuUi.Cover(ui,ui.dp(6));addView(cover,new LayoutParams(ui.dp(56),ui.dp(56)));
   LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(VERTICAL);texts.setPaddingRelative(ui.dp(16),0,ui.dp(8),0);
   title=ui.label("",19,ui.text,false);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);
   subtitle=ui.label("",15,ui.secondary,false);subtitle.setSingleLine(true);subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
   texts.addView(title);texts.addView(subtitle);addView(texts,new LayoutParams(0,-2,1));
   remove=HuControls.button(ui,"encore_icon_minus_alt_24",HuText.get("remove_from_queue"),ui.dp(68),v->{if(entry!=null && removing.add(entry.item.getQueueId())){session.removeAt(entry.index);setAlpha(.4f);}});addView(remove);
   setOnClickListener(v->{if(entry!=null && !entry.playing)session.skipTo(entry.item.getQueueId());});
  }
  void bind(Entry e){
   entry=e;MediaDescriptionCompat d=e.item.getDescription();
   title.setText(d==null || d.getTitle()==null?"":d.getTitle());title.setTextColor(e.playing?ui.accent:ui.text);
   CharSequence sub=d==null?null:d.getSubtitle();subtitle.setText(sub==null?"":sub);subtitle.setVisibility(sub==null || sub.length()==0?GONE:VISIBLE);
   cover.setImageDrawable(null);HuCovers.into(cover,d==null || d.getIconUri()==null?null:d.getIconUri().toString(),ui.dp(56));
   remove.setVisibility(e.playing?INVISIBLE:VISIBLE);setAlpha(removing.contains(e.item.getQueueId())?.4f:1f);
   setContentDescription(title.getText());
  }
 }
 /** Removes the tracks added to the queue, from the last one up, so earlier indexes stay valid. */
 private void clearQueued(){
  List<QueueItem> queue=session.queue();
  for(int i=queue.size()-1;i>=0;i--){MediaDescriptionCompat d=queue.get(i).getDescription();if(isQueuedGroup(d==null || d.getExtras()==null?null:d.getExtras().getString(GROUP)))session.removeAt(i);}
 }
 /** The backend's own label for tracks added to the queue (string queue_next_queued). */
 private boolean isQueuedGroup(String group){
  if(group==null)return false;int id=getResources().getIdentifier("queue_next_queued","string",getContext().getPackageName());
  String label=id==0?"Next in queue":getResources().getString(id);return group.trim().startsWith(label.trim());
 }
 private static String uri(String value,String prefix){return value!=null && value.startsWith(prefix)?value:null;}
 /** Song or artist radio from the current track (spotify:station:track:… / spotify:station:artist:…). */
 private void radio(boolean artist){
  String u=artist?uri(session.artistUri(),"spotify:artist:"):uri(session.trackUri(),"spotify:track:");
  if(u!=null)session.playUri("spotify:station:"+u.substring("spotify:".length()));
 }
 private void updateSleep(){
  long left=HuSleep.left(getContext());boolean end=HuSleep.endOfTrack(getContext());
  sleep.setText(left<=0?HuText.get("sleep_timer"):HuText.get("sleep_timer")+" · "+(end?HuText.get("end_of_track"):String.format(HuText.get("minutes"),(left+59999)/60000)));
  sleep.setTextColor(left>0?ui.accent:ui.text);
  removeCallbacks(sleepLabel);if(left>0)postDelayed(sleepLabel,30000);
 }
}
