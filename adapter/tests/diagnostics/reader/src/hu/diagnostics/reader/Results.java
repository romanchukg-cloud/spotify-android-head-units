package hu.diagnostics.reader;
import android.content.*;import android.database.*;import android.net.Uri;
/** Test-only result channel, contains synthetic test status strings only. */
public final class Results extends ContentProvider {
 public boolean onCreate(){return true;}
 public Cursor query(Uri u,String[] p,String s,String[] a,String o){MatrixCursor c=new MatrixCursor(new String[]{"result"});c.addRow(new Object[]{getContext().getSharedPreferences("results",0).getString(u.getLastPathSegment(),"")});return c;}
 public String getType(Uri u){return "text/plain";}public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){return 0;}public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
}
