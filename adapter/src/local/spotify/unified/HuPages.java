package local.spotify.unified;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * The content pages of our interface. Home shows the backend's groups ("Good evening", "Made
 * for …") as rows of cards; Recents and Browse are grids; Library has its sections (Playlists,
 * Podcasts, Albums, Artists, Downloads) as filter pills above a grid. A card opens its page
 * (HuDetail, or a grid with a back button for categories); a playable-only card plays.
 */
final class HuPages {
 private HuPages(){}
 interface Open {void item(MediaItem item);}

 static TextView heading(HuUi ui,String text){TextView t=ui.label(text,28,ui.text,true);t.setPadding(ui.dp(8),ui.dp(18),ui.dp(8),ui.dp(6));return t;}
 static TextView message(HuUi ui,String text){TextView t=ui.label(text,20,ui.secondary,false);t.setGravity(Gravity.CENTER);t.setPadding(ui.dp(24),ui.dp(48),ui.dp(24),ui.dp(48));return t;}
 static int card(HuUi ui){return ui.dp(HuUi.wide(ui.activity)?168:136);}
 /**
  * A horizontal row that starts at its start edge. HorizontalScrollView only does that for
  * right-to-left layouts on its first layout, and our rows are often filled later.
  */
 static HorizontalScrollView row(HuUi ui){
  HorizontalScrollView s=new HorizontalScrollView(ui.activity);s.setHorizontalScrollBarEnabled(false);
  s.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){
   private int width=-1;
   public void onLayoutChange(View v,int l,int t,int r,int b,int ol,int ot,int or,int ob){
    View child=s.getChildCount()>0?s.getChildAt(0):null;int w=child==null?0:child.getWidth();
    if(w==width)return;width=w;
    if(s.getLayoutDirection()==View.LAYOUT_DIRECTION_RTL)s.post(()->s.scrollTo(w,0));
   }
  });
  return s;
 }

 /** Home: one heading and one horizontal row per group. */
 static View home(HuUi ui,String id,Open open){
  ScrollView scroll=new ScrollView(ui.activity);scroll.setFillViewport(true);
  LinearLayout list=new LinearLayout(ui.activity);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(ui.dp(16),ui.dp(4),ui.dp(16),ui.dp(16));scroll.addView(list);
  list.addView(message(ui,HuText.get("loading")));
  HuSession.get(ui.activity).children(id,items->{
   list.removeAllViews();
   if(items.isEmpty()){list.addView(message(ui,HuText.get("empty")));return;}
   for(Map.Entry<String,List<MediaItem>> g:HuBrowse.groups(items).entrySet()){
    if(!g.getKey().isEmpty())list.addView(heading(ui,g.getKey()));
    HorizontalScrollView rowScroll=row(ui);
    LinearLayout row=new LinearLayout(ui.activity);rowScroll.addView(row);
    for(MediaItem item:g.getValue()){HuBrowse.Card c=new HuBrowse.Card(ui,card(ui));c.bind(item,v->open.item(item));row.addView(c);}
    list.addView(rowScroll);
   }
  });
  return scroll;
 }

 /** A grid of a node's children under a heading; with back, a back button before the heading. */
 static View grid(HuUi ui,String title,String id,Open open,Runnable back){
  LinearLayout page=new LinearLayout(ui.activity);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(ui.dp(16),ui.dp(4),ui.dp(16),0);
  if(back!=null){
   LinearLayout top=new LinearLayout(ui.activity);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(0,ui.dp(8),0,0);
   ImageView b=HuControls.button(ui,"encore_icon_chevron_left_24",HuText.get("back"),ui.dp(64),v->back.run());if(b.getDrawable()!=null)b.getDrawable().setAutoMirrored(true);top.addView(b);
   TextView h=heading(ui,title==null?"":title);h.setPadding(ui.dp(8),0,ui.dp(8),0);top.addView(h,new LinearLayout.LayoutParams(0,-2,1));page.addView(top);
  }else if(title!=null)page.addView(heading(ui,title));
  FrameLayout body=new FrameLayout(ui.activity);page.addView(body,new LinearLayout.LayoutParams(-1,0,1));
  fill(ui,body,id,open);
  return page;
 }
 private static void fill(HuUi ui,FrameLayout body,String id,Open open){
  body.removeAllViews();body.addView(message(ui,HuText.get("loading")));
  HuSession.get(ui.activity).children(id,items->{
   body.removeAllViews();
   if(items.isEmpty()){body.addView(message(ui,HuText.get("empty")));return;}
   GridView grid=new GridView(ui.activity);int size=card(ui);
   grid.setColumnWidth(size+ui.dp(16));grid.setNumColumns(GridView.AUTO_FIT);grid.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
   grid.setVerticalSpacing(ui.dp(4));grid.setSelector(new android.graphics.drawable.ColorDrawable(0));grid.setClipToPadding(false);grid.setPadding(0,0,0,ui.dp(16));
   grid.setAdapter(new BaseAdapter(){
    public int getCount(){return items.size();}public Object getItem(int p){return items.get(p);}public long getItemId(int p){return p;}
    public View getView(int p,View old,ViewGroup parent){HuBrowse.Card c=old instanceof HuBrowse.Card?(HuBrowse.Card)old:new HuBrowse.Card(ui,size);MediaItem item=items.get(p);c.bind(item,v->open.item(item));return c;}
   });
   body.addView(grid);
  });
 }

 /** Library: its sections as filter pills; the chosen one's items as a grid. */
 static View library(HuUi ui,String id,Open open){
  LinearLayout page=new LinearLayout(ui.activity);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(ui.dp(16),ui.dp(4),ui.dp(16),0);
  page.addView(heading(ui,HuText.get("library")));
  HorizontalScrollView pillsScroll=row(ui);
  LinearLayout pills=new LinearLayout(ui.activity);pills.setPadding(ui.dp(4),ui.dp(4),ui.dp(4),ui.dp(8));pillsScroll.addView(pills);page.addView(pillsScroll);
  FrameLayout body=new FrameLayout(ui.activity);page.addView(body,new LinearLayout.LayoutParams(-1,0,1));
  body.addView(message(ui,HuText.get("loading")));
  HuSession.get(ui.activity).children(id,sections->{
   body.removeAllViews();
   if(sections.isEmpty()){body.addView(message(ui,HuText.get("empty")));return;}
   List<TextView> all=new ArrayList<>();
   for(MediaItem section:sections){
    TextView pill=ui.label(HuBrowse.title(section),18,ui.text,true);pill.setGravity(Gravity.CENTER);pill.setMinHeight(ui.dp(60));pill.setPadding(ui.dp(26),0,ui.dp(26),0);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,ui.dp(60));lp.setMarginEnd(ui.dp(12));pills.addView(pill,lp);all.add(pill);
    pill.setOnClickListener(v->{select(ui,all,pill);fill(ui,body,HuBrowse.id(section),open);});
   }
   select(ui,all,all.get(0));fill(ui,body,HuBrowse.id(sections.get(0)),open);
  });
  return page;
 }
 private static void select(HuUi ui,List<TextView> pills,TextView chosen){
  for(TextView p:pills){boolean on=p==chosen;p.setBackground(ui.pressable(on?ui.selected:ui.control,ui.dp(30)));p.setTextColor(on?ui.selectedInk:ui.text);}
 }
}
