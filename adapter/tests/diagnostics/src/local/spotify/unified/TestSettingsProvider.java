package local.spotify.unified;
import android.content.*;import android.database.Cursor;import android.net.Uri;import android.os.*;
/** Test-only private bridge for the production Support UI, with mandatory fake delivery. */
public final class TestSettingsProvider extends ContentProvider {
 static int failures;
 public boolean onCreate(){DiagnosticLog.init(getContext());return true;}
 public Bundle call(String method,String arg,Bundle b){if(Binder.getCallingUid()!=android.os.Process.myUid())throw new SecurityException();try{
  if(method.equals("diagnostics_notice"))return AutomaticDiagnostics.state(getContext());
  if(method.equals("diagnostics_failure")){failures++;return new Bundle();} // Trigger is inspected; never schedule a real network worker in fixtures.
  if(method.equals("diagnostics_continue")){AutomaticDiagnostics.accept(getContext(),b.getBoolean("auto_choice"));return AutomaticDiagnostics.state(getContext());}
  if(method.equals("diagnostics_auto")){AutomaticDiagnostics.choice(getContext(),b.getBoolean("auto_choice"));return AutomaticDiagnostics.state(getContext());}
  if(method.equals("diagnostics_status"))return DiagnosticsQueue.summary(getContext());
  if(method.equals("diagnostics_preview")){Bundle out=new Bundle();out.putString("body",DiagnosticReport.preview(getContext(),b.getString("category","other")));return out;}
  if(method.equals("diagnostics_send")){String id=DiagnosticsQueue.approve(getContext(),b.getString("body"),b.getBoolean("public_consent"));DiagnosticsQueue.process(getContext(),new FakeDiagnosticTransport());Bundle out=new Bundle();out.putString("id",id);return out;}
  if(method.equals("diagnostics_export")){if(b!=null && b.containsKey("id"))b.putString("body",DiagnosticsQueue.body(getContext(),b.getString("id")));return DiagnosticReport.create(getContext(),b);}
  if(method.equals("diagnostics_cancel")){DiagnosticsQueue.cancel(getContext(),b.getString("id"));return DiagnosticsQueue.summary(getContext());}
  if(method.equals("diagnostics_remove")){DiagnosticsQueue.remove(getContext(),b.getString("id"));return DiagnosticsQueue.summary(getContext());}
  throw new IllegalArgumentException();
 }catch(Exception e){throw new IllegalStateException("Synthetic bridge failure");}}
 public Cursor query(Uri u,String[] p,String s,String[] a,String o){return null;}public String getType(Uri u){return null;}public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){return 0;}public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
}
