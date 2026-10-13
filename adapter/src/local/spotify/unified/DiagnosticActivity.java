package local.spotify.unified;
import android.app.*;import android.content.*;import android.graphics.Color;import android.graphics.Typeface;import android.net.Uri;import android.os.*;import android.widget.*;
import org.json.*;import java.io.*;import java.util.concurrent.*;
/** Manual approval, export and receipt UI. Background diagnostics never opens a dialog. */
public final class DiagnosticActivity extends Activity {
 private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());
 private LinearLayout reports;private TextView status;private Switch automatic;private Spinner category;private Button send,export;private Uri saveSource;private boolean destroyed,updating,visible;
 private final Runnable poll=new Runnable(){public void run(){if(visible && !destroyed){refresh();main.postDelayed(this,5000);}}};
 public void onCreate(Bundle state){super.onCreate(state);DiagnosticLog.init(this);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff121212);LinearLayout list=new LinearLayout(this);list.setOrientation(1);int p=dp(20);list.setPadding(p,p,p,p);scroll.addView(list);setContentView(scroll);
  label(list,"Support",26);label(list,HeadUnitBuild.LABEL+" · "+HeadUnitBuild.VERSION_CODE+" · Android "+Build.VERSION.RELEASE,16);
  label(list,DiagnosticsConfig.DISCLOSURE,18);
  automatic=new Switch(this);automatic.setText("Automatically send diagnostics");automatic.setTextColor(Color.WHITE);automatic.setTextSize(18);automatic.setMinHeight(dp(60));automatic.setEnabled(false);list.addView(automatic);
  label(list,"Automatic reports cover measured integration failures only, at most one new report per 24 hours, after Continue on the first-use notice. Server support is checked before each automatic upload. Turning this off stops automatic retries and attempts to cancel queued automatic reports; delivery already in progress cannot be recalled.",18);
  label(list,"For ambient lighting, enable Music rhythm in the car's own settings. A successful API call does not prove that the lamps or cluster displayed anything.",18);
  label(list,"Problem",18);category=new Spinner(this);category.setBackgroundColor(0xff303030);category.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Playback","Cluster media","Steering-wheel buttons","Authentication","Other (including ambient lighting)"}));list.addView(category,new LinearLayout.LayoutParams(-1,dp(56)));
  status=label(list,"",16);status.setTextColor(0xff1ed760);send=button(list,"Send diagnostics");export=button(list,"Export diagnostics");send.setOnClickListener(v->prepare(false));export.setOnClickListener(v->prepare(true));
  label(list,"Pending reports",22);label(list,"Pending is local; Queued means accepted by the server. Report sent is shown only after confirmed GitHub delivery. Cancellation cannot recall a report already being delivered or a public issue. Retries stop after 24 hours. Export works without sending.",16);
  reports=new LinearLayout(this);reports.setOrientation(1);list.addView(reports);
  automatic.setOnCheckedChangeListener((v,choice)->{if(updating)return;Bundle b=new Bundle();b.putBoolean("auto_choice",choice);operation("diagnostics_auto",b);});
  if(state!=null){String uri=state.getString("save_source");if(uri!=null)saveSource=Uri.parse(uri);}
 }
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private TextView label(LinearLayout list,String text,int size){TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(0,dp(10),0,dp(10));list.addView(t);return t;}
 private Button button(LinearLayout list,String text){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(19);b.setMinHeight(dp(60));list.addView(b,new LinearLayout.LayoutParams(-1,-2));return b;}
 private void busy(boolean yes){send.setEnabled(!yes);export.setEnabled(!yes);}
 private void refresh(){worker.execute(()->{try{Bundle b=HeadUnitSettings.call(this,"diagnostics_status",null);main.post(()->{if(!destroyed)render(b);});}catch(RuntimeException e){main.post(()->{if(!destroyed)status.setText("Diagnostics status unavailable. Export remains available.");});}});}
 private void operation(String method,Bundle args){worker.execute(()->{try{HeadUnitSettings.call(this,method,args);main.post(()->{if(!destroyed)refresh();});}catch(RuntimeException e){main.post(()->{if(!destroyed)status.setText("The operation could not be completed. Try again or export the report.");});}});}
 private String category(){return new String[]{"playback","cluster_media","wheel_buttons","authentication","other"}[category.getSelectedItemPosition()];}
 private void prepare(boolean exporting){busy(true);status.setText("Preparing sanitized report…");Bundle args=new Bundle();args.putString("category",category());
  worker.execute(()->{try{DiagnosticLog.flush();String body=HeadUnitSettings.call(this,"diagnostics_preview",args).getString("body");main.post(()->{if(destroyed)return;busy(false);status.setText("");if(exporting)exportBody(body,null);else preview(body);});}catch(RuntimeException e){main.post(()->{if(!destroyed){busy(false);status.setText("Could not prepare the report. Try again.");}});}});
 }
 private void preview(String body){
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff121212);LinearLayout list=new LinearLayout(this);list.setOrientation(1);list.setPadding(dp(16),dp(8),dp(16),dp(8));scroll.addView(list);label(list,DiagnosticsConfig.DISCLOSURE+"\nPress Send public report only if you agree. No report has been uploaded by opening this preview.",17);
  TextView json=label(list,"",13);json.setTypeface(Typeface.MONOSPACE);try{json.setText(new JSONObject(body).toString(2));}catch(JSONException e){return;}
  new AlertDialog.Builder(this).setTitle("Preview public diagnostics").setView(scroll).setNegativeButton("Cancel",null).setNeutralButton("Export",(d,w)->exportBody(body,null)).setPositiveButton("Send public report",(d,w)->{
   busy(true);Bundle args=new Bundle();args.putString("body",body);args.putBoolean("public_consent",true);
   worker.execute(()->{try{Bundle result=HeadUnitSettings.call(this,"diagnostics_send",args);main.post(()->{if(!destroyed){busy(false);status.setText("Pending — local report "+result.getString("id"));refresh();}});}catch(RuntimeException e){main.post(()->{if(!destroyed){busy(false);status.setText("Could not queue the report. The queue holds three reports; remove one first if it is full. Export remains available.");}});}});
  }).show();
 }
 private void render(Bundle b){updating=true;automatic.setChecked(b.getBoolean("auto_choice",true));automatic.setEnabled(true);updating=false;reports.removeAllViews();
  try{JSONArray rows=new JSONArray(b.getString("reports","[]"));if(rows.length()==0){label(reports,"No pending reports.",16);return;}
   for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);String id=row.getString("id"),state=row.getString("status"),issue=row.optString("issue_url");
    label(reports,("automatic".equals(row.optString("source"))?"Automatic report ":"Manual report ")+id+"\n"+stateLabel(state)+("automatic_support_unavailable".equals(row.optString("error"))?"\nWaiting for verified server support":""),17);
    if(row.getBoolean("can_cancel"))button(reports,"Cancel pending report").setOnClickListener(v->{Bundle args=new Bundle();args.putString("id",id);operation("diagnostics_cancel",args);});
    if(row.getBoolean("can_export"))button(reports,"Export this report").setOnClickListener(v->exportBody(null,id));
    if(DiagnosticsConfig.issueUrl(issue))button(reports,"Open public GitHub issue").setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(issue)));}catch(ActivityNotFoundException e){status.setText("No browser is available on this head unit.");}});
    if(row.getBoolean("can_remove"))button(reports,"Remove local copy").setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Remove local report?").setMessage("This frees a queue slot. It does not delete or recall any public GitHub issue.").setNegativeButton("Keep",null).setPositiveButton("Remove",(d,w)->{Bundle args=new Bundle();args.putString("id",id);operation("diagnostics_remove",args);}).show());
   }
  }catch(JSONException e){status.setText("Diagnostics status unavailable.");}
 }
 private String stateLabel(String state){switch(state){case "pending":return "Pending — waiting locally for upload";case "queued":return "Queued — accepted by server";case "delivering":return "Delivery in progress — cannot recall";case "delivery_uncertain":return "Delivery is being reconciled by the server";case "submitted":return "Report sent — GitHub delivery confirmed";case "failed":return "Report failed — export is available";case "cancelled":return "Cancelled by server";case "cancel_uncertain":return "Stopped locally; server acceptance is unknown and a report may still be published";case "expired":return "Retry window ended; a server-accepted report may still be delivered";case "cancelling":return "Cancellation requested — awaiting confirmation";default:return "Pending";}}
 private void exportBody(String body,String id){Bundle args=new Bundle();if(id!=null)args.putString("id",id);else args.putString("body",body);busy(true);
  worker.execute(()->{try{Bundle result=HeadUnitSettings.call(this,"diagnostics_export",args);main.post(()->{if(destroyed)return;busy(false);Uri uri=Uri.parse(result.getString("uri"));String name=result.getString("report_id");new AlertDialog.Builder(this).setTitle("Export diagnostics").setItems(new String[]{"Save ZIP","Share ZIP"},(d,n)->{if(n==0)save(uri,name);else share(uri,name);}).show();});}catch(RuntimeException e){main.post(()->{if(!destroyed){busy(false);status.setText("Export could not be prepared. Try again.");}});}});
 }
 private void share(Uri uri,String id){Intent send=new Intent(Intent.ACTION_SEND).setType("application/zip").putExtra(Intent.EXTRA_STREAM,uri).putExtra(Intent.EXTRA_SUBJECT,"Spotify diagnostics "+id).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);send.setClipData(ClipData.newRawUri("Spotify diagnostics",uri));try{startActivity(Intent.createChooser(send,"Share diagnostics"));}catch(ActivityNotFoundException e){status.setText("No compatible app. Try Save ZIP.");}}
 private void save(Uri uri,String id){saveSource=uri;try{startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/zip").putExtra(Intent.EXTRA_TITLE,"Spotify-diagnostics-"+id+".zip"),71);}catch(ActivityNotFoundException e){status.setText("No file picker available. Try Share ZIP.");}}
 protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);if(saveSource!=null)state.putString("save_source",saveSource.toString());}
 protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=71 || result!=RESULT_OK || data==null || data.getData()==null || saveSource==null)return;Uri source=saveSource,destination=data.getData();worker.execute(()->{try(InputStream in=getContentResolver().openInputStream(source);OutputStream out=getContentResolver().openOutputStream(destination,"w")){if(in==null || out==null)throw new IOException();byte[] bytes=new byte[8192];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);main.post(()->{if(!destroyed)status.setText("Report exported. Exporting does not upload it.");});}catch(Exception e){main.post(()->{if(!destroyed)status.setText("Could not save the report.");});}});}
 protected void onResume(){super.onResume();visible=true;main.post(poll);}
 protected void onPause(){visible=false;main.removeCallbacks(poll);super.onPause();}
 protected void onDestroy(){destroyed=true;worker.shutdown();main.removeCallbacksAndMessages(null);super.onDestroy();}
}
