package local.spotify.unified;
import android.app.Service;import android.content.Intent;import android.os.IBinder;
public final class TestLogService extends Service {
 public int onStartCommand(Intent intent,int flags,int id){new Thread(()->{DiagnosticLog.init(this);DiagnosticLog.focus("ducked");DiagnosticLog.flush();stopSelf();}).start();return START_NOT_STICKY;}
 public IBinder onBind(Intent intent){return null;}
}
