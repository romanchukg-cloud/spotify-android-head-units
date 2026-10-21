package local.spotify.unified;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.*;
import java.util.ArrayList;
/**
 * Spotify Connect for our interface, in the music process: the account's devices (this car among
 * them), the one that plays, and moving playback to another. The backend's ConnectService client
 * (a field of its media session callback, captured as the backend builds it: patch "# Connect")
 * talks to Spotify's core through an Esperanto router; we send the core's own requests to that
 * router (sp://esperanto/spotify.connect.esperanto.proto.ConnectService/State, Pull, Transfer)
 * and decode the replies ourselves (ConnectWire). One
 * subscription keeps the state; the interface reads it through the private settings provider
 * (connect_get, connect_transfer) and observes CHANGED. A sign-in builds a new session callback,
 * and the subscription follows it.
 */
public final class HeadUnitConnect {
 static final Uri CHANGED=Uri.parse("content://com.spotify.music.local.headunit/connect");
 private static final String SERVICE="sp://esperanto/spotify.connect.esperanto.proto.ConnectService/";
 private static volatile Object callback;
 private static Object router,lifetime;private static Context app;
 private static Bundle state=new Bundle();
 private HeadUnitConnect(){}

 /** The backend built its media session callback (a new session after a sign-in). */
 public static synchronized void session(Object value){
  callback=value;release();router=null;state=new Bundle();
  if(app!=null)subscribe();
 }
 /** Provider calls in the music process. */
 static synchronized Bundle call(Context c,String method,Bundle extras){
  app=c.getApplicationContext();
  if("connect_get".equals(method)){subscribe();return new Bundle(state);}
  if("connect_transfer".equals(method) && extras!=null){
   Bundle out=new Bundle();
   boolean self=extras.getBoolean("self");String id=extras.getString("id"),local=extras.getString("local_id");
   out.putBoolean("sent",self?send("Pull",new byte[0],null,null):send("Transfer",ConnectWire.transfer(id),local!=null && !local.equals(id)?local:null,extras.getString("type")));
   return out;
  }
  throw new IllegalArgumentException("Unknown connect operation");
 }

 private static void subscribe(){
  if(lifetime!=null)return;
  Object r=router();if(r==null)return;
  lifetime=resolve(r,"SUB","State",ConnectWire.stateRequest(),(status,body)->{
   synchronized(HeadUnitConnect.class){
    if(status!=200){Log.w("SpotifyHU","Connect state: status "+status);lifetime=null;return;}
    try{state=bundle(ConnectWire.state(body));}catch(RuntimeException e){Log.w("SpotifyHU","Connect state unreadable",e);return;}
   }
   notifyChanged();
  },e->{synchronized(HeadUnitConnect.class){lifetime=null;}Log.w("SpotifyHU","Connect state failed: "+e.getClass().getSimpleName());});
  if(lifetime!=null)Log.i("SpotifyHU","Connect: watching devices");
 }
 /**
  * A single request; a Transfer the core refuses is tried once more with the device's local
  * identifier (which of the two identifiers the core takes is not documented).
  */
 private static boolean send(String method,byte[] body,String fallback,String type){
  Object r=router();if(r==null)return false;
  Object sent=resolve(r,"POST",method,body,(status,reply)->{
   int code=status==200?ConnectWire.status(reply):-1;
   Log.i("SpotifyHU","Connect "+method+(type!=null?" to "+type:"")+": "+(code==0?"ok":"failed (http "+status+", status "+code+")"));
   if(code!=0 && fallback!=null)send(method,ConnectWire.transfer(fallback),null,type);
  },e->Log.w("SpotifyHU","Connect "+method+" failed: "+e.getClass().getSimpleName()));
  return sent!=null;
 }
 private static void release(){
  Object l=lifetime;lifetime=null;
  if(l!=null)try{l.getClass().getMethod("release").invoke(l);}catch(ReflectiveOperationException | RuntimeException ignored){}
 }
 private static void notifyChanged(){Context c=app;if(c!=null)c.getContentResolver().notifyChange(CHANGED,null);}

 // The router: session callback (bd/j) .g → ConnectService client (yr/b) .a → its
 // CoroutineClientBase → private transport (CosmosCoroutineTransport) → private router.
 private static Object router(){
  if(router!=null)return router;
  Object cb=callback;if(cb==null)return null;
  try{
   Object client=field(cb,"g"),base=client==null?null:field(client,"a"),transport=base==null?null:field(base,"transport");
   router=transport==null?null:field(transport,"router");
  }catch(ReflectiveOperationException | RuntimeException e){Log.w("SpotifyHU","Connect unavailable: "+e.getClass().getSimpleName());}
  return router;
 }
 private static Object field(Object o,String name)throws ReflectiveOperationException{
  for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass())
   try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}catch(NoSuchFieldException ignored){}
  throw new NoSuchFieldException(name);
 }
 interface Reply {void reply(int status,byte[] body);}
 interface Failure {void failed(Throwable e);}
 /** router.resolve(new Request(action, uri, body), callback); returns the Lifetime, or null. */
 private static Object resolve(Object router,String action,String method,byte[] body,Reply reply,Failure failure){
  try{
   ClassLoader l=router.getClass().getClassLoader();
   Class<?> request=Class.forName("com.spotify.cosmos.cosmos.Request",true,l),callbackType=Class.forName("com.spotify.cosmos.cosmos.ResolveCallback",true,l);
   Object req=request.getConstructor(String.class,String.class,byte[].class).newInstance(action,SERVICE+method,body);
   Object cb=Proxy.newProxyInstance(l,new Class<?>[]{callbackType},(proxy,m,args)->{
    if("onResolved".equals(m.getName())){Object r=args[0];reply.reply((Integer)r.getClass().getMethod("getStatus").invoke(r),(byte[])r.getClass().getMethod("getBody").invoke(r));}
    else if("onError".equals(m.getName()))failure.failed((Throwable)args[0]);
    else if("hashCode".equals(m.getName()))return System.identityHashCode(proxy);
    else if("equals".equals(m.getName()))return proxy==args[0];
    else if("toString".equals(m.getName()))return "HeadUnitConnect";
    return null;
   });
   Method resolve=null;for(Method m:router.getClass().getMethods())if(m.getName().equals("resolve") && m.getParameterTypes().length==2)resolve=m;
   if(resolve==null)return null;
   return resolve.invoke(router,req,cb);
  }catch(ReflectiveOperationException | RuntimeException e){Log.w("SpotifyHU","Connect request failed: "+e.getClass().getSimpleName());return null;}
 }

 /** The state as a Bundle for the interface: devices with id, local_id, name, type, self, active, activating, disabled, group, offline, category. */
 static Bundle bundle(ConnectWire.State s){
  Bundle out=new Bundle();out.putString("local_name",s.localName);out.putBoolean("local",s.local);ArrayList<Bundle> list=new ArrayList<>();
  for(ConnectWire.Device d:s.devices){
   Bundle b=new Bundle();b.putString("id",d.id);b.putString("local_id",d.localId);b.putString("name",d.name);b.putString("type",d.type);
   b.putBoolean("self",d.self);b.putBoolean("active",d.active);b.putBoolean("activating",d.activating);b.putBoolean("disabled",d.disabled);
   b.putBoolean("group",d.group);b.putBoolean("offline",d.offline);b.putInt("category",d.category);list.add(b);
  }
  out.putParcelableArrayList("devices",list);return out;
 }
}
