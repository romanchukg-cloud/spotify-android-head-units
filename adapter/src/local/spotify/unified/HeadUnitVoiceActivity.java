package local.spotify.unified;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import java.util.ArrayList;
/** Invisible step between the microphone button and the system's speech recognition (player process). */
public final class HeadUnitVoiceActivity extends Activity {
 protected void onCreate(Bundle state){
  super.onCreate(state);
  if(state!=null)return;
  try{startActivityForResult(HeadUnitVoice.recognizer(),1);}catch(ActivityNotFoundException e){finish();}
 }
 protected void onActivityResult(int request,int result,Intent data){
  ArrayList<String> heard=data==null?null:data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
  String query=heard==null || heard.isEmpty() || heard.get(0)==null?"":heard.get(0).trim();
  if(result==RESULT_OK && !query.isEmpty())HeadUnitVoice.result(this,query);
  finish();
 }
}
