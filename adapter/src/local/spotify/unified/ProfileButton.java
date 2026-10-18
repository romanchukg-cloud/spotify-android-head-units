package local.spotify.unified;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
public final class ProfileButton {
 private static final String TAG="local.driver.profiles.button";
 public static final String ACK="local.profile.launch.ack";
 public static void install(Activity a){
  String name=a.getClass().getName();
  if(name.equals("local.bydui.com.vivid.spotify.MainActivity") || a instanceof HuPlayerActivity){
   ResultReceiver ack=a.getIntent().getParcelableExtra(ACK);
   if(ack!=null){a.getIntent().removeExtra(ACK);ack.send(1,null);}
   if(a.getIntent().getBooleanExtra("local.profile.qr",false)){
    a.getIntent().removeExtra("local.profile.qr");
    if(a instanceof HuPlayerActivity)((HuPlayerActivity)a).signIn();else openLogin(a);
   }
   return;
  }
  ViewGroup content=a.findViewById(android.R.id.content);
  if(content==null || content.findViewWithTag(TAG)!=null)return;
  if(name.equals("com.spotify.automotive.settingspage.SettingsPageActivity")){
   ScrollView scroll=findScroll(content);
   if(scroll!=null && scroll.getChildAt(0) instanceof LinearLayout){
    LinearLayout list=(LinearLayout)scroll.getChildAt(0);
    Button entry=button(a);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
    p.setMargins(0,dp(a,20),0,dp(a,20));
    list.addView(entry,Math.min(1,list.getChildCount()),p);
   }
  }else if(name.equals("com.spotify.automotive.loginv2.view.QrCodeLoginActivityV2")){
   // Use the native help row as an inset-aware anchor. Do not shrink the QR
   // viewport: some SDK layouts measure its image beyond a reduced scroll area (fitQr only grows it).
   int helpId=a.getResources().getIdentifier("help_button","id",a.getPackageName());
   View help=a.findViewById(helpId);
   if(help!=null && content instanceof FrameLayout){
    Button entry=button(a);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-2,-2);
    // As tall as the help button it sits beside: its text size and no vertical padding, so the label is not cut.
    if(help instanceof TextView)entry.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,((TextView)help).getTextSize());
    entry.setMinHeight(0);entry.setMinimumHeight(0);entry.setPadding(dp(a,16),0,dp(a,16),0);
    fitQr(a);
    content.addView(entry,p);
    Runnable place=()->{
     if(a.isDestroyed())return;
     int[] origin=new int[2],anchor=new int[2],parent=new int[2];content.getLocationInWindow(origin);help.getLocationInWindow(anchor);
     ((View)help.getParent()).getLocationInWindow(parent);
     int left=Math.max(dp(a,24),parent[0]-origin[0]);int top=anchor[1]-origin[1];
     int width=Math.min(dp(a,260),anchor[0]-origin[0]-left-dp(a,16));
     android.graphics.Rect visible=new android.graphics.Rect();
     entry.setVisibility(help.isShown() && help.getGlobalVisibleRect(visible) && top>=0 && width>0?View.VISIBLE:View.INVISIBLE);
     if(p.leftMargin!=left || p.topMargin!=top || p.width!=width || p.height!=help.getHeight()){
      p.leftMargin=left;p.topMargin=top;p.width=width;p.height=help.getHeight();entry.setLayoutParams(p);
     }
    };
    content.getViewTreeObserver().addOnGlobalLayoutListener(()->place.run());
    content.getViewTreeObserver().addOnScrollChangedListener(()->place.run());
    content.post(place);
   }
  }
 }
 /**
  * Short screens (a split screen, displays about 600 dp tall): the sign-in card is cut at its
  * bottom edge and the QR code with it. The card first gets small outer margins and, if the code
  * still does not fit, loses the Spotify logo above the tabs; the scroll area only grows.
  */
 private static void fitQr(Activity a){
  View card=find(a,"login_content"),logo=find(a,"login_logo"),qr=find(a,"scannable_qr_code");
  if(card==null || qr==null)return;
  int[] step={0};
  card.getViewTreeObserver().addOnGlobalLayoutListener(()->{
   if(a.isDestroyed() || step[0]>=2 || !qr.isShown() || qr.getHeight()==0)return;
   int scrolled=0;for(View v=qr;v!=null && v!=card;v=v.getParent() instanceof View?(View)v.getParent():null)if(v instanceof ScrollView)scrolled+=v.getScrollY();
   int[] q=new int[2],c=new int[2];qr.getLocationInWindow(q);card.getLocationInWindow(c);
   if(q[1]+scrolled+qr.getHeight()<=c[1]+card.getHeight())return;
   if(step[0]==0){
    step[0]=1;
    if(card.getLayoutParams() instanceof ViewGroup.MarginLayoutParams){ViewGroup.MarginLayoutParams m=(ViewGroup.MarginLayoutParams)card.getLayoutParams();int small=dp(a,12);
     if(m.topMargin>small || m.bottomMargin>small){m.topMargin=Math.min(m.topMargin,small);m.bottomMargin=Math.min(m.bottomMargin,small);card.setLayoutParams(m);return;}}
   }
   step[0]=2;if(logo!=null && logo.getVisibility()==View.VISIBLE)logo.setVisibility(View.GONE);
  });
 }
 private static View find(Activity a,String name){int id=a.getResources().getIdentifier(name,"id",a.getPackageName());return id==0?null:a.findViewById(id);}
 private static ScrollView findScroll(View view){
  if(view instanceof ScrollView)return (ScrollView)view;
  if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;
   for(int i=0;i<group.getChildCount();i++){ScrollView found=findScroll(group.getChildAt(i));if(found!=null)return found;}
  }return null;
 }
 private static int dp(Activity a,int n){return (int)(n*a.getResources().getDisplayMetrics().density);}
 private static Button button(Activity a){
  Button button=new Button(a);button.setTag(TAG);button.setText(ProfileText.get("profiles"));button.setAllCaps(false);
  button.setTextSize(22);button.setTextColor(Color.WHITE);button.setMinHeight(dp(a,64));button.setPadding(dp(a,20),dp(a,12),dp(a,20),dp(a,12));
  GradientDrawable shape=new GradientDrawable();shape.setColor(0xff242424);shape.setCornerRadius(dp(a,12));shape.setStroke(dp(a,1),0xff1db954);button.setBackground(shape);
  button.setOnClickListener(v->a.startActivity(new Intent(a,ProfilesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
  return button;
 }
 private static void openLogin(Activity a){
  Handler main=new Handler(Looper.getMainLooper());
  main.postDelayed(new Runnable(){int attempts;public void run(){
   if(a.isFinishing() || a.isDestroyed())return;
   try{
    java.lang.reflect.Field field=a.getClass().getDeclaredField("G");field.setAccessible(true);Object manager=field.get(a);
    if(manager!=null && Boolean.TRUE.equals(manager.getClass().getMethod("p",Activity.class).invoke(manager,a)))return;
   }catch(ReflectiveOperationException | RuntimeException ignored){}
   if(++attempts<180)main.postDelayed(this,250);
   else Toast.makeText(a,ProfileText.get("loginRetry"),Toast.LENGTH_LONG).show();
  }},500);
 }
}
