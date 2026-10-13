package local.spotify.unified;
import android.app.Activity;import android.content.*;import android.net.Uri;
public final class TestHarness extends Activity {
 public void open(Uri uri,String name,boolean grant){Intent i=new Intent().setClassName("hu.diagnostics.reader","hu.diagnostics.reader.ReaderActivity").putExtra("uri",uri.toString()).putExtra("case",name);if(grant){i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);i.setClipData(ClipData.newRawUri("Synthetic report",uri));}startActivity(i);}
}
