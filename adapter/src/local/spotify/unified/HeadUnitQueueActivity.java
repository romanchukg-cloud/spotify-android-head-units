package local.spotify.unified;
import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.*;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.List;
import java.util.concurrent.*;
/**
 * The queue editor (player process): the backend's media-session queue with large rows. A row plays
 * that track; its minus button removes it (support-library command REMOVE_QUEUE_ITEM_AT, which the
 * backend turns into a new queue without the track). The header starts a song or artist radio
 * (spotify:station:…) from the current track, clears the tracks added to the queue and opens Jam.
 */
public final class HeadUnitQueueActivity extends Activity {
 static final String REMOVE_AT="android.support.v4.media.session.command.REMOVE_QUEUE_ITEM_AT",INDEX="android.support.v4.media.session.command.ARGUMENT_INDEX",
  GROUP="android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT",ARTIST="com.spotify.music.extra.ARTIST_URI",
  SERVICE="com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService";
 private final Handler main=new Handler(Looper.getMainLooper());
 private final ExecutorService covers=Executors.newSingleThreadExecutor();
 private MediaBrowser browser;private MediaController controller;private HuUi ui;private LinearLayout rows;private TextView songRadio,artistRadio,clear;
 private final MediaController.Callback callback=new MediaController.Callback(){
  public void onQueueChanged(List<MediaSession.QueueItem> queue){render();}
  public void onMetadataChanged(MediaMetadata metadata){render();}
 };
 protected void attachBaseContext(Context base){super.attachBaseContext(HeadUnitSettings.scaled(base));}
 protected void onCreate(Bundle state){
  super.onCreate(state);ui=new HuUi(this);ui.window();
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(ui.bg);scroll.setFillViewport(true);
  LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(ui.dp(20),ui.dp(4),ui.dp(32),ui.dp(24));scroll.addView(page);
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setMinimumHeight(ui.dp(84));
  header.addView(ui.back());TextView title=ui.label(HuText.get("queue"),34,ui.text,true);title.setPadding(ui.dp(8),0,0,0);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  songRadio=pill(HuText.get("song_radio"),v->radio(false));artistRadio=pill(HuText.get("artist_radio"),v->radio(true));clear=pill(HuText.get("clear_queue"),v->clearQueued());
  header.addView(pill(HuText.get("jam"),v->HeadUnitJam.open(this)));header.addView(songRadio);header.addView(artistRadio);header.addView(clear);page.addView(header);
  rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);page.addView(rows);
  setContentView(scroll);
  browser=new MediaBrowser(this,new ComponentName(getPackageName(),SERVICE),new MediaBrowser.ConnectionCallback(){
   public void onConnected(){controller=new MediaController(HeadUnitQueueActivity.this,browser.getSessionToken());controller.registerCallback(callback,main);render();}
   public void onConnectionFailed(){finish();}
  },null);
  browser.connect();
 }
 protected void onDestroy(){if(controller!=null)controller.unregisterCallback(callback);if(browser!=null)browser.disconnect();covers.shutdownNow();super.onDestroy();}
 private TextView pill(String text,View.OnClickListener click){
  TextView t=ui.label(text,18,ui.text,true);t.setGravity(Gravity.CENTER);t.setMinHeight(ui.dp(64));t.setPadding(ui.dp(24),0,ui.dp(24),0);
  t.setBackground(ui.pressable(ui.control,ui.dp(32)));t.setOnClickListener(click);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,ui.dp(64));p.setMarginStart(ui.dp(12));t.setLayoutParams(p);return t;
 }
 /** The queue as rows, grouped by the backend's group titles (Now playing, Next in queue, Next from …). */
 private void render(){
  if(controller==null)return;rows.removeAllViews();
  List<MediaSession.QueueItem> queue=controller.getQueue();PlaybackState state=controller.getPlaybackState();
  long active=state==null?MediaSession.QueueItem.UNKNOWN_ID:state.getActiveQueueItemId();
  MediaMetadata now=controller.getMetadata();
  songRadio.setVisibility(trackUri(now)!=null?View.VISIBLE:View.GONE);artistRadio.setVisibility(artistUri(now)!=null?View.VISIBLE:View.GONE);
  boolean queued=false;String group=null;
  if(queue==null || queue.isEmpty()){TextView empty=ui.label(HuText.get("queue_empty"),20,ui.secondary,false);empty.setPadding(ui.dp(12),ui.dp(24),0,0);rows.addView(empty);}
  else for(int i=0;i<queue.size();i++){
   MediaSession.QueueItem item=queue.get(i);MediaDescription d=item.getDescription();boolean playing=item.getQueueId()==active;
   String g=playing?HuText.get("now_playing"):d.getExtras()==null?null:d.getExtras().getString(GROUP);
   if(g!=null){g=g.trim();if(g.endsWith(":"))g=g.substring(0,g.length()-1);}
   if(isQueuedGroup(g))queued=true;
   if(g!=null && !g.isEmpty() && !g.equals(group)){TextView h=ui.label(g.toUpperCase(),16,ui.secondary,true);h.setLetterSpacing(0.06f);h.setPadding(ui.dp(12),ui.dp(20),0,ui.dp(6));rows.addView(h);group=g;}
   rows.addView(row(item,i,playing));
  }
  clear.setVisibility(queued?View.VISIBLE:View.GONE);
 }
 private View row(MediaSession.QueueItem item,int index,boolean playing){
  MediaDescription d=item.getDescription();
  LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setMinimumHeight(ui.dp(88));r.setPadding(ui.dp(12),ui.dp(8),ui.dp(8),ui.dp(8));
  r.setBackground(ui.pressable(playing?ui.control:ui.bg,ui.dp(16)));
  ImageView cover=new ImageView(this);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setBackground(HuUi.round(ui.control,ui.dp(8)));cover.setClipToOutline(true);
  r.addView(cover,new LinearLayout.LayoutParams(ui.dp(64),ui.dp(64)));load(cover,d.getIconUri());
  LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.setPadding(ui.dp(16),0,ui.dp(8),0);
  TextView t=ui.label(String.valueOf(d.getTitle()),20,playing?ui.accent:ui.text,true);t.setSingleLine(true);t.setEllipsize(android.text.TextUtils.TruncateAt.END);texts.addView(t);
  if(d.getSubtitle()!=null){TextView s=ui.label(d.getSubtitle().toString(),16,ui.secondary,false);s.setSingleLine(true);s.setEllipsize(android.text.TextUtils.TruncateAt.END);texts.addView(s);}
  r.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
  if(!playing){
   ImageView remove=new ImageView(this);Drawable minus=icon("encore_icon_minus_alt_24");if(minus!=null)remove.setImageDrawable(minus);
   int pad=ui.dp(18);remove.setPadding(pad,pad,pad,pad);remove.setBackground(ui.pressable(ui.bg,ui.dp(38)));remove.setContentDescription(HuText.get("remove_from_queue"));
   remove.setOnClickListener(v->{removeAt(index);r.setAlpha(.4f);v.setEnabled(false);});
   r.addView(remove,new LinearLayout.LayoutParams(ui.dp(76),ui.dp(76)));
  }
  r.setOnClickListener(v->{controller.getTransportControls().skipToQueueItem(item.getQueueId());finish();});
  return r;
 }
 private Drawable icon(String name){
  int id=getResources().getIdentifier(name,"drawable",getPackageName());if(id==0)return null;
  Drawable d=getDrawable(id).mutate();d.setTint(ui.text);return d;
 }
 private void load(ImageView view,Uri uri){
  if(uri==null)return;
  covers.execute(()->{
   try(java.io.InputStream in=getContentResolver().openInputStream(uri)){
    BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=2;Bitmap b=BitmapFactory.decodeStream(in,null,o);
    if(b!=null)main.post(()->{if(view.isAttachedToWindow())view.setImageBitmap(b);});
   }catch(Exception ignored){}
  });
 }
 private void removeAt(int index){Bundle b=new Bundle();b.putInt(INDEX,index);controller.sendCommand(REMOVE_AT,b,null);}
 /** Removes the tracks added to the queue, from the last one up, so earlier indexes stay valid. */
 private void clearQueued(){
  List<MediaSession.QueueItem> queue=controller.getQueue();if(queue==null)return;
  for(int i=queue.size()-1;i>=0;i--){Bundle e=queue.get(i).getDescription().getExtras();if(isQueuedGroup(e==null?null:e.getString(GROUP)))removeAt(i);}
 }
 /** The backend's own label for tracks added to the queue (string queue_next_queued). */
 private boolean isQueuedGroup(String group){
  if(group==null)return false;int id=getResources().getIdentifier("queue_next_queued","string",getPackageName());
  String label=id==0?"Next in queue":getString(id);return group.trim().startsWith(label.trim());
 }
 static String trackUri(MediaMetadata m){
  String uri=m==null?null:m.getString(MediaMetadata.METADATA_KEY_MEDIA_URI);
  return uri!=null && uri.startsWith("spotify:track:")?uri:null;
 }
 static String artistUri(MediaMetadata m){
  String uri=m==null?null:m.getString(ARTIST);
  return uri!=null && uri.startsWith("spotify:artist:")?uri:null;
 }
 /** spotify:track:ID → spotify:station:track:ID (likewise for an artist). */
 static String station(String uri){return uri==null?null:"spotify:station:"+uri.substring("spotify:".length());}
 private void radio(boolean artist){
  MediaMetadata m=controller==null?null:controller.getMetadata();String uri=station(artist?artistUri(m):trackUri(m));
  if(uri==null)return;controller.getTransportControls().playFromUri(Uri.parse(uri),new Bundle());finish();
 }
}
