package local.spotify.unified;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.media.MediaBrowserCompat;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;
/**
 * Offline downloads on playlist and album pages. Spotify Automotive publishes the download state of
 * a browsable item as one of its custom browser actions (download, downloading, downloaded) and
 * performs the action it is sent; downloaded items appear under Library → Downloads. The page reads
 * the item's current state through the backend's own media browser (the support-library classes of
 * the backend DEX) and shows one button next to the favourite button: download; downloading (tap
 * stops); downloaded (tap asks before removing). Items without a download action get no button.
 */
public final class HeadUnitDownloads {
 static final String DOWNLOAD="download",DOWNLOADING="downloading",DOWNLOADED="downloaded";
 private static final String UI="com.spotify.music.ui",TAG="hu_download",
  ACTIONS="androidx.media.utils.extras.CUSTOM_BROWSER_ACTION_ID_LIST",ITEM="androidx.media.utils.extras.KEY_CUSTOM_BROWSER_ACTION_MEDIA_ITEM_ID",
  SERVICE="com.spotify.automotive.mediabrowserservice.AutomotiveMediaBrowserService";
 private static final long POLL=2000,PENDING=30*60*1000L;
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static MediaBrowserCompat browser;private static boolean connected;
 private static final ArrayList<Runnable> waiting=new ArrayList<>();
 private HeadUnitDownloads(){}

 /** DetailFragment, once its view exists: the page root and its MediaItem. */
 public static void detail(View root,Parcelable item){
  try{
   String id=mediaId(item);if(root==null || id==null)return;
   int favourite=root.getResources().getIdentifier("btn_favorite","id",UI);
   View holder=favourite==0?null:root.findViewById(favourite);
   if(holder instanceof LinearLayout)new Button((LinearLayout)holder,id).refresh();
  }catch(RuntimeException e){android.util.Log.w("SpotifyHU","Download button unavailable",e);}
 }
 /** The interface's MediaItem (relocated support library) uses the backend's wire format. */
 static String mediaId(Parcelable item){
  if(item==null)return null;Parcel p=Parcel.obtain();
  try{item.writeToParcel(p,0);p.setDataPosition(0);return MediaBrowserCompat.MediaItem.CREATOR.createFromParcel(p).getMediaId();}
  finally{p.recycle();}
 }
 /** The item's download state, or null when it cannot be downloaded. */
 static String state(Bundle extras){
  ArrayList<String> actions=extras==null?null:extras.getStringArrayList(ACTIONS);
  if(actions!=null)for(String a:actions)if(DOWNLOAD.equals(a) || DOWNLOADING.equals(a) || DOWNLOADED.equals(a))return a;
  return null;
 }
 /** Runs the task once the backend browser is connected (main thread). */
 private static void withBrowser(Context c,Runnable task){
  if(browser!=null && connected){task.run();return;}
  waiting.add(task);if(browser!=null)return;
  browser=new MediaBrowserCompat(c.getApplicationContext(),new ComponentName(c.getPackageName(),SERVICE),new MediaBrowserCompat.ConnectionCallback(){
   public void onConnected(){connected=true;ArrayList<Runnable> tasks=new ArrayList<>(waiting);waiting.clear();for(Runnable t:tasks)t.run();}
   public void onConnectionFailed(){reset();}
   public void onConnectionSuspended(){reset();}
  },null);
  browser.connect();
 }
 private static void reset(){waiting.clear();if(browser!=null)try{browser.disconnect();}catch(RuntimeException ignored){}browser=null;connected=false;}

 private static final class Button implements View.OnClickListener,View.OnAttachStateChangeListener {
  final LinearLayout holder;final ImageView view;final String id;final boolean dark;final Runnable poll=this::refresh;String state,reported;boolean attached=true,busy;long pendingUntil;
  Button(LinearLayout holder,String id){
   this.holder=holder;this.id=id;Context c=holder.getContext();
   dark=(c.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
   View old=holder.findViewWithTag(TAG);if(old!=null)holder.removeView(old);
   ViewGroup.LayoutParams h=holder.getLayoutParams();int touch=h!=null && h.height>0?h.height:Math.round(56*c.getResources().getDisplayMetrics().density);
   View favourite=holder.getChildCount()>0?holder.getChildAt(0):null;int icon=favourite!=null && favourite.getLayoutParams()!=null && favourite.getLayoutParams().width>0?favourite.getLayoutParams().width:touch/2;
   view=new ImageView(c);view.setTag(TAG);view.setScaleType(ImageView.ScaleType.FIT_CENTER);int pad=Math.max(0,(touch-icon)/2);view.setPadding(pad,pad,pad,pad);
   android.util.TypedValue ripple=new android.util.TypedValue();
   if(c.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,ripple,true) && ripple.resourceId!=0)view.setBackgroundResource(ripple.resourceId);
   view.setVisibility(View.GONE);view.setOnClickListener(this);view.addOnAttachStateChangeListener(this);
   holder.addView(view,0,new LinearLayout.LayoutParams(touch,touch));
  }
  void refresh(){
   withBrowser(holder.getContext(),()->{if(browser!=null)browser.getItem(id,new MediaBrowserCompat.ItemCallback(){
    public void onItemLoaded(MediaBrowserCompat.MediaItem item){show(item==null || item.getDescription()==null?null:state(item.getDescription().getExtras()));}
    public void onError(String itemId){show(null);}
   });});
  }
  void show(String value){
   if(!attached)return;busy=false;
   if(value==null?reported!=null:!value.equals(reported)){reported=value;android.util.Log.i("SpotifyHU","Download state "+value);}
   // A requested download can report "download" until Spotify starts it; it shows as downloading meanwhile.
   if(DOWNLOAD.equals(value) && SystemClock.elapsedRealtime()<pendingUntil)value=DOWNLOADING;
   else if(!DOWNLOADING.equals(value))pendingUntil=0;
   state=value;
   if(value==null){view.setVisibility(View.GONE);return;}
   Context c=view.getContext();int res=c.getResources().getIdentifier("ic_action_"+value,"drawable",c.getPackageName());
   Drawable d=res==0?null:c.getDrawable(res);if(d==null){view.setVisibility(View.GONE);return;}
   d=d.mutate();d.setTint(DOWNLOADED.equals(value)?(dark?0xff1ed760:0xff0f7a36):dark?0xffffffff:0xff121212);
   view.setImageDrawable(d);view.setVisibility(View.VISIBLE);view.setAlpha(1f);
   view.setContentDescription(HuText.get(DOWNLOAD.equals(value)?"download":DOWNLOADING.equals(value)?"stop_download":"remove_download"));
   main.removeCallbacks(poll);if(DOWNLOADING.equals(value))main.postDelayed(poll,POLL);
  }
  public void onClick(View v){
   if(state==null || busy)return;
   if(DOWNLOADED.equals(state)){
    new AlertDialog.Builder(v.getContext()).setTitle(HuText.get("remove_ask")).setPositiveButton(HuText.get("remove"),(d,w)->send(DOWNLOADED)).setNegativeButton(HuText.get("cancel"),null).show();
    return;
   }
   pendingUntil=DOWNLOAD.equals(state)?SystemClock.elapsedRealtime()+PENDING:0;send(state);
   if(pendingUntil>0)show(DOWNLOADING);
  }
  void send(String action){
   busy=true;view.setAlpha(.5f);Bundle extras=new Bundle();extras.putString(ITEM,id);
   withBrowser(view.getContext(),()->{if(browser!=null)browser.sendCustomAction(action,extras,new MediaBrowserCompat.CustomActionCallback(){
    public void onResult(String a,Bundle e,Bundle result){refresh();}
    public void onError(String a,Bundle e,Bundle data){
     String message=data==null?null:data.getString("androidx.media.utils.extras.KEY_CUSTOM_BROWSER_ACTION_RESULT_MESSAGE");
     Toast.makeText(view.getContext(),message!=null?message:HuText.get("download_error"),Toast.LENGTH_LONG).show();pendingUntil=0;refresh();}
   });});
  }
  public void onViewAttachedToWindow(View v){if(!attached){attached=true;refresh();}}
  public void onViewDetachedFromWindow(View v){attached=false;main.removeCallbacks(poll);}
 }
}
