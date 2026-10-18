package local.spotify.unified;
import android.app.*;
/**
 * The app's processes: the main one runs Spotify's own Application (the music backend); the
 * interface (":bydui", the process name kept from earlier builds), settings and accounts run a
 * plain Application. Every screen gets the diagnostics log; the main screen, once per task, the
 * opening work (auto resume, diagnostics notice, update offer) unless a profile switch opened it.
 */
public final class UnifiedFactory extends AppComponentFactory {
 public Application instantiateApplication(ClassLoader cl,String name) throws InstantiationException,IllegalAccessException,ClassNotFoundException {
  String process=Application.getProcessName();
  boolean own=process!=null && (process.endsWith(":settings") || process.endsWith(":profiles") || process.endsWith(":bydui"));
  Application app=super.instantiateApplication(cl,own?"android.app.Application":name);
  app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks(){
   private int playerTask=-1;
   public void onActivityResumed(Activity a){
    if(a instanceof HuPlayerActivity && playerTask!=a.getTaskId()){
     playerTask=a.getTaskId();
     boolean switching=a.getIntent().hasExtra(ProfileButton.ACK) || a.getIntent().getBooleanExtra("local.profile.qr",false);
     if(!switching){try{HeadUnitSettings.call(a,"ui_open",null);}catch(RuntimeException ignored){}DiagnosticNoticeActivity.maybeShow(a);UpdateDialog.maybeOffer(a);}
    }
    ProfileButton.install(a);
    String own=a.getClass().getName();
    if(own.equals(DiagnosticActivity.class.getName()) || own.equals(DiagnosticNoticeActivity.class.getName()) || own.equals(ProfilesActivity.class.getName())){
     HuUi.adopt(a);
    }
   }
   public void onActivityCreated(Activity a,android.os.Bundle b){DiagnosticLog.init(a);}public void onActivityStarted(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,android.os.Bundle b){}public void onActivityDestroyed(Activity a){}
  });return app;
 }
}
