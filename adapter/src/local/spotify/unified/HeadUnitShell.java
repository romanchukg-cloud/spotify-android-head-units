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
 private CornerMask mask;
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
  systemBars(a.getWindow(),s.dark);
 }
 private View find(String name){int id=activity.getResources().getIdentifier(name,"id",UI);return id==0?null:activity.findViewById(id);}
 public void onGlobalLayout(){
  systemBars(activity.getWindow(),dark);
  try{
   View left=find("root_menu"),right=find("right_driver_root_menu"),pager=find("view_pager"),sheet=find("rl_container");
   if(left==null || pager==null || !(left.getParent() instanceof ViewGroup))return;
   ViewGroup root=(ViewGroup)left.getParent();
   if(!(root.getBackground() instanceof GradientDrawable)){GradientDrawable g=new GradientDrawable();g.setColor(ground);root.setBackground(g);}
   // The app's own corners: black masks blend with BYD's black status bar and dock.
   if(mask==null){mask=new CornerMask(radius+gap);root.getOverlay().add(mask);}
   if(mask.getBounds().width()!=root.getWidth() || mask.getBounds().height()!=root.getHeight())mask.setBounds(0,0,root.getWidth(),root.getHeight());
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
   if(sheet!=null){int[] p=window(pager);place(sheet,p[0],p[0]+pager.getWidth(),-1,-1);
    if(sheet.getParent() instanceof View){View holder=(View)sheet.getParent();if(holder.getBackground()!=null)holder.setBackground(null);
     // The expanded player covers the content card exactly, rounded corners included.
     margins(holder,-1,gap,-1,gap);}
    // The interface dims the sheet's rectangle while the player is open; clipped to the card's
    // rounded shape, the dimming no longer shows at the player's corners.
    if(!sheet.getClipToOutline()){final float corner=radius;sheet.setOutlineProvider(new ViewOutlineProvider(){public void getOutline(View view,android.graphics.Outline outline){outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),corner);}});sheet.setClipToOutline(true);}}
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
 /**
  * BYD's status bar and dock follow the foreground window's light/dark bar appearance (SystemUI
  * LightBarController): without a light request they turn black even in the light theme.
  */
 @SuppressWarnings("deprecation")
 static void systemBars(Window w,boolean dark){barColor(w,dark?0xff000000:0xffe4e4e4);lightBars(w,dark);}
 /** The window paints the area under the bars in the ground colour instead of a translucent black scrim. */
 static void barColor(Window w,int color){
  int translucent=WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS|WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
  if((w.getAttributes().flags&translucent)!=0)w.clearFlags(translucent);
  if((w.getAttributes().flags&WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)==0)w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
  if(w.getStatusBarColor()!=color)w.setStatusBarColor(color);
  if(w.getNavigationBarColor()!=color)w.setNavigationBarColor(color);
 }
 @SuppressWarnings("deprecation")
 static void lightBars(Window w,boolean dark){
  View d=w.getDecorView();int light=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
  int flags=d.getSystemUiVisibility(),wanted=dark?flags&~light:flags|light;
  if(flags!=wanted)d.setSystemUiVisibility(wanted);
  if(android.os.Build.VERSION.SDK_INT>=30){WindowInsetsController c=w.getInsetsController();
   int mask=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
   if(c!=null && (c.getSystemBarsAppearance()&mask)!=(dark?0:mask))c.setSystemBarsAppearance(dark?0:mask,mask);}
 }
 /** Paints the area outside a rounded rectangle black, concentric with the cards' corners. */
 static final class CornerMask extends android.graphics.drawable.Drawable {
  private final float radius;private final android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);private final android.graphics.Path path=new android.graphics.Path();
  CornerMask(float radius){this.radius=radius;paint.setColor(0xff000000);}
  protected void onBoundsChange(android.graphics.Rect b){path.reset();path.setFillType(android.graphics.Path.FillType.EVEN_ODD);path.addRect(b.left,b.top,b.right,b.bottom,android.graphics.Path.Direction.CW);path.addRoundRect(b.left,b.top,b.right,b.bottom,radius,radius,android.graphics.Path.Direction.CW);}
  public void draw(android.graphics.Canvas c){c.drawPath(path,paint);}
  public void setAlpha(int a){paint.setAlpha(a);}
  public void setColorFilter(android.graphics.ColorFilter f){paint.setColorFilter(f);}
  public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
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
