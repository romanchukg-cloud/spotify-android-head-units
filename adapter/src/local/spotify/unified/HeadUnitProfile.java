package local.spotify.unified;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import java.util.Locale;
public final class HeadUnitProfile {
 public enum Kind { BYD, AAOS, GENERIC }
 private static volatile Kind profile;
 private static Boolean vendor;
 public static synchronized boolean isByd(){
  if(vendor==null){
   boolean found=false;
   try{Class.forName("android.hardware.bydauto.instrument.BYDAutoInstrumentDevice");found=true;}catch(ClassNotFoundException|LinkageError ignored){}
   String maker=Build.MANUFACTURER;
   try{maker=(String)Class.forName("android.os.SystemProperties").getMethod("get",String.class).invoke(null,"ro.product.manufacturer");}catch(Exception ignored){}
   vendor=found || maker.toLowerCase(Locale.ROOT).contains("byd");
  }return vendor;
 }
 public static synchronized Kind get(Context c){
  if(profile==null){profile=isByd()?Kind.BYD:c.getPackageManager().hasSystemFeature("android.hardware.type.automotive")?Kind.AAOS:Kind.GENERIC;Log.i("SpotifyHU","Device profile="+profile);}
  return profile;
 }
 public static void configure(Context c){
  int wanted=get(c)==Kind.BYD?PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
  ComponentName component=new ComponentName(c.getPackageName(),"local.bydui.com.vivid.spotify.service.MediaManagerService");
  if(c.getPackageManager().getComponentEnabledSetting(component)!=wanted)c.getPackageManager().setComponentEnabledSetting(component,wanted,PackageManager.DONT_KILL_APP);
 }
}
