package local.spotify.unified;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
public final class HeadUnitSettingsProvider extends ContentProvider {
 public boolean onCreate(){HeadUnitPreferences.init(getContext());HeadUnitProfile.configure(getContext());HeadUnitRuntime.init(getContext());return true;}
 public Bundle call(String method,String arg,Bundle extras){
  if(Binder.getCallingUid()!=android.os.Process.myUid())throw new SecurityException("Private settings");
  if("set".equals(method)){HeadUnitPreferences.write(extras);if(!HeadUnitPreferences.stopOnClose())local.spotify.close.PlaybackGate.change(getContext(),false);}
  else if(!"get".equals(method))throw new IllegalArgumentException("Unknown settings operation");
  return HeadUnitPreferences.read();
 }
 public Cursor query(Uri u,String[] p,String s,String[] a,String o){throw new UnsupportedOperationException();}
 public String getType(Uri u){return null;}public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
