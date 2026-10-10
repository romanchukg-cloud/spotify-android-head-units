package local.spotify.close;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
public final class ControlProvider extends ContentProvider {
  public boolean onCreate(){ PlaybackGate.init(getContext()); return true; }
  public Bundle call(String method,String arg,Bundle extras){
    if(!"open".equals(method) && !"close".equals(method)) throw new IllegalArgumentException("Unknown operation");
    if(getContext().getPackageManager().checkSignatures(android.os.Binder.getCallingUid(),android.os.Process.myUid())!=android.content.pm.PackageManager.SIGNATURE_MATCH) throw new SecurityException("Signer mismatch");
    if("open".equals(method)) {
      if(android.os.Binder.getCallingUid()!=android.os.Process.myUid() && !PlaybackGate.trustedLocalUi(getContext(),android.os.Binder.getCallingUid(),getCallingPackage())) throw new SecurityException("Unexpected UI caller");
      PlaybackGate.openForUi(getContext(),extras==null?null:extras.getBinder("uiLease"));
    } else { local.spotify.unified.HeadUnitPreferences.init(getContext()); if(local.spotify.unified.HeadUnitPreferences.stopOnClose()){local.spotify.unified.HeadUnitRuntime.explicitPause(getContext());PlaybackGate.change(getContext(),true);} }
    Bundle b=new Bundle(); b.putBoolean("closed",PlaybackGate.blocked()); return b;
  }
  public Cursor query(Uri u,String[] p,String s,String[] a,String o){throw new UnsupportedOperationException();}
  public String getType(Uri u){return null;}
  public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
  public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
  public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
