package local.spotify.unified;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
/** The edit button on the interface's queue page (patch_resources.py adds it hidden); it opens HeadUnitQueueActivity. */
public final class HeadUnitQueue {
 private static final String UI="com.spotify.music.ui";
 private HeadUnitQueue(){}
 /** QueueFragment, once its view exists. */
 public static void screen(View root){
  try{
   if(root==null)return;Context c=root.getContext();
   int buttonId=c.getResources().getIdentifier("hu_queue_edit","id",UI),iconId=c.getResources().getIdentifier("hu_queue_edit_icon","id",UI);
   View button=buttonId==0?null:root.findViewById(buttonId);View icon=iconId==0?null:root.findViewById(iconId);
   int res=c.getResources().getIdentifier("encore_icon_edit_24","drawable",c.getPackageName());
   if(button==null || !(icon instanceof ImageView) || res==0)return;
   boolean dark=(c.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
   Drawable d=c.getDrawable(res).mutate();d.setTint(dark?0xffffffff:0xff121212);((ImageView)icon).setImageDrawable(d);
   button.setContentDescription(HuText.get("edit_queue"));
   button.setOnClickListener(v->{try{v.getContext().startActivity(new Intent(v.getContext(),HeadUnitQueueActivity.class));}catch(RuntimeException e){android.util.Log.w("SpotifyHU","Queue editor unavailable",e);}});
   button.setVisibility(View.VISIBLE);
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Queue edit button unavailable",e);}
 }
}
