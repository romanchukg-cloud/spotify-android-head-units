package local.spotify.unified;
import android.content.*;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.os.*;
import android.speech.RecognizerIntent;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import java.lang.ref.WeakReference;
/**
 * Voice search on the search screen. A microphone button in the search field opens the head
 * unit's own speech recognition (RecognizerIntent); it appears only when the system has one. The
 * spoken text plays the best match through the backend's media session (PLAY_FROM_SEARCH) and is
 * searched in the field, so the results stay on screen for another choice.
 */
public final class HeadUnitVoice {
 private static final String UI="com.spotify.music.ui",TAG="hu_voice",SERVICE="com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService";
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static WeakReference<EditText> field;
 private HeadUnitVoice(){}

 /** SearchFragment, once its view exists. */
 public static void search(View root){
  try{
   if(root==null || !available(root.getContext()))return;
   View input=find(root,"input_layout"),edit=find(root,"edit_search"),remove=find(root,"icon_remove");
   if(!(input instanceof RelativeLayout) || !(edit instanceof EditText) || remove==null)return;
   EditText text=(EditText)edit;field=new WeakReference<>(text);
   RelativeLayout layout=(RelativeLayout)input;View old=layout.findViewWithTag(TAG);if(old!=null)layout.removeView(old);
   Context c=root.getContext();ViewGroup.LayoutParams r=remove.getLayoutParams();
   int size=r!=null && r.height>0?r.height:Math.round(56*c.getResources().getDisplayMetrics().density);
   int res=c.getResources().getIdentifier("encore_icon_mic_24","drawable",c.getPackageName());if(res==0)return;
   android.graphics.drawable.Drawable icon=c.getDrawable(res).mutate();icon.setTint(text.getCurrentTextColor());
   ImageView mic=new ImageView(c);mic.setTag(TAG);mic.setImageDrawable(icon);mic.setScaleType(ImageView.ScaleType.FIT_CENTER);
   int pad=size/4;mic.setPadding(pad,pad,pad,pad);mic.setContentDescription(HuText.get("voice"));
   android.util.TypedValue ripple=new android.util.TypedValue();
   if(c.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,ripple,true) && ripple.resourceId!=0)mic.setBackgroundResource(ripple.resourceId);
   mic.setOnClickListener(v->{try{v.getContext().startActivity(new Intent(v.getContext(),HeadUnitVoiceActivity.class));}catch(RuntimeException e){android.util.Log.w("SpotifyHU","Voice search unavailable",e);}});
   // Next to the clear button, or at the end of the field while the clear button is hidden.
   RelativeLayout.LayoutParams p=new RelativeLayout.LayoutParams(size,size);
   if(remove.getId()!=View.NO_ID)p.addRule(RelativeLayout.START_OF,remove.getId());
   p.alignWithParent=true;p.addRule(RelativeLayout.CENTER_VERTICAL);
   p.setMarginEnd(r instanceof ViewGroup.MarginLayoutParams?((ViewGroup.MarginLayoutParams)r).getMarginEnd():0);
   layout.addView(mic,p);
   text.setPaddingRelative(text.getPaddingStart(),text.getPaddingTop(),text.getPaddingEnd()+size,text.getPaddingBottom());
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Voice button unavailable",e);}
 }
 private static View find(View root,String name){int id=root.getResources().getIdentifier(name,"id",UI);return id==0?null:root.findViewById(id);}
 static boolean available(Context c){return !c.getPackageManager().queryIntentActivities(recognizer(),0).isEmpty();}
 static Intent recognizer(){
  return new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
   .putExtra(RecognizerIntent.EXTRA_LANGUAGE,java.util.Locale.getDefault().toLanguageTag()).putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1)
   .putExtra(RecognizerIntent.EXTRA_PROMPT,HuText.get("voice_prompt"));
 }
 /** The recognised text: plays the best match and shows the results for it. */
 static void result(Context c,String query){
  play(c,query);
  EditText text=field==null?null:field.get();
  if(text!=null && text.isAttachedToWindow()){text.setText(query);text.setSelection(query.length());text.onEditorAction(EditorInfo.IME_ACTION_SEARCH);}
 }
 private static void play(Context c,String query){
  Context app=c.getApplicationContext();final MediaBrowser[] holder=new MediaBrowser[1];
  holder[0]=new MediaBrowser(app,new ComponentName(app.getPackageName(),SERVICE),new MediaBrowser.ConnectionCallback(){
   public void onConnected(){
    try{new MediaController(app,holder[0].getSessionToken()).getTransportControls().playFromSearch(query,new Bundle());android.util.Log.i("SpotifyHU","Voice search played");}
    catch(RuntimeException e){android.util.Log.w("SpotifyHU","Voice search playback failed",e);}
    main.postDelayed(holder[0]::disconnect,5000);
   }
   public void onConnectionFailed(){android.util.Log.w("SpotifyHU","Voice search: backend browser unavailable");}
  },null);
  holder[0].connect();
 }
}
