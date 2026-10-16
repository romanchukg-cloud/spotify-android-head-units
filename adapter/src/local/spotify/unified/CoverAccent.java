package local.spotify.unified;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.ImageView;
import java.util.WeakHashMap;
/**
 * New style, "Colour from cover art": tints the mini-player card and the player panel with the
 * most vivid colour of the current cover. The interface has no Palette library, so the colour
 * is taken from a 24x24 sample of the cover bitmap the interface has already loaded.
 */
final class CoverAccent {
 private static final String UI="com.spotify.music.ui";private static final long PERIOD=700;
 private static final WeakHashMap<Activity,CoverAccent> running=new WeakHashMap<>();
 private final Activity activity;private final Handler main=new Handler(Looper.getMainLooper());private final boolean dark;
 private Drawable lastCover;private Drawable barOriginal,pageOriginal,barTint,pageTint;private View bar,page;private Integer lastColor;
 private final Runnable tick=this::tick;
 private CoverAccent(Activity a){activity=a;dark=(a.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;}
 static void start(Activity a,boolean enabled){
  CoverAccent c=running.get(a);
  if(!enabled || !HeadUnitStyle.modern()){if(c!=null){c.stopTicking();c.restore();running.remove(a);}return;}
  if(c==null){c=new CoverAccent(a);running.put(a,c);}
  c.stopTicking();c.main.post(c.tick);
 }
 static void stop(Activity a){CoverAccent c=running.get(a);if(c!=null)c.stopTicking();}
 private void stopTicking(){main.removeCallbacks(tick);}
 private View find(String name){int id=activity.getResources().getIdentifier(name,"id",UI);return id==0?null:activity.findViewById(id);}
 private void tick(){
  try{
   View cover=find("now_playing_bar_cover");
   if(cover!=null){
    bar=(View)cover.getParent();page=find("now_playing_page_bg");
    int id=activity.getResources().getIdentifier("iv_cover","id",UI);
    View art=id==0?null:cover.findViewById(id);
    Drawable d=art instanceof ImageView?((ImageView)art).getDrawable():null;
    if(d!=null && d!=lastCover){Integer color=vivid(d);if(color!=null){lastCover=d;lastColor=color;barTint=null;pageTint=null;}}
    // Views can appear or be rebound after the colour was taken (the player panel, a new process).
    if(lastColor!=null)apply(lastColor);
    if(d!=null)queueCover(d);
   }
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Cover colour unavailable",e);}
  main.postDelayed(tick,PERIOD);
 }
 /** The average colour of the strongest of 12 hue buckets, weighted by saturation and brightness. */
 static Integer vivid(Drawable d){
  int w=d.getIntrinsicWidth(),h=d.getIntrinsicHeight();if(w<=0 || h<=0)return null;
  Bitmap sample=Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sample);Rect old=new Rect(d.getBounds());d.setBounds(0,0,24,24);d.draw(c);d.setBounds(old);
  float[] weight=new float[12],r=new float[12],g=new float[12],b=new float[12];float[] hsv=new float[3];
  for(int y=0;y<24;y++)for(int x=0;x<24;x++){int p=sample.getPixel(x,y);if(Color.alpha(p)<128)continue;Color.colorToHSV(p,hsv);if(hsv[1]<0.2f || hsv[2]<0.2f)continue;
   int k=Math.min(11,(int)(hsv[0]/30f));float wt=hsv[1]*hsv[2];weight[k]+=wt;r[k]+=Color.red(p)*wt;g[k]+=Color.green(p)*wt;b[k]+=Color.blue(p)*wt;}
  sample.recycle();int best=-1;for(int k=0;k<12;k++)if(weight[k]>0 && (best<0 || weight[k]>weight[best]))best=k;
  if(best<0 || weight[best]<2f)return null;
  return Color.rgb(Math.round(r[best]/weight[best]),Math.round(g[best]/weight[best]),Math.round(b[best]/weight[best]));
 }
 /** Darkened (night) or lightened (day) until text keeps at least 7:1 contrast. */
 static int tone(int color,boolean dark,float strength){
  float[] hsv=new float[3];Color.colorToHSV(color,hsv);
  if(dark){hsv[1]=Math.min(hsv[1],0.75f);hsv[2]=strength;while(hsv[2]>0.05f && luminance(Color.HSVToColor(hsv))>0.1f)hsv[2]-=0.02f;}
  else{hsv[1]=Math.min(hsv[1],0.35f)*strength;hsv[2]=0.97f;while(hsv[1]>0f && luminance(Color.HSVToColor(hsv))<0.45f)hsv[1]-=0.02f;}
  return Color.HSVToColor(hsv);
 }
 static float luminance(int c){return 0.2126f*lin(Color.red(c))+0.7152f*lin(Color.green(c))+0.0722f*lin(Color.blue(c));}
 private static float lin(int v){float s=v/255f;return s<=0.03928f?s/12.92f:(float)Math.pow((s+0.055)/1.055,2.4);}
 private void apply(int color){
  float density=activity.getResources().getDisplayMetrics().density;
  if(bar!=null && (barTint==null || bar.getBackground()!=barTint)){
   if(barOriginal==null)barOriginal=bar.getBackground();
   if(barTint==null){GradientDrawable d=new GradientDrawable();float t=18*density,b=22*density;d.setCornerRadii(new float[]{t,t,t,t,b,b,b,b});d.setColor(tone(color,dark,0.42f));barTint=d;}
   bar.setBackground(barTint);
  }
  if(page!=null && (pageTint==null || page.getBackground()!=pageTint)){
   if(pageOriginal==null)pageOriginal=page.getBackground();int base=dark?0xff181818:0xffffffff;
   if(pageTint==null){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{tone(color,dark,dark?0.45f:1f),base,base});d.setCornerRadius(22*density);pageTint=d;}
   page.setBackground(pageTint);
  }
 }
 /**
  * The queue's "Now playing" row gets no artwork from Spotify Automotive, so its cover stays
  * empty; show the mini-player's cover there, rounded like the other rows.
  */
 private void queueCover(Drawable cover){
  View list=find("queue");if(!(list instanceof ViewGroup) || !list.isShown())return;
  int coverId=activity.getResources().getIdentifier("img_cover","id",UI);if(coverId==0)return;
  ViewGroup rows=(ViewGroup)list;
  for(int i=0;i<rows.getChildCount();i++){
   View row=rows.getChildAt(i);if(!row.isSelected())continue;
   View v=row.findViewById(coverId);if(!(v instanceof ImageView))continue;ImageView image=(ImageView)v;
   Drawable shown=image.getDrawable();
   if(shown!=null && image.getTag(TAG)==null)continue;   // real artwork
   if(shown!=null && image.getTag(TAG)==cover)continue;  // already showing the current cover
   Drawable copy=cover.getConstantState()!=null?cover.getConstantState().newDrawable(activity.getResources()).mutate():cover;
   image.setImageDrawable(copy);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setVisibility(View.VISIBLE);image.setTag(TAG,cover);
   final float radius=8*activity.getResources().getDisplayMetrics().density;
   image.setOutlineProvider(new ViewOutlineProvider(){public void getOutline(View view,Outline outline){outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),radius);}});
   image.setClipToOutline(true);
  }
 }
 private static final int TAG=0x7f5a0001;
 private void restore(){if(bar!=null && barOriginal!=null)bar.setBackground(barOriginal);if(page!=null && pageOriginal!=null)page.setBackground(pageOriginal);lastCover=null;lastColor=null;barTint=null;pageTint=null;}
}
