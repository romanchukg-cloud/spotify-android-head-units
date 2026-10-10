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
  if(name.equals("local.bydui.com.vivid.spotify.MainActivity")){
   ResultReceiver ack=a.getIntent().getParcelableExtra(ACK);
   if(ack!=null){a.getIntent().removeExtra(ACK);ack.send(1,null);}
   if(a.getIntent().getBooleanExtra("local.profile.qr",false)){
    a.getIntent().removeExtra("local.profile.qr");openLogin(a);
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
   // viewport: some SDK layouts measure its image beyond a reduced scroll area.
   int helpId=a.getResources().getIdentifier("help_button","id",a.getPackageName());
   View help=a.findViewById(helpId);
   if(help!=null && content instanceof FrameLayout){
    Button entry=button(a);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-2,-2);
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
