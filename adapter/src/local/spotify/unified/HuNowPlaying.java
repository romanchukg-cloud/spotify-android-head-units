package local.spotify.unified;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
/**
 * Our player: a large cover, title, artist and like, a seek bar with times and the controls
 * (shuffle, previous, play/pause, next, repeat), on a gradient from the cover's colour. Wide
 * screens put the cover beside the controls, narrow ones above them. The top bar collapses the
 * player and opens the queue editor and Jam.
 */
final class HuNowPlaying extends LinearLayout implements HuSession.Listener {
 private final HuUi ui;private final HuSession session;private final boolean accent;
 private final ImageView cover,play,like,shuffle,repeat;private final TextView title,artist,elapsed,remaining;private final SeekBar seek;
 private final GradientDrawable background;private String art;private boolean dragging;
 private final Runnable tick=new Runnable(){public void run(){progress();if(isAttachedToWindow() && session.playing())postDelayed(this,500);}};
 HuNowPlaying(HuUi ui,boolean accent,Runnable collapse){
  super(ui.activity);this.ui=ui;this.accent=accent;session=HuSession.get(ui.activity);
  setOrientation(VERTICAL);setPadding(ui.dp(24),ui.dp(8),ui.dp(32),ui.dp(24));setClickable(true);
  background=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{ui.bg,ui.bg});setBackground(background);
  // Top bar: collapse, then the queue editor and Jam.
  LinearLayout top=new LinearLayout(ui.activity);top.setGravity(Gravity.CENTER_VERTICAL);
  top.addView(HuControls.button(ui,"encore_icon_chevron_down_24",HuText.get("collapse"),ui.dp(76),v->collapse.run()));
  TextView label=ui.label(HuText.get("now_playing"),18,ui.secondary,true);label.setPadding(ui.dp(8),0,0,0);top.addView(label,new LayoutParams(0,-2,1));
  top.addView(HuControls.button(ui,"encore_icon_group_session_24",HuText.get("start_jam"),ui.dp(76),v->HeadUnitJam.open(getContext())));
  top.addView(HuControls.button(ui,"encore_icon_queue_24",HuText.get("queue"),ui.dp(76),v->getContext().startActivity(new Intent(getContext(),HeadUnitQueueActivity.class))));
  addView(top);
  boolean wide=getResources().getConfiguration().screenWidthDp>getResources().getConfiguration().screenHeightDp*1.2f;
  LinearLayout body=new LinearLayout(ui.activity);body.setOrientation(wide?HORIZONTAL:VERTICAL);body.setGravity(Gravity.CENTER);
  addView(body,new LayoutParams(-1,0,1));
  int size=Math.round(Math.min(getResources().getConfiguration().screenHeightDp*(wide?0.62f:0.4f),getResources().getConfiguration().screenWidthDp*(wide?0.38f:0.7f)));
  cover=new ImageView(ui.activity);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setBackground(HuUi.round(ui.control,ui.dp(14)));cover.setClipToOutline(true);
  body.addView(cover,new LayoutParams(ui.dp(size),ui.dp(size)));
  LinearLayout column=new LinearLayout(ui.activity);column.setOrientation(VERTICAL);column.setGravity(Gravity.CENTER_VERTICAL);
  if(wide)column.setPadding(ui.dp(48),0,0,0);else column.setPadding(0,ui.dp(24),0,0);
  body.addView(column,wide?new LayoutParams(0,-1,1):new LayoutParams(-1,-2));
  // Title and artist with like at the end.
  LinearLayout heading=new LinearLayout(ui.activity);heading.setGravity(Gravity.CENTER_VERTICAL);
  LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(VERTICAL);
  title=HuControls.title(ui,34,2);artist=ui.label("",22,ui.secondary,false);artist.setSingleLine(true);artist.setEllipsize(android.text.TextUtils.TruncateAt.END);
  texts.addView(title);texts.addView(artist);heading.addView(texts,new LayoutParams(0,-2,1));
  like=HuControls.button(ui,"encore_icon_heart_24","",ui.dp(76),v->session.send(session.like()));heading.addView(like);
  column.addView(heading);
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
  LayoutParams seekParams=new LayoutParams(-1,ui.dp(48));seekParams.topMargin=ui.dp(24);column.addView(seek,seekParams);
  LinearLayout times=new LinearLayout(ui.activity);elapsed=ui.label("0:00",16,ui.secondary,false);remaining=ui.label("0:00",16,ui.secondary,false);remaining.setGravity(Gravity.END);
  times.setPadding(ui.dp(16),0,ui.dp(16),0);times.addView(elapsed,new LayoutParams(0,-2,1));times.addView(remaining,new LayoutParams(0,-2,1));column.addView(times);
  // Controls.
  LinearLayout controls=new LinearLayout(ui.activity);controls.setGravity(Gravity.CENTER);
  shuffle=HuControls.button(ui,"encore_icon_shuffle_24",HuText.get("shuffle"),ui.dp(76),v->session.send(session.shuffle()));
  play=HuControls.play(ui,ui.dp(96),v->session.playPause());
  repeat=HuControls.button(ui,"encore_icon_repeat_24",HuText.get("repeat"),ui.dp(76),v->session.cycleRepeat());
  controls.addView(shuffle);controls.addView(HuControls.button(ui,"encore_icon_skip_back_24",HuText.get("previous"),ui.dp(84),v->session.previous()));
  ((LayoutParams)play.getLayoutParams()).setMargins(ui.dp(20),0,ui.dp(20),0);controls.addView(play);
  controls.addView(HuControls.button(ui,"encore_icon_skip_forward_24",HuText.get("next"),ui.dp(84),v->session.next()));controls.addView(repeat);
  LayoutParams controlsParams=new LayoutParams(-1,-2);controlsParams.topMargin=ui.dp(16);column.addView(controls,controlsParams);
 }
 protected void onAttachedToWindow(){super.onAttachedToWindow();session.add(this);}
 protected void onDetachedFromWindow(){session.remove(this);removeCallbacks(tick);super.onDetachedFromWindow();}
 public void changed(HuSession s){
  String t=s.title();title.setText(t==null?HuText.get("nothing_playing"):t);artist.setText(s.artist()==null?"":s.artist());
  HuControls.playing(ui,play,s.playing());HuControls.custom(ui,like,s.like(),s.liked());HuControls.custom(ui,shuffle,s.shuffle(),s.shuffled());
  int mode=s.repeat();repeat.setImageDrawable(HuControls.icon(getContext(),mode==HuSession.REPEAT_ONE?"encore_icon_repeat_once_24":"encore_icon_repeat_24",mode==HuSession.REPEAT_NONE?ui.text:ui.accent));
  String a=s.art();
  if(!java.util.Objects.equals(a,art)){art=a;HuCovers.load(getContext(),a,ui.dp(480),(b,color)->{if(java.util.Objects.equals(a,art)){cover.setImageBitmap(b);tint(color);}});}
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
