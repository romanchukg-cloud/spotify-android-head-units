package local.spotify.unified;
import android.app.*;
public final class UnifiedFactory extends AppComponentFactory {
 public Application instantiateApplication(ClassLoader cl,String name) throws InstantiationException,IllegalAccessException,ClassNotFoundException {
  String process=Application.getProcessName();String actual=process!=null && (process.endsWith(":settings") || process.endsWith(":profiles"))?"android.app.Application":process!=null && process.endsWith(":bydui")?"local.bydui.com.vivid.spotify.byd.BYDApplication":name;
  Application app=super.instantiateApplication(cl,actual);
  app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks(){
   private int playerTask=-1;
   public void onActivityResumed(Activity a){
    if(a.getClass().getName().equals("local.bydui.com.vivid.spotify.MainActivity")){
     try{int scale=HeadUnitSettings.call(a,"get",null).getInt("scale",100);if(a.getResources().getConfiguration().densityDpi!=HeadUnitSettings.density(a,scale)){a.recreate();return;}}catch(RuntimeException ignored){}
    }
    if(a.getClass().getName().equals("local.bydui.com.vivid.spotify.MainActivity") && playerTask!=a.getTaskId()){
     playerTask=a.getTaskId();
     boolean switching=a.getIntent().hasExtra(ProfileButton.ACK) || a.getIntent().getBooleanExtra("local.profile.qr",false);
     if(!switching){try{HeadUnitSettings.call(a,"ui_open",null);}catch(RuntimeException ignored){}DiagnosticNoticeActivity.maybeShow(a);UpdateDialog.maybeOffer(a);}
    }
    ProfileButton.install(a);
   }
   public void onActivityCreated(Activity a,android.os.Bundle b){DiagnosticLog.init(a);}public void onActivityStarted(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,android.os.Bundle b){}public void onActivityDestroyed(Activity a){}
  });return app;
 }
}
