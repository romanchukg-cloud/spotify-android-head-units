package local.spotify.unified;
import android.app.*;
public final class UnifiedFactory extends AppComponentFactory {
 public Application instantiateApplication(ClassLoader cl,String name) throws InstantiationException,IllegalAccessException,ClassNotFoundException {
  String process=Application.getProcessName();String actual=process!=null && process.endsWith(":settings")?"android.app.Application":process!=null && process.endsWith(":bydui")?"local.bydui.com.vivid.spotify.byd.BYDApplication":name;
  Application app=super.instantiateApplication(cl,actual);
  app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks(){
   public void onActivityResumed(Activity a){if(a.getClass().getName().equals("local.bydui.com.vivid.spotify.MainActivity")){try{int scale=HeadUnitSettings.call(a,"get",null).getInt("scale",100);if(a.getResources().getConfiguration().densityDpi!=HeadUnitSettings.density(a,scale))a.recreate();}catch(RuntimeException ignored){}}}
   public void onActivityCreated(Activity a,android.os.Bundle b){}public void onActivityStarted(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,android.os.Bundle b){}public void onActivityDestroyed(Activity a){}
  });return app;
 }
}
