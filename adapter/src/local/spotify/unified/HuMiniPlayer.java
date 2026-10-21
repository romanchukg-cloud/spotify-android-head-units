package local.spotify.unified;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
/**
 * Our mini-player: a rounded card with the controls first, then like, cover and title (the New
 * style order) and the time (elapsed / length, where the card is wide enough), on the blurred
 * cover (with "Colour from cover art"; plain otherwise) and a thin progress line. Tapping the cover or the title, or a swipe up, opens the player; a swipe to the
 * left or right switches to the next or previous track (HuSkipSwipe). The controls and
 * the progress line stay left-to-right in right-to-left languages, as media controls do in
 * Material.
 */
final class HuMiniPlayer extends LinearLayout implements HuSession.Listener {
 private final HuUi ui;private final HuSession session;private final ImageView play,like,cover;private final TextView title,artist,time;private boolean roomy=true;
 private final Paint progress=new Paint(Paint.ANTI_ALIAS_FLAG),track=new Paint(Paint.ANTI_ALIAS_FLAG);private final boolean accent;private float fraction;private String art;private int tint;
 private final Runnable tick=new Runnable(){public void run(){update();if(isAttachedToWindow() && session.playing())postDelayed(this,1000);}};
 HuMiniPlayer(HuUi ui,boolean accent,Runnable expand){
  super(ui.activity);this.ui=ui;this.accent=accent;session=HuSession.get(ui.activity);setWillNotDraw(false);
  setGravity(Gravity.CENTER_VERTICAL);setMinimumHeight(ui.dp(104));setPaddingRelative(ui.dp(12),ui.dp(10),ui.dp(20),ui.dp(10));
  tint=ui.card;GradientDrawable bg=HuUi.round(tint,ui.dp(22));setBackground(bg);setClipToOutline(true);setOutlineProvider(ViewOutlineProvider.BACKGROUND);
  // Playback controls keep their left-to-right order in right-to-left languages (Material).
  LinearLayout controls=new LinearLayout(ui.activity);controls.setGravity(Gravity.CENTER_VERTICAL);controls.setLayoutDirection(LAYOUT_DIRECTION_LTR);addView(controls);
  controls.addView(HuControls.button(ui,"encore_icon_skip_back_24",HuText.get("previous"),ui.dp(64),v->session.previous()));
  play=HuControls.play(ui,ui.dp(64),v->session.playPause());((LayoutParams)play.getLayoutParams()).setMargins(ui.dp(8),0,ui.dp(8),0);controls.addView(play);
  controls.addView(HuControls.button(ui,"encore_icon_skip_forward_24",HuText.get("next"),ui.dp(64),v->session.next()));
  like=HuControls.button(ui,"encore_icon_heart_24","",ui.dp(64),v->session.send(session.like()));((LayoutParams)like.getLayoutParams()).setMarginStart(ui.dp(12));addView(like);
  cover=new HuUi.Cover(ui,ui.dp(8));
  LayoutParams c=new LayoutParams(ui.dp(76),ui.dp(76));c.setMarginStart(ui.dp(24));addView(cover,c);
  LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(VERTICAL);texts.setPaddingRelative(ui.dp(16),0,0,0);
  title=HuControls.title(ui,21,1);artist=ui.label("",17,ui.secondary,false);artist.setSingleLine(true);artist.setEllipsize(android.text.TextUtils.TruncateAt.END);
  texts.addView(title);texts.addView(artist);addView(texts,new LayoutParams(0,-2,1));
  // Tabular digits, so the time does not shift as it counts; always left-to-right.
  time=ui.label("",17,ui.secondary,false);time.setFontFeatureSettings("tnum");time.setTextDirection(TEXT_DIRECTION_LTR);time.setVisibility(GONE);
  LayoutParams tp=new LayoutParams(-2,-2);tp.setMarginStart(ui.dp(16));addView(time,tp);
  cover.setOnClickListener(v->expand.run());texts.setOnClickListener(v->expand.run());this.expand=expand;slop=android.view.ViewConfiguration.get(ui.activity).getScaledTouchSlop();
  skip=new HuSkipSwipe(ui,session,cover,texts);
  progress.setColor(ui.text);track.setColor(ui.text);track.setAlpha(46);
  // Right-hand drive mirrors the card: the controls on the right, the title next to the cover.
  if(ui.rhd)setLayoutDirection(ui.activity.getResources().getConfiguration().getLayoutDirection()==LAYOUT_DIRECTION_RTL?LAYOUT_DIRECTION_LTR:LAYOUT_DIRECTION_RTL);
 }
 // A swipe up anywhere on the card opens the player, as a tap on the cover or title does.
 private final Runnable expand;private final int slop;private float downX,downY;private final HuSkipSwipe skip;
 public boolean onInterceptTouchEvent(MotionEvent e){
  if(skip.touch(e))return true;
  if(e.getActionMasked()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();return false;}
  if(e.getActionMasked()==MotionEvent.ACTION_MOVE){float dx=e.getRawX()-downX,dy=downY-e.getRawY();if(dy>slop*2 && dy>Math.abs(dx)*1.5f){expand.run();return true;}}
  return false;
 }
 public boolean onTouchEvent(MotionEvent e){
  if(skip.touch(e))return true;
  if(e.getActionMasked()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();return true;}
  if(e.getActionMasked()==MotionEvent.ACTION_MOVE){float dx=e.getRawX()-downX,dy=downY-e.getRawY();if(dy>slop*2 && dy>Math.abs(dx)*1.5f){expand.run();return true;}}
  return super.onTouchEvent(e);
 }
 protected void onAttachedToWindow(){super.onAttachedToWindow();session.add(this);}
 protected void onDetachedFromWindow(){session.remove(this);removeCallbacks(tick);super.onDetachedFromWindow();}
 public void changed(HuSession s){
  // A playback error (Premium required, no connection) shows in place of the artist.
  String t=s.title(),error=s.playbackError();title.setText(t==null?HuText.get("nothing_playing"):t);
  artist.setText(error!=null?error:s.artist()==null?"":s.artist());artist.setTextColor(error!=null?0xffe22134:ui.secondary);
  HuControls.playing(ui,play,s.playing());HuControls.custom(ui,like,s.like(),s.liked());
  String a=s.art();if(a==null?art!=null:!a.equals(art)){art=a;HuCovers.load(getContext(),a,ui.dp(76),(b,color)->{if(java.util.Objects.equals(a,art)){cover.setImageBitmap(b);if(accent && b!=null)setBackground(new HuUi.Blur(ui,b,ui.dp(22),ui.dark?0xb3000000:0xb3ffffff));else{if(!(getBackground() instanceof GradientDrawable)){tint=0;setBackground(HuUi.round(ui.card,ui.dp(22)));}tint(color);}}});}
  update();removeCallbacks(tick);if(s.playing())postDelayed(tick,1000);
 }
 private void tint(Integer color){
  if(!accent)color=null;
  int t=color==null?ui.card:CoverAccent.tone(color,ui.dark,0.42f);if(t==tint)return;tint=t;
  ((GradientDrawable)getBackground()).setColor(t);
 }
 private void update(){
  long d=session.duration(),p=session.position();fraction=d>0?Math.min(1f,p/(float)d):0f;
  boolean shown=d>0 && roomy;time.setVisibility(shown?VISIBLE:GONE);if(shown)time.setText(HuControls.time(Math.min(p,d))+" / "+HuControls.time(d));
  invalidate();
 }
 /** The time needs a wide card (a full screen or half of one); narrower, the title keeps the room. */
 protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);boolean r=w>=ui.dp(640);if(r!=roomy){roomy=r;post(this::update);}}
 /** The progress: a thin rounded bar inside the card, clear of its rounded corners, over a faint track. */
 protected void dispatchDraw(Canvas c){
  super.dispatchDraw(c);float h=ui.dp(3),inset=ui.dp(22),bottom=getHeight()-ui.dp(6),width=getWidth()-2*inset;if(width<=0)return;
  c.drawRoundRect(inset,bottom-h,inset+width,bottom,h/2,h/2,track);
  if(fraction>0)c.drawRoundRect(inset,bottom-h,inset+Math.max(h,width*fraction),bottom,h/2,h/2,progress);
 }
}
