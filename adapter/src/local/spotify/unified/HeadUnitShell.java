package local.spotify.unified;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
/**
 * New style: the navigation rail and the content area become separate rounded cards with a
 * small gap, on a darker (night) or greyer (day) ground. MainFragment positions the rail, the
 * pager and the bottom sheet in code, so the cards are applied after every layout pass.
 */
final class HeadUnitShell implements ViewTreeObserver.OnGlobalLayoutListener,ViewTreeObserver.OnPreDrawListener {
 private static final String UI="com.spotify.music.ui";private static final float GAP=6,RADIUS=22;
 private static final java.util.WeakHashMap<Activity,HeadUnitShell> installed=new java.util.WeakHashMap<>();
 private final Activity activity;private final boolean dark;private final int gap,radius,ground,card,rail;
 private HeadUnitShell(Activity a){
  activity=a;float d=a.getResources().getDisplayMetrics().density;gap=Math.round(GAP*d);radius=Math.round(RADIUS*d);
  dark=(a.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
  ground=dark?0xff000000:0xffe4e4e4;card=dark?0xff121212:0xfff6f6f6;rail=dark?0xff121212:0xffffffff;
 }
 static void install(Activity a){
  if(!HeadUnitStyle.modern() || installed.containsKey(a))return;
  HeadUnitShell s=new HeadUnitShell(a);installed.put(a,s);
  ViewTreeObserver o=a.getWindow().getDecorView().getViewTreeObserver();o.addOnGlobalLayoutListener(s);o.addOnPreDrawListener(s);
 }
 private View find(String name){int id=activity.getResources().getIdentifier(name,"id",UI);return id==0?null:activity.findViewById(id);}
 public void onGlobalLayout(){
  try{
   View left=find("root_menu"),right=find("right_driver_root_menu"),pager=find("view_pager"),sheet=find("rl_container");
   if(left==null || pager==null || !(left.getParent() instanceof ViewGroup))return;
   ViewGroup root=(ViewGroup)left.getParent();
   if(!(root.getBackground() instanceof GradientDrawable)){GradientDrawable g=new GradientDrawable();g.setColor(ground);root.setBackground(g);}
   View menu=right!=null && right.getVisibility()==View.VISIBLE?right:left;boolean rightHand=menu==right;
   if(menu.getVisibility()!=View.VISIBLE)return;
   card(menu,rail);card(pager,card);
   if(menu.getWidth()==0 || pager.getWidth()==0)return;
   int width=root.getWidth();int[] r=window(root);
   if(menu.getWidth()>menu.getHeight()){
    // Narrow and portrait layouts put the menu across the top; the pager follows below it.
    place(menu,r[0]+gap,r[0]+width-gap,gap,0);place(pager,r[0]+gap,r[0]+width-gap,gap,gap);
   }else if(rightHand){
    place(menu,window(menu)[0],r[0]+width-gap,gap,gap);
    place(pager,r[0]+gap,window(menu)[0]-gap,gap,gap);
   }else{
    place(menu,r[0]+gap,window(menu)[0]+menu.getWidth(),gap,gap);
    place(pager,window(menu)[0]+menu.getWidth()+gap,r[0]+width-gap,gap,gap);
   }
   // The mini-player and the expanded player stay aligned with the content card. Their
   // container spans the gap next to the rail, so it must not paint its own background.
   if(sheet!=null){int[] p=window(pager);place(sheet,p[0],p[0]+pager.getWidth(),-1,-1);if(sheet.getParent() instanceof View && ((View)sheet.getParent()).getBackground()!=null)((View)sheet.getParent()).setBackground(null);}
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Rounded layout unavailable",e);}
 }
 /**
  * The expanded player's panel lies under the mini-player. Collapsed, it must not show around
  * the mini-player card; it fades in while the sheet is dragged open.
  */
 public boolean onPreDraw(){
  try{
   View sheet=find("rl_container"),page=find("now_playing_page_bg"),bar=find("bar");
   if(sheet==null || page==null || bar==null || !(sheet.getParent() instanceof View) || page.getBackground()==null)return true;
   float collapsed=((View)sheet.getParent()).getHeight()-bar.getHeight();
   float open=collapsed<=0?1f:Math.max(0f,Math.min(1f,(collapsed-sheet.getTop())/(collapsed*0.25f)));
   int alpha=Math.round(255*open);if(page.getBackground().getAlpha()!=alpha)page.getBackground().mutate().setAlpha(alpha);
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Player panel fade unavailable",e);}
  return true;
 }
 private static int[] window(View v){int[] xy=new int[2];v.getLocationInWindow(xy);return xy;}
 /**
  * Moves a view so its left and right edges sit at the given window x positions. MainFragment
  * anchors these views to each other in code, so margins are derived from the current positions.
  */
 private static void place(View v,int left,int right,int top,int bottom){
  if(!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))return;
  ViewGroup.MarginLayoutParams p=(ViewGroup.MarginLayoutParams)v.getLayoutParams();int[] xy=window(v);
  int anchorLeft=xy[0]-p.leftMargin,anchorRight=xy[0]+v.getWidth()+p.rightMargin;
  margins(v,left-anchorLeft,top,anchorRight-right,bottom);
 }
 private void card(View v,int color){
  if(v.getBackground() instanceof GradientDrawable && v.getClipToOutline())return;
  GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);v.setBackground(g);
  v.setOutlineProvider(ViewOutlineProvider.BACKGROUND);v.setClipToOutline(true);
 }
 /** Sets the given margins (-1 keeps one) only when they differ, so layout passes settle. */
 private static void margins(View v,int l,int t,int r,int b){
  if(!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))return;
  ViewGroup.MarginLayoutParams p=(ViewGroup.MarginLayoutParams)v.getLayoutParams();
  int nl=l<0?p.leftMargin:l,nt=t<0?p.topMargin:t,nr=r<0?p.rightMargin:r,nb=b<0?p.bottomMargin:b;
  if(p.leftMargin==nl && p.topMargin==nt && p.rightMargin==nr && p.bottomMargin==nb && !p.isMarginRelative())return;
  // Absolute margins: relative start/end would swap them in right-to-left languages.
  p.setMarginStart(Integer.MIN_VALUE);p.setMarginEnd(Integer.MIN_VALUE);p.setMargins(nl,nt,nr,nb);v.setLayoutParams(p);
 }
}
