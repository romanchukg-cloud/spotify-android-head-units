package local.spotify.unified;
import android.graphics.*;
import android.graphics.drawable.*;
/**
 * "Colour from cover art": the most vivid colour of a cover and its tone for backgrounds. There is
 * no Palette library, so the colour is taken from a 24x24 sample of the cover.
 */
final class CoverAccent {
 private CoverAccent(){}
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
}
