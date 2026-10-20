package local.spotify.unified;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
/**
 * The compact player another app shows in its panel (PanelProviderService): cover, title and
 * artist, previous / play-pause / next, a thin progress line with elapsed and remaining time, with rounded corners in the host's colours (black by
 * night, white by day). A low, wide panel gets a strip, a tall and wide one the cover beside the
 * details, any other a column; sizes grow a little with the block but never stretch. Data and commands go
 * through our session with the Spotify backend; it connects only once the backend runs, so a
 * host that binds starts neither the backend nor the music. Signed out or without a session it
 * says "Open Spotify", and a tap opens Spotify; with nothing playing it says so, and play resumes.
 */
final class PanelPlayerView extends FrameLayout implements HuSession.Listener {
 private final HuSession session;private final Runnable open;
 private boolean night,listening;private int widthPx,heightPx;
 private ImageView cover,play;private TextView title,artist,elapsed,remaining;private Progress progress;private String art;
 private int bg,text,secondary;private final float radius;
 private final Runnable tick=new Runnable(){public void run(){update();if(isAttachedToWindow())postDelayed(this,1000);}};
 private final Runnable watch=new Runnable(){public void run(){if(!listening && backendRunning())listen();if(!listening && isAttachedToWindow())postDelayed(this,5000);}};
 /** radiusPx: the block's corner radius (negative: 22 dp); outside it the block is transparent, so the host's card shows. */
 PanelPlayerView(Context c,boolean night,int widthPx,int heightPx,float radiusPx,Runnable open){
  super(c);session=HuSession.get(c);this.open=open;this.night=night;this.widthPx=widthPx;this.heightPx=heightPx;
  radius=radiusPx>=0?radiusPx:22*c.getResources().getDisplayMetrics().density;
  setOutlineProvider(ViewOutlineProvider.BACKGROUND);setClipToOutline(true);build();
 }
 void size(int w,int h){if(w==widthPx && h==heightPx)return;widthPx=w;heightPx=h;build();}
 void theme(boolean night){if(this.night==night)return;this.night=night;build();}
 private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}

 protected void onAttachedToWindow(){super.onAttachedToWindow();removeCallbacks(watch);post(watch);post(tick);}
 protected void onDetachedFromWindow(){removeCallbacks(watch);removeCallbacks(tick);release();super.onDetachedFromWindow();}
 /** Stops listening to the session (the block is closed). */
 void release(){if(listening){session.remove(this);listening=false;}}
 private void listen(){listening=true;session.add(this);}
 /** The Spotify backend's process (the main one) is running; our session would otherwise start it. */
 private boolean backendRunning(){
  if(session.ready())return true;
  ActivityManager am=getContext().getSystemService(ActivityManager.class);
  java.util.List<ActivityManager.RunningAppProcessInfo> list=am==null?null:am.getRunningAppProcesses();
  if(list!=null)for(ActivityManager.RunningAppProcessInfo p:list)if(getContext().getPackageName().equals(p.processName))return true;
  return false;
 }

 private static final int STRIP=0,WIDE=1,COLUMN=2;
 /**
  * A low, wide block is a strip; a tall and wide one puts the cover beside the details, as our
  * player does; anything else is a column with room for the details under the cover.
  */
 private int mode(){float d=density(),w=widthPx/d,h=heightPx/d;return w>=1.25f*h?(h<=420?STRIP:WIDE):COLUMN;}
 private float density(){return getResources().getDisplayMetrics().density;}
 private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}

 private void build(){
  removeAllViews();art=null;
  bg=night?0xff000000:0xffffffff;text=night?0xffffffff:0xff000000;secondary=night?0xffb3b3b3:0xff6a6a6a;setBackground(round(bg,radius));
  int mode=mode();float d=density(),wDp=widthPx/d,hDp=heightPx/d;int pad=dp(mode==STRIP?12:16);
  // Sizes start at our mini-player's and grow a little in large blocks, but never stretch.
  int cover=0;float s;
  if(mode==WIDE){
   cover=(int)Math.min(Math.min(heightPx-2*pad,widthPx*0.45f),dp(520));
   s=clamp(Math.min((widthPx-cover-2*pad-dp(24))/d/380f,hDp/480f),1f,1.4f);
  }else s=mode==STRIP?clamp(hDp/280f,0.85f,1.15f):clamp(Math.min(wDp/380f,hDp/560f),1f,1.4f);
  int side=dp((mode==STRIP?60:64)*s),main=dp((mode==STRIP?72:80)*s),gap=dp(10*s);
  LinearLayout box=new LinearLayout(getContext());box.setPadding(pad,pad,pad,pad);addView(box,new LayoutParams(-1,-1));
  this.cover=new ImageView(getContext());this.cover.setScaleType(ImageView.ScaleType.CENTER_CROP);this.cover.setBackground(round(night?0xff282828:0xffe8e8e8,dp(mode==STRIP?8:12)));
  this.cover.setOutlineProvider(ViewOutlineProvider.BACKGROUND);this.cover.setClipToOutline(true);this.cover.setOnClickListener(v->open.run());
  title=label((mode==STRIP?21:22)*s,text,true);title.setMaxLines(mode==COLUMN?2:1);artist=label((mode==STRIP?16:17)*s,secondary,false);artist.setMaxLines(1);
  LinearLayout words=new LinearLayout(getContext());words.setOrientation(LinearLayout.VERTICAL);words.addView(title);words.addView(artist);words.setOnClickListener(v->open.run());
  progress=new Progress();progress.line=dp(3*Math.min(s,1.2f));
  // Elapsed and remaining time under the progress line, left to right in every language.
  LinearLayout times=new LinearLayout(getContext());times.setLayoutDirection(LAYOUT_DIRECTION_LTR);
  elapsed=label(14*s,secondary,false);elapsed.setMaxLines(1);elapsed.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);elapsed.setGravity(Gravity.START);
  remaining=label(14*s,secondary,false);remaining.setMaxLines(1);remaining.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);remaining.setGravity(Gravity.END);
  times.addView(elapsed,new LinearLayout.LayoutParams(0,-2,1));times.addView(remaining,new LinearLayout.LayoutParams(0,-2,1));
  // Controls keep their left-to-right order in right-to-left languages, as media controls do.
  LinearLayout controls=new LinearLayout(getContext());controls.setGravity(Gravity.CENTER);controls.setLayoutDirection(LAYOUT_DIRECTION_LTR);
  controls.addView(button("encore_icon_skip_back_24",HuText.get("previous"),side,v->session.previous()));
  play=button("encore_icon_play_24",HuText.get("play"),main,v->session.playPause());play.setBackground(round(text,main/2f));
  LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(main,main);pp.setMargins(gap,0,gap,0);controls.addView(play,pp);
  controls.addView(button("encore_icon_skip_forward_24",HuText.get("next"),side,v->session.next()));
  int controlsWidth=2*side+main+2*gap;
  if(mode==STRIP){
   // Cover at most 30 % of the width; with little room it goes first, then the texts.
   box.setOrientation(LinearLayout.HORIZONTAL);box.setGravity(Gravity.CENTER_VERTICAL);
   cover=(int)Math.min(heightPx-2*pad,widthPx*0.3f);int spare=widthPx-2*pad-controlsWidth-dp(22);
   if(spare-cover-dp(14)<dp(120))cover=0;int middleWidth=spare-(cover>0?cover+dp(14):0);
   if(cover>0)box.addView(this.cover,new LinearLayout.LayoutParams(cover,cover));
   if(middleWidth<dp(60) && heightPx-2*pad>=main+dp(48*s)){
    // Too narrow for the texts beside the controls (the minimum size): the title above them.
    box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);title.setGravity(Gravity.CENTER);artist.setGravity(Gravity.CENTER);
    title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);artist.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    box.addView(words,new LinearLayout.LayoutParams(-1,-2));LinearLayout.LayoutParams c=new LinearLayout.LayoutParams(-2,-2);c.topMargin=dp(6);box.addView(controls,c);
   }else if(middleWidth>=dp(60)){
    LinearLayout middle=new LinearLayout(getContext());middle.setOrientation(LinearLayout.VERTICAL);middle.setGravity(Gravity.CENTER_VERTICAL);
    middle.addView(words);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(16*s));p.topMargin=dp(4);middle.addView(progress,p);middle.addView(times,new LinearLayout.LayoutParams(-1,-2));
    LinearLayout.LayoutParams m=new LinearLayout.LayoutParams(0,-2,1);m.setMarginStart(cover>0?dp(14):0);m.setMarginEnd(dp(8));box.addView(middle,m);
   }else box.setGravity(Gravity.CENTER);
   if(controls.getParent()==null)box.addView(controls,new LinearLayout.LayoutParams(-2,-2));
  }else{
   LinearLayout info=new LinearLayout(getContext());info.setOrientation(LinearLayout.VERTICAL);info.setGravity(Gravity.CENTER_VERTICAL);
   info.addView(words);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(18*s));p.topMargin=dp(10*s);info.addView(progress,p);info.addView(times,new LinearLayout.LayoutParams(-1,-2));
   LinearLayout.LayoutParams c=new LinearLayout.LayoutParams(-1,-2);c.topMargin=dp(8*s);info.addView(controls,c);
   if(mode==WIDE){
    box.setOrientation(LinearLayout.HORIZONTAL);box.setGravity(Gravity.CENTER_VERTICAL);
    box.addView(this.cover,new LinearLayout.LayoutParams(cover,cover));
    LinearLayout.LayoutParams i=new LinearLayout.LayoutParams(0,-2,1);i.setMarginStart(dp(24));box.addView(info,i);
   }else{
    // The cover takes what the details leave (about 220 dp of them), centred with them.
    box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);
    cover=(int)Math.min(Math.min(widthPx-2*pad,heightPx-2*pad-dp(220*s)),dp(520));
    int infoWidth=Math.min(widthPx-2*pad,Math.max(cover,Math.max(controlsWidth,dp(360*s))));
    if(cover>=dp(72)){LinearLayout.LayoutParams cv=new LinearLayout.LayoutParams(cover,cover);cv.bottomMargin=dp(14*s);box.addView(this.cover,cv);}
    box.addView(info,new LinearLayout.LayoutParams(infoWidth,-2));
   }
  }
  // Signed out or without a session: a single "Open Spotify" target.
  boolean row=mode==STRIP;
  LinearLayout hint=new LinearLayout(getContext());hint.setGravity(Gravity.CENTER);hint.setOrientation(row?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
  hint.setBackground(round(bg,radius));hint.setTag("open");hint.setClickable(true);hint.setOnClickListener(v->open.run());hint.setMinimumHeight(dp(48));
  ImageView logo=new ImageView(getContext());logo.setImageDrawable(HuControls.icon(getContext(),"encore_icon_spotify_logo_24",0xff1ed760));hint.addView(logo,new LinearLayout.LayoutParams(dp(48*s),dp(48*s)));
  TextView say=label(22*s,text,true);say.setText(HuText.get("panel_open"));say.setGravity(Gravity.CENTER);say.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);say.setPadding(dp(14),dp(10),dp(14),dp(10));hint.addView(say);
  addView(hint,new LayoutParams(-1,-1));
  changed(session);
 }
 private TextView label(float sp,int color,boolean bold){
  TextView t=new TextView(getContext());t.setTextSize(sp);t.setTextColor(color);t.setSingleLine(false);t.setEllipsize(android.text.TextUtils.TruncateAt.END);
  t.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);Typeface f=HuUi.figtree(getContext(),bold);t.setTypeface(f!=null?f:bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);return t;
 }
 private ImageView button(String icon,String description,int size,View.OnClickListener click){
  ImageView b=new ImageView(getContext());b.setScaleType(ImageView.ScaleType.FIT_CENTER);int pad=size*28/100;b.setPadding(pad,pad,pad,pad);
  b.setImageDrawable(HuControls.icon(getContext(),icon,text));b.setContentDescription(description);b.setOnClickListener(click);
  b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(night?0x33ffffff:0x22000000),null,round(0xff000000,size/2f)));
  b.setLayoutParams(new LinearLayout.LayoutParams(size,size));return b;
 }
 private static GradientDrawable round(int color,float radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}

 public void changed(HuSession s){
  // Connected and signed in: the player, with "Nothing is playing" until there is a track (play
  // then resumes Spotify's last context); otherwise "Open Spotify".
  boolean player=listening && s.ready() && s.signIn()==null;
  View hint=findViewWithTag("open");if(hint!=null)hint.setVisibility(player?GONE:VISIBLE);
  if(!player)return;
  String t=s.title()==null?HuText.get("nothing_playing"):s.title();
  title.setText(t);String error=s.playbackError();artist.setText(error!=null?error:s.artist()==null?"":s.artist());artist.setTextColor(error!=null?0xffe22134:secondary);
  boolean on=s.playing();play.setImageDrawable(HuControls.icon(getContext(),on?"encore_icon_pause_24":"encore_icon_play_24",bg));play.setContentDescription(HuText.get(on?"pause":"play"));
  String a=s.art();
  if(a==null){art=null;cover.setImageDrawable(null);}
  else if(!a.equals(art)){art=a;ViewGroup.LayoutParams lp=cover.getLayoutParams();int size=Math.max(dp(64),lp==null?0:lp.width);HuCovers.load(getContext(),a,size,(b,color)->{if(a.equals(art))cover.setImageBitmap(b);});}
  update();
 }
 private void update(){
  if(progress==null)return;long d=listening?session.duration():0,at=listening?Math.min(session.position(),d>0?d:Long.MAX_VALUE):0;
  progress.fraction=d>0?Math.min(1f,at/(float)d):0f;progress.invalidate();
  elapsed.setText(HuControls.time(at));remaining.setText("-"+HuControls.time(Math.max(0,d-at)));
 }

 /** A thin rounded progress line over a faint track, centred in its box, always left to right. */
 private final class Progress extends View {
  float fraction;int line;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  Progress(){super(PanelPlayerView.this.getContext());}
  protected void onDraw(Canvas c){
   float h=line>0?line:dp(3),y=(getHeight()-h)/2f,w=getWidth();if(w<=0)return;
   paint.setColor(text);paint.setAlpha(night?70:46);c.drawRoundRect(0,y,w,y+h,h/2,h/2,paint);
   if(fraction>0){paint.setColor(text);c.drawRoundRect(0,y,Math.max(h,w*fraction),y+h,h/2,h/2,paint);}
  }
 }
}
