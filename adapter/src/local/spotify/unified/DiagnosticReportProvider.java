package local.spotify.unified;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.*;

/** Non-exported provider: only an explicit temporary read grant exposes one ZIP. */
public final class DiagnosticReportProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 private File file(Uri uri)throws FileNotFoundException{
  if(Binder.getCallingUid()!=android.os.Process.myUid() && getContext().checkUriPermission(uri,Binder.getCallingPid(),Binder.getCallingUid(),Intent.FLAG_GRANT_READ_URI_PERMISSION)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Report requires read grant");
  List<String> path=uri.getPathSegments();if(!(getContext().getPackageName()+".local.diagnostics").equals(uri.getAuthority()) || path.size()!=2 || !path.get(0).equals("report") || !path.get(1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.zip"))throw new FileNotFoundException("Unknown report");
  try{
   File root=DiagnosticReport.directory(getContext()).getCanonicalFile(),f=new File(root,path.get(1));
   if(!f.getCanonicalFile().equals(f) || !f.isFile() || System.currentTimeMillis()-f.lastModified()>DiagnosticReport.EXPIRY_MS)throw new FileNotFoundException("Report expired or unavailable");return f;
  }catch(IOException e){throw new FileNotFoundException("Report unavailable");}
 }
 public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{if(!"r".equals(mode))throw new FileNotFoundException("Read-only report");return ParcelFileDescriptor.open(file(uri),ParcelFileDescriptor.MODE_READ_ONLY);}
 public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){
  try{File f=file(uri);String[] columns=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor cursor=new MatrixCursor(columns);Object[] row=new Object[columns.length];for(int i=0;i<columns.length;i++)row[i]=OpenableColumns.DISPLAY_NAME.equals(columns[i])?"Spotify-diagnostics-"+f.getName():OpenableColumns.SIZE.equals(columns[i])?f.length():null;cursor.addRow(row);return cursor;}catch(FileNotFoundException e){return null;}
 }
 public String getType(Uri uri){return "application/zip";}
 public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException();}public int delete(Uri uri,String s,String[] a){throw new UnsupportedOperationException();}public int update(Uri uri,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
