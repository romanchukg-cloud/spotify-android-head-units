package local.spotify.unified;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * The navigation of our interface: Home, Recents, Browse, Library, Search and settings. A rail at
 * the side on landscape screens; a bar along the bottom on portrait ones, within reach.
 */
final class HuRail extends LinearLayout {
 interface Pick {void page(String key);}
 static final String[] PAGES={"home","recents","browse","library","search"};
 private static final String[] ICONS={"encore_icon_home_24","encore_icon_recently_played_24","encore_icon_browse_24","encore_icon_collection_24","encore_icon_search_24"};
 private static final String[] ACTIVE={"encore_icon_home_active_24","encore_icon_recently_played_24","encore_icon_browse_active_24","encore_icon_collection_active_24","encore_icon_search_active_24"};
 private final HuUi ui;private final Map<String,LinearLayout> items=new HashMap<>();private final Map<String,ImageView> icons=new HashMap<>();private final Map<String,TextView> labels=new HashMap<>();
 HuRail(HuUi ui,boolean bar,Pick pick,Runnable settings){
  super(ui.activity);this.ui=ui;setOrientation(bar?HORIZONTAL:VERTICAL);setGravity(bar?Gravity.CENTER_VERTICAL:Gravity.CENTER_HORIZONTAL);
  int pad=ui.dp(bar?6:10);setPadding(pad,pad,pad,pad);
  setBackground(HuUi.round(ui.dark?0xff121212:0xffffffff,ui.dp(22)));setClipToOutline(true);setOutlineProvider(ViewOutlineProvider.BACKGROUND);
  for(int i=0;i<=PAGES.length;i++){
   // The pages, then settings: an item like the others, with its label, that opens a screen.
   String key=i<PAGES.length?PAGES[i]:"settings_short";
   LinearLayout item=new LinearLayout(ui.activity);item.setOrientation(VERTICAL);item.setGravity(Gravity.CENTER);item.setPadding(0,ui.dp(bar?6:10),0,ui.dp(bar?6:10));
   ImageView icon=new ImageView(ui.activity);item.addView(icon,new LayoutParams(ui.dp(32),ui.dp(32)));
   TextView label=ui.label(HuText.get(key),15,ui.secondary,false);label.setGravity(Gravity.CENTER);label.setPadding(0,ui.dp(6),0,0);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);item.addView(label);
   item.setContentDescription(HuText.get(i<PAGES.length?key:"settings"));
   addView(item,bar?new LayoutParams(0,-1,1):new LayoutParams(-1,0,1));
   if(i==PAGES.length){item.setBackground(ui.pressable(0x00000000,ui.dp(18)));icon.setImageDrawable(HuControls.icon(ui.activity,"encore_icon_gears_24",ui.secondary));item.setOnClickListener(v->settings.run());break;}
   item.setOnClickListener(v->pick.page(key));items.put(key,item);icons.put(key,icon);labels.put(key,label);
  }
 }
 void select(String key){
  for(int i=0;i<PAGES.length;i++){
   String k=PAGES[i];boolean on=k.equals(key);
   items.get(k).setBackground(on?HuUi.round(ui.dark?0xff2a2a2a:0xffebebeb,ui.dp(18)):null);
   icons.get(k).setImageDrawable(HuControls.icon(ui.activity,on?ACTIVE[i]:ICONS[i],on?ui.text:ui.secondary));
   labels.get(k).setTextColor(on?ui.text:ui.secondary);labels.get(k).setTypeface(ui.font(on));
  }
 }
}
