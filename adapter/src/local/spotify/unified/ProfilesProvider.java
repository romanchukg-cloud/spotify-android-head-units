package local.spotify.unified;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
/** Private provider; only the other processes of this installed APK may call. */
public final class ProfilesProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 public Bundle call(String method,String arg,Bundle extras){
  if(Binder.getCallingUid()!=android.os.Process.myUid())throw new SecurityException("Private profiles");
  try{return DriverProfiles.call(getContext(),method,arg,extras);}
  catch(Exception e){
   Throwable cause=e;while(cause instanceof java.lang.reflect.InvocationTargetException && cause.getCause()!=null)cause=cause.getCause();
   android.util.Log.w("SpotifyProfiles","Profile operation failed: "+method+" ("+cause.getClass().getSimpleName()+")");
   Bundle b=new Bundle();b.putBoolean("ready",true);b.putBoolean("error",true);return b;
  }
 }
 public Cursor query(Uri u,String[] p,String s,String[] a,String o){throw new UnsupportedOperationException();}
 public String getType(Uri u){return null;}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
 public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
