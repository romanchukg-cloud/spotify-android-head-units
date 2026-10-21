package local.spotify.unified;
import android.app.Dialog;
import android.content.Context;
import android.database.ContentObserver;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;
/**
 * Spotify Connect in our interface: which device plays and the menu that moves playback to
 * another one (HeadUnitConnect does it in the music process). The state comes through the
 * private settings provider, again whenever it notifies a change; views follow it as listeners
 * (the player's device button and line, the mini-player's line).
 */
final class HuConnect {
 interface Listener {void devices(Bundle state);}
 private static final ExecutorService worker=Executors.newSingleThreadExecutor();
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static final List<Listener> listeners=new ArrayList<>();
 private static Bundle state=new Bundle();private static ContentObserver observer;private static Context app;
 private HuConnect(){}

 static void add(Context c,Listener l){
  app=c.getApplicationContext();listeners.add(l);l.devices(state);
  if(observer==null){observer=new ContentObserver(main){public void onChange(boolean self){refresh();}};app.getContentResolver().registerContentObserver(HeadUnitConnect.CHANGED,false,observer);}
  refresh();
 }
 static void remove(Listener l){
  listeners.remove(l);
  if(listeners.isEmpty() && observer!=null){app.getContentResolver().unregisterContentObserver(observer);observer=null;}
 }
 static void refresh(){
  Context c=app;if(c==null)return;
  worker.execute(()->{
   Bundle b;try{b=HeadUnitSettings.call(c,"connect_get",null);}catch(RuntimeException e){return;}
   main.post(()->{state=b;for(Listener l:new ArrayList<>(listeners))l.devices(b);});
  });
 }

 static List<Bundle> devices(Bundle s){ArrayList<Bundle> d=s==null?null:s.getParcelableArrayList("devices");return d==null?new ArrayList<>():d;}
 /** The device that plays, or null. */
 static Bundle active(Bundle s){for(Bundle d:devices(s))if(d.getBoolean("active"))return d;return null;}
 /** Another device plays (not this car). */
 static boolean elsewhere(Bundle s){Bundle a=active(s);return a!=null && !a.getBoolean("self");}
 static String name(Bundle d){
  String n=d.getString("name");if(d.getBoolean("self"))return HuText.get("this_car");
  return n==null || n.isEmpty()?HuText.get("devices"):n;
 }
 /** Spotify's icon for the device's type (its Connect type, or its output's category). */
 static String icon(Bundle d){
  if(d.getBoolean("self"))return "encore_icon_device_car_24";
  if(d.getBoolean("group"))return "encore_icon_device_multispeaker_24";
  String t=String.valueOf(d.getString("type")).toUpperCase(Locale.ROOT);
  if(t.contains("SMARTPHONE") || t.contains("PHONE"))return "encore_icon_device_mobile_24";
  if(t.contains("TABLET"))return "encore_icon_device_tablet_24";
  if(t.contains("COMPUTER"))return "encore_icon_device_computer_24";
  if(t.contains("TV") || t.contains("STB") || t.contains("GAME") || t.contains("CAST_VIDEO"))return "encore_icon_device_tv_24";
  if(t.contains("AUTOMOBILE"))return "encore_icon_device_car_24";
  if(t.contains("SMARTWATCH") || t.contains("WATCH"))return "encore_icon_watch_24";
  if(t.contains("CAST"))return "encore_icon_chromecast_disconnected_24";
  if(t.contains("SPEAKER") || t.contains("AVR") || t.contains("DONGLE") || t.contains("HOME"))return "encore_icon_device_speaker_24";
  int c=d.getInt("category");return c==1?"encore_icon_device_car_24":c==2?"encore_icon_headphones_24":c==3?"encore_icon_device_speaker_24":"encore_icon_device_other_24";
 }

 /** The device menu: this car first, then the others that are online; the playing one in green. */
 static void menu(HuUi ui){
  android.app.Activity a=ui.activity;Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
  LinearLayout card=new LinearLayout(a);card.setOrientation(LinearLayout.VERTICAL);card.setBackground(HuUi.round(ui.card,ui.dp(22)));card.setPadding(ui.dp(24),ui.dp(24),ui.dp(24),ui.dp(16));
  card.addView(ui.label(HuText.get("devices"),26,ui.text,true));
  LinearLayout rows=new LinearLayout(a);rows.setOrientation(LinearLayout.VERTICAL);rows.setPadding(0,ui.dp(12),0,0);card.addView(rows);
  TextView hint=ui.label(HuText.get("devices_hint"),15,ui.secondary,false);hint.setPadding(0,ui.dp(12),0,0);card.addView(hint);
  Listener fill=s->{
   rows.removeAllViews();List<Bundle> list=new ArrayList<>();
   for(Bundle d:devices(s))if(d.getBoolean("self"))list.add(0,d);else if(!d.getBoolean("offline") || d.getBoolean("active"))list.add(d);
   if(list.isEmpty()){rows.addView(ui.label(HuText.get("loading"),18,ui.secondary,false));return;}
   for(Bundle d:list)rows.addView(row(ui,d,()->{transfer(a,d);dialog.dismiss();}));
  };
  add(a,fill);dialog.setOnDismissListener(x->remove(fill));
  ScrollView scroll=new ScrollView(a);scroll.addView(card);dialog.setContentView(scroll);
  Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));w.setLayout(Math.min(ui.dp(560),a.getResources().getDisplayMetrics().widthPixels-ui.dp(32)),ViewGroup.LayoutParams.WRAP_CONTENT);}
  dialog.show();
 }
 private static View row(HuUi ui,Bundle d,Runnable pick){
  boolean on=d.getBoolean("active"),busy=d.getBoolean("activating"),off=d.getBoolean("disabled");int color=on?ui.accent:off?ui.secondary:ui.text;
  LinearLayout r=new LinearLayout(ui.activity);r.setGravity(Gravity.CENTER_VERTICAL);r.setMinimumHeight(ui.dp(76));r.setPadding(ui.dp(12),ui.dp(8),ui.dp(12),ui.dp(8));
  r.setBackground(ui.pressable(ui.card,ui.dp(14)));
  ImageView i=new ImageView(ui.activity);i.setImageDrawable(HuControls.icon(ui.activity,icon(d),color));r.addView(i,new LinearLayout.LayoutParams(ui.dp(36),ui.dp(36)));
  LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(LinearLayout.VERTICAL);texts.setPaddingRelative(ui.dp(18),0,0,0);
  TextView n=ui.label(name(d),20,color,on);n.setSingleLine(true);n.setEllipsize(android.text.TextUtils.TruncateAt.END);texts.addView(n);
  String sub=on?HuText.get("playing_here"):busy?HuText.get("loading"):null;
  if(sub!=null){TextView s=ui.label(sub,15,on?ui.accent:ui.secondary,false);texts.addView(s);}
  r.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
  if(!on && !off)r.setOnClickListener(v->pick.run());
  return r;
 }
 static void transfer(Context c,Bundle d){
  Bundle args=new Bundle();args.putBoolean("self",d.getBoolean("self"));args.putString("id",d.getString("id"));args.putString("local_id",d.getString("local_id"));args.putString("type",d.getString("type"));
  worker.execute(()->{try{HeadUnitSettings.call(c,"connect_transfer",args);}catch(RuntimeException e){android.util.Log.w("SpotifyHU","Connect transfer unavailable",e);}});
 }
}
