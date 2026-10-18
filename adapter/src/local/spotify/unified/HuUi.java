package local.spotify.unified;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
/** Building blocks for the New style of the adapter's own screens (settings, accounts, support). */
final class HuUi {
 final Activity activity;final boolean dark;
 final int bg,card,text,secondary,divider,accent,control,selected,selectedInk;
 HuUi(Activity a){
  activity=a;dark=(a.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
  bg=dark?0xff121212:0xfff6f6f6;card=dark?0xff1c1c1c:0xffffffff;text=dark?0xffffffff:0xff121212;secondary=dark?0xffb3b3b3:0xff555555;
  divider=dark?0x12ffffff:0x14000000;accent=dark?0xff1ed760:0xff0f7a36;control=dark?0xff2e2e2e:0xffebebeb;selected=dark?0xffffffff:0xff121212;selectedInk=dark?0xff121212:0xffffffff;
 }
 int dp(float v){return Math.round(v*activity.getResources().getDisplayMetrics().density);}
 void window(){Window w=activity.getWindow();w.setStatusBarColor(bg);w.setNavigationBarColor(bg);HeadUnitShell.lightBars(w,dark);corners(activity);}
 private static final java.util.WeakHashMap<Activity,Boolean> cornered=new java.util.WeakHashMap<>();
 /**
  * The app's rounded outer corners (card radius 22 dp + gap 6 dp), as on the main screen: a black
  * mask over the content, black like BYD's status bar and dock.
  */
 static void corners(Activity a){
  if(cornered.containsKey(a))return;View root=a.findViewById(android.R.id.content);if(root==null)return;cornered.put(a,Boolean.TRUE);
  HeadUnitShell.CornerMask mask=new HeadUnitShell.CornerMask(28*a.getResources().getDisplayMetrics().density);root.getOverlay().add(mask);
  root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(mask.getBounds().width()!=v.getWidth() || mask.getBounds().height()!=v.getHeight())mask.setBounds(0,0,v.getWidth(),v.getHeight());});
 }
 /**
  * A cover image with rounded corners. The corners are cut by the view's own outline, and the grey
  * placeholder behind it is there only until the image arrives: under the image its anti-aliased
  * edge showed as a thin light rim around the cover.
  */
 static class Cover extends ImageView {
  private final int placeholder;private float radius;
  Cover(HuUi ui,float radius){
   super(ui.activity);placeholder=ui.control;this.radius=radius;setScaleType(ScaleType.CENTER_CROP);
   setOutlineProvider(new ViewOutlineProvider(){public void getOutline(View v,Outline o){o.setRoundRect(0,0,v.getWidth(),v.getHeight(),Cover.this.radius);}});
   setClipToOutline(true);setBackground(round(placeholder,radius));
  }
  /** A new corner radius (round for artists: half the size). */
  void shape(float r){if(r==radius)return;radius=r;invalidateOutline();if(getDrawable()==null)setBackground(round(placeholder,r));}
  public void setImageDrawable(Drawable d){
   super.setImageDrawable(d);boolean empty=d==null || d instanceof BitmapDrawable && ((BitmapDrawable)d).getBitmap()==null;
   setBackground(empty?round(placeholder,radius):null);
  }
 }
 static GradientDrawable round(int color,float radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
 Drawable pressable(int color,float radius){return new RippleDrawable(android.content.res.ColorStateList.valueOf(dark?0x33ffffff:0x22000000),round(color,radius),round(0xff000000,radius));}
 private static Typeface regular,heavy;
 /** Figtree from the interface resources (font/hu_figtree_family), or null to keep the system font. */
 Typeface font(boolean bold){
  if(regular==null){
   int id=activity.getResources().getIdentifier("hu_figtree_family","font","com.spotify.music.ui");
   if(id!=0)try{Typeface f=activity.getResources().getFont(id);regular=Typeface.create(f,400,false);heavy=Typeface.create(f,700,false);}catch(RuntimeException e){android.util.Log.w("SpotifyHU","Figtree unavailable",e);}
  }
  return bold?heavy:regular;
 }
 /**
  * A text that starts at the interface's start edge (the right in right-to-left languages), also
  * when it holds Latin names such as a track title; centred or end gravity keeps its alignment.
  */
 static final class Label extends TextView {
  private final boolean ready;
  Label(Context c){super(c);ready=true;setTextAlignment(TEXT_ALIGNMENT_VIEW_START);}
  public void setGravity(int gravity){
   super.setGravity(gravity);if(!ready)return;
   int h=gravity&Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
   setTextAlignment(h==0 || h==Gravity.START || h==Gravity.LEFT?TEXT_ALIGNMENT_VIEW_START:TEXT_ALIGNMENT_GRAVITY);
  }
 }
 TextView label(String value,float sp,int color,boolean bold){TextView t=new Label(activity);t.setText(value);t.setTextSize(sp);t.setTextColor(color);Typeface f=font(bold);t.setTypeface(f!=null?f:bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);return t;}
 /** A rounded card with an upper-case section caption. */
 LinearLayout card(String caption){
  LinearLayout c=new LinearLayout(activity);c.setOrientation(LinearLayout.VERTICAL);c.setBackground(round(card,dp(20)));c.setPadding(dp(20),dp(8),dp(20),dp(8));
  TextView h=label(caption.toUpperCase(),17,secondary,true);h.setLetterSpacing(0.06f);h.setPadding(0,dp(10),0,dp(4));c.addView(h);return c;
 }
 void divider(LinearLayout parent){View v=new View(activity);v.setBackgroundColor(divider);parent.addView(v,new LinearLayout.LayoutParams(-1,1));}
 /** A full-width row (at least 84 dp, larger than the 76 dp touch target) with a title, optional hint and a trailing control. */
 LinearLayout row(String title,String hint,View trailing){
  LinearLayout r=new LinearLayout(activity);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);r.setMinimumHeight(dp(84));
  LinearLayout words=new LinearLayout(activity);words.setOrientation(LinearLayout.VERTICAL);
  words.addView(label(title,21,text,true));
  if(hint!=null){TextView h=label(hint,17,secondary,false);h.setPadding(0,dp(2),0,0);words.addView(h);}
  r.addView(words,new LinearLayout.LayoutParams(0,-2,1));
  if(trailing!=null){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.leftMargin=dp(16);r.addView(trailing,p);}
  return r;
 }
 /** A 64x36 dp switch; the whole row toggles it. */
 static final class Toggle extends View {
  private final HuUi ui;private boolean on,enabled=true;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  Toggle(HuUi ui,boolean on){super(ui.activity);this.ui=ui;this.on=on;}
  boolean on(){return on;}
  void set(boolean value){on=value;invalidate();}
  void active(boolean value){enabled=value;invalidate();}
  protected void onMeasure(int w,int h){setMeasuredDimension(ui.dp(64),ui.dp(36));}
  protected void onDraw(Canvas c){
   float h=getHeight(),w=getWidth(),r=h/2;
   paint.setColor(on && enabled?ui.accent:ui.dark?0xff535353:0xffbdbdbd);c.drawRoundRect(0,0,w,h,r,r,paint);
   paint.setColor(0xffffffff);float x=on?w-r:r;c.drawCircle(x,r,r-ui.dp(4),paint);
  }
 }
 LinearLayout switchRow(String title,String hint,Toggle toggle,View.OnClickListener click){
  LinearLayout r=row(title,hint,toggle);r.setBackground(pressable(card,dp(12)));r.setOnClickListener(click);r.setContentDescription(title);return r;
 }
 /** A pill-shaped two-option selector. */
 LinearLayout segmented(String[] labels,int selectedIndex,java.util.function.IntConsumer pick){
  LinearLayout s=new LinearLayout(activity);s.setBackground(round(control,dp(30)));s.setPadding(dp(4),dp(4),dp(4),dp(4));
  for(int i=0;i<labels.length;i++){
   final int index=i;TextView t=label(labels[i],20,i==selectedIndex?selectedInk:text,true);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(52));t.setPadding(dp(26),0,dp(26),0);
   if(i==selectedIndex)t.setBackground(round(selected,dp(26)));
   t.setOnClickListener(v->pick.accept(index));s.addView(t,new LinearLayout.LayoutParams(-2,dp(52)));
  }
  return s;
 }
 TextView circle(String glyph,String description,View.OnClickListener click){
  TextView t=label(glyph,26,text,true);t.setGravity(Gravity.CENTER);t.setBackground(pressable(control,dp(32)));t.setContentDescription(description);t.setOnClickListener(click);
  t.setLayoutParams(new LinearLayout.LayoutParams(dp(64),dp(64)));return t;
 }
 /** A tappable tile for the bottom row of the settings screen. */
 TextView tile(String title,View.OnClickListener click){
  TextView t=label(title,20,text,true);t.setGravity(Gravity.CENTER_VERTICAL);t.setMinHeight(dp(84));t.setMaxLines(2);t.setEllipsize(android.text.TextUtils.TruncateAt.END);t.setPadding(dp(20),0,dp(20),0);
  t.setBackground(pressable(card,dp(20)));t.setOnClickListener(click);return t;
 }
 /** A 76 dp round back button drawn as a chevron. */
 View back(){
  View v=new View(activity){final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);{p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
   protected void onDraw(Canvas c){p.setColor(text);float cx=getWidth()/2f,cy=getHeight()/2f,s=dp(9);Path path=new Path();path.moveTo(cx+s*0.5f,cy-s);path.lineTo(cx-s*0.5f,cy);path.lineTo(cx+s*0.5f,cy+s);c.drawPath(path,p);}};
  v.setBackground(pressable(bg,dp(38)));v.setContentDescription("Back");v.setOnClickListener(x->activity.finish());v.setLayoutParams(new LinearLayout.LayoutParams(dp(76),dp(76)));
  if(activity.getResources().getConfiguration().getLayoutDirection()==View.LAYOUT_DIRECTION_RTL)v.setScaleX(-1);
  return v;
 }
 static boolean wide(Context c){return c.getResources().getConfiguration().screenWidthDp>=900;}
 private static final java.util.WeakHashMap<View,Boolean> adopted=new java.util.WeakHashMap<>();
 /**
  * New style for the adapter's simpler screens (support, diagnostics notice, accounts): restyles
  * their standard widgets and the Classic hard-coded colours, also for views added later.
  */
 static void adopt(Activity a){
  HuUi ui=new HuUi(a);ui.window();View root=a.findViewById(android.R.id.content);if(root==null)return;
  ui.walk(root);root.getViewTreeObserver().addOnGlobalLayoutListener(()->ui.walk(root));
 }
 private int map(int color){
  switch(color){case 0xffffffff:return text;case 0xffb3b3b3:case 0xffa7a7a8:return secondary;case 0xff1ed760:return accent;case 0xff121212:case 0xff000000:case 0xff181818:return bg;case 0xff303030:case 0xff282828:return control;default:return color;}
 }
 private void walk(View v){
  if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)walk(g.getChildAt(i));}
  if(adopted.containsKey(v))return;adopted.put(v,Boolean.TRUE);
  if(v instanceof TextView){TextView t=(TextView)v;Typeface f=font(t.getTypeface()!=null && t.getTypeface().isBold());if(f!=null)t.setTypeface(f);}
  Drawable background=v.getBackground();
  if(background instanceof ColorDrawable)v.setBackgroundColor(map(((ColorDrawable)background).getColor()));
  if(v instanceof Spinner){v.setBackground(round(control,dp(16)));v.setMinimumHeight(dp(76));return;}
  if(v instanceof Switch){Switch s=(Switch)v;android.content.res.ColorStateList states=new android.content.res.ColorStateList(new int[][]{{android.R.attr.state_checked},{}},new int[]{accent,dark?0xff535353:0xffbdbdbd});
   s.setThumbTintList(android.content.res.ColorStateList.valueOf(0xffffffff));s.setTrackTintList(states);s.setTrackTintMode(PorterDuff.Mode.SRC);s.setTextColor(text);s.setMinHeight(dp(76));return;}
  if(v instanceof CheckBox){((CheckBox)v).setButtonTintList(android.content.res.ColorStateList.valueOf(accent));((CheckBox)v).setTextColor(text);((CheckBox)v).setMinHeight(dp(76));return;}
  if(v instanceof EditText){EditText e=(EditText)v;e.setBackground(round(control,dp(16)));e.setTextColor(text);e.setHintTextColor(secondary);e.setPadding(dp(20),dp(14),dp(20),dp(14));return;}
  if(v instanceof Button){Button b=(Button)v;b.setAllCaps(false);b.setTextColor(text);b.setBackground(pressable(control,dp(38)));b.setMinHeight(dp(76));b.setStateListAnimator(null);return;}
  if(v instanceof TextView){TextView t=(TextView)v;t.setTextColor(map(t.getCurrentTextColor()));t.setLinkTextColor(accent);}
  if(v instanceof SeekBar){((SeekBar)v).setProgressTintList(android.content.res.ColorStateList.valueOf(text));((SeekBar)v).setThumbTintList(android.content.res.ColorStateList.valueOf(text));}
 }
}
