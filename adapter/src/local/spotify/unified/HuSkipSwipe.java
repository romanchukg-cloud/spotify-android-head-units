package local.spotify.unified;
import android.view.*;
/**
 * A sideways swipe that switches tracks, for the mini-player and the player: the content (the
 * cover, and in the mini-player the title) follows the finger; far or fast enough, a swipe to the
 * left plays the next track and one to the right the previous track. The direction is the same in
 * right-to-left languages, as the controls stay left-to-right. The owning view passes it every
 * event from onInterceptTouchEvent and onTouchEvent; once it returns true the swipe owns the touch.
 */
final class HuSkipSwipe {
 private final HuUi ui;private final HuSession session;private final View[] content;private final int slop;
 private float downX,downY,from;private boolean active;private VelocityTracker velocity;
 HuSkipSwipe(HuUi ui,HuSession session,View... content){
  this.ui=ui;this.session=session;this.content=content;slop=ViewConfiguration.get(ui.activity).getScaledTouchSlop();
 }
 boolean active(){return active;}
 boolean touch(MotionEvent e){
  switch(e.getActionMasked()){
   case MotionEvent.ACTION_DOWN:
    downX=e.getRawX();downY=e.getRawY();active=false;if(velocity!=null)velocity.recycle();velocity=VelocityTracker.obtain();add(e);return false;
   case MotionEvent.ACTION_MOVE:{
    add(e);float dx=e.getRawX()-downX,dy=e.getRawY()-downY;
    if(!active && Math.abs(dx)>slop*2 && Math.abs(dx)>=Math.abs(dy)*1.5f){active=true;from=dx;for(View v:content)v.animate().cancel();}
    if(active)follow(dx-from);
    return active;
   }
   case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:{
    add(e);boolean was=active;active=false;float speed=0;
    if(velocity!=null){velocity.computeCurrentVelocity(1000);speed=velocity.getXVelocity();velocity.recycle();velocity=null;}
    if(was)finish(e.getRawX()-downX-from,speed,e.getActionMasked()==MotionEvent.ACTION_UP);
    return was;
   }
  }
  return active;
 }
 /** The velocity in screen coordinates: the content moves with the finger. */
 private void add(MotionEvent e){
  if(velocity==null)return;
  MotionEvent m=MotionEvent.obtain(e);m.setLocation(e.getRawX(),e.getRawY());velocity.addMovement(m);m.recycle();
 }
 private void follow(float t){
  float alpha=Math.max(0.3f,1-Math.abs(t)/ui.dp(320));
  for(View v:content){v.setTranslationX(t);v.setAlpha(alpha);}
 }
 private void finish(float t,float speed,boolean up){
  boolean fling=Math.abs(speed)>ui.dp(800) && Math.signum(speed)==Math.signum(t) && Math.abs(t)>slop;
  if(!up || !(Math.abs(t)>ui.dp(96) || fling)){for(View v:content)v.animate().translationX(0).alpha(1).setDuration(160).start();return;}
  boolean next=t<0;if(next)session.next();else session.previous();
  // Out on the side of the swipe, back in from the other side.
  View parent=(View)content[0].getParent();float out=(parent!=null?parent.getWidth():ui.dp(400))*(next?-1:1);
  for(View v:content)v.animate().translationX(out).alpha(0).setDuration(140).withEndAction(()->{
   v.setTranslationX(-Math.signum(out)*ui.dp(48));v.animate().translationX(0).alpha(1).setDuration(200).start();
  }).start();
 }
}
