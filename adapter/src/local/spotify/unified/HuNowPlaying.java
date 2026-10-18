package local.spotify.unified;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
/**
 * Our player: a large cover, title, artist and like, a seek bar with times and the controls
 * (shuffle, previous, play/pause, next, repeat), on the blurred cover (with "Colour from cover
 * art"; plain otherwise). Wide windows put the controls on the driver's side and the cover at the
 * far end (right, or left in right-to-left languages, by layout mirroring); narrow ones put the
 * cover above them. The cover takes only the room the texts and controls leave, so a split screen
 * keeps the controls in view; small windows get smaller controls. The seek bar, times and controls
 * stay left-to-right in right-to-left languages, as media controls do in Material. The top bar
 * collapses the player and opens the sleep timer, Jam and the queue; a swipe down collapses it
 * too.
 */
final class HuNowPlaying extends LinearLayout implements HuSession.Listener {
 private final HuUi ui;private final HuSession session;private final boolean accent;private final Runnable collapse;
 private final ImageView cover,play,like,shuffle,repeat,sleep;private final TextView title,artist,elapsed,remaining;private final SeekBar seek;
 private final GradientDrawable background;private String art;private boolean dragging;
 private final int slop;private float downX,downY;private boolean swiping;private VelocityTracker velocity;
 private final Runnable tick=new Runnable(){public void run(){progress();if(isAttachedToWindow() && session.playing())postDelayed(this,500);}};
 /** width and height: the room the player gets, in dp. */
 HuNowPlaying(HuUi ui,boolean accent,float width,float height,Runnable collapse,Runnable queue){
  super(ui.activity);this.ui=ui;this.accent=accent;this.collapse=collapse;session=HuSession.get(ui.activity);
  slop=ViewConfiguration.get(ui.activity).getScaledTouchSlop();
  boolean wide=width>height*1.2f;
  // The column's width decides the size of the controls: full ones need about 460 dp, compact
  // ones 380, small ones 320.
  float column=wide?(width-56)/1.7f-48:width-56;boolean compact=column<480 || height<480,small=column<380;
  setOrientation(VERTICAL);setPaddingRelative(ui.dp(small?16:24),ui.dp(8),ui.dp(small?16:32),ui.dp(compact?12:24));setClickable(true);
  background=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{ui.bg,ui.bg});background.setCornerRadius(ui.dp(22));setBackground(background);
  setOutlineProvider(ViewOutlineProvider.BACKGROUND);setClipToOutline(true);
  // Top bar: collapse, then the sleep timer, Jam and the queue.
  int bar=ui.dp(compact?64:76);
  LinearLayout top=new LinearLayout(ui.activity);top.setGravity(Gravity.CENTER_VERTICAL);
  top.addView(HuControls.button(ui,"encore_icon_chevron_down_24",HuText.get("collapse"),bar,v->collapse.run()));
  TextView label=ui.label(HuText.get("now_playing"),18,ui.secondary,true);label.setPaddingRelative(ui.dp(8),0,0,0);top.addView(label,new LayoutParams(0,-2,1));
  sleep=HuControls.button(ui,"encore_icon_moon_24",HuText.get("sleep_timer"),bar,v->HuSleep.menu(ui,session,this::sleepState));top.addView(sleep);
  top.addView(HuControls.button(ui,"encore_icon_group_session_24",HuText.get("start_jam"),bar,v->HeadUnitJam.open(getContext())));
  top.addView(HuControls.button(ui,"encore_icon_queue_24",HuText.get("queue"),bar,v->queue.run()));
  addView(top);
  LinearLayout body=new LinearLayout(ui.activity);body.setOrientation(wide?HORIZONTAL:VERTICAL);body.setGravity(Gravity.CENTER);
  addView(body,new LayoutParams(-1,0,1));
  // The cover is a square as large as its box allows.
  FrameLayout box=new FrameLayout(ui.activity);
  cover=new HuUi.Cover(ui,ui.dp(14)){protected void onMeasure(int w,int h){super.onMeasure(w,h);int s=Math.min(ui.dp(640),Math.min(getMeasuredWidth(),getMeasuredHeight()));setMeasuredDimension(s,s);}};
  box.addView(cover,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));
  LinearLayout col=new LinearLayout(ui.activity);col.setOrientation(VERTICAL);col.setGravity(Gravity.CENTER_VERTICAL);
  if(wide){col.setPaddingRelative(0,0,ui.dp(48),0);body.addView(col,new LayoutParams(0,-1,1));body.addView(box,new LayoutParams(0,-1,0.7f));}
  else{LayoutParams b=new LayoutParams(-1,0,1);b.topMargin=ui.dp(compact?4:12);body.addView(box,b);col.setPadding(0,ui.dp(compact?12:24),0,0);body.addView(col,new LayoutParams(-1,-2));}
  // Title and artist with like at the end.
  LinearLayout heading=new LinearLayout(ui.activity);heading.setGravity(Gravity.CENTER_VERTICAL);
  LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(VERTICAL);
  title=HuControls.title(ui,compact?26:34,2);artist=ui.label("",compact?18:22,ui.secondary,false);artist.setSingleLine(true);artist.setEllipsize(android.text.TextUtils.TruncateAt.END);
  texts.addView(title);texts.addView(artist);heading.addView(texts,new LayoutParams(0,-2,1));
  like=HuControls.button(ui,"encore_icon_heart_24","",bar,v->session.send(session.like()));heading.addView(like);
  col.addView(heading);
  // Seek bar and times.
  seek=new SeekBar(ui.activity);seek.setMax(1000);
  android.content.res.ColorStateList ink=android.content.res.ColorStateList.valueOf(ui.text);
  seek.setProgressTintList(ink);seek.setThumbTintList(ink);seek.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(ui.secondary));
  seek.setMinimumHeight(ui.dp(48));
  seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
   public void onStartTrackingTouch(SeekBar s){dragging=true;}
   public void onProgressChanged(SeekBar s,int p,boolean user){if(user)elapsed.setText(HuControls.time(session.duration()*p/1000));}
   public void onStopTrackingTouch(SeekBar s){dragging=false;session.seek(session.duration()*s.getProgress()/1000);}
  });
  seek.setLayoutDirection(LAYOUT_DIRECTION_LTR);LayoutParams seekParams=new LayoutParams(-1,ui.dp(48));seekParams.topMargin=ui.dp(compact?8:24);col.addView(seek,seekParams);
  LinearLayout times=new LinearLayout(ui.activity);elapsed=ui.label("0:00",16,ui.secondary,false);remaining=ui.label("0:00",16,ui.secondary,false);remaining.setGravity(Gravity.END);
  times.setLayoutDirection(LAYOUT_DIRECTION_LTR);times.setPadding(ui.dp(16),0,ui.dp(16),0);times.addView(elapsed,new LayoutParams(0,-2,1));times.addView(remaining,new LayoutParams(0,-2,1));col.addView(times);
  // Controls.
  int side=ui.dp(small?56:compact?64:76),skip=ui.dp(small?60:compact?72:84),main=ui.dp(small?72:compact?80:96),gap=ui.dp(small?8:compact?12:20);
  LinearLayout controls=new LinearLayout(ui.activity);controls.setGravity(Gravity.CENTER);controls.setLayoutDirection(LAYOUT_DIRECTION_LTR);
  shuffle=HuControls.button(ui,"encore_icon_shuffle_24",HuText.get("shuffle"),side,v->session.send(session.shuffle()));
  play=HuControls.play(ui,main,v->session.playPause());
  repeat=HuControls.button(ui,"encore_icon_repeat_24",HuText.get("repeat"),side,v->session.cycleRepeat());
  controls.addView(shuffle);controls.addView(HuControls.button(ui,"encore_icon_skip_back_24",HuText.get("previous"),skip,v->session.previous()));
  ((LayoutParams)play.getLayoutParams()).setMargins(gap,0,gap,0);controls.addView(play);
  controls.addView(HuControls.button(ui,"encore_icon_skip_forward_24",HuText.get("next"),skip,v->session.next()));controls.addView(repeat);
  // Very small windows keep previous, play and next only.
  if(column<330){shuffle.setVisibility(GONE);repeat.setVisibility(GONE);}
  LayoutParams controlsParams=new LayoutParams(-1,-2);controlsParams.topMargin=ui.dp(compact?4:16);col.addView(controls,controlsParams);
 }
 protected void onAttachedToWindow(){super.onAttachedToWindow();session.add(this);sleepState();}
 /** The moon is green while the sleep timer runs. */
 private void sleepState(){boolean on=HuSleep.left(getContext())>0;sleep.setImageDrawable(HuControls.icon(getContext(),on?"encore_icon_moon_active_24":"encore_icon_moon_24",on?ui.accent:ui.text));}
 protected void onDetachedFromWindow(){session.remove(this);removeCallbacks(tick);super.onDetachedFromWindow();}

 // A downward swipe anywhere outside the seek bar drags the player down; far or fast enough, it collapses.
 public boolean onInterceptTouchEvent(MotionEvent e){
  switch(e.getActionMasked()){
   case MotionEvent.ACTION_DOWN:downX=e.getRawX();downY=e.getRawY();swiping=false;track(e,true);break;
   case MotionEvent.ACTION_MOVE:track(e,false);if(begin(e))return true;break;
   case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:release();break;
  }
  return false;
 }
 public boolean onTouchEvent(MotionEvent e){
  switch(e.getActionMasked()){
   case MotionEvent.ACTION_DOWN:downX=e.getRawX();downY=e.getRawY();swiping=false;track(e,true);super.onTouchEvent(e);return true;
   case MotionEvent.ACTION_MOVE:track(e,false);if(swiping || begin(e))setTranslationY(Math.max(0,e.getRawY()-downY));return true;
   case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:
    track(e,false);boolean was=swiping;float speed=0;
    if(velocity!=null){velocity.computeCurrentVelocity(1000);speed=velocity.getYVelocity();}
    release();
    if(was){if(e.getActionMasked()==MotionEvent.ACTION_UP && (getTranslationY()>getHeight()*0.25f || speed>ui.dp(1200)))collapse.run();else animate().translationY(0).setDuration(160).start();}
    else super.onTouchEvent(e);
    return true;
  }
  return super.onTouchEvent(e);
 }
 private boolean begin(MotionEvent e){
  float dx=e.getRawX()-downX,dy=e.getRawY()-downY;
  if(swiping || dy<=slop || dy<Math.abs(dx)*1.5f)return swiping;
  swiping=true;downY=e.getRawY();animate().cancel();return true;
 }
 /** The velocity in screen coordinates: the view itself moves with the finger. */
 private void track(MotionEvent e,boolean reset){
  if(reset || velocity==null){if(velocity!=null)velocity.recycle();velocity=VelocityTracker.obtain();}
  MotionEvent m=MotionEvent.obtain(e);m.setLocation(e.getRawX(),e.getRawY());velocity.addMovement(m);m.recycle();
 }
 private void release(){swiping=false;if(velocity!=null){velocity.recycle();velocity=null;}}

 public void changed(HuSession s){
  // A playback error (Premium required, no connection) shows in place of the artist.
  String t=s.title(),error=s.playbackError();title.setText(t==null?HuText.get("nothing_playing"):t);
  artist.setText(error!=null?error:s.artist()==null?"":s.artist());artist.setTextColor(error!=null?0xffe22134:ui.secondary);
  HuControls.playing(ui,play,s.playing());HuControls.custom(ui,like,s.like(),s.liked());HuControls.custom(ui,shuffle,s.shuffle(),s.shuffled());
  int mode=s.repeat();repeat.setImageDrawable(HuControls.icon(getContext(),mode==HuSession.REPEAT_ONE?"encore_icon_repeat_once_24":"encore_icon_repeat_24",mode==HuSession.REPEAT_NONE?ui.text:ui.accent));
  String a=s.art();
  if(!java.util.Objects.equals(a,art)){art=a;HuCovers.load(getContext(),a,ui.dp(480),(b,color)->{if(java.util.Objects.equals(a,art)){cover.setImageBitmap(b);if(accent && b!=null)setBackground(new HuUi.Blur(ui,b,ui.dp(22),ui.dark?0x99000000:0x99ffffff));else{setBackground(background);tint(color);}}});}
  progress();removeCallbacks(tick);if(s.playing())postDelayed(tick,500);
 }
 private void tint(Integer color){
  int top=!accent || color==null?ui.bg:CoverAccent.tone(color,ui.dark,ui.dark?0.45f:1f);
  background.setColors(new int[]{top,ui.bg});
 }
 private void progress(){
  long d=session.duration(),p=session.position();
  if(!dragging){seek.setProgress(d>0?(int)(p*1000/d):0);elapsed.setText(HuControls.time(p));}
  remaining.setText("-"+HuControls.time(Math.max(0,d-p)));
 }
}
