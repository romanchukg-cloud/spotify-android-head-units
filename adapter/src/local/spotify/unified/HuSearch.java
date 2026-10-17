package local.spotify.unified;
import android.content.*;
import android.graphics.Typeface;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.util.*;
/**
 * Search in our interface: a field with clear and, when the head unit has speech recognition, a
 * microphone before it; results of the backend's search while typing, in its groups (top results, songs,
 * albums, artists, playlists, podcasts, episodes, audiobooks) through a HuList. A spoken query
 * plays the best match at once and shows its results for another choice. With an empty field the
 * recent searches (kept in this process's preferences) can be searched again.
 */
final class HuSearch {
 private static final String PREFS="hu_search",RECENT="recent";private static final int KEEP=8;private static final long DELAY=450;
 final View view;private final HuUi ui;private final HuSession session;private final EditText field;private final ImageView clear;
 private final HuList results;private final LinearLayout recents;private final ScrollView recentScroll;private final TextView message;
 private final Runnable run=this::search;private String shown;

 HuSearch(HuUi ui,String query,HuPages.Open open,Runnable voice){
  this.ui=ui;session=HuSession.get(ui.activity);
  LinearLayout page=new LinearLayout(ui.activity);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(ui.dp(16),ui.dp(16),ui.dp(16),0);view=page;
  // The field: search icon, text and clear in a pill; the microphone beside it.
  LinearLayout bar=new LinearLayout(ui.activity);bar.setGravity(Gravity.CENTER_VERTICAL);
  LinearLayout pill=new LinearLayout(ui.activity);pill.setGravity(Gravity.CENTER_VERTICAL);pill.setBackground(HuUi.round(ui.control,ui.dp(36)));pill.setPaddingRelative(ui.dp(22),0,ui.dp(6),0);
  ImageView icon=new ImageView(ui.activity);icon.setImageDrawable(HuControls.icon(ui.activity,"encore_icon_search_24",ui.secondary));pill.addView(icon,new LinearLayout.LayoutParams(ui.dp(28),ui.dp(28)));
  field=new EditText(ui.activity);field.setBackground(null);field.setSingleLine(true);field.setTextSize(20);field.setTextColor(ui.text);field.setHintTextColor(ui.secondary);field.setHint(HuText.get("search_hint"));
  Typeface f=ui.font(false);if(f!=null)field.setTypeface(f);
  field.setInputType(InputType.TYPE_CLASS_TEXT);field.setImeOptions(EditorInfo.IME_ACTION_SEARCH|EditorInfo.IME_FLAG_NO_EXTRACT_UI);field.setPaddingRelative(ui.dp(14),0,ui.dp(8),0);
  pill.addView(field,new LinearLayout.LayoutParams(0,ui.dp(72),1));
  clear=HuControls.button(ui,"encore_icon_x_24",HuText.get("clear"),ui.dp(60),v->field.setText(""));clear.setVisibility(View.INVISIBLE);pill.addView(clear);
  // The microphone comes first, nearest the driver's hand (on the right in right-to-left languages).
  if(voice!=null && HeadUnitVoice.available(ui.activity)){
   ImageView mic=HuControls.button(ui,"encore_icon_mic_24",HuText.get("voice"),ui.dp(72),v->{hideKeyboard();voice.run();});
   mic.setBackground(ui.pressable(ui.control,ui.dp(36)));((LinearLayout.LayoutParams)mic.getLayoutParams()).setMarginEnd(ui.dp(10));bar.addView(mic);
  }
  bar.addView(pill,new LinearLayout.LayoutParams(0,ui.dp(72),1));
  page.addView(bar);
  // Below: results, recent searches or a message.
  FrameLayout body=new FrameLayout(ui.activity);LinearLayout.LayoutParams bodyParams=new LinearLayout.LayoutParams(-1,0,1);bodyParams.topMargin=ui.dp(8);page.addView(body,bodyParams);
  results=new HuList(ui,"search",item->{remember(field.getText().toString());open.item(item);});results.view.setPadding(0,0,0,ui.dp(16));
  results.view.setOnScrollListener(new AbsListView.OnScrollListener(){
   public void onScrollStateChanged(AbsListView v,int state){if(state!=SCROLL_STATE_IDLE)hideKeyboard();}
   public void onScroll(AbsListView v,int first,int visible,int total){}
  });
  recents=new LinearLayout(ui.activity);recents.setOrientation(LinearLayout.VERTICAL);recentScroll=new ScrollView(ui.activity);recentScroll.addView(recents);
  message=HuPages.message(ui,"");
  body.addView(results.view,new FrameLayout.LayoutParams(-1,-1));body.addView(recentScroll,new FrameLayout.LayoutParams(-1,-1));body.addView(message,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
  field.addTextChangedListener(new TextWatcher(){
   public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){}
   public void afterTextChanged(Editable e){
    clear.setVisibility(e.length()>0?View.VISIBLE:View.INVISIBLE);view.removeCallbacks(run);
    if(e.toString().trim().isEmpty())idle();else view.postDelayed(run,DELAY);
   }
  });
  field.setOnEditorActionListener((v,action,event)->{
   if(action!=EditorInfo.IME_ACTION_SEARCH && (event==null || event.getKeyCode()!=KeyEvent.KEYCODE_ENTER))return false;
   view.removeCallbacks(run);search();remember(field.getText().toString());hideKeyboard();return true;
  });
  if(query!=null && !query.trim().isEmpty()){field.setText(query);field.setSelection(query.length());}else idle();
 }
 String query(){return field.getText().toString();}
 /** A spoken query: plays the best match and shows the results for it. */
 void spoken(String q){session.playSearch(q);remember(q);field.setText(q);field.setSelection(q.length());view.removeCallbacks(run);search();}

 private void search(){
  String q=field.getText().toString().trim();if(q.isEmpty()){idle();return;}
  if(q.equals(shown) && results.view.getVisibility()==View.VISIBLE)return;
  if(results.view.getVisibility()!=View.VISIBLE){message.setText(HuText.get("loading"));show(message);}
  session.search(q,items->{
   if(!q.equals(field.getText().toString().trim()))return;
   shown=q;
   if(items.isEmpty()){message.setText(HuText.get("no_results"));show(message);return;}
   results.fill(items,true);results.view.setSelection(0);show(results.view);
  });
 }
 /** No query: the recent searches, or what can be searched for. */
 private void idle(){
  shown=null;recents.removeAllViews();List<String> list=recent();
  if(list.isEmpty()){message.setText(HuText.get("search_start"));show(message);return;}
  recents.addView(HuPages.heading(ui,HuText.get("recent_searches")));
  for(String q:list){
   LinearLayout row=new LinearLayout(ui.activity);row.setGravity(Gravity.CENTER_VERTICAL);row.setMinimumHeight(ui.dp(72));row.setPaddingRelative(ui.dp(12),0,ui.dp(12),0);row.setBackground(ui.pressable(0x00000000,ui.dp(14)));
   ImageView i=new ImageView(ui.activity);i.setImageDrawable(HuControls.icon(ui.activity,"encore_icon_recently_played_24",ui.secondary));row.addView(i,new LinearLayout.LayoutParams(ui.dp(28),ui.dp(28)));
   TextView t=ui.label(q,19,ui.text,false);t.setSingleLine(true);t.setEllipsize(TextUtils.TruncateAt.END);t.setPaddingRelative(ui.dp(18),0,0,0);row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
   row.setOnClickListener(v->{hideKeyboard();field.setText(q);field.setSelection(q.length());view.removeCallbacks(run);search();remember(q);});
   recents.addView(row,new LinearLayout.LayoutParams(-1,-2));
  }
  TextView clearAll=ui.label(HuText.get("clear_recent"),17,ui.text,true);clearAll.setGravity(Gravity.CENTER);clearAll.setMinHeight(ui.dp(64));clearAll.setPadding(ui.dp(28),0,ui.dp(28),0);
  clearAll.setBackground(ui.pressable(ui.control,ui.dp(32)));clearAll.setOnClickListener(v->{prefs().edit().remove(RECENT).apply();idle();});
  LinearLayout.LayoutParams c=new LinearLayout.LayoutParams(-2,ui.dp(64));c.topMargin=ui.dp(16);c.bottomMargin=ui.dp(16);c.gravity=Gravity.CENTER_HORIZONTAL;recents.addView(clearAll,c);
  show(recentScroll);
 }
 private void show(View v){
  results.view.setVisibility(v==results.view?View.VISIBLE:View.GONE);recentScroll.setVisibility(v==recentScroll?View.VISIBLE:View.GONE);message.setVisibility(v==message?View.VISIBLE:View.GONE);
 }
 private void hideKeyboard(){
  InputMethodManager m=(InputMethodManager)ui.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
  if(m!=null)m.hideSoftInputFromWindow(field.getWindowToken(),0);field.clearFocus();
 }
 private SharedPreferences prefs(){return ui.activity.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
 private List<String> recent(){
  List<String> list=new ArrayList<>();for(String q:prefs().getString(RECENT,"").split("\n"))if(!q.trim().isEmpty())list.add(q);return list;
 }
 /** Keeps a query on top of the recent searches (once, the newest eight). */
 private void remember(String q){
  q=q==null?"":q.trim();if(q.isEmpty())return;
  List<String> list=recent();for(Iterator<String> it=list.iterator();it.hasNext();)if(it.next().equalsIgnoreCase(q))it.remove();
  list.add(0,q);while(list.size()>KEEP)list.remove(list.size()-1);
  prefs().edit().putString(RECENT,TextUtils.join("\n",list)).apply();
 }
}
