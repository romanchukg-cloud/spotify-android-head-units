package hu.appremote;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import com.spotify.android.appremote.api.*;
import com.spotify.protocol.client.*;
import com.spotify.protocol.types.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
/**
 * Our App Remote (HuAppRemoteService, debug build) checked with Spotify's own SDK, as Waze's
 * audio player uses it: connect, the player state (subscription and call), user status,
 * capabilities, the browse tree, a cover, the library state, an unsupported call's error and
 * disconnecting. It plays nothing and changes no library.
 */
public class AppRemoteTests extends Instrumentation {
 private int passed;private SpotifyAppRemote remote;
 private void ok(boolean value,String label){if(!value)throw new AssertionError(label);passed++;Log.i("HU_APPREMOTE","PASS "+label);}
 public void onCreate(Bundle b){start();}
 @SuppressWarnings("unchecked")
 private <T> T call(Supplier<CallResult<T>> s)throws Exception{
  CountDownLatch done=new CountDownLatch(1);AtomicReference<Object> out=new AtomicReference<>();
  runOnMainSync(()->s.get().setResultCallback(r->{out.set(r);done.countDown();}).setErrorCallback(e->{out.set(e);done.countDown();}));
  if(!done.await(20,TimeUnit.SECONDS))throw new AssertionError("Timed out");
  if(out.get() instanceof Throwable)throw new AssertionError("Error: "+out.get());
  return (T)out.get();
 }
 private Throwable failure(Supplier<CallResult<Empty>> s)throws Exception{
  CountDownLatch done=new CountDownLatch(1);AtomicReference<Object> out=new AtomicReference<>();
  runOnMainSync(()->s.get().setResultCallback(r->{out.set(r);done.countDown();}).setErrorCallback(e->{out.set(e);done.countDown();}));
  if(!done.await(20,TimeUnit.SECONDS))throw new AssertionError("Timed out");
  return out.get() instanceof Throwable?(Throwable)out.get():null;
 }
 @SuppressWarnings("unchecked")
 private <T> T first(Supplier<Subscription<T>> s)throws Exception{
  CountDownLatch done=new CountDownLatch(1);AtomicReference<Object> out=new AtomicReference<>();
  runOnMainSync(()->s.get().setEventCallback(e->{if(out.compareAndSet(null,e))done.countDown();}).setErrorCallback(e->{if(out.compareAndSet(null,e))done.countDown();}));
  if(!done.await(20,TimeUnit.SECONDS))throw new AssertionError("Timed out");
  if(out.get() instanceof Throwable)throw new AssertionError("Error: "+out.get());
  return (T)out.get();
 }
 public void onStart(){
  Bundle out=new Bundle();
  try{
   CountDownLatch connected=new CountDownLatch(1);AtomicReference<Object> result=new AtomicReference<>();
   ConnectionParams params=new ConnectionParams.Builder("hu-appremote-test").setRedirectUri("hu-appremote://callback").showAuthView(false).build();
   runOnMainSync(()->SpotifyAppRemote.connect(getTargetContext(),params,new Connector.ConnectionListener(){
    public void onConnected(SpotifyAppRemote r){result.set(r);connected.countDown();}
    public void onFailure(Throwable t){result.set(t);connected.countDown();}
   }));
   ok(connected.await(30,TimeUnit.SECONDS),"connect answered");
   if(result.get() instanceof Throwable)throw new AssertionError("Connect failed: "+result.get());
   remote=(SpotifyAppRemote)result.get();ok(remote.isConnected(),"connected");
   PlayerState st=first(()->remote.getPlayerApi().subscribeToPlayerState());
   ok(st.playbackOptions!=null && st.playbackRestrictions!=null && st.playbackRestrictions.canSkipNext,"player state event with options and restrictions");
   ok(st.playbackPosition>=0 && (st.isPaused?st.playbackSpeed==0f:st.playbackSpeed>0f),"position and speed");
   PlayerState now=call(()->remote.getPlayerApi().getPlayerState());
   ok(now.playbackOptions!=null && (now.track==null || now.track.name!=null),"player state call");
   if(now.track!=null)ok(now.track.artist!=null && now.track.album!=null && now.track.artists!=null,"track artist and album");
   UserStatus status=first(()->remote.getUserApi().subscribeToUserStatus());
   ok(status.code==0 || status.code==1,"user status");
   Capabilities caps=call(()->remote.getUserApi().getCapabilities());
   ok(caps.canPlayOnDemand,"capabilities");
   ListItems root=call(()->remote.getContentApi().getRecommendedContentItems(ContentApi.ContentType.DEFAULT));
   ok(root.items!=null && root.total==root.items.length,"root items");
   Log.i("HU_APPREMOTE","root items: "+root.items.length);
   ListItem folder=null;for(ListItem i:root.items)if(i.hasChildren){folder=i;break;}
   java.util.List<ListItem> covers=new java.util.ArrayList<>();
   for(ListItem i:root.items)covers.add(i);
   if(folder!=null){
    ListItem f=folder;ListItems page=call(()->remote.getContentApi().getChildrenOfItem(f,5,0));
    ok(page.items!=null && page.items.length<=5 && page.offset==0 && page.total>=page.items.length,"children page");
    Log.i("HU_APPREMOTE","children of "+f.title+": "+page.items.length+" of "+page.total);
    for(ListItem i:page.items)covers.add(i);
   }
   // A cover: the root's are the backend's own pictures; others need the network. Offline, a
   // cover that cannot load must fail cleanly.
   boolean cover=false;int tried=0;
   for(ListItem i:covers){
    if(i.imageUri==null || i.imageUri.raw==null || i.imageUri.raw.isEmpty() || tried>=4)continue;tried++;
    ImageUri u=i.imageUri;
    try{Bitmap b=call(()->remote.getImagesApi().getImage(u,Image.Dimension.THUMBNAIL));ok(b!=null && b.getWidth()>0,"cover image");cover=true;break;}
    catch(AssertionError e){ok(String.valueOf(e.getMessage()).contains("RemoteClientException"),"unavailable cover fails cleanly");Log.i("HU_APPREMOTE","cover unavailable: "+u.raw);}
   }
   if(!cover)Log.i("HU_APPREMOTE","no cover could load (offline?), tried "+tried);
   LibraryState lib=call(()->remote.getUserApi().getLibraryState("spotify:track:0000000000000000000000"));
   ok(!lib.isAdded,"library state of another track");
   Throwable unsupported=failure(()->remote.getPlayerApi().queue("spotify:track:0000000000000000000000"));
   ok(unsupported!=null,"unsupported call fails cleanly");
   ok(remote.isConnected(),"still connected after an error");
   runOnMainSync(()->SpotifyAppRemote.disconnect(remote));Thread.sleep(500);
   ok(!remote.isConnected(),"disconnected");
   Log.i("HU_APPREMOTE","PASS app remote checks="+passed);out.putString("stream","PASS app remote checks="+passed+"\n");finish(0,out);
  }catch(Throwable t){
   Log.e("HU_APPREMOTE","FAIL",t);out.putString("stream","FAIL "+t+"\n");finish(1,out);
  }
 }
}
