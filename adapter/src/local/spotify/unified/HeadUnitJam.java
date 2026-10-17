package local.spotify.unified;
import android.app.Activity;
import android.content.*;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;
/**
 * Jam: passengers scan a QR code to listen along and add to the queue. Spotify's own settings page
 * (SettingsPageActivity) runs the whole flow (Start a Jam, QR code, End Jam). The interface opens it
 * with an extra that scrolls the page to the Jam section, so the QR code fits on the head unit's
 * screen (patch section "# Jam" calls opened() from the page's onCreate).
 */
public final class HeadUnitJam {
 private static final String PAGE="com.spotify.automotive.settingspage.SettingsPageActivity",SECTION="local.spotify.unified.SECTION";
 private HeadUnitJam(){}
 static void open(Context c){
  try{c.startActivity(new Intent().setClassName(c.getPackageName(),PAGE).putExtra(SECTION,"jam").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
  catch(RuntimeException e){android.util.Log.w("SpotifyHU","Jam page unavailable",e);}
 }
 /**
  * Spotify's settings page, after onCreate: when the interface asked for Jam, keeps the Jam
  * section at the top while the page loads (profile, QR code after Start a Jam) until the user
  * scrolls the page.
  */
 public static void opened(Activity page){
  try{
   if(page==null || page.getIntent()==null || !"jam".equals(page.getIntent().getStringExtra(SECTION)))return;
   int id=page.getResources().getIdentifier("jam_title","id",page.getPackageName());
   View title=id==0?null:page.findViewById(id);if(title==null)return;
   ScrollView scroll=null;for(ViewParent p=title.getParent();p instanceof View;p=p.getParent())if(p instanceof ScrollView){scroll=(ScrollView)p;break;}
   if(scroll==null)return;final ScrollView s=scroll;final boolean[] touched={false};
   s.setOnTouchListener((v,e)->{touched[0]=true;return false;});
   s.getViewTreeObserver().addOnGlobalLayoutListener(()->{if(!touched[0])s.scrollTo(0,top(title,s));});
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Jam section unavailable",e);}
 }
 /** The title's offset inside the scroll view's content, with a little room above it. */
 private static int top(View title,ScrollView scroll){
  int top=0;for(View v=title;v!=null && v!=scroll;v=v.getParent() instanceof View?(View)v.getParent():null)top+=v.getTop();
  return Math.max(0,top-title.getHeight()/2);
 }
}
