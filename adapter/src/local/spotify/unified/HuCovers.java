package local.spotify.unified;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import java.util.concurrent.*;
/**
 * Covers for our interface: the backend's image provider (content://com.spotify.music.mediaapi/…)
 * decoded off the main thread, kept in a small memory cache, with the cover's vivid colour
 * (CoverAccent.vivid) for tinting.
 */
final class HuCovers {
 interface Done {void cover(Bitmap bitmap,Integer color);}
 private static final ExecutorService worker=Executors.newSingleThreadExecutor();
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static final LruCache<String,Bitmap> bitmaps=new LruCache<String,Bitmap>(24*1024*1024){protected int sizeOf(String k,Bitmap b){return b.getByteCount();}};
 private static final LruCache<String,Integer> colors=new LruCache<>(64);
 private HuCovers(){}
 /**
  * Loads the cover at about the given size; done runs on the main thread (bitmap null when missing).
  * Right after the backend starts its image provider can fail; a failed cover is tried twice more.
  */
 static void load(Context c,String uri,int size,Done done){load(c,uri,size,done,0);}
 private static void load(Context c,String uri,int size,Done done,int attempt){
  if(uri==null){done.cover(null,null);return;}
  String key=uri+"@"+size;Bitmap cached=bitmaps.get(key);
  if(cached!=null){done.cover(cached,colors.get(uri));return;}
  Context app=c.getApplicationContext();
  worker.execute(()->{
   Bitmap b=null;Integer color=colors.get(uri);
   try{
    BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;
    try(java.io.InputStream in=app.getContentResolver().openInputStream(Uri.parse(uri))){BitmapFactory.decodeStream(in,null,o);}
    int sample=1;while(o.outWidth/(sample*2)>=size && o.outHeight/(sample*2)>=size)sample*=2;
    BitmapFactory.Options d=new BitmapFactory.Options();d.inSampleSize=sample;
    try(java.io.InputStream in=app.getContentResolver().openInputStream(Uri.parse(uri))){b=BitmapFactory.decodeStream(in,null,d);}
    if(b!=null){bitmaps.put(key,b);if(color==null){color=CoverAccent.vivid(new BitmapDrawable(app.getResources(),b));if(color!=null)colors.put(uri,color);}}
   }catch(Exception e){android.util.Log.w("SpotifyHU","Cover unavailable: "+e.getClass().getSimpleName());}
   final Bitmap result=b;final Integer tint=color;
   if(result==null && attempt<2){main.postDelayed(()->load(app,uri,size,done,attempt+1),attempt==0?1500:4000);return;}
   main.post(()->done.cover(result,tint));
  });
 }
 /** Shows the cover in an image view unless the view has meanwhile been given another one. */
 static void into(ImageView view,String uri,int size){
  view.setTag(uri);
  load(view.getContext(),uri,size,(b,color)->{if(uri==null?view.getTag()==null:uri.equals(view.getTag()))view.setImageBitmap(b);});
 }
}
