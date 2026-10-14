package local.spotify.unified;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
public final class HeadUnitSettingsProvider extends ContentProvider {
 public boolean onCreate(){HeadUnitIdentity.init(getContext());HeadUnitAccounts.init(getContext());DiagnosticLog.init(getContext());HeadUnitPreferences.init(getContext());HeadUnitProfile.configure(getContext());HeadUnitRuntime.init(getContext());DiagnosticUploadJob.schedule(getContext());try{AppUpdates.init(getContext());}catch(Throwable ignored){}return true;}
 public Bundle call(String method,String arg,Bundle extras){
  if(Binder.getCallingUid()!=android.os.Process.myUid())throw new SecurityException("Private settings");
  if(method.startsWith("update_")){try{return AppUpdates.call(getContext(),method,extras);}catch(Throwable e){throw new IllegalStateException("Update operation unavailable");}}
  if(method.startsWith("diagnostics_")){try{
   if("diagnostics_status".equals(method))return DiagnosticsQueue.summary(getContext());
   if("diagnostics_notice".equals(method))return AutomaticDiagnostics.state(getContext());
   if("diagnostics_failure".equals(method)){AutomaticDiagnostics.observeBackend(getContext(),extras.getString("stage"),extras.getString("code"));return new Bundle();}
   if("diagnostics_continue".equals(method)){AutomaticDiagnostics.accept(getContext(),extras.getBoolean("auto_choice"));return AutomaticDiagnostics.state(getContext());}
   if("diagnostics_auto".equals(method)){AutomaticDiagnostics.choice(getContext(),extras.getBoolean("auto_choice"));return AutomaticDiagnostics.state(getContext());}
   if("diagnostics_preview".equals(method)){Bundle out=new Bundle();out.putString("body",DiagnosticReport.preview(getContext(),extras.getString("category","other")));return out;}
   if("diagnostics_send".equals(method)){Bundle out=new Bundle();out.putString("id",DiagnosticsQueue.approve(getContext(),extras.getString("body"),extras.getBoolean("public_consent",false)));DiagnosticUploadJob.schedule(getContext());DiagnosticUploadJob.kick(getContext());return out;}
   if("diagnostics_cancel".equals(method)){DiagnosticsQueue.cancel(getContext(),extras.getString("id"));DiagnosticUploadJob.kick(getContext());return DiagnosticsQueue.summary(getContext());}
   if("diagnostics_remove".equals(method)){DiagnosticsQueue.remove(getContext(),extras.getString("id"));return DiagnosticsQueue.summary(getContext());}
   if("diagnostics_export".equals(method)){if(extras!=null && extras.containsKey("id"))extras.putString("body",DiagnosticsQueue.body(getContext(),extras.getString("id")));return DiagnosticReport.create(getContext(),extras);}
   throw new IllegalArgumentException("Unknown diagnostic operation");
  }catch(Exception e){throw new IllegalStateException("Diagnostics operation unavailable");}}
  if("set".equals(method)){HeadUnitPreferences.write(extras);HeadUnitRuntime.resumeActive();if(!HeadUnitPreferences.stopOnClose())local.spotify.close.PlaybackGate.change(getContext(),false);}
  else if("ui_open".equals(method))HeadUnitRuntime.playerOpened(getContext());
  else if(!"get".equals(method))throw new IllegalArgumentException("Unknown settings operation");
  return HeadUnitPreferences.read();
 }
 public Cursor query(Uri u,String[] p,String s,String[] a,String o){throw new UnsupportedOperationException();}
 public String getType(Uri u){return null;}public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
