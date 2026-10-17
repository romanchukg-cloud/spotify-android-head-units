package local.spotify.unified;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
/** Buttons shared by our mini-player and player: Encore icons from the backend's resources. */
final class HuControls {
 private HuControls(){}
 static Drawable icon(Context c,String name,int tint){
  int id=c.getResources().getIdentifier(name,"drawable",c.getPackageName());if(id==0)return null;
  Drawable d=c.getDrawable(id).mutate();d.setTint(tint);return d;
 }
 /** A round, borderless icon button of the given touch size. */
 static ImageView button(HuUi ui,String icon,String description,int size,View.OnClickListener click){
  ImageView b=new ImageView(ui.activity);b.setScaleType(ImageView.ScaleType.FIT_CENTER);
  int pad=size*3/10;b.setPadding(pad,pad,pad,pad);b.setImageDrawable(icon(ui.activity,icon,ui.text));
  b.setBackground(ui.pressable(0x00000000,size/2f));b.setContentDescription(description);b.setOnClickListener(click);
  b.setLayoutParams(new LinearLayout.LayoutParams(size,size));return b;
 }
 /** Play/pause: a filled circle in the text colour with the icon in the background colour. */
 static ImageView play(HuUi ui,int size,View.OnClickListener click){
  ImageView b=new ImageView(ui.activity);b.setScaleType(ImageView.ScaleType.FIT_CENTER);int pad=size*28/100;b.setPadding(pad,pad,pad,pad);
  b.setBackground(ui.pressable(ui.text,size/2f));b.setOnClickListener(click);b.setLayoutParams(new LinearLayout.LayoutParams(size,size));return b;
 }
 static void playing(HuUi ui,ImageView b,boolean playing){
  b.setImageDrawable(icon(ui.activity,playing?"encore_icon_pause_24":"encore_icon_play_24",ui.bg));
  b.setContentDescription(HuText.get(playing?"pause":"play"));
 }
 /** A session custom action's own icon (Spotify's like / shuffle states); hidden when absent. */
 static void custom(HuUi ui,ImageView b,PlaybackStateCompat.CustomAction a,boolean active){
  if(a==null){b.setVisibility(View.INVISIBLE);return;}
  b.setVisibility(View.VISIBLE);Drawable d=null;
  try{if(a.getIcon()!=0){d=ui.activity.getDrawable(a.getIcon()).mutate();d.setTint(active?ui.accent:ui.text);}}catch(RuntimeException ignored){}
  b.setImageDrawable(d);b.setContentDescription(a.getName());
 }
 static TextView title(HuUi ui,float sp,int lines){
  TextView t=ui.label("",sp,ui.text,true);t.setMaxLines(lines);t.setEllipsize(android.text.TextUtils.TruncateAt.END);t.setGravity(Gravity.START);return t;
 }
 static String time(long ms){long s=Math.max(0,ms/1000);return s/60+":"+(s%60<10?"0":"")+s%60;}
}
