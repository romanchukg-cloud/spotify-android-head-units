package local.spotify.unified;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
/**
 * The widgets' providers (HuWidgets): they draw the kept state when the launcher places or
 * refreshes a widget, in the interface process, without starting the Spotify backend.
 */
public abstract class HuWidgetProvider extends AppWidgetProvider {
 public void onUpdate(Context c,AppWidgetManager manager,int[] ids){render(c);}
 /** Resized: before Android 12 the layout for the new size is ours to choose (later, the host's). */
 public void onAppWidgetOptionsChanged(Context c,AppWidgetManager manager,int id,android.os.Bundle options){if(android.os.Build.VERSION.SDK_INT<31)render(c);}
 private void render(Context c){
  PendingResult result=goAsync();
  new Thread(()->{try{HuWidgets.render(c.getApplicationContext());}finally{result.finish();}},"hu-widget").start();
 }
}
