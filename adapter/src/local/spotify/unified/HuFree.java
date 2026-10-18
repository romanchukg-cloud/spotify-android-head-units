package local.spotify.unified;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.*;
/**
 * "This app is free": people have sold this app as an official product. The notice says it is
 * free, that a buyer was misled and should ask for the money back, and that it comes only from the
 * official page (address and a QR code to open it on a phone). Shown once per version over our
 * main screen, as a line with a small code on the sign-in panel and on Spotify's QR sign-in
 * screen, and from "About this app" in Head unit settings. A copy signed with another key than
 * the release certificate (the notice can only be removed by changing and re-signing the APK)
 * says instead that it has been changed, every time the main screen opens.
 */
final class HuFree {
 private HuFree(){}
 static final String URL="https://romanchukg-cloud.github.io/spotify-android-head-units/",SHORT="romanchukg-cloud.github.io/spotify-android-head-units";
 /** The terms the notice stands for; kept in the code (and logged for a changed copy) so they travel with it. */
 static final String NOTICE="Spotify for Android Head Units is distributed free of charge from "+URL+". Selling it, or removing or hiding its free-of-charge notice or the check of its signing certificate, is not permitted. It is unofficial and not made by Spotify or BYD.";
 private static Boolean genuine;
 /**
  * Whether this installation is signed with the release certificate. Removing the notice means
  * changing and re-signing the APK with another key, so a changed copy fails this check (and
  * Android refuses our updates over it).
  */
 static boolean genuine(Context c){
  if(genuine!=null)return genuine;
  boolean ok=true;
  try{
   android.content.pm.PackageInfo i=c.getPackageManager().getPackageInfo(c.getPackageName(),android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES|android.content.pm.PackageManager.GET_SIGNATURES);
   android.content.pm.Signature[] s=i.signingInfo!=null?(i.signingInfo.hasMultipleSigners()?i.signingInfo.getApkContentsSigners():i.signingInfo.getSigningCertificateHistory()):i.signatures;
   ok=false;if(s!=null)for(android.content.pm.Signature x:s)if(UpdateFeed.SIGNER.equals(UpdateDownload.hex(java.security.MessageDigest.getInstance("SHA-256").digest(x.toByteArray()))))ok=true;
  }catch(Exception e){android.util.Log.w("SpotifyHU","Signing certificate unreadable",e);ok=true;}
  if(!ok)android.util.Log.w("SpotifyHU","Changed copy (not the release certificate). "+NOTICE);
  genuine=ok;return ok;
 }
 /** The official page as a QR code (version 4, level M; generated and decoded with CoreImage). */
 private static final String[] MODULES={
  "111111100010101010111001001111111",
  "100000100000110100000010101000001",
  "101110101010101100001101001011101",
  "101110101110110011010011101011101",
  "101110101011000011111101001011101",
  "100000101100101101000000001000001",
  "111111101010101010101010101111111",
  "000000001000001110000110100000000",
  "101111100100011101010010101111100",
  "010110001110110010011101001101101",
  "001000111001011100101110010010110",
  "001100011100101110010100100011110",
  "101011100110100111000010110011001",
  "110000000100100010111011001001011",
  "001111111101110110001100011011010",
  "100011011110111100001100011010100",
  "010101101010011011010011110110001",
  "110001001110000010111111011101101",
  "000011110000010110001000001110110",
  "101010011011010100101100010111101",
  "011110100000011001110011110111011",
  "101000010100110010110101101001001",
  "100100100101100100000110000101010",
  "100110010011000010101110101101101",
  "100001111110110001100011111111011",
  "000000001101011011110000100010101",
  "111111100100000110001011101010100",
  "100000101011001010011111100011101",
  "101110101011100101000010111111000",
  "101110101010101011011001010010111",
  "101110101110101111001001001101000",
  "100000100111110110100111010011100",
  "111111101000001101010011101101010"};

 /** The QR code: dark modules on a white square with a 2-module margin. */
 static final class Code extends View {
  private final Paint dark=new Paint();private final Paint light=new Paint(Paint.ANTI_ALIAS_FLAG);private final float corner;
  Code(Context c,float corner){super(c);this.corner=corner;dark.setColor(0xff000000);light.setColor(0xffffffff);setContentDescription(SHORT);}
  protected void onDraw(Canvas c){
   int n=MODULES.length,q=2;float s=Math.min(getWidth(),getHeight())/(float)(n+2*q);float ox=(getWidth()-s*(n+2*q))/2,oy=(getHeight()-s*(n+2*q))/2;
   c.drawRoundRect(ox,oy,ox+s*(n+2*q),oy+s*(n+2*q),corner,corner,light);
   for(int y=0;y<n;y++)for(int x=0;x<n;x++)if(MODULES[y].charAt(x)=='1'){float l=ox+(x+q)*s,t=oy+(y+q)*s;c.drawRect(l,t,l+s+0.5f,t+s+0.5f,dark);}
  }
 }

 /** The notice: title, text, the official page's address and its QR code (beside the text on wide screens). */
 static View notice(HuUi ui,boolean wide){
  LinearLayout box=new LinearLayout(ui.activity);box.setOrientation(wide?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER_VERTICAL);
  LinearLayout texts=new LinearLayout(ui.activity);texts.setOrientation(LinearLayout.VERTICAL);
  boolean ok=genuine(ui.activity);
  TextView title=ui.label(HuText.get(ok?"free_title":"modified_title"),26,ok?ui.text:0xffe22134,true);texts.addView(title);
  TextView body=ui.label(HuText.get(ok?"free_body":"modified_body"),19,ui.text,false);body.setPadding(0,ui.dp(12),0,0);body.setLineSpacing(0,1.15f);texts.addView(body);
  TextView page=ui.label(HuText.get("official_page")+": "+SHORT,16,ui.secondary,false);page.setPadding(0,ui.dp(14),0,0);page.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);texts.addView(page);
  Code code=new Code(ui.activity,ui.dp(10));int size=ui.dp(wide?200:180);
  if(wide){box.addView(texts,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(size,size);cp.setMarginStart(ui.dp(28));box.addView(code,cp);}
  else{box.addView(texts,new LinearLayout.LayoutParams(-1,-2));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(size,size);cp.topMargin=ui.dp(18);cp.gravity=Gravity.CENTER_HORIZONTAL;box.addView(code,cp);}
  return box;
 }
 /** A short line with a small code, for sign-in screens: colours given for the screen behind it. */
 static View line(Context c,float density,int text,int secondary,float textSize){
  LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER_VERTICAL);
  Code code=new Code(c,6*density);int size=Math.round(84*density);row.addView(code,new LinearLayout.LayoutParams(size,size));
  LinearLayout texts=new LinearLayout(c);texts.setOrientation(LinearLayout.VERTICAL);texts.setPaddingRelative(Math.round(14*density),0,0,0);
  TextView a=new TextView(c);a.setText(HuText.get(genuine(c)?"free_short":"modified_short"));a.setTextColor(genuine(c)?text:0xffe22134);a.setTextSize(textSize);a.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);texts.addView(a);
  TextView b=new TextView(c);b.setText(HuText.get("official_page")+": "+SHORT);b.setTextColor(secondary);b.setTextSize(textSize-2);b.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);b.setPadding(0,Math.round(4*density),0,0);texts.addView(b);
  row.addView(texts,new LinearLayout.LayoutParams(-2,-2));
  return row;
 }
 /** The notice as a dialog; version: the app's version line (settings), or null. */
 static void show(Activity a,String version){
  HuUi ui=new HuUi(a);android.content.res.Configuration cf=a.getResources().getConfiguration();boolean wide=cf.screenWidthDp>=640;
  Dialog d=new Dialog(a);d.requestWindowFeature(Window.FEATURE_NO_TITLE);
  LinearLayout card=new LinearLayout(a);card.setOrientation(LinearLayout.VERTICAL);card.setBackground(HuUi.round(ui.card,ui.dp(22)));card.setPadding(ui.dp(32),ui.dp(28),ui.dp(32),ui.dp(20));
  card.addView(notice(ui,wide));
  if(version!=null){TextView v=ui.label(version,15,ui.secondary,false);v.setPadding(0,ui.dp(14),0,0);card.addView(v);}
  TextView ok=ui.label(HuText.get("got_it"),19,0xff000000,true);ok.setGravity(Gravity.CENTER);ok.setPadding(ui.dp(40),0,ui.dp(40),0);ok.setBackground(ui.pressable(0xff1ed760,ui.dp(32)));ok.setOnClickListener(x->d.dismiss());
  LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-2,ui.dp(64));op.topMargin=ui.dp(20);op.gravity=Gravity.END;card.addView(ok,op);
  ScrollView scroll=new ScrollView(a);scroll.addView(card);d.setContentView(scroll);
  Window w=d.getWindow();if(w!=null){w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
   int width=Math.min(ui.dp(wide?820:560),a.getResources().getDisplayMetrics().widthPixels-ui.dp(32));w.setLayout(width,ViewGroup.LayoutParams.WRAP_CONTENT);}
  d.show();
 }
 /** Once per version over the main screen; a changed copy says so every time the screen opens. */
 static void maybeShow(Activity a){
  android.content.SharedPreferences p=a.getSharedPreferences("hu_free",Context.MODE_PRIVATE);
  if(genuine(a) && p.getInt("seen",0)==HeadUnitBuild.VERSION_CODE)return;
  p.edit().putInt("seen",HeadUnitBuild.VERSION_CODE).apply();
  a.getWindow().getDecorView().postDelayed(()->{if(!a.isFinishing() && !a.isDestroyed())show(a,null);},800);
 }
 /**
  * Spotify's QR sign-in screen: one or two lines of text at the bottom (the screen's own QR code is
  * the sign-in code, so no second code here), shown where the sign-in card leaves room for it.
  */
 static void loginScreen(Activity a,FrameLayout content){
  if(content.findViewWithTag("hu_free")!=null)return;float d=a.getResources().getDisplayMetrics().density;
  TextView t=new TextView(a);t.setTag("hu_free");t.setText(HuText.get(genuine(a)?"free_short":"modified_short")+"  ·  "+HuText.get("official_page")+": "+SHORT);
  t.setTextColor(0xe6ffffff);t.setTextSize(15);t.setMaxLines(2);t.setGravity(Gravity.CENTER);t.setBackground(HuUi.round(0xb3000000,14*d));
  int pad=Math.round(8*d);t.setPadding(Math.round(16*d),pad,Math.round(16*d),pad);
  FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);p.bottomMargin=Math.round(6*d);p.leftMargin=p.rightMargin=Math.round(24*d);content.addView(t,p);
  int card=a.getResources().getIdentifier("login_content","id",a.getPackageName());View c=card==0?null:a.findViewById(card);
  content.getViewTreeObserver().addOnGlobalLayoutListener(()->{
   if(c==null || t.getHeight()==0)return;
   int[] cc=new int[2],oc=new int[2];c.getLocationInWindow(cc);content.getLocationInWindow(oc);
   int room=content.getHeight()-(cc[1]-oc[1]+c.getHeight());
   int vis=room>=t.getHeight()+Math.round(10*d)?View.VISIBLE:View.INVISIBLE;if(t.getVisibility()!=vis)t.setVisibility(vis);
  });
 }
}
