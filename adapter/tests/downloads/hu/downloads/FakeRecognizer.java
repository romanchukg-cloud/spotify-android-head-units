package hu.downloads;
import android.app.Activity;import android.content.Intent;import android.os.Bundle;import android.speech.RecognizerIntent;import java.util.ArrayList;
/** Test double for the system speech recogniser: answers RECOGNIZE_SPEECH with a fixed phrase. */
public class FakeRecognizer extends Activity {
 public void onCreate(Bundle b){super.onCreate(b);
  android.util.Log.i(Probe.TAG,"RECOGNIZER asked prompt="+getIntent().getStringExtra(RecognizerIntent.EXTRA_PROMPT)+" language="+getIntent().getStringExtra(RecognizerIntent.EXTRA_LANGUAGE));
  ArrayList<String> heard=new ArrayList<>();heard.add("Imagine Dragons Believer");
  setResult(RESULT_OK,new Intent().putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS,heard));finish();}
}
