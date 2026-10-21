package local.spotify.unified;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.*;
import android.os.Build;
import android.os.SystemClock;
import android.util.AtomicFile;
import android.util.Log;
import android.util.SizeF;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;
import java.io.*;
import java.util.concurrent.*;
import org.json.JSONObject;
/**
 * The home-screen widgets (HuWidgetWide, placed at about 4 x 2 cells, and HuWidgetSquare, about
 * 2 x 2; layouts in res-hu, chosen by the widget's size): cover, title, artist, elapsed and
 * remaining time and previous / play-pause / next. The Spotify backend's process follows its media session
 * (metadata and playback hooks through HeadUnitRuntime), keeps the state and a rounded cover in
 * files and pushes the widgets when the track, play/pause or position changes; the providers
 * draw the kept state when the launcher asks, and never start the backend. The times count by
 * themselves (Chronometer), so playing needs no updates. Without the backend the last track stays,
 * paused, and Play starts Spotify to resume it. Signed out (the track is forgotten) or with nothing
 * kept: "Open Spotify". A tap on the widget opens Spotify; the buttons go to HuWidgetActions.
 */
final class HuWidgets {
 private static final ExecutorService worker=Executors.newSingleThreadExecutor();
 private static String title,artist,art;private static long duration;
 private static boolean loaded,track,playing,signedOut;private static long position,positionAt;
 // What the widgets show now: the cover, play/pause, signing out and the position at a time.
 // The layouts by the size (dp) each needs inside the host's own padding. Android 12+ hosts pick the
 // closest that fits (RemoteViews by size); before, render() does the same with the reported size.
 private static final int SMALL=0,STRIP=1,STRIP_TIMES=2,SQUARE=3,LARGE=4;
 private static final float[][] SIZES={{168,152},{326,80},{400,90},{232,220},{540,250}};
 private static final int[] LAYOUTS={HuR.layout_hu_widget_small,HuR.layout_hu_widget_strip,HuR.layout_hu_widget_strip,HuR.layout_hu_widget_square,HuR.layout_hu_widget_wide};
 private static String shownArt;private static boolean shownPlaying,shownSignedOut,shown;private static long shownPosition,shownAt;
 private HuWidgets(){}

 /** Metadata from the backend's media session (MediaSessionCompat.setMetadata), read by name as HeadUnitRuntime does. */
 static synchronized void metadata(Context c,Object value){
  load(c);
  String t=text(value,"android.media.metadata.TITLE"),a=text(value,"android.media.metadata.ARTIST");
  Object u=call(value,"getString","android.media.metadata.ALBUM_ART_URI"),length=call(value,"getLong","android.media.metadata.DURATION");
  long d=length instanceof Long?Math.max(0,(Long)length):0;
  // A session without a track (as the backend starts) keeps the last one: Play resumes it.
  track=t!=null;if(t==null || eq(t,title) && eq(a,artist) && eq(u,art) && d==duration)return;
  title=t;artist=a;art=u==null?null:u.toString();duration=d;publish(c.getApplicationContext());
 }
 /** Playback state from the backend's media session (MediaSessionCompat.setPlaybackState), read by name. */
 static synchronized void playback(Context c,Object value){
  Object st=call(value,"getState"),ex=call(value,"getExtras"),pos=call(value,"getPosition"),upd=call(value,"getLastPositionUpdateTime");
  if(!(st instanceof Integer))return;
  load(c);int state=(Integer)st;boolean now=state==HuSession.STATE_PLAYING || state==HuSession.STATE_BUFFERING;
  android.os.Bundle extras=ex instanceof android.os.Bundle?(android.os.Bundle)ex:null;boolean out=state==HuSession.STATE_ERROR && extras!=null && extras.get("android.media.extras.ERROR_RESOLUTION_ACTION_INTENT")!=null;
  long at=upd instanceof Long && (Long)upd>0?(Long)upd:SystemClock.elapsedRealtime();
  // Without a track in the session (the backend starting) the kept track keeps its position.
  if(track){position=pos instanceof Long?Math.max(0,(Long)pos):0;positionAt=at;}
  playing=now;signedOut=out;
  // Signed out: the account's last track is not shown to whoever signs in next.
  if(out){title=null;artist=null;art=null;duration=0;}
  // Only what the widget shows: play/pause, signing out and a jump of the position (a seek).
  long expected=shownPosition+(shownPlaying?SystemClock.elapsedRealtime()-shownAt:0);
  if(shown && now==shownPlaying && out==shownSignedOut && Math.abs(current()-expected)<3000)return;
  publish(c.getApplicationContext());
 }
 /** Once per process: the kept track and position, paused, so a starting backend does not forget them. */
 private static void load(Context c){
  if(loaded)return;loaded=true;
  try{
   JSONObject s=new JSONObject(new String(new AtomicFile(new File(c.getFilesDir(),"hu_widget.json")).readFully(),java.nio.charset.StandardCharsets.UTF_8));
   if(s.isNull("title") || s.optBoolean("signed_out"))return;
   title=s.optString("title");artist=s.optString("artist");art=s.optString("art");if(art.isEmpty())art=null;duration=s.optLong("duration");
   position=s.optLong("position");positionAt=SystemClock.elapsedRealtime();shownArt=art;
  }catch(IOException | org.json.JSONException ignored){}
 }
 /** The backend's session ended: the widgets keep the track, paused. */
 static synchronized void ended(Context c){load(c);if(!playing)return;position=current();positionAt=SystemClock.elapsedRealtime();playing=false;publish(c.getApplicationContext());}
 private static long current(){long p=position+(playing?SystemClock.elapsedRealtime()-positionAt:0);return duration>0?Math.min(p,duration):p;}

 /** Keeps the state (and, for a new track, the cover) and updates the placed widgets. */
 private static void publish(Context c){
  JSONObject state=new JSONObject();
  try{
   state.put("title",title==null?JSONObject.NULL:title).put("artist",artist==null?"":artist).put("art",art==null?"":art).put("duration",duration)
    .put("playing",playing).put("signed_out",signedOut).put("position",current()).put("at",SystemClock.elapsedRealtime()).put("boot",boot());
  }catch(org.json.JSONException e){return;}
  shown=true;shownPlaying=playing;shownSignedOut=signedOut;shownPosition=current();shownAt=SystemClock.elapsedRealtime();
  String cover=art;boolean newCover=!eq(cover,shownArt);shownArt=cover;
  worker.execute(()->{
   if(newCover)saveCover(c,cover);
   write(new File(c.getFilesDir(),"hu_widget.json"),state.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
   render(c);
  });
 }
 private static void saveCover(Context c,String uri){
  File file=new File(c.getFilesDir(),"hu_widget_cover.png");
  if(uri==null || uri.isEmpty()){file.delete();return;}
  try{
   BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;
   try(InputStream in=c.getContentResolver().openInputStream(android.net.Uri.parse(uri))){BitmapFactory.decodeStream(in,null,o);}
   int sample=1;while(Math.min(o.outWidth,o.outHeight)/(sample*2)>=320)sample*=2;
   o=new BitmapFactory.Options();o.inSampleSize=sample;Bitmap b;
   try(InputStream in=c.getContentResolver().openInputStream(android.net.Uri.parse(uri))){b=BitmapFactory.decodeStream(in,null,o);}
   if(b==null){file.delete();return;}
   // A square, rounded as the widgets' cards (16 of 240 dp), so no launcher has to clip it.
   int side=Math.min(320,Math.min(b.getWidth(),b.getHeight()));Bitmap out=Bitmap.createBitmap(side,side,Bitmap.Config.ARGB_8888);
   Canvas canvas=new Canvas(out);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);float r=side*16f/240f;
   canvas.drawRoundRect(0,0,side,side,r,r,p);p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
   int crop=Math.min(b.getWidth(),b.getHeight());Rect src=new Rect((b.getWidth()-crop)/2,(b.getHeight()-crop)/2,(b.getWidth()+crop)/2,(b.getHeight()+crop)/2);
   canvas.drawBitmap(b,src,new Rect(0,0,side,side),p);
   ByteArrayOutputStream bytes=new ByteArrayOutputStream();out.compress(Bitmap.CompressFormat.PNG,100,bytes);write(file,bytes.toByteArray());
  }catch(IOException | RuntimeException e){Log.w("SpotifyHU","Widget cover unavailable",e);file.delete();}
 }
 private static void write(File f,byte[] data){
  AtomicFile a=new AtomicFile(f);FileOutputStream out=null;
  try{out=a.startWrite();out.write(data);a.finishWrite(out);}catch(IOException e){if(out!=null)a.failWrite(out);Log.w("SpotifyHU","Widget state not saved",e);}
 }

 /** Draws the kept state into every placed widget; any process of the app may call it. */
 static void render(Context c){
  AppWidgetManager manager=AppWidgetManager.getInstance(c);if(manager==null)return;
  int[] wide=manager.getAppWidgetIds(new ComponentName(c,HuWidgetWide.class)),square=manager.getAppWidgetIds(new ComponentName(c,HuWidgetSquare.class));
  if(wide.length==0 && square.length==0)return;
  JSONObject s=null;File state=new File(c.getFilesDir(),"hu_widget.json");
  try{byte[] data=new AtomicFile(state).readFully();s=new JSONObject(new String(data,java.nio.charset.StandardCharsets.UTF_8));}
  catch(IOException | org.json.JSONException ignored){}
  // Kept as playing, but the backend is gone or the unit has restarted since (the times count from
  // the boot): it stopped, when is unknown, so it stays paused at the kept position (the track's
  // start or the last seek) until Spotify runs again.
  boolean sameBoot=s!=null && Math.abs(boot()-s.optLong("boot"))<10000;
  if(s!=null && s.optBoolean("playing") && (!sameBoot || !backendRunning(c))){
   try{s.put("at",SystemClock.elapsedRealtime()).put("boot",boot()).put("playing",false);}catch(org.json.JSONException ignored){}
   write(state,s.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
  Bitmap cover=null;File f=new File(c.getFilesDir(),"hu_widget_cover.png");if(f.exists())cover=BitmapFactory.decodeFile(f.getPath());
  int[] ids=java.util.Arrays.copyOf(wide,wide.length+square.length);System.arraycopy(square,0,ids,wide.length,square.length);
  if(Build.VERSION.SDK_INT>=31){
   // One cover for every size: the sized views share their bitmaps.
   java.util.Map<SizeF,RemoteViews> all=new android.util.ArrayMap<>();
   for(int k=0;k<SIZES.length;k++)all.put(new SizeF(SIZES[k][0],SIZES[k][1]),views(c,k,s,cover));
   manager.updateAppWidget(ids,new RemoteViews(all));
  }else for(int id:ids)manager.updateAppWidget(id,views(c,layout(c,manager,id),s,cover));
 }
 /** Before Android 12: the layout for the size the launcher reports (in landscape its widest and lowest), else the provider's minimum. */
 private static int layout(Context c,AppWidgetManager manager,int id){
  android.os.Bundle o=manager.getAppWidgetOptions(id);boolean land=c.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
  float w=o.getInt(land?AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH:AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),h=o.getInt(land?AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT:AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
  if(w<=0 || h<=0){AppWidgetProviderInfo info=manager.getAppWidgetInfo(id);float d=c.getResources().getDisplayMetrics().density;w=info==null?0:info.minWidth/d;h=info==null?0:info.minHeight/d;}
  return fit(w,h);
 }
 /** The closest of SIZES that fits in w x h dp, as RemoteViews chooses on Android 12+; if none fits, the smallest. */
 static int fit(float w,float h){
  int pick=SMALL;float best=Float.MAX_VALUE;
  for(int k=0;k<SIZES.length;k++){float dw=w-SIZES[k][0],dh=h-SIZES[k][1];if(dw<0 || dh<0)continue;float d=dw*dw+dh*dh;if(d<best){best=d;pick=k;}}
  return pick;
 }
 /** When the unit started, by the wall clock (the base of SystemClock.elapsedRealtime). */
 private static long boot(){return System.currentTimeMillis()-SystemClock.elapsedRealtime();}
 private static boolean backendRunning(Context c){
  if(c.getPackageName().equals(android.app.Application.getProcessName()))return true;
  ActivityManager am=c.getSystemService(ActivityManager.class);java.util.List<ActivityManager.RunningAppProcessInfo> list=am==null?null:am.getRunningAppProcesses();
  if(list!=null)for(ActivityManager.RunningAppProcessInfo p:list)if(c.getPackageName().equals(p.processName))return true;
  return false;
 }
 static RemoteViews views(Context c,int kind,JSONObject s,Bitmap cover){
  RemoteViews v=new RemoteViews(c.getPackageName(),LAYOUTS[kind]);
  Intent open=new Intent().setClassName(c.getPackageName(),"local.bydui.com.vivid.spotify.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  v.setOnClickPendingIntent(HuR.id_hu_widget_root,PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
  String title=s==null || s.isNull("title")?null:s.optString("title");
  if(title==null || s.optBoolean("signed_out")){
   v.setViewVisibility(HuR.id_hu_widget_player,View.GONE);v.setViewVisibility(HuR.id_hu_widget_open,View.VISIBLE);
   icon(c,v,HuR.id_hu_widget_logo,"encore_icon_spotify_logo_24");v.setTextViewText(HuR.id_hu_widget_open_text,HuText.get("panel_open"));
   return v;
  }
  v.setViewVisibility(HuR.id_hu_widget_player,View.VISIBLE);v.setViewVisibility(HuR.id_hu_widget_open,View.GONE);
  v.setTextViewText(HuR.id_hu_widget_title,title);v.setTextViewText(HuR.id_hu_widget_artist,s.optString("artist"));
  if(cover!=null)v.setImageViewBitmap(HuR.id_hu_widget_cover,cover);
  boolean playing=s.optBoolean("playing");long duration=s.optLong("duration"),position=Math.max(0,s.optLong("position")+(playing?SystemClock.elapsedRealtime()-s.optLong("at"):0));
  // The narrow strip has no room for the times (its layout hides them).
  boolean times=kind!=STRIP;
  if(times && duration>0){
   position=Math.min(position,duration);long now=SystemClock.elapsedRealtime();
   v.setViewVisibility(HuR.id_hu_widget_times,View.VISIBLE);
   v.setChronometer(HuR.id_hu_widget_elapsed,now-position,null,playing);
   v.setChronometer(HuR.id_hu_widget_remaining,now+(duration-position),"-%s",playing);v.setChronometerCountDown(HuR.id_hu_widget_remaining,true);
   // Stopped, a chronometer shows its time when the launcher applies the views, maybe later: paused times are text.
   if(!playing){
    v.setTextViewText(HuR.id_hu_widget_elapsed,android.text.format.DateUtils.formatElapsedTime(position/1000));
    v.setTextViewText(HuR.id_hu_widget_remaining,"-"+android.text.format.DateUtils.formatElapsedTime((duration-position)/1000));
   }
  }else if(times)v.setViewVisibility(HuR.id_hu_widget_times,View.INVISIBLE);
  icon(c,v,HuR.id_hu_widget_previous,"encore_icon_skip_back_24");icon(c,v,HuR.id_hu_widget_next,"encore_icon_skip_forward_24");
  icon(c,v,HuR.id_hu_widget_play,playing?"encore_icon_pause_24":"encore_icon_play_24");
  v.setContentDescription(HuR.id_hu_widget_play,HuText.get(playing?"pause":"play"));
  v.setContentDescription(HuR.id_hu_widget_previous,HuText.get("previous"));v.setContentDescription(HuR.id_hu_widget_next,HuText.get("next"));
  v.setOnClickPendingIntent(HuR.id_hu_widget_previous,action(c,1,KeyEvent.KEYCODE_MEDIA_PREVIOUS));
  v.setOnClickPendingIntent(HuR.id_hu_widget_play,action(c,2,playing?KeyEvent.KEYCODE_MEDIA_PAUSE:KeyEvent.KEYCODE_MEDIA_PLAY));
  v.setOnClickPendingIntent(HuR.id_hu_widget_next,action(c,3,KeyEvent.KEYCODE_MEDIA_NEXT));
  return v;
 }
 /** A Spotify (Encore) icon; the layouts tint it with day / night colours, so the host's theme decides. */
 private static void icon(Context c,RemoteViews v,int view,String name){
  int id=c.getResources().getIdentifier(name,"drawable",c.getPackageName());if(id!=0)v.setImageViewResource(view,id);
 }
 private static PendingIntent action(Context c,int code,int key){
  return PendingIntent.getBroadcast(c,code,new Intent(HuWidgetActions.ACTION).setClass(c,HuWidgetActions.class).putExtra("key",key),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 }
 private static String text(Object m,String key){Object t=call(m,"getText",key);return t==null?null:t.toString();}
 private static Object call(Object o,String name,String... key){return HeadUnitRuntime.call(o,name,key);}
 private static boolean eq(Object a,Object b){return a==null?b==null:a.equals(b);}
}
