package local.spotify.unified;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
/**
 * Our mini-player: a rounded card with the controls first, then like, cover and title (the New
 * style order), tinted with the cover's colour and a thin progress line. Tapping the cover or the
 * title opens the player. The controls and the progress line stay left-to-right in right-to-left
 * languages, as media controls do in Material.
 */
final class HuMiniPlayer extends LinearLayout implements HuSession.Listener {
 private final HuUi ui;private final HuSession session;private final ImageView play,like,cover;private final TextView title,artist;
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
  cover=new ImageView(ui.activity);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setBackground(HuUi.round(ui.control,ui.dp(8)));cover.setClipToOutline(true);
  LayoutParams c=new LayoutParams(ui.dp(76),ui.dp(76));c.setMarginStart(ui.dp(24));addView(cover,c);
  LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(VERTICAL);texts.setPaddingRelative(ui.dp(16),0,0,0);
  title=HuControls.title(ui,21,1);artist=ui.label("",17,ui.secondary,false);artist.setSingleLine(true);artist.setEllipsize(android.text.TextUtils.TruncateAt.END);
  texts.addView(title);texts.addView(artist);addView(texts,new LayoutParams(0,-2,1));
  cover.setOnClickListener(v->expand.run());texts.setOnClickListener(v->expand.run());
  progress.setColor(ui.text);track.setColor(ui.text);track.setAlpha(46);
 }
 protected void onAttachedToWindow(){super.onAttachedToWindow();session.add(this);}
 protected void onDetachedFromWindow(){session.remove(this);removeCallbacks(tick);super.onDetachedFromWindow();}
 public void changed(HuSession s){
  String t=s.title();title.setText(t==null?HuText.get("nothing_playing"):t);artist.setText(s.artist()==null?"":s.artist());
  HuControls.playing(ui,play,s.playing());HuControls.custom(ui,like,s.like(),s.liked());
  String a=s.art();if(a==null?art!=null:!a.equals(art)){art=a;HuCovers.load(getContext(),a,ui.dp(76),(b,color)->{if(java.util.Objects.equals(a,art)){cover.setImageBitmap(b);tint(color);}});}
  update();removeCallbacks(tick);if(s.playing())postDelayed(tick,1000);
 }
 private void tint(Integer color){
  if(!accent)color=null;
  int t=color==null?ui.card:CoverAccent.tone(color,ui.dark,0.42f);if(t==tint)return;tint=t;
  ((GradientDrawable)getBackground()).setColor(t);
 }
 private void update(){long d=session.duration();fraction=d>0?Math.min(1f,session.position()/(float)d):0f;invalidate();}
 /** The progress: a thin rounded bar inside the card, clear of its rounded corners, over a faint track. */
 protected void dispatchDraw(Canvas c){
  super.dispatchDraw(c);float h=ui.dp(3),inset=ui.dp(22),bottom=getHeight()-ui.dp(6),width=getWidth()-2*inset;if(width<=0)return;
  c.drawRoundRect(inset,bottom-h,inset+width,bottom,h/2,h/2,track);
  if(fraction>0)c.drawRoundRect(inset,bottom-h,inset+Math.max(h,width*fraction),bottom,h/2,h/2,progress);
 }
}
