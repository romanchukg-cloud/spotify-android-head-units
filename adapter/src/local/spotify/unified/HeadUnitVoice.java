package local.spotify.unified;
import android.content.*;
import android.speech.RecognizerIntent;
/**
 * Voice search: the head unit's own speech recognition (RecognizerIntent). Search shows its
 * microphone only when the system has a recogniser (BYD DiLink has none for apps).
 */
public final class HeadUnitVoice {
 private HeadUnitVoice(){}
 static boolean available(Context c){return !c.getPackageManager().queryIntentActivities(recognizer(),0).isEmpty();}
 static Intent recognizer(){
  return new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
   .putExtra(RecognizerIntent.EXTRA_LANGUAGE,java.util.Locale.getDefault().toLanguageTag()).putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1)
   .putExtra(RecognizerIntent.EXTRA_PROMPT,HuText.get("voice_prompt"));
 }
}
