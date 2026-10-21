package local.spotify.unified;
import android.app.Activity;
import android.appwidget.*;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.*;
/**
 * Debug builds only: hosts the home-screen widgets (HuWidgetWide and HuWidgetSquare) at sizes
 * that pick each of their layouts, as a launcher would, after `adb shell appwidget grantbind
 * --package com.spotify.music --user 0`. Refresh asks the providers to draw again; Sample writes
 * a made-up playing track (state file and cover, as HuWidgets keeps them) and draws it, for
 * checks without playback; Preview a paused one for the preview pictures.
 */
public final class WidgetTestActivity extends Activity {
 private static final int HOST=0x48550001;
 private AppWidgetHost host;private final java.util.List<Integer> ids=new java.util.ArrayList<>();private TextView status;private LinearLayout widgets;
 protected void onCreate(Bundle state){
  super.onCreate(state);
  LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(16),dp(8),dp(16),dp(8));page.setBackgroundColor(0xff3a3a3a);
  LinearLayout bar=new LinearLayout(this);page.addView(bar);
  status=new TextView(this);status.setTextColor(0xffffffff);status.setTextSize(14);
  Button refresh=new Button(this);refresh.setText("Refresh");refresh.setOnClickListener(v->{HuWidgets.render(getApplicationContext());say("Rendered");});bar.addView(refresh);
  Button sample=new Button(this);sample.setText("Sample");sample.setOnClickListener(v->{sample("A rather long song title that does not fit","Sample Artist",true);say("Sample");});bar.addView(sample);
  // The picture behind the launchers' previews before Android 12 (res-hu/drawable-nodpi/hu_widget_preview_*).
  Button preview=new Button(this);preview.setText("Preview");preview.setOnClickListener(v->{sample("Song","Artist",false);say("Preview");});bar.addView(preview);
  // HuBroadcasts with made-up session objects; a receiver here logs what other apps would get.
  Button broadcast=new Button(this);broadcast.setText("Broadcast");broadcast.setOnClickListener(v->{broadcasts();say("Broadcast");});bar.addView(broadcast);bar.addView(status);
  widgets=new LinearLayout(this);widgets.setOrientation(LinearLayout.VERTICAL);page.addView(widgets);
  setContentView(page);
  host=new AppWidgetHost(this,HOST);host.deleteHost();host=new AppWidgetHost(this,HOST);
  AppWidgetManager manager=AppWidgetManager.getInstance(this);
  // Rows of {provider, width dp, height dp}: strip, strip with times, small; large, square.
  Object[][][] rows={{{HuWidgetWide.class,360,110},{HuWidgetWide.class,480,120},{HuWidgetSquare.class,200,180}},{{HuWidgetWide.class,688,284},{HuWidgetSquare.class,268,268}}};
  for(Object[][] row:rows){
   LinearLayout line=new LinearLayout(this);line.setGravity(android.view.Gravity.TOP);widgets.addView(line);
   for(Object[] w:row){
    int id=host.allocateAppWidgetId(),width=(Integer)w[1],height=(Integer)w[2];
    if(!manager.bindAppWidgetIdIfAllowed(id,new ComponentName(this,(Class<?>)w[0]))){host.deleteAppWidgetId(id);say("Bind not allowed: adb shell appwidget grantbind --package com.spotify.music --user 0");return;}
    ids.add(id);AppWidgetHostView view=host.createView(getApplicationContext(),id,manager.getAppWidgetInfo(id));
    view.updateAppWidgetSize(null,width,height,width,height);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(width),dp(height));p.setMarginEnd(dp(24));p.topMargin=dp(16);line.addView(view,p);
   }
  }
  say("Bound "+ids.size());
 }
 protected void onStart(){super.onStart();host.startListening();}
 protected void onStop(){host.stopListening();super.onStop();}
 protected void onDestroy(){for(int id:ids)host.deleteAppWidgetId(id);try{unregisterReceiver(heard);}catch(IllegalArgumentException ignored){}super.onDestroy();}
 private void sample(String title,String artist,boolean playing){
  java.io.File dir=getFilesDir();int side=320;
  android.graphics.Bitmap b=android.graphics.Bitmap.createBitmap(side,side,android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas c=new android.graphics.Canvas(b);
  android.graphics.Paint p=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);p.setShader(new android.graphics.LinearGradient(0,0,side,side,0xffe91e63,0xff3f51b5,android.graphics.Shader.TileMode.CLAMP));
  float r=side*16f/240f;c.drawRoundRect(0,0,side,side,r,r,p);
  try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,"hu_widget_cover.png"))){b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
   org.json.JSONObject s=new org.json.JSONObject().put("title",title).put("artist",artist).put("art","sample").put("duration",215000)
    .put("playing",playing).put("signed_out",false).put("position",62000).put("at",android.os.SystemClock.elapsedRealtime()).put("boot",System.currentTimeMillis()-android.os.SystemClock.elapsedRealtime());
   try(java.io.FileOutputStream json=new java.io.FileOutputStream(new java.io.File(dir,"hu_widget.json"))){json.write(s.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));}
  }catch(java.io.IOException | org.json.JSONException e){say("Sample failed: "+e);return;}
  HuWidgets.render(getApplicationContext());
 }
 private final android.content.BroadcastReceiver heard=new android.content.BroadcastReceiver(){public void onReceive(android.content.Context c,android.content.Intent i){
  StringBuilder b=new StringBuilder(i.getAction());Bundle e=i.getExtras();if(e!=null)for(String k:new java.util.TreeSet<>(e.keySet()))if(!k.equals("timeSent"))b.append(' ').append(k).append('=').append(e.get(k));
  android.util.Log.i("SpotifyHU","Widget test heard "+b+(e!=null && e.containsKey("timeSent")?" timeSent":""));
 }};
 private void broadcasts(){
  android.content.IntentFilter f=new android.content.IntentFilter();for(String a:new String[]{HuBroadcasts.METADATA,HuBroadcasts.PLAYBACK,HuBroadcasts.QUEUE})f.addAction(a);
  try{unregisterReceiver(heard);}catch(IllegalArgumentException ignored){}
  if(android.os.Build.VERSION.SDK_INT>=33)registerReceiver(heard,f,RECEIVER_EXPORTED);else registerReceiver(heard,f);
  android.content.Context c=getApplicationContext();HeadUnitPreferences.init(c);
  // Expected: the track; playing; nothing for a normal second; paused; a seek; the queue.
  HuBroadcasts.metadata(c,new Metadata());HuBroadcasts.playback(c,new State(3,62000));HuBroadcasts.playback(c,new State(3,63000));
  HuBroadcasts.playback(c,new State(2,63000));HuBroadcasts.playback(c,new State(2,120000));HuBroadcasts.queue(c);
 }
 /** Session objects as HuBroadcasts reads them: by method name. */
 public static final class Metadata{
  public CharSequence getText(String k){switch(k){case "android.media.metadata.TITLE":return "Song";case "android.media.metadata.ARTIST":return "Artist";case "android.media.metadata.ALBUM":return "Album";case "android.media.metadata.MEDIA_URI":return "spotify:track:4uLU6hMCjMI75M1A2tKUQC";default:return null;}}
  public long getLong(String k){return "android.media.metadata.DURATION".equals(k)?215000:0;}
 }
 public static final class State{
  private final int state;private final long position;State(int state,long position){this.state=state;this.position=position;}
  public int getState(){return state;} public long getPosition(){return position;} public long getLastPositionUpdateTime(){return android.os.SystemClock.elapsedRealtime();} public Bundle getExtras(){return null;}
 }
 private void say(String s){status.setText(s);android.util.Log.i("SpotifyHU","Widget test: "+s);}
 private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
