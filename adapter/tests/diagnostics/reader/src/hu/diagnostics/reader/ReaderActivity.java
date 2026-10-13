package hu.diagnostics.reader;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import java.io.*;
public final class ReaderActivity extends Activity {
 public void onCreate(Bundle state){super.onCreate(state);String result;
  try(InputStream in=getContentResolver().openInputStream(Uri.parse(getIntent().getStringExtra("uri")))){result=in!=null && in.read()=='P' && in.read()=='K'?"READ_OK":"INVALID_ZIP";}
  catch(Exception e){result=e.getClass().getSimpleName();}
  getSharedPreferences("results",0).edit().putString(getIntent().getStringExtra("case"),result).commit();finish();
 }
}
