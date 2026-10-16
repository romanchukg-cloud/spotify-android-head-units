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
     boolean cover=true;try{android.os.Bundle settings=HeadUnitSettings.call(a,"get",null);if(HeadUnitSettings.stale(a)){HeadUnitSettings.restartPlayer(a);return;}cover=settings.getBoolean("cover_accent",true);}catch(RuntimeException ignored){}
     CoverAccent.start(a,cover);HeadUnitShell.install(a);
    }
    if(a.getClass().getName().equals("local.bydui.com.vivid.spotify.MainActivity") && playerTask!=a.getTaskId()){
     playerTask=a.getTaskId();
     boolean switching=a.getIntent().hasExtra(ProfileButton.ACK) || a.getIntent().getBooleanExtra("local.profile.qr",false);
     if(!switching){try{HeadUnitSettings.call(a,"ui_open",null);}catch(RuntimeException ignored){}DiagnosticNoticeActivity.maybeShow(a);UpdateDialog.maybeOffer(a);}
    }
    ProfileButton.install(a);
    String own=a.getClass().getName();
    if(own.equals(DiagnosticActivity.class.getName()) || own.equals(DiagnosticNoticeActivity.class.getName()) || own.equals(ProfilesActivity.class.getName())){
     try{if(HeadUnitStyle.modern(HeadUnitSettings.call(a,"get",null).getString("ui_style",HeadUnitStyle.NEW)))HuUi.adopt(a);}catch(RuntimeException ignored){}
    }
   }
   public void onActivityCreated(Activity a,android.os.Bundle b){DiagnosticLog.init(a);}public void onActivityStarted(Activity a){}public void onActivityPaused(Activity a){CoverAccent.stop(a);}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,android.os.Bundle b){}public void onActivityDestroyed(Activity a){}
  });return app;
 }
}
