package local.spotify.unified;
import android.animation.ValueAnimator;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.SystemClock;
import android.view.*;
import android.view.animation.*;
import android.view.animation.Interpolator;
import java.util.*;
/**
 * Motion for our interface, in one place: Material 3's easing and durations; the skeletons shown
 * while the backend loads (placeholders in the shape of the cards, grids and rows that come, with
 * one light sweeping across them all); loaded content taking their place (the skeleton fades out
 * where it is while the items rise in one after another); the pages' transitions in the content
 * card; and new covers fading in over the old ones. Without animations (Android's animator scale
 * set to off) everything simply appears. A fading view is drawn through a hardware layer while it
 * fades, so a page or card costs one texture per frame, not its whole drawing.
 */
final class HuMotion {
 private HuMotion(){}
 /** Material 3: entering things (emphasized decelerate), leaving ones (emphasized accelerate), small changes (standard). */
 static final Interpolator ENTER=new PathInterpolator(0.05f,0.7f,0.1f,1f),EXIT=new PathInterpolator(0.3f,0f,0.8f,0.15f),STANDARD=new PathInterpolator(0.2f,0f,0f,1f);
 static final int SHORT=150,MEDIUM=300,LONG=400;
 private static final int STEP=30,LAST=240;
 static boolean animated(){return ValueAnimator.areAnimatorsEnabled();}
 private static float dp(View v,float value){return value*v.getResources().getDisplayMetrics().density;}

 /**
  * A skeleton (or anything) leaves: it moves into the host's overlay, where it stays drawn in its
  * place above the content that replaces it, and fades out there. host: an ancestor that stays
  * (not a list or grid itself, whose children cannot be moved).
  */
 static void leave(View v,ViewGroup host){
  if(v==null || !(v.getParent() instanceof ViewGroup))return;
  if(v instanceof Skeleton)((Skeleton)v).frozen=true;
  if(!host.isAttachedToWindow() || !animated()){((ViewGroup)v.getParent()).removeView(v);return;}
  ViewGroupOverlay overlay=host.getOverlay();overlay.add(v);
  v.animate().cancel();
  v.animate().alpha(0f).setStartDelay(0).setDuration(SHORT).setInterpolator(STANDARD).withLayer().withEndAction(()->overlay.remove(v)).start();
 }
 /** Once laid out, a group's children from skip on (a list's header stays) rise in one after another. */
 static void rise(ViewGroup g,int skip){
  once(g,()->{List<View> views=new ArrayList<>();for(int i=skip;i<g.getChildCount();i++)views.add(g.getChildAt(i));stagger(views);});
 }
 /** Once laid out, these views rise in one after another, in this order. */
 static void rise(View root,List<View> views){once(root,()->stagger(views));}
 /** Only the views on screen move (12 dp up while they fade in, 30 ms apart, the last within a quarter second); the rest are simply there. */
 private static void stagger(List<View> views){
  if(!animated())return;
  Rect r=new Rect();int n=0;
  for(View v:views){
   if(!v.getGlobalVisibleRect(r))continue;
   v.animate().cancel();v.setAlpha(0f);v.setTranslationY(dp(v,12));
   v.animate().alpha(1f).translationY(0f).setStartDelay(Math.min(LAST,n++*STEP)).setDuration(MEDIUM).setInterpolator(ENTER).withLayer().start();
  }
 }
 /** Runs once, right before the view's next frame (after its layout); a view not on screen waits until it is. */
 private static void once(View v,Runnable r){
  v.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
   public boolean onPreDraw(){ViewTreeObserver o=v.getViewTreeObserver();if(o.isAlive())o.removeOnPreDrawListener(this);r.run();return true;}
  });
 }
 /** A view that appears fades in. */
 static void fadeIn(View v){
  v.animate().cancel();if(!animated()){v.setAlpha(1f);return;}
  v.setAlpha(0f);v.animate().alpha(1f).setStartDelay(0).setDuration(SHORT).setInterpolator(STANDARD).withLayer().start();
 }

 /**
  * A page in the content card replaces another. Opening and going back (direction 1 and -1) move
  * along the reading direction, as Material's shared axis: the old page fades out with a nudge
  * and the new one comes 30 dp from the side it lies on. Switching tabs (0) fades through: the old
  * page fades out, the new one fades in, growing slightly. The new page starts as the old one is
  * gone, so the two never show through each other; the old one leaves in the card's overlay, so
  * it takes no touches.
  */
 static void swap(ViewGroup card,View old,View next,int direction){
  next.animate().cancel();next.setAlpha(1f);next.setTranslationX(0f);next.setScaleX(1f);next.setScaleY(1f);
  if(old==null || old==next || !card.isAttachedToWindow() || !animated()){if(old!=null && old!=next && old.getParent() instanceof ViewGroup)((ViewGroup)old.getParent()).removeView(old);return;}
  float side=(card.getLayoutDirection()==View.LAYOUT_DIRECTION_RTL?-1:1)*direction;
  ViewGroupOverlay overlay=card.getOverlay();if(old.getParent()==card)overlay.add(old);
  old.animate().cancel();
  old.animate().alpha(0f).translationX(-side*dp(card,8)).setStartDelay(0).setDuration(90).setInterpolator(EXIT).withLayer().withEndAction(()->{overlay.remove(old);old.setAlpha(1f);old.setTranslationX(0f);}).start();
  next.setAlpha(0f);
  if(direction!=0){
   next.setTranslationX(side*dp(card,30));
   next.animate().alpha(1f).translationX(0f).setStartDelay(90).setDuration(270).setInterpolator(ENTER).withLayer().start();
  }else{
   next.setScaleX(0.97f);next.setScaleY(0.97f);
   next.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(90).setDuration(240).setInterpolator(ENTER).withLayer().start();
  }
 }

 /**
  * A column whose items move to their new places when one above them appears or goes (a line
  * that comes or a skeleton that leaves), instead of jumping.
  */
 static void moves(ViewGroup g){
  android.animation.LayoutTransition t=new android.animation.LayoutTransition();
  t.disableTransitionType(android.animation.LayoutTransition.APPEARING);t.disableTransitionType(android.animation.LayoutTransition.DISAPPEARING);
  t.setStartDelay(android.animation.LayoutTransition.CHANGE_DISAPPEARING,0);t.setStartDelay(android.animation.LayoutTransition.CHANGE_APPEARING,0);
  t.setStagger(android.animation.LayoutTransition.CHANGE_APPEARING,0);t.setStagger(android.animation.LayoutTransition.CHANGE_DISAPPEARING,0);
  t.setDuration(MEDIUM);t.setInterpolator(android.animation.LayoutTransition.CHANGE_APPEARING,ENTER);t.setInterpolator(android.animation.LayoutTransition.CHANGE_DISAPPEARING,ENTER);
  g.setLayoutTransition(t);
 }
 /** A new background (a new cover's blur) fades in over the one shown, then takes its place. */
 static void background(View v,Drawable next){
  Drawable old=v.getBackground();if(old instanceof TransitionDrawable)old=((TransitionDrawable)old).getDrawable(1);
  if(old==null || old==next || !v.isAttachedToWindow() || !animated()){v.setBackground(next);return;}
  TransitionDrawable t=new TransitionDrawable(new Drawable[]{old,next});v.setBackground(t);t.startTransition(LONG);
  v.postDelayed(()->{if(v.getBackground()==t)v.setBackground(next);},LONG+50);
 }

 /**
  * Placeholders while something loads, in the shape of what comes: Home's sections (a heading
  * and a row of cards), a grid of cards, track rows (with covers, or numbered as on albums),
  * Library's filter pills, or one line. One light band sweeps across the screen, so separate
  * skeletons shimmer together; it runs only while the skeleton is drawn.
  */
 static final class Skeleton extends View {
  static final int SECTIONS=0,GRID=1,ROWS=2,NUMBERED=3,PILLS=4,LINE=5;
  private static final long PERIOD=1500;
  private final HuUi ui;private final int kind,count;private final Paint base=new Paint(Paint.ANTI_ALIAS_FLAG),shine=new Paint(Paint.ANTI_ALIAS_FLAG);
  /** The boxes: left, top, right, bottom and corner radius, five floats each (drawn as rounded rectangles, which the GPU draws cheaply every frame). */
  private float[] boxes=new float[0];private int used;private final Matrix matrix=new Matrix();private final int[] at=new int[2];private final float band;
  /** Leaving: the light stops where it is (in an overlay the position on screen is not known). */
  boolean frozen;private float x=Float.NaN;
  /** count: sections, rows or pills; a grid or a line fills its view. */
  Skeleton(HuUi ui,int kind,int count){
   super(ui.activity);this.ui=ui;this.kind=kind;this.count=count;
   base.setColor(ui.control);band=ui.dp(260);
   shine.setShader(new LinearGradient(0,0,band,0,new int[]{0x00ffffff,ui.dark?0x1affffff:0xa0ffffff,0x00ffffff},null,Shader.TileMode.CLAMP));
   setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
  }
  private int cover(){return HuPages.card(ui);}
  /** HuBrowse.Card: 8 dp padding, the cover, a title (10 dp above it) and a subtitle, 10 dp padding. */
  private int cardHeight(){return ui.dp(8+10+24+20+10)+cover();}
  private int sectionHeight(){return ui.dp(60)+cardHeight();}
  private static final int[] PILL={150,124,136,112,128};
  protected void onMeasure(int w,int h){
   int width=MeasureSpec.getSize(w),height=MeasureSpec.getSize(h);boolean free=MeasureSpec.getMode(h)==MeasureSpec.UNSPECIFIED;
   switch(kind){
    case SECTIONS:setMeasuredDimension(width,count*sectionHeight());break;
    case ROWS:case NUMBERED:setMeasuredDimension(width,count*ui.dp(76));break;
    case PILLS:{int sum=0;for(int i=0;i<count;i++)sum+=ui.dp(PILL[i%PILL.length]+12);setMeasuredDimension(sum,ui.dp(60));break;}
    case LINE:setMeasuredDimension(width,height);break;
    default:setMeasuredDimension(width,free?2*cardHeight():height);
   }
  }
  protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);build(w,h);}
  /** A rounded box at a start-relative place (mirrored in right-to-left layouts). */
  private void box(float start,float top,float width,float height,float radius){
   float left=getLayoutDirection()==LAYOUT_DIRECTION_RTL?getWidth()-start-width:start;
   if(used+5>boxes.length)boxes=Arrays.copyOf(boxes,Math.max(80,boxes.length*2));
   boxes[used++]=left;boxes[used++]=top;boxes[used++]=left+width;boxes[used++]=top+height;boxes[used++]=radius;
  }
  private void card(float start,float top){
   int s=cover();float l=start+ui.dp(8);box(l,top+ui.dp(8),s,s,ui.dp(8));
   float y=top+ui.dp(8)+s+ui.dp(16);box(l,y,s*0.8f,ui.dp(14),ui.dp(7));box(l,y+ui.dp(25),s*0.55f,ui.dp(12),ui.dp(6));
  }
  private void build(int w,int h){
   used=0;int s=cover(),column=s+ui.dp(16),cardH=cardHeight();
   switch(kind){
    case SECTIONS:
     for(int i=0;i<count;i++){
      float top=i*sectionHeight();box(ui.dp(8),top+ui.dp(22),ui.dp(i%2==0?260:190),ui.dp(26),ui.dp(13));
      for(float x=0;x<w;x+=column)card(x,top+ui.dp(60));
     }
     break;
    case GRID:{
     int columns=Math.max(1,w/column);float gap=(w-columns*column)/(float)(columns+1);
     for(float y=0;y<h;y+=cardH+ui.dp(4))for(int c=0;c<columns;c++)card(gap+c*(column+gap),y);
     break;
    }
    case ROWS:case NUMBERED:{
     // HuList's rows: 8 dp padding, a 56 dp cover (4 dp after it) or a 48 dp number, the texts 16 dp after; the length at the end.
     boolean covers=kind==ROWS;float text=ui.dp(covers?8+4+56+16:8+48+16),room=Math.max(ui.dp(80),w-text-ui.dp(16+64));
     for(int i=0;i<count;i++){
      float top=i*ui.dp(76);
      if(covers)box(ui.dp(12),top+ui.dp(10),ui.dp(56),ui.dp(56),ui.dp(6));else box(ui.dp(8+17),top+ui.dp(31),ui.dp(14),ui.dp(14),ui.dp(7));
      box(text,top+ui.dp(20),room*(i%3==0?0.55f:i%3==1?0.7f:0.45f),ui.dp(15),ui.dp(7.5f));
      box(text,top+ui.dp(44),room*(i%2==0?0.36f:0.28f),ui.dp(12),ui.dp(6));
      box(w-ui.dp(16+36),top+ui.dp(32),ui.dp(36),ui.dp(12),ui.dp(6));
     }
     break;
    }
    case PILLS:{float x=0;for(int i=0;i<count;i++){int pw=PILL[i%PILL.length];box(x,0,ui.dp(pw),ui.dp(60),ui.dp(30));x+=ui.dp(pw+12);}break;}
    case LINE:box(0,0,w,h,h/2f);break;
    default:break;
   }
  }
  protected void onAttachedToWindow(){super.onAttachedToWindow();invalidate();}
  protected void onDraw(Canvas c){
   for(int i=0;i<used;i+=5)c.drawRoundRect(boxes[i],boxes[i+1],boxes[i+2],boxes[i+3],boxes[i+4],boxes[i+4],base);
   if(!frozen || Float.isNaN(x)){
    // The band's place on the screen, not in this view: every skeleton shows the same sweep.
    getLocationOnScreen(at);float screen=getResources().getDisplayMetrics().widthPixels;
    float f=(SystemClock.uptimeMillis()%PERIOD)/(float)PERIOD,sx=-band+(screen+2*band)*f;
    if(getLayoutDirection()==LAYOUT_DIRECTION_RTL)sx=screen-sx-band;
    x=sx-at[0];
   }
   matrix.setTranslate(x,0);shine.getShader().setLocalMatrix(matrix);
   // Only the boxes the band crosses get its light.
   for(int i=0;i<used;i+=5)if(boxes[i+2]>x && boxes[i]<x+band)c.drawRoundRect(boxes[i],boxes[i+1],boxes[i+2],boxes[i+3],boxes[i+4],boxes[i+4],shine);
   if(!frozen && isAttachedToWindow() && animated())postInvalidateOnAnimation();
  }
 }
}
